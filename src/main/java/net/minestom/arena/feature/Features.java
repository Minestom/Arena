package net.minestom.arena.feature;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToDoubleBiFunction;
import java.util.function.ToLongFunction;

public final class Features {
    public static @NotNull Feature combat(boolean combat, ToDoubleBiFunction<Entity, Entity> damageFunction, ToLongFunction<Entity> invulnerabilityFunction) {
        return new CombatFeature(combat, damageFunction, invulnerabilityFunction);
    }

    public static @NotNull Feature drop() {
        return new DropFeature();
    }
}
