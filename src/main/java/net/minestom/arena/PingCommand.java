package net.minestom.arena;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public final class PingCommand extends Command {
    public PingCommand() {
        super("ping", "latency");

        setDefaultExecutor((sender, context) -> {
            final Player player = (Player) sender;
            Messenger.info(player, "Your ping is " + player.getLatency() + "ms");
        });
    }
}
