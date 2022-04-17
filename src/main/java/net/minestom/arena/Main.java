package net.minestom.arena;

import net.minestom.arena.command.GroupCommand;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;

public class Main {
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new GroupCommand());

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(Lobby.INSTANCE);
            player.setRespawnPoint(new Pos(0, 42, 0));
        });

        minecraftServer.start("0.0.0.0", 25565);
    }
}
