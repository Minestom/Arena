package net.minestom.arena.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.item.ItemHideFlag;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Contract;

public final class ItemUtils {
    private ItemUtils() {}

    @Contract("null -> null; !null -> !null")
    public static Component stripItalics(Component component) {
        if (component == null) return null;

        if (component.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET) {
            component = component.decoration(TextDecoration.ITALIC, false);
        }

        return component;
    }

    @Contract("null -> null; !null -> !null")
    public static ItemStack stripItalics(ItemStack itemStack) {
        if (itemStack == null) return null;

        return itemStack.withDisplayName(ItemUtils::stripItalics)
                .withLore(lore -> lore.stream()
                        .map(ItemUtils::stripItalics)
                        .toList());
    }

    @Contract("null -> null; !null -> !null")
    public static ItemStack hideFlags(ItemStack itemStack) {
        if (itemStack == null) return null;

        return itemStack.withMeta(builder -> builder.hideFlag(ItemHideFlag.values()));
    }
}
