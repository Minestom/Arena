package net.minestom.arena;

import net.minestom.arena.game.ArenaCommand;
import net.minestom.arena.group.GroupCommand;
import net.minestom.arena.group.GroupEvent;
import net.minestom.arena.utils.ServerProperties;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.extras.lan.OpenToLAN;
import net.minestom.server.extras.velocity.VelocityProxy;

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
