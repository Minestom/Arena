package net.minestom.arena;

import net.minestom.arena.command.InstancesCommand;
import net.minestom.arena.command.LobbyCommand;
import net.minestom.arena.command.StopCommand;
import net.minestom.arena.game.ArenaCommand;
import net.minestom.arena.team.TeamCommand;
import net.minestom.arena.team.TeamEvent;
import net.minestom.arena.utils.ServerProperties;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.extras.lan.OpenToLAN;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;

public class Main {
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new TeamCommand());
        commandManager.register(new LobbyCommand());
        commandManager.register(new ArenaCommand());
        commandManager.register(new InstancesCommand());
        commandManager.register(new StopCommand());

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(Lobby.INSTANCE);
            player.setRespawnPoint(new Pos(0, 42, 0));
        });

        // Until lighting is implemented, give players night vision.
        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            if (event.isFirstSpawn()) {
                final Player player = event.getPlayer();
                player.setGameMode(GameMode.ADVENTURE);

                player.addEffect(
                        new Potion(PotionEffect.NIGHT_VISION,
                                (byte) 1, Integer.MAX_VALUE,
                                (byte) (Potion.AMBIENT_FLAG + Potion.ICON_FLAG + Potion.PARTICLES_FLAG)
                        )
                );

                MessageUtils.sendInfoMessage(player, "Welcome to the Minestom Arena!");
            }
        });

        TeamEvent.hook(MinecraftServer.getGlobalEventHandler());

        final String forwardingSecret = ServerProperties.getForwardingSecret();
        if (forwardingSecret != null) {
            VelocityProxy.enable(forwardingSecret);
        } else {
            OpenToLAN.open();
        }

        final String address = ServerProperties.getServerAddress("0.0.0.0");
        final int port = ServerProperties.getServerPort(25565);
        minecraftServer.start(address, port);
        System.out.println("Server startup done! Listening on "+address+":"+port);
    }
}
