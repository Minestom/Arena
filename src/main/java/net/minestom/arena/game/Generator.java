package net.minestom.arena.game;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

public sealed interface Generator<T extends Entity, G extends GenerationContext> permits GeneratorImpl {
    @Contract("_ -> new")
    static <T extends Entity, G extends GenerationContext> @NotNull Builder<T, G> builder(@NotNull Function<G, T> function) {
        return new GeneratorImpl.Builder<>(function);
    }

    static <T extends Entity, G extends GenerationContext> @NotNull List<T> generateAll(
            @NotNull List<Generator<? extends T, G>> generators, int amount, Supplier<G> contextSupplier) {

        final Map<Generator<? extends T, G>, G> contextMap = new HashMap<>();
        final List<T> entities = new ArrayList<>();

        for (Generator<? extends T, G> generator : generators)
            contextMap.put(generator, contextSupplier.get());

        while (entities.size() < amount) {
            final Generator<? extends T, G> generator = generators.get(ThreadLocalRandom.current().nextInt(generators.size()));
            final G context = contextMap.get(generator);
            final Optional<? extends T> mob = generator.generate(context);

            if (mob.isPresent()) {
                entities.add(mob.get());
                context.incrementGenerated();
            }
        }

        return entities;
    }

    @NotNull Optional<T> generate(@NotNull G context);

    sealed interface Builder<T extends Entity, G extends GenerationContext> permits GeneratorImpl.Builder {
        @Contract("_ -> this")
        @NotNull Builder<T, G> chance(double chance);

        @Contract("_ -> this")
        @NotNull Builder<T, G> condition(@NotNull Condition<G> condition);

        @Contract("_ -> this")
        @NotNull Builder<T, G> controller(@NotNull Controller<G> controller);

        @Contract("_ -> this")
        @NotNull Builder<T, G> preference(@NotNull Preference<G> preference);

        @Contract("_ -> this")
        default @NotNull Builder<T, G> preference(@NotNull ToDoubleFunction<G> isPreferred) {
            return preference(isPreferred, 1);
        }

        @Contract("_, _ -> this")
        default @NotNull Builder<T, G> preference(@NotNull ToDoubleFunction<G> isPreferred, double weight) {
            return preference(new Preference<>() {
                @Override
                public double isPreferred(@NotNull G context) {
                    return isPreferred.applyAsDouble(context);
                }

                @Override
                public double weight() {
                    return weight;
                }
            });
        }

        @NotNull Generator<T, G> build();
    }

    @FunctionalInterface
    interface Condition<G extends GenerationContext> {
        boolean isMet(@NotNull G context);
    }

    @FunctionalInterface
    interface Controller<G extends GenerationContext> {
        @NotNull Control control(@NotNull G context);

        enum Control {
            ALLOW,
            DISALLOW,
            OK
        }

        static <G extends GenerationContext> @NotNull Controller<G> minCount(int count) {
            return context -> context.generated() <= count
                    ? Control.ALLOW
                    : Control.OK;
        }

        static <G extends GenerationContext> @NotNull Controller<G> maxCount(int count) {
            return context -> context.generated() >= count
                    ? Control.DISALLOW
                    : Control.OK;
        }
    }

    interface Preference<G extends GenerationContext> {
        double isPreferred(@NotNull G context);

        double weight();
    }
}
