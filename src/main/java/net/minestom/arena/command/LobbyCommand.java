package net.minestom.arena.command;

import net.minestom.arena.Lobby;
import net.minestom.arena.MessageUtils;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;

public final class LobbyCommand extends Command {
    public LobbyCommand() {
        super("lobby");
        setCondition(Conditions::playerOnly);

        setDefaultExecutor((sender, context) -> {
            final Player player = (Player) sender;
            if (player.getInstance() == Lobby.INSTANCE) {
                MessageUtils.sendInfoMessage(player, "You are already in the lobby!");
                return;
            }
            MessageUtils.sendWarnMessage(player, "Welcome to the lobby!");
            player.setInstance(Lobby.INSTANCE);
        });
    }
}
