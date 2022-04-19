package net.minestom.arena;

import net.minestom.arena.group.Group;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public final class LeaveCommand extends Command {
    public LeaveCommand() {
        super("leave", "l");
        setCondition(CommandUtils::arenaOnly);

        setDefaultExecutor((sender, context) -> {
            final Player player = (Player) sender;
            Messenger.info(player, "You left the arena. Your last stage was " + Group.findGroup(player)
                    .arena()
                    .stage());
            player.setInstance(Lobby.INSTANCE);
            player.setHealth(player.getMaxHealth());
        });
    }
}
