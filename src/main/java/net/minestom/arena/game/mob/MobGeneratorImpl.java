package net.minestom.arena.game.mob;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntFunction;
import java.util.function.Predicate;

record MobGeneratorImpl<T extends ArenaMob>(IntFunction<T> supplier,
                                            Predicate<GenerationContext> shouldGenerate) implements MobGenerator<T> {

    @Override
    public @NotNull Optional<T> generate(@NotNull GenerationContext context) {
        return shouldGenerate.test(context)
                ? Optional.of(supplier.apply(context.stage()))
                : Optional.empty();
    }

    static final class Builder<T extends ArenaMob> implements MobGenerator.Builder<T> {
        final IntFunction<T> supplier;
        final List<Condition> conditions = new ArrayList<>();
        final List<Controller> controllers = new ArrayList<>();
        final List<Preference> preferences = new ArrayList<>();

        double chance = 1;

        Builder(@NotNull IntFunction<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public MobGenerator.@NotNull Builder<T> chance(double chance) {
            this.chance = chance;
            return this;
        }

        @Override
        public @NotNull MobGenerator.Builder<T> condition(@NotNull Condition condition) {
            conditions.add(condition);
            return this;
        }

        @Override
        public MobGenerator.@NotNull Builder<T> controller(@NotNull Controller controller) {
            controllers.add(controller);
            return this;
        }

        @Override
        public @NotNull MobGenerator.Builder<T> preference(@NotNull Preference preference) {
            preferences.add(preference);
            return this;
        }

        @Override
        public @NotNull MobGenerator<T> build() {
            return new MobGeneratorImpl<>(supplier, context -> {
                final Random random = ThreadLocalRandom.current();

                // Chance
                if (random.nextDouble() > chance)
                    return false;

                // Conditions
                for (Condition condition : conditions) {
                    if (!condition.isMet(context))
                        return false;
                }

                // Controllers
                for (Controller controller : controllers) {
                    switch (controller.isSatisfied(context)) {
                        case GENERATE:
                            return true;
                        case ENOUGH:
                            return false;
                    };
                }

                // Preferences
                double score = 0;
                for (Preference preference : preferences) {
                    score += preference.isPreferred(context) * preference.weight();
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
