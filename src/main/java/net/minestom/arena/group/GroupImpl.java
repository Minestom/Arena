package net.minestom.arena.group;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.arena.Messenger;
import net.minestom.arena.group.displays.GroupDisplay;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

final class GroupImpl implements Group {
    private final Set<Player> players = new HashSet<>();
    private final Set<Player> pendingInvites = Collections.newSetFromMap(new WeakHashMap<>());

    private Player leader;
    private GroupDisplay display;

    @Override
    public @NotNull Player leader() {
        return leader;
    }

    GroupImpl(@NotNull Player leader) {
        this.leader = leader;
        players.add(leader);
    }

    @Override
    public @NotNull Set<Player> members() {
        return Set.copyOf(players);
    }

    @Override
    public @NotNull GroupDisplay display() {
        return display;
    }

    @Override
    public void setDisplay(@NotNull GroupDisplay display) {
        if (this.display != null) this.display.clean();
        this.display = display;
        display.update();
    }

    public void addPendingInvite(@NotNull Player player) {
        pendingInvites.add(player);
    }

    public @NotNull Set<Player> getPendingInvites() {
        return pendingInvites;
    }

    public void addMember(@NotNull Player player) {
        if (players.add(player)) {
            pendingInvites.remove(player);
            players.forEach(p -> Messenger.info(p, player.getName().append(Component.text(" has joined your group"))));
            display.update();
        }
    }

    public boolean removeMember(@NotNull Player player) {
        if (players.remove(player)) {
            players.forEach(p -> Messenger.info(p, player.getName().append(Component.text(" has left your group"))));
            display.update();
            return true;
        }
        return false;
    }

    public @NotNull Component getInviteMessage() {
        return leader.getName()
                .append(Component.text(" Has invited you to join their group. "))
                .append(Component.text("[Accept]").color(NamedTextColor.GREEN).clickEvent(
                        ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/group accept " + leader.getUsername())
                ));
    }

    public Component getAcceptedMessage() {
        return Component.text("You have been added to ")
                .append(leader.getName())
                .append(Component.text("'s group"));
    }

    public void setLeader(@NotNull Player player) {
        this.leader = player;
        players.forEach(p -> Messenger.info(p, player.getName().append(Component.text(" has become the group leader"))));
        display.update();
    }
}
