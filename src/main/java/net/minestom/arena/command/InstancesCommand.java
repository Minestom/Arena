package net.minestom.arena.command;

import net.minestom.arena.Lobby;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.instance.Instance;

// Debug command for the instances in this game.
public class InstancesCommand extends Command {

    public InstancesCommand() {
        super("instances");

        setDefaultExecutor((sender, context) -> {
            for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
                sender.sendMessage(instance.getUniqueId() + (instance == Lobby.INSTANCE ? " (Lobby)" : ""));
            }
        });
    }

}
