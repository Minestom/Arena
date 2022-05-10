package net.minestom.arena.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ArenaManager {
    private static final List<Arena> ARENAS = new CopyOnWriteArrayList<>();
    private static volatile boolean stopQueued;

    static void register(@NotNull Arena arena) {
        ARENAS.add(arena);
    }

    static void unregister(@NotNull Arena arena) {
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

        MinecraftServer.getSchedulerManager()
                .buildTask(MinecraftServer::stopCleanly)
                .delay(Duration.ofSeconds(1))
                .schedule();
    }
}
