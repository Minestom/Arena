package net.minestom.arena.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.arena.utils.ItemUtils;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public record ArenaOption(@NotNull String name, @NotNull String description,
                          @NotNull TextColor color, @NotNull Material material) {

    public @NotNull ItemStack item() {
        return ItemUtils.stripItalics(ItemStack.builder(material)
                .displayName(Component.text(name, color))
                .lore(Component.text(description, NamedTextColor.GRAY))
                .meta(ItemUtils::hideFlags)
                .build());
    }
}
