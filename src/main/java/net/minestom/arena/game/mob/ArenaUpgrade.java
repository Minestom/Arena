package net.minestom.arena.game.mob;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.arena.utils.ItemUtils;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

record ArenaUpgrade(String name, String description, TextColor color, Material material,
                    @Nullable BiConsumer<Player, Integer> apply, @Nullable Consumer<Player> remove, int cost) {
    public ItemStack itemStack(int level) {
        return ItemUtils.stripItalics(ItemStack.builder(material)
                .displayName(Component.text(name, color))
                .lore(
                        Component.text(description, NamedTextColor.GRAY),
                        Component.empty(),
                        Component.text("Buy this team upgrade for " + cost + " coins", NamedTextColor.GOLD),
                        Component.text("The current level of this upgrade is " + level, NamedTextColor.GOLD)
                )
                .meta(ItemUtils::hideFlags)
                .meta(builder -> {
                    if (level != 0) builder.enchantment(Enchantment.PROTECTION, (short) 1);
                })
                .build()
        );
    }
}
