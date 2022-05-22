package net.minestom.arena.game.mob;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.arena.utils.ItemUtils;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;

record ArenaUpgrade(String name, String description, TextColor color, Material material,
                    @Nullable BiConsumer<Player, Integer> apply, @Nullable Consumer<Player> remove,
                    @NotNull IntFunction<String> effect, int cost, float costMultiplier, int maxLevel) {
    public ItemStack itemStack(int level) {
        return ItemUtils.stripItalics(ItemStack.builder(material)
                .displayName(Component.text(name, color))
                .lore(
                        Component.text(description, NamedTextColor.GRAY),
                        Component.empty(),
                        Component.text("Buy this team upgrade for " + cost(level) + " coins", NamedTextColor.GOLD),
                        Component.text(effect.apply(level), NamedTextColor.YELLOW)
                )
                .meta(ItemUtils::hideFlags)
                .meta(builder -> {
                    if (level >= maxLevel) builder.enchantment(Enchantment.PROTECTION, (short) 1);
                })
                .build()
        );
    }

    public int cost(int level) {
        return (int) (cost * Math.pow(costMultiplier, level));
    }
}
