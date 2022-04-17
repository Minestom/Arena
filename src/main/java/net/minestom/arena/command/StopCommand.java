package net.minestom.arena.command;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.jetbrains.annotations.NotNull;

public class StopCommand extends Command {
    public StopCommand() {
        super("stop");

        setDefaultExecutor(this::stop);
    }

    private void stop(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
        if (commandSender instanceof ConsoleSender) {
            MinecraftServer.stopCleanly();
        }
    }
}
