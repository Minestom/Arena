package net.minestom.arena.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.arena.game.mob.MobArena;
import net.minestom.arena.group.Group;
import net.minestom.arena.utils.ItemUtils;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

enum ArenaType {
    MOB("Mob Arena", NamedTextColor.GREEN, Material.ZOMBIE_HEAD, MobArena::new, MobArena.OPTIONS);

    private final ItemStack item;
    private final BiFunction<Group, Set<ArenaOption>, Arena> supplier;
    private final List<ArenaOption> availableOptions;

    ArenaType(@NotNull String name, @NotNull TextColor color, @NotNull Material material,
              @NotNull BiFunction<Group, Set<ArenaOption>, Arena> supplier,
              @NotNull List<ArenaOption> availableOptions) {

        item = ItemUtils.stripItalics(ItemStack.builder(material)
                .displayName(Component.text(name, color))
                .meta(ItemUtils::hideFlags)
                .build());
        this.supplier = supplier;
        this.availableOptions = availableOptions;
    }

    public ItemStack item() {
        return item;
    }

    public List<ArenaOption> availableOptions() {
        return List.copyOf(availableOptions);
    }

    public Arena createInstance(Group group, Set<ArenaOption> options) {
        return supplier.apply(group, Set.copyOf(options));
    }
}
