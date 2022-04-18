package net.minestom.arena.command;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;

public final class StopCommand extends Command {
    public StopCommand() {
        super("stop");
        setCondition(Conditions::consoleOnly);
        setDefaultExecutor((sender, context) -> MinecraftServer.stopCleanly());
    }
}
