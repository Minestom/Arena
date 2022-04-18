package net.minestom.arena.feature;

import org.jetbrains.annotations.NotNull;

public final class Features {
    public static @NotNull Feature combat() {
        return new CombatFeature(false);
    }
}
