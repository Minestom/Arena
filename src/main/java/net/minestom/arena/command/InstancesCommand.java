package net.minestom.arena.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

                if (instance == Lobby.INSTANCE) {
                    sender.sendMessage(
                            Component.text(instance.getUniqueId().toString(), NamedTextColor.GREEN)
                                    .append(Component.text(" (Lobby)", NamedTextColor.GRAY))
                    );
                } else {
                    sender.sendMessage(Component.text(instance.getUniqueId().toString(), NamedTextColor.BLUE));
                }
            }
        });
    }

}
