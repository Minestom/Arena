package net.minestom.arena;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.arena.utils.ItemUtils;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public final class Items {
    public static final ItemStack COIN = ItemUtils.stripItalics(ItemStack.builder(Material.SUNFLOWER)
            .displayName(Component.text("Coin", NamedTextColor.GOLD))
            .lore(Component.empty(), Component.text("Use me to buy things!", NamedTextColor.GRAY))
            .build());
    public static final ItemStack CONTINUE = ItemUtils.stripItalics(ItemStack.builder(Material.FEATHER)
            .displayName(Component.text("Continue", NamedTextColor.RED))
            .lore(Component.text("Click me to continue", NamedTextColor.GRAY))
            .build());
}
