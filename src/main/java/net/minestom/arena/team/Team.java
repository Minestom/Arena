package net.minestom.arena.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.entity.Player;

import java.util.*;

public class Team {
    private final List<Player> players = new ArrayList<>();
    private Player owner;

    public Team(Player owner) {
        this.owner = owner;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
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
