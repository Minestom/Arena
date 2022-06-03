package net.minestom.arena.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.arena.game.mob.MobArena;
import net.minestom.arena.group.Group;
import net.minestom.arena.utils.ItemUtils;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiFunction;

enum ArenaType {
    MOB("Mob Arena", NamedTextColor.GREEN, Material.ZOMBIE_HEAD, MobArena.class, MobArena.OPTIONS);

    private final ItemStack item;
    private final Constructor<? extends Arena> supplier;
    private final List<ArenaOption> availableOptions;

    private final Class<? extends Arena> clazz;
    private final String metricsDisplayName;
    private static final Map<Class<? extends Arena>, ArenaType> classToType = new HashMap<>();

    static {
        for (ArenaType value : ArenaType.values()) {
            classToType.put(value.clazz, value);
        }
    }

    ArenaType(@NotNull String name, @NotNull TextColor color, @NotNull Material material,
              @NotNull Class<? extends Arena> clazz,
              @NotNull List<ArenaOption> availableOptions) {

        item = ItemUtils.stripItalics(ItemStack.builder(material)
                .displayName(Component.text(name, color))
                .meta(ItemUtils::hideFlags)
                .build());
        this.metricsDisplayName = name.toLowerCase(Locale.ROOT).replace(' ', '_');
        this.clazz = clazz;
        try {
            this.supplier = clazz.getConstructor(Group.class, Set.class);
        } catch (NoSuchMethodException e) {
            final RuntimeException ex = new RuntimeException("Arena doesn't implement the required constructor", e);
            MinecraftServer.getExceptionManager().handleException(ex);
            throw ex;
        }
        this.availableOptions = List.copyOf(availableOptions);
    }

    public ItemStack item() {
        return item;
    }

    public List<ArenaOption> availableOptions() {
        return availableOptions;
    }

    public Arena createInstance(Group group, Set<ArenaOption> options) {
        try {
            return supplier.newInstance(group, Set.copyOf(options));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            final RuntimeException ex = new RuntimeException(e);
            MinecraftServer.getExceptionManager().handleException(ex);
            throw ex;
        }
    }

    public static @Nullable ArenaType typeOf(Arena arena) {
        return classToType.get(arena.getClass());
    }

    public static String getMetricsDisplayName(Arena arena) {
        final ArenaType type = ArenaType.typeOf(arena);
        return type == null ? "unknown" : type.metricsDisplayName;
    }
}
