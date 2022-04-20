package net.minestom.arena.feature;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToDoubleBiFunction;

public final class Features {
    public static @NotNull Feature combat(boolean combat, ToDoubleBiFunction<Entity, Entity> damageModifier) {
        return new CombatFeature(combat, damageModifier);
    }

    public static @NotNull Feature combat(boolean combat) {
        return new CombatFeature(combat);
    }

    public static @NotNull Feature combat() {
        return new CombatFeature(false);
    }

    public static @NotNull Feature drop() {
        return new DropFeature();
    }
}
