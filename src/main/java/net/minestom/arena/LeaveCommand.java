package net.minestom.arena;

import net.minestom.arena.CommandUtils;
import net.minestom.arena.Lobby;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public final class LeaveCommand extends Command {
    public LeaveCommand() {
        super("leave");
        setCondition(CommandUtils::arenaOnly);

        setDefaultExecutor((sender, context) -> {
            final Player player = (Player) sender;
            player.setInstance(Lobby.INSTANCE);
        });
    }
}
