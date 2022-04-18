package net.minestom.arena;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public final class Messenger {
    public static final TextColor PINK_COLOR = TextColor.color(209, 72, 212);
    public static final TextColor ORANGE_COLOR = TextColor.color(232, 175, 53);

    public static void info(Audience audience, String message) {
        audience.sendMessage(Component.text("! ", PINK_COLOR)
                .append(Component.text(message, NamedTextColor.GRAY)));
    }

    public static void warn(Audience audience, String message) {
        audience.sendMessage(Component.text("* ", ORANGE_COLOR)
                .append(Component.text(message, NamedTextColor.GRAY)));
    }
}
