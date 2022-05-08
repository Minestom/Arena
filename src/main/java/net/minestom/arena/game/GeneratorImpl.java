package net.minestom.arena.game;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;

record GeneratorImpl<T extends Entity, G extends GenerationContext>(Function<G, T> function,
                                       Predicate<G> shouldGenerate) implements Generator<T, G> {

    @Override
    public @NotNull Optional<T> generate(@NotNull G context) {
        return shouldGenerate.test(context)
                ? Optional.of(function.apply(context))
                : Optional.empty();
    }

    static final class Builder<T extends Entity, G extends GenerationContext> implements Generator.Builder<T, G> {
        final Function<G, T> function;
        final List<Condition<G>> conditions = new ArrayList<>();
        final List<Controller<G>> controllers = new ArrayList<>();
        final List<Preference<G>> preferences = new ArrayList<>();

        double chance = 1;

        Builder(@NotNull Function<G, T> function) {
            this.function = function;
        }

        @Override
        public Generator.@NotNull Builder<T, G> chance(double chance) {
            this.chance = chance;
            return this;
        }

        @Override
        public @NotNull Generator.Builder<T, G> condition(@NotNull Condition<G> condition) {
            conditions.add(condition);
            return this;
        }

        @Override
        public Generator.@NotNull Builder<T, G> controller(@NotNull Controller<G> controller) {
            controllers.add(controller);
            return this;
        }

        @Override
        public @NotNull Generator.Builder<T, G> preference(@NotNull Preference<G> preference) {
            preferences.add(preference);
            return this;
        }

        @Override
        public @NotNull Generator<T, G> build() {
            return new GeneratorImpl<>(function, context -> {
                final Random random = ThreadLocalRandom.current();

                // Chance
                if (random.nextDouble() > chance)
                    return false;

                // Conditions
                for (Condition<G> condition : conditions) {
                    if (!condition.isMet(context))
                        return false;
                }

                // Controllers
                for (Controller<G> controller : controllers) {
                    switch (controller.control(context)) {
                        case ALLOW:
                            return true;
                        case DISALLOW:
                            return false;
                    }
                }

                // Preferences
                double score = 0;
                for (Preference<G> preference : preferences) {
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
