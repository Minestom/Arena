package net.minestom.arena;

import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

public final class CommandUtils {
    public static boolean lobbyOnly(CommandSender sender, String commandString) {
        if (!(sender instanceof Player player)) return false;
        final Instance instance = player.getInstance();
        return instance == null || instance == Lobby.INSTANCE;
    }

    public static boolean arenaOnly(CommandSender sender, String commandString) {
        if (!(sender instanceof Player player)) return false;
        final Instance instance = player.getInstance();
        return instance != null && instance != Lobby.INSTANCE;
    }
}
