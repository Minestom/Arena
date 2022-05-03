package net.minestom.arena.game.mob;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiPredicate;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

record MobGeneratorImpl<T extends ArenaMob>(IntFunction<T> supplier,
                                            BiPredicate<MobArena, Integer> shouldGenerate) implements MobGenerator<T> {

    @Override
    public @NotNull Queue<T> generate(@NotNull MobArena arena, int needed) {
        return Stream.generate(() -> shouldGenerate.test(arena, needed) ? supplier.apply(arena.stage()) : null)
                .limit(needed).filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayDeque::new));
    }

    static final class Builder<T extends ArenaMob> implements MobGenerator.Builder<T> {
        final IntFunction<T> supplier;
        final List<Condition> conditions = new ArrayList<>();
        final List<Preference> preferences = new ArrayList<>();
        double chance = 1;

        Builder(@NotNull IntFunction<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public @NotNull MobGenerator.Builder<T> condition(@NotNull Condition condition) {
            conditions.add(condition);
            return this;
        }

        @Override
        public @NotNull MobGenerator.Builder<T> preference(@NotNull Preference preference) {
            preferences.add(preference);
            return this;
        }

        @Override
        public MobGenerator.@NotNull Builder<T> chance(double chance) {
            this.chance = chance;
            return this;
        }

        @Override
        public MobGenerator<T> build() {
            return new MobGeneratorImpl<>(supplier, (arena, needed) -> {
                final Random random = ThreadLocalRandom.current();

                // Chance
                if (random.nextDouble() > chance)
                    return false;

                // Conditions
                for (Condition condition : conditions) {
                    if (!condition.test(arena))
                        return false;
                }

                // Preferences
                double score = 0;
                for (Preference preference : preferences) {
                    score += preference.applyAsDouble(arena) * preference.weight();
                }
                final double chance = score / preferences.stream()
                        .mapToDouble(Preference::weight)
                        .sum();

                // NaN if no preferences
                return random.nextDouble() <= chance || Double.isNaN(chance);
            });
        }
    }
}
