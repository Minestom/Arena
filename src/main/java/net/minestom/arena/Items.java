package net.minestom.arena;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

public final class Items {
    public static final ItemStack COIN = ItemStack.builder(Material.SUNFLOWER)
            .displayName(Component.text("Coin", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false))
            .lore(List.of(
                    Component.empty(),
                    Component.text("Use me to buy things!", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
            ))
            .build();
}
