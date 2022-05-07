package net.minestom.arena.game.mob;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.arena.utils.ItemUtils;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

@Getter
@Accessors(fluent = true)
public abstract class ArenaUpgrade {

    private final String name;
    private final String description;
    private final TextColor color;
    private final Material material;
    private final int cost;

    public ArenaUpgrade(String name, String description, TextColor color, Material material, int cost) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.material = material;
        this.cost = cost;
    }

    public abstract void apply(Player player);
    public abstract void reset(Player player);

    public ItemStack itemStack(boolean unlocked) {
        return ItemUtils.stripItalics(ItemStack.builder(material)
                .displayName(Component.text(name, color))
                .lore(
                        Component.text(description, NamedTextColor.GRAY),
                        Component.empty(),
                        unlocked ? Component.text("You already have this upgrade.", NamedTextColor.RED) :
                                Component.text("Buy this team upgrade for " + cost + " coins", NamedTextColor.GOLD)
                )
                .meta(builder -> {
                    ItemUtils.hideFlags(builder);
                    if (unlocked) builder.enchantment(Enchantment.PROTECTION, (short) 1);
                    return builder;
                })
                .build()
        );
    }
}
