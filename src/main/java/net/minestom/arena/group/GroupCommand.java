package net.minestom.arena.group;

import net.kyori.adventure.text.Component;
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
            if (sender instanceof Player player) {
                if (GroupManager.getGroup(player) == null) {
                    GroupManager.createGroup(player);
                    sender.sendMessage("Group created");
                } else {
                    sender.sendMessage("You are in a group");
                }
            }
        }, Literal("create"));

        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                GroupManager.removePlayer(player);
            }
        }, Literal("leave"));

        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                Group group = GroupManager.getGroup(player);
                if (group == null) {
                    sender.sendMessage("You are not the owner of a group");
                } else {
                    GroupManager.removeGroup(player);
                }
            }
        }, Literal("disband"));

        addSyntax((sender, context) -> {
            final EntityFinder finder = context.get("player");
            final Player player = finder.findFirstPlayer(sender);

            if (sender instanceof Player inviter) {
                if (player != null) {
                    GroupImpl group = GroupManager.getGroup(inviter);
                    if (group != null) {
                        Component invite = group.getInvite();
                        player.sendMessage(invite);
                    } else {
                        sender.sendMessage("You are not in a group. Use /group create");
                    }
                } else {
                    sender.sendMessage("Player not found");
                }
            }
        }, Literal("invite"), Entity("player").onlyPlayers(true).singleEntity(true));

        addSyntax((sender, context) -> {
            final EntityFinder finder = context.get("player");
            final Player player = finder.findFirstPlayer(sender);
            if (player != null) {
                GroupImpl group = GroupManager.getGroup(player);
                if (group != null) {
                    if (sender instanceof Player invitee) {
                        boolean wasInvited = group.getPendingInvites().contains(invitee);
                        if (wasInvited) {
                            group.addPlayer(invitee);
                            invitee.sendMessage(Component.text("You have been added to ")
                                    .append(group.getOwner())
                                    .append(Component.text("'s group")));
                        } else {
                            invitee.sendMessage(Component.text("You have not been invited to ")
                                    .append(group.getOwner()).append(Component.text("'s group")));
                        }
                    }
                } else {
                    sender.sendMessage("Group not found");
                }
            } else {
                sender.sendMessage("Group not found");
            }
        }, Literal("accept"), Entity("player").onlyPlayers(true).singleEntity(true));
    }
}
