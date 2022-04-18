package net.minestom.arena.group;

import net.kyori.adventure.text.Component;
import net.minestom.arena.CommandUtils;
import net.minestom.arena.Messenger;
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
                    GroupImpl group = GroupManager.getMemberGroup(inviter);

                    if (group == null) {
                        group = GroupManager.getGroup(inviter);
                        Messenger.info(sender, "Group created");
                    } else if (group.members().contains(player)) {
                        Messenger.warn(sender, player.getName().append(Component.text(" is already in this group.")));
                    }

                    if (!group.members().contains(player)) {
                        Component invite = group.getInviteMessage();
                        group.addPendingInvite(player);
                        Messenger.info(player, invite);
                        Messenger.info(inviter, Component.text("Invite sent to ").append(player.getName()));
                    }
                } else {
                    Messenger.warn(sender, "Player not found");
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
                        GroupManager.getGroup(newLeader);
                        Messenger.info(sender, "Group created");
                    } else if (group.leader() == newLeader) {
                        Messenger.warn(sender, "You are already the leader");
                    } else if (group.leader() != player) {
                        Messenger.warn(sender, "You are not the leader of this group");
                    } else {
                        group.setLeader(newLeader);
                    }
                } else {
                    Messenger.warn(sender, "Player not found");
                }
            }
            // TODO: only show players in the group
        }, Literal("leader"), Entity("player").onlyPlayers(true).singleEntity(true));

        addSyntax((sender, context) -> {
            final EntityFinder finder = context.get("player");
            final Player toKick = finder.findFirstPlayer(sender);

            if (sender instanceof Player player) {
                if (toKick != null) {
                    GroupImpl group = GroupManager.getMemberGroup(player);

                    if (group == null) {
                        Messenger.info(sender, "You are not in a group");
                    } else if (group.leader() == player) {
                        if (toKick == player) {
                            GroupManager.removePlayer(player);
                        } else {
                            group.removeMember(toKick);
                        }
                    } else {
                        Messenger.warn(player, "You are not the leader of this group");
                    }
                } else {
                    Messenger.warn(sender, "Player not found");
                }
            }
            // TODO: only show players in the group
        }, Literal("kick"), Entity("player").onlyPlayers(true).singleEntity(true));

        addSyntax((sender, context) -> {
            final EntityFinder finder = context.get("player");
            final Player player = finder.findFirstPlayer(sender);
            if (player != null) {
                GroupImpl group = GroupManager.getGroup(player);

                if (sender instanceof Player invitee) {
                    boolean wasInvited = group.getPendingInvites().contains(invitee);
                    if (wasInvited) {
                        GroupManager.removePlayer(invitee); // Remove from old group
                        group.addMember(invitee);
                        Component accepted = group.getAcceptedMessage();
                        invitee.sendMessage(accepted);
                    } else if (group.members().contains(invitee)) {
                        Messenger.warn(invitee, "You are already in this group");
                    } else {
                        Messenger.warn(invitee, Component.text("You have not been invited to ")
                                .append(group.leader().getName()).append(Component.text("'s group")));
                    }
                }
            } else {
                Messenger.warn(sender, "Group not found");
            }
        }, Literal("accept"), Entity("player").onlyPlayers(true).singleEntity(true));
    }
}
