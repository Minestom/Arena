package net.minestom.arena.game.mob;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

sealed interface MobGenerator<T extends ArenaMob> permits MobGeneratorImpl {
    @Contract("_ -> new")
    static <T extends ArenaMob> @NotNull Builder<T> builder(@NotNull IntFunction<T> supplier) {
        return new MobGeneratorImpl.Builder<>(supplier);
    }

    static @NotNull Queue<Entity> generateAll(@NotNull List<MobGenerator<? extends ArenaMob>> generators, MobArena arena, int needed) {
        Queue<Entity> entities = new ArrayDeque<>();
        while (entities.size() < needed) {
            List<Entity> list = new ArrayList<>();
            for (MobGenerator<? extends ArenaMob> generator : generators)
                list.addAll(generator.generate(arena, needed - entities.size()));
            Collections.shuffle(list);
            entities.addAll(list.subList(0, Math.min(needed - entities.size(), list.size())));
        }

        return entities;
    }

    @NotNull Queue<T> generate(@NotNull MobArena arena, int needed);

    sealed interface Builder<T extends ArenaMob> permits MobGeneratorImpl.Builder {
        @Contract("_ -> this")
        @NotNull Builder<T> condition(@NotNull Condition condition);

        @Contract("_ -> this")
        @NotNull Builder<T> preference(@NotNull Preference preference);

        @Contract("_ -> this")
        @NotNull Builder<T> chance(double chance);

        @Contract("_ -> this")
        default @NotNull Builder<T> preference(@NotNull ToDoubleFunction<MobArena> toDoubleFunction) {
            return preference(toDoubleFunction, 1);
        }

        @Contract("_, _ -> this")
        default @NotNull Builder<T> preference(@NotNull ToDoubleFunction<MobArena> toDoubleFunction, double weight) {
            return preference(new Preference() {
                @Override
                public double applyAsDouble(MobArena value) {
                    return toDoubleFunction.applyAsDouble(value);
                }

                @Override
                public double weight() {
                    return weight;
                }
            });
        }

        MobGenerator<T> build();
    }

    @FunctionalInterface
    interface Condition extends Predicate<MobArena> {
        static Condition hasClass(ArenaClass arenaClass) {
            return arena -> arena.instance()
                    .getPlayers()
                    .stream()
                    .anyMatch(player -> arena.playerClass(player)
                            .equals(arenaClass));
        }
    }

    interface Preference extends ToDoubleFunction<MobArena> {
        double weight();
    }
}
