package net.minestom.arena.game.mob;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.arena.utils.ItemUtils;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

record ArenaClass(String name, String description, String icon, TextColor color, Material material, Kit kit, int cost) {
    public void apply(Player player) {
        kit.apply(player);
    }

    public ItemStack itemStack() {
        return ItemUtils.stripItalics(ItemStack.builder(material)
                .displayName(Component.text(icon + " " + name, color))
                .lore(List.of(
                        Component.text(description, NamedTextColor.GRAY),
                        Component.empty(),
                        Component.text("Switch to this class for " + cost + " coins", NamedTextColor.GOLD)
                ))
                .meta(ItemUtils::hideFlags)
                .build()
        );
    }
}
