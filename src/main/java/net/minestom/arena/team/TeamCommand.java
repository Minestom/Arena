package net.minestom.arena.team;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;

import static net.minestom.server.command.builder.arguments.ArgumentType.Entity;
import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

public final class TeamCommand extends Command {
    public TeamCommand() {
        super("team");
        setCondition(Conditions::playerOnly);

        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                if (TeamManager.getTeam(player) == null) {
                    TeamManager.createTeam(player);
                    sender.sendMessage("Team created");
                } else {
                    sender.sendMessage("You are in a team");
                }
            }
        }, Literal("create"));

        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                TeamManager.removePlayer(player);
            }
        }, Literal("leave"));

        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                Team team = TeamManager.getTeam(player);

                if (team == null) {
                    sender.sendMessage("You are not the owner of a team");
                } else {
                    TeamManager.removeTeam(player);
                }
            }
        }, Literal("disband"));

        addSyntax((sender, context) -> {
            final EntityFinder finder = context.get("player");
            final Player player = finder.findFirstPlayer(sender);

            if (sender instanceof Player inviter) {
                if (player != null) {
                    Team team = TeamManager.getTeam(inviter);

                    if (team != null) {
                        Component invite = team.getInvite();
                        player.sendMessage(invite);
                    } else {
                        sender.sendMessage("You are not in a team. Use /team create");
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
                Team team = TeamManager.getTeam(player);

                if (team != null) {
                    if (sender instanceof Player invitee) {
                        boolean wasInvited = team.getPendingInvites().contains(invitee);

                        if (wasInvited) {
                            team.addPlayer(invitee);
                            invitee.sendMessage(
                                    Component.text("You have been added to ")
                                            .append(team.getOwner())
                                            .append(Component.text("'s team")));
                        } else {
                            invitee.sendMessage(
                                    Component.text("You have not been invited to ")
                                            .append(team.getOwner()).append(Component.text("'s team")));
                        }
                    }
                } else {
                    sender.sendMessage("Team not found");
                }
            } else {
                sender.sendMessage("Team not found");
            }
        }, Literal("accept"), Entity("player").onlyPlayers(true).singleEntity(true));
    }
}