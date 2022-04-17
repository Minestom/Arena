package net.minestom.arena;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class MessageUtils {

    public static final TextColor pinkColor = TextColor.color(209, 72, 212);
    public static final TextColor orangeColor = TextColor.color(232, 175, 53);

    public static void sendInfoMessage(Audience audience, String message) {
        audience.sendMessage(
                Component.text("! ", pinkColor)
                        .append(Component.text(message, NamedTextColor.GRAY))
        );
    }

    public static void sendWarnMessage(Audience audience, String message) {
        audience.sendMessage(
                Component.text("* ", orangeColor)
                        .append(Component.text(message, NamedTextColor.GRAY))
        );
    }

}
