package net.minestom.arena.group;

import net.kyori.adventure.text.Component;
import net.minestom.arena.CommandUtils;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;

import static net.minestom.server.command.builder.arguments.ArgumentType.Entity;
import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

public final class GroupCommand extends Command {
    public GroupCommand() {
        super("group");
        setCondition(CommandUtils::lobbyOnly);

        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                GroupManager.removePlayer(player);
            }
        }, Literal("leave"));

        addSyntax((sender, context) -> {
            final EntityFinder finder = context.get("player");
            final Player player = finder.findFirstPlayer(sender);

            if (sender instanceof Player inviter) {
                if (player != null) {
                    GroupImpl group = GroupManager.getGroup(inviter);

                    if (group.members().contains(player)) {
                        sender.sendMessage(player.getName().append(Component.text(" is already in this group.")));
                    } else {
                        Component invite = group.getInviteMessage();
                        group.addPendingInvite(player);
                        player.sendMessage(invite);
                    }
                } else {
                    sender.sendMessage("Player not found");
                }
            }
        }, Literal("invite"), Entity("player").onlyPlayers(true).singleEntity(true));

        addSyntax((sender, context) -> {
            final EntityFinder finder = context.get("player");
            final Player newLeader = finder.findFirstPlayer(sender);

            if (sender instanceof Player player) {
                if (newLeader != null) {
                    GroupImpl group = GroupManager.getMemberGroup(player);

                    if (group == null) {
                        sender.sendMessage("You are not in a group.");
                    } else if (group.leader() == newLeader) {
                        sender.sendMessage("You are already the leader");
                    } else if (group.leader() != player) {
                        sender.sendMessage("You are not the leader of this group.");
                    } else {
                        group.setLeader(newLeader);
                    }
                } else {
                    sender.sendMessage("Player not found");
                }
            }
            // TODO: only show players in the group
        }, Literal("leader"), Entity("player").onlyPlayers(true).singleEntity(true));

        addSyntax((sender, context) -> {
            final EntityFinder finder = context.get("player");
            final Player player = finder.findFirstPlayer(sender);
            if (player != null) {
                GroupImpl group = GroupManager.getGroup(player);
                if (sender instanceof Player invitee) {
                    boolean wasInvited = group.getPendingInvites().contains(invitee);
                    if (wasInvited) {
                        GroupManager.removePlayer(invitee); // Remove from old group
                        group.addPlayer(invitee);
                        Component accepted = group.getAcceptedMessage();
                        invitee.sendMessage(accepted);
                    } else if (group.members().contains(invitee)) {
                        invitee.sendMessage(Component.text("You have already joined this group"));
                    } else {
                        invitee.sendMessage(Component.text("You have not been invited to ")
                                .append(group.leader().getName()).append(Component.text("'s group")));
                    }
                }
            } else {
                sender.sendMessage("Group not found");
            }
        }, Literal("accept"), Entity("player").onlyPlayers(true).singleEntity(true));
    }
}
