package net.minestom.arena.feature;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class Features {
    public static @NotNull Feature combat() {
        return new CombatFeature(false);
    }
    public static @NotNull Feature death(@NotNull Supplier<Component> endMessage) {
        return new DeathFeature(endMessage);
    }
}
