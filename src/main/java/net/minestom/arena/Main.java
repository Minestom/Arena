package net.minestom.arena;

import net.kyori.adventure.text.Component;
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
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.extras.lan.OpenToLAN;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.MathUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
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
        }

        // Events
        {
            GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();

            // Group events
            GroupEvent.hook(handler);

            // Login
            handler.addListener(PlayerLoginEvent.class, event -> {
                final Player player = event.getPlayer();
                event.setSpawningInstance(Lobby.INSTANCE);
                player.setRespawnPoint(new Pos(0, 42, 0));
            });

            handler.addListener(PlayerSpawnEvent.class, event -> {
                if (!event.isFirstSpawn()) return;
                final Player player = event.getPlayer();
                player.setGameMode(GameMode.ADVENTURE);
                player.setEnableRespawnScreen(false);
            });

            String favicon = "";
            try {
                BufferedImage image = ImageIO.read(new File("./src/main/resources/favicon.png"));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(image, "png", outputStream);
                favicon = "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            String finalFavicon = favicon;
            handler.addListener(ServerListPingEvent.class, event -> {
                ResponseData responseData = event.getResponseData();
                responseData.setDescription(Component.text("Minestom Arena").color(Messenger.ORANGE_COLOR));
                responseData.setFavicon(finalFavicon);
            });

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

                final Component header = Component.text("Minestom demo")
                        .append(Component.newline()).append(Component.text("Players: " + players.size()))
                        .append(Component.newline()).append(Component.newline())
                        .append(Component.text("RAM USAGE: " + ramUsage + " MB").append(Component.newline())
                                .append(Component.text("TICK TIME: " + MathUtils.round(tickMonitor.getTickTime(), 2) + "ms"))).append(Component.newline());
                final Component footer = Component.newline().append(Component.text("Project: minestom.net").append(Component.newline())
                        .append(Component.text("Source: github.com/Minestom/Minestom")).append(Component.newline())
                        .append(Component.text("Arena: github.com/Minestom/Arena")));

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
