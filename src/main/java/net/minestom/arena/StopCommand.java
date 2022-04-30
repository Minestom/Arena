package net.minestom.arena;

import net.minestom.arena.game.GameManager;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;

public final class StopCommand extends Command {
    public StopCommand() {
        super("stop");
        setCondition((sender, commandString) -> sender instanceof ConsoleSender);
        setDefaultExecutor((sender, context) -> GameManager.stopServer());
    }
}
