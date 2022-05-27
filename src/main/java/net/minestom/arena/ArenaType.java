package net.minestom.arena;

import net.minestom.arena.game.Arena;
import net.minestom.arena.game.mob.MobArena;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum ArenaType {
    MOB_ARENA("mob_arena", MobArena.class),
    UNKNOWN("unknown", null);

    private final String displayName;
    private final Class<? extends Arena> clazz;
    private static final Map<Class<? extends Arena>, ArenaType> classToType = new HashMap<>();

    static {
        for (ArenaType value : ArenaType.values()) {
            classToType.put(value.clazz, value);
        }
    }

    ArenaType(String displayName, Class<? extends Arena> clazz) {
        this.displayName = displayName;
        this.clazz = clazz;
    }

    public static <T extends Arena> ArenaType typeOf(T arena) {
        return classToType.getOrDefault(arena.getClass(), ArenaType.UNKNOWN);
    }

    public static String[] types() {
        return Arrays.stream(ArenaType.values()).map(x -> x.displayName).toArray(String[]::new);
    }

    public String getDisplayName() {
        return displayName;
    }
}
