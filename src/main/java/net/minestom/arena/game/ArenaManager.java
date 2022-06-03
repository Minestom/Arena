package net.minestom.arena.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.arena.Metrics;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public final class ArenaManager {
    private static final List<Arena> ARENAS = new CopyOnWriteArrayList<>();
    private static volatile boolean stopQueued;

    static void register(@NotNull Arena arena) {
        Metrics.GAMES_IN_PROGRESS.labels(ArenaType.getMetricsDisplayName(arena)).inc();
        ARENAS.add(arena);
    }

    static void unregister(@NotNull Arena arena) {
        Metrics.GAMES_IN_PROGRESS.labels(ArenaType.getMetricsDisplayName(arena)).dec();
        Metrics.GAMES_PLAYED.labels(ArenaType.getMetricsDisplayName(arena)).inc();
        ARENAS.remove(arena);
        if (ARENAS.size() == 0 && stopQueued)
            stopServerNow();
    }

    public static @NotNull @UnmodifiableView List<Arena> list() {
        return Collections.unmodifiableList(ARENAS);
    }

    public static void stopServer() {
        Collection<Arena> arenas = list();
        stopQueued = true;
        for (Arena arena : arenas) arena.stop();
        if (arenas.size() == 0) stopServerNow();
    }

    private static void stopServerNow() {
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers())
            player.kick(Component.text("Server is shutting down", NamedTextColor.RED));

        CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS)
                .execute(MinecraftServer::stopCleanly);
    }
}
