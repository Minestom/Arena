package net.minestom.arena;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

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

    public static CompletableFuture<Void> countdown(Audience audience, int from) {
        final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        final AtomicInteger countdown = new AtomicInteger(from);
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            final int count = countdown.getAndDecrement();
            if (count <= 0) {
                completableFuture.complete(null);
                return TaskSchedule.stop();
            }

            audience.showTitle(Title.title(Component.text(count, NamedTextColor.GREEN), Component.empty()));
            audience.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_PLING, Sound.Source.BLOCK, 1, 1), Sound.Emitter.self());

            return TaskSchedule.seconds(1);
        });

        return completableFuture;
    }
}
