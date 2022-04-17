package net.minestom.arena;

import net.minestom.arena.command.GroupCommand;
import net.minestom.arena.command.InstancesCommand;
import net.minestom.arena.command.LobbyCommand;
import net.minestom.arena.game.ArenaCommand;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.extras.lan.OpenToLAN;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;

public class Main {
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new GroupCommand());
        commandManager.register(new LobbyCommand());
        commandManager.register(new ArenaCommand());
        commandManager.register(new InstancesCommand());

        OpenToLAN.open();

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(Lobby.INSTANCE);
            player.setRespawnPoint(new Pos(0, 42, 0));
        });

        // Until lighting is implemented, give players night vision.
        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            final Player player = event.getPlayer();

            player.addEffect(
                    new Potion(PotionEffect.NIGHT_VISION,
                            (byte) 1, Integer.MAX_VALUE,
                            (byte) (Potion.AMBIENT_FLAG + Potion.ICON_FLAG + Potion.PARTICLES_FLAG)
                    )
            );
        });

        minecraftServer.start("0.0.0.0", 25565);
    }
}
