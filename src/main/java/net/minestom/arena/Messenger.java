package net.minestom.arena;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public final class Messenger {
    public static final TextColor PINK_COLOR = TextColor.color(209, 72, 212);
    public static final TextColor ORANGE_COLOR = TextColor.color(232, 175, 53);

    public static void info(Audience audience, String message) {
        info(audience, Component.text(message));
    }

    public static void info(Audience audience, Component message) {
        audience.sendMessage(Component.text("! ", PINK_COLOR)
                .append(message.color(NamedTextColor.GRAY)));
    }

    public static void warn(Audience audience, String message) {
        warn(audience, Component.text(message));
    }

    public static void warn(Audience audience, Component message) {
        audience.sendMessage(Component.text("* ", ORANGE_COLOR)
                .append(message.color(NamedTextColor.GRAY)));
    }
}
