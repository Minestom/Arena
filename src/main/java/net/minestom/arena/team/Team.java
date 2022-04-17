package net.minestom.arena.team;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Team {
    private final Set<Player> players = new HashSet<>();
    private final Set<Player> pendingInvites = Collections.newSetFromMap(new WeakHashMap<>());
    private final Audience playersAsAudience = PacketGroupingAudience.of(players);

    private Player owner;

    public Team(Player owner) {
        this.owner = owner;
    }

    public @NotNull Set<Player> getPlayers() {
        return players;
    }

    public @NotNull Audience getPlayersAsAudience() {
        return playersAsAudience;
    }

    public void addPendingInvite(@NotNull Player player) {
        pendingInvites.add(player);
    }

    public @NotNull Set<Player> getPendingInvites() {
        return pendingInvites;
    }

    public void addPlayer(@NotNull Player player) {
        players.add(player);
        pendingInvites.remove(player);
    }

    public void removePlayer(@NotNull Player player) {
        if (players.contains(player)) {
            players.remove(player);
            playersAsAudience.sendMessage(player.getName().append(Component.text(" has left your team.")));
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

    public @NotNull Component getOwner() {
        return owner.getName();
    }

    public void setOwner(@NotNull Player player) {
        this.owner = player;
    }

    public void disband() {
        playersAsAudience.sendMessage(Component.text("Your team has been disbanded."));
        players.clear();
    }
}
