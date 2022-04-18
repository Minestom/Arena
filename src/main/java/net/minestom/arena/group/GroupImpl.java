package net.minestom.arena.group;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

final class GroupImpl implements Group {
    private final Set<Player> players = new HashSet<>();
    private final Set<Player> pendingInvites = Collections.newSetFromMap(new WeakHashMap<>());

    private Player leader;

    @Override
    public @NotNull Player leader() {
        return leader;
    }

    GroupImpl(@NotNull Player leader) {
        this.leader = leader;
        players.add(leader);
    }

    @Override
    public @NotNull List<Player> members() {
        return List.copyOf(players);
    }

    public void addPendingInvite(@NotNull Player player) {
        pendingInvites.add(player);
    }

    public @NotNull Set<Player> getPendingInvites() {
        return pendingInvites;
    }

    public void addPlayer(@NotNull Player player) {
        players.forEach(p -> p.sendMessage(player.getName().append(Component.text(" has joined your group"))));
        players.add(player);
        pendingInvites.remove(player);
    }

    public void removePlayer(@NotNull Player player) {
        if (players.contains(player)) {
            players.remove(player);
            players.forEach(p -> p.sendMessage(player.getName().append(Component.text(" has left your group"))));
        }
    }

    public @NotNull Component getInviteMessage() {
        return leader.getName()
                .append(Component.text(" Has invited you to join their group. "))
                .append(Component.text("[Accept]").clickEvent(
                        ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/group accept " + leader.getUsername())
                ));
    }

    public Component getAcceptedMessage() {
        return (Component.text("You have been added to ")
                .append(leader.getName())
                .append(Component.text("'s group")));
    }

    public void setLeader(@NotNull Player player) {
        this.leader = player;
    }
}
