package net.minestom.arena;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.arena.utils.ItemUtils;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public final class Items {
    public static final ItemStack CLOSE = ItemUtils.stripItalics(ItemStack.builder(Material.BARRIER)
            .displayName(Component.text("Close", NamedTextColor.RED))
            .lore(Component.text("Close this page", NamedTextColor.GRAY))
            .build());
    public static final ItemStack CONTINUE = ItemUtils.stripItalics(ItemStack.builder(Material.FEATHER)
            .displayName(Component.text("Continue", NamedTextColor.GOLD))
            .lore(Component.text("Continue to the next stage", NamedTextColor.GRAY))
            .build());
    public static final ItemStack BACK = ItemUtils.stripItalics(ItemStack.builder(Material.ARROW)
            .displayName(Component.text("Back", NamedTextColor.AQUA))
            .lore(Component.text("Go back to the previous page", NamedTextColor.GRAY))
            .build());

    private Items() {}
}
