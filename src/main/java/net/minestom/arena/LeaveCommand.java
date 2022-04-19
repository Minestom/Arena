package net.minestom.arena;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public final class LeaveCommand extends Command {
    public LeaveCommand() {
        super("leave", "l");
        setCondition(CommandUtils::arenaOnly);

        setDefaultExecutor((sender, context) -> {
            final Player player = (Player) sender;
            player.setInstance(Lobby.INSTANCE);
            player.setHealth(player.getMaxHealth());
        });
    }
}
