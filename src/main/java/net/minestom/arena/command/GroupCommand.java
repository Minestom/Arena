package net.minestom.arena.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;

import static net.minestom.server.command.builder.arguments.ArgumentType.Entity;
import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

public final class GroupCommand extends Command {
    public GroupCommand() {
        super("group");
        setCondition(Conditions::playerOnly);

        addSyntax((sender, context) -> {
            final EntityFinder finder = context.get("player");
            final Player player = finder.findFirstPlayer(sender);
            // TODO
        }, Literal("invite"), Entity("player").onlyPlayers(true).singleEntity(true));
    }
}
