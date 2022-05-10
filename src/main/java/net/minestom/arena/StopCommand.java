package net.minestom.arena;

import net.minestom.arena.game.ArenaManager;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public final class StopCommand extends Command {
    public StopCommand() {
        super("stop");
        setCondition((sender, commandString) -> sender instanceof ConsoleSender ||
                (sender instanceof Player player && player.getPermissionLevel() == 4));
        setDefaultExecutor((sender, context) -> ArenaManager.stopServer());
    }
}
