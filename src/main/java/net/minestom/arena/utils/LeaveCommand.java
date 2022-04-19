package net.minestom.arena.utils;

import net.minestom.arena.CommandUtils;
import net.minestom.arena.Lobby;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class LeaveCommand extends Command {

    public LeaveCommand() {
        super("leave");
        setCondition(CommandUtils::arenaOnly);

        setDefaultExecutor((sender, context) -> {
            Player player = (Player) sender;

            player.setInstance(Lobby.INSTANCE);
        });
    }

}
