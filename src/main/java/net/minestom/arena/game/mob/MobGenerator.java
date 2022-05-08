package net.minestom.arena.game.mob;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntFunction;
import java.util.function.ToDoubleFunction;

sealed interface MobGenerator<T extends ArenaMob> permits MobGeneratorImpl {
    @Contract("_ -> new")
    static <T extends ArenaMob> @NotNull Builder<T> builder(@NotNull IntFunction<T> supplier) {
        return new MobGeneratorImpl.Builder<>(supplier);
    }

    static @NotNull List<Entity> generateAll(@NotNull List<MobGenerator<? extends ArenaMob>> generators, @NotNull MobArena arena, int needed) {
        final Object2IntMap<MobGenerator<? extends ArenaMob>> generated = new Object2IntOpenHashMap<>();
        final List<Entity> entities = new ArrayList<>();
        while (entities.size() < needed) {
            MobGenerator<? extends ArenaMob> generator = generators.get(ThreadLocalRandom.current().nextInt(generators.size()));
            Optional<? extends ArenaMob> mob = generator.generate(new GenerationContext(arena, generated.getOrDefault(generator, 0)));

            if (mob.isPresent()) {
                entities.add(mob.get());
                generated.computeInt(generator, (g, i) -> i == null ? 1 : i + 1);
            }
        }

        return entities;
    }

    @NotNull Optional<T> generate(@NotNull GenerationContext context);

    sealed interface Builder<T extends ArenaMob> permits MobGeneratorImpl.Builder {
        @Contract("_ -> this")
        @NotNull Builder<T> chance(double chance);

        @Contract("_ -> this")
        @NotNull Builder<T> condition(@NotNull Condition condition);

        @Contract("_ -> this")
        @NotNull Builder<T> controller(@NotNull Controller controller);

        @Contract("_ -> this")
        @NotNull Builder<T> preference(@NotNull Preference preference);

        @Contract("_ -> this")
        default @NotNull Builder<T> preference(@NotNull ToDoubleFunction<GenerationContext> isPreferred) {
            return preference(isPreferred, 1);
        }

        @Contract("_, _ -> this")
        default @NotNull Builder<T> preference(@NotNull ToDoubleFunction<GenerationContext> isPreferred, double weight) {
            return preference(new Preference() {
                @Override
                public double isPreferred(@NotNull GenerationContext context) {
                    return isPreferred.applyAsDouble(context);
                }

                @Override
                public double weight() {
                    return weight;
                }
            });
        }

        @NotNull MobGenerator<T> build();
    }

    @FunctionalInterface
    interface Condition {
        boolean isMet(@NotNull GenerationContext context);

        static Condition hasClass(@NotNull ArenaClass arenaClass) {
            return context -> context.arena().instance()
                    .getPlayers()
                    .stream()
                    .anyMatch(player -> context.arena().playerClass(player)
                            .equals(arenaClass));
        }
    }

    @FunctionalInterface
    interface Controller {
        @NotNull Control isSatisfied(@NotNull GenerationContext context);

        enum Control {
            GENERATE,
            ENOUGH,
            OK
        }

        static @NotNull Controller minCount(int count) {
            return context -> context.generated() <= count
                    ? Control.GENERATE
                    : Control.OK;
        }

        static @NotNull Controller maxCount(int count) {
            return context -> context.generated() >= count
                    ? Control.ENOUGH
                    : Control.OK;
        }
    }

    interface Preference {
        double isPreferred(@NotNull GenerationContext context);

        double weight();
    }
}
