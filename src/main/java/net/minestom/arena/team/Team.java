package net.minestom.arena.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.entity.Player;

import java.util.*;

public class Team {
    private final Set<Player> players = new HashSet<>();
    private final Set<Player> pendingInvites = Collections.newSetFromMap(new WeakHashMap<>());

    private Player owner;

    public Team(Player owner) {
        this.owner = owner;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public void addPendingInvite(Player player) {
        pendingInvites.add(player);
    }

    public Set<Player> getPendingInvites() {
        return pendingInvites;
    }

    public void addPlayer(Player player) {
        players.add(player);
        pendingInvites.remove(player);
    }

    public void removePlayer(Player player) {
        if (players.contains(player)) {
            players.remove(player);
            players.forEach(p -> p.sendMessage(player.getName().append(Component.text(" has left your team."))));
        }
    }

    public UUID getTeamUUID() {
        return owner.getUuid();
    }

    public Component getInvite() {
        return owner.getName()
                .append(Component.text(" Has invited you to join his team. "))
                .append(Component.text("[Accept]").clickEvent(
                    ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/team accept " + owner.getUsername())
                ));
    }

    public Component getOwner() {
        return owner.getName();
    }

    public void setOwner(Player player) {
        this.owner = player;
    }

    public void disband() {
        players.forEach(player -> player.sendMessage(Component.text("Your team has been disbanded.")));
        players.clear();
    }
}
