package net.minestom.arena;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.arena.game.ArenaCommand;
import net.minestom.arena.group.GroupCommand;
import net.minestom.arena.group.GroupEvent;
import net.minestom.arena.utils.ServerProperties;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.extras.lan.OpenToLAN;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.MathUtils;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

public final class Main {
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        // Commands
        {
            CommandManager manager = MinecraftServer.getCommandManager();
            manager.register(new GroupCommand());
            manager.register(new ArenaCommand());
            manager.register(new StopCommand());
            manager.register(new LeaveCommand());
        }

        // Events
        {
            GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();

            // Group events
            GroupEvent.hook(handler);
            // Server list
            ServerList.hook(handler);

            // Login
            handler.addListener(PlayerLoginEvent.class, event -> {
                final Player player = event.getPlayer();
                event.setSpawningInstance(Lobby.INSTANCE);
                player.setRespawnPoint(new Pos(0, 42, 0));
            });

            handler.addListener(PlayerSpawnEvent.class, event -> {
                if (!event.isFirstSpawn()) return;
                final Player player = event.getPlayer();
                Messenger.info(player, "Welcome to the Minestom Demo Server.");
                player.setGameMode(GameMode.ADVENTURE);
                player.setEnableRespawnScreen(false);
            });

            // Prevent dropping
            handler.addListener(ItemDropEvent.class, event -> event.setCancelled(true));

            // Monitoring
            AtomicReference<TickMonitor> lastTick = new AtomicReference<>();
            handler.addListener(ServerTickMonitorEvent.class, event -> lastTick.set(event.getTickMonitor()));

            // Header/footer
            MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
                if (players.isEmpty()) return;

                final Runtime runtime = Runtime.getRuntime();
                final TickMonitor tickMonitor = lastTick.get();
                final long ramUsage = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;

                final Component header = Component.newline()
                        .append(Component.text("Minestom Arena Demo", Messenger.PINK_COLOR))
                        .append(Component.newline()).append(Component.text("Players: " + players.size()))
                        .append(Component.newline()).append(Component.newline())
                        .append(Component.text("RAM USAGE: " + ramUsage + " MB", NamedTextColor.GRAY).append(Component.newline())
                                .append(Component.text("TICK TIME: " + MathUtils.round(tickMonitor.getTickTime(), 2) + "ms", NamedTextColor.GRAY))).append(Component.newline());
                final Component footer = Component.newline().append(Component.text("Project: minestom.net").append(Component.newline())
                                .append(Component.text("    Source: github.com/Minestom/Minestom    ", Messenger.ORANGE_COLOR)).append(Component.newline())
                                .append(Component.text("Arena: github.com/Minestom/Arena", Messenger.ORANGE_COLOR)))
                        .append(Component.newline());

                Audiences.players().sendPlayerListHeaderAndFooter(header, footer);

            }, TaskSchedule.tick(10), TaskSchedule.tick(10));
        }

        final String forwardingSecret = ServerProperties.getForwardingSecret();
        if (forwardingSecret != null) {
            VelocityProxy.enable(forwardingSecret);
        } else {
            OpenToLAN.open();
        }

        final String address = ServerProperties.getServerAddress("0.0.0.0");
        final int port = ServerProperties.getServerPort(25565);
        minecraftServer.start(address, port);
        System.out.println("Server startup done! Listening on " + address + ":" + port);
    }
}
