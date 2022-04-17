package net.minestom.arena.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Team {
    private final Set<Player> players = new HashSet<>();
    private final Player owner;

    public Team(Player owner) {
        this.owner = owner;
    }

    public Set<Player> getPlayers() {
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
        return owner.getName().append(Component.text(" Has invited you to join his team.")).append(Component.text("Accept?").clickEvent(
                ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/team accept " + owner.getName())
        ));
    }

    public Component getOwner() {
        return owner.getName();
    }
}
