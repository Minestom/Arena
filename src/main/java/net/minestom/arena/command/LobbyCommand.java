package net.minestom.arena.command;

import net.minestom.arena.Lobby;
import net.minestom.arena.game.Arena;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;

public class LobbyCommand extends Command {

    public LobbyCommand() {
        super("lobby");
        setCondition(Conditions::playerOnly);

        setDefaultExecutor((sender, context) -> {
            Player player = (Player) sender;

            Arena arena = Arena.getArena(player);

            if (player.getInstance() == Lobby.INSTANCE) {
                player.sendMessage("You are already in the lobby!");
                return;
            }

            // They aren't in a lobby nor in an arena. Safe to send them to the lobby.
            if (arena == null) {
                player.setInstance(Lobby.INSTANCE);
                return;
            }

            // They're in an arena. Leave the arena and send them back.
            arena.leave(player);
        });
    }

}
