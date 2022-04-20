package net.minestom.arena.group.displays;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.arena.Messenger;
import net.minestom.arena.group.Group;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupSidebarDisplay implements GroupDisplay {
    private final Sidebar sidebar;

    public GroupSidebarDisplay() {
        sidebar = new Sidebar(Component.text("Group"));
    }

    private List<Sidebar.ScoreboardLine> createLines(Set<Player> players, Player leader) {
        List<Sidebar.ScoreboardLine> lines = new java.util.ArrayList<>();
        for (Player player : players) {
            if (player.equals(leader)) {
                lines.add(new Sidebar.ScoreboardLine(player.getUuid().toString(), Component.text("☆ ").color(NamedTextColor.WHITE).append(player.getName().color(Messenger.ORANGE_COLOR)), 1));
            } else {
                lines.add(new Sidebar.ScoreboardLine(player.getUuid().toString(), player.getName(), 0));
            }
        }

        return lines;
    }

    @Override
    public void update(Group group) {
        Set<Sidebar.ScoreboardLine> lines = sidebar.getLines();
        for (Sidebar.ScoreboardLine line : lines) {
            sidebar.removeLine(line.getId());
        }

        Set<Player> toUpdate = new HashSet<>(group.members());
        toUpdate.retainAll(sidebar.getPlayers());

        Set<Player> toRemove = new HashSet<>(sidebar.getPlayers());
        toRemove.removeAll(toUpdate);
        for (Player player : toRemove) {
            sidebar.removeViewer(player);
        }

        for (Sidebar.ScoreboardLine line : createLines(group.members(), group.leader()))
            sidebar.createLine(line);

        Set<Player> toAdd = new HashSet<>(group.members());
        toAdd.removeAll(sidebar.getPlayers());
        for (Player player : toAdd) {
            sidebar.addViewer(player);
        }
    }
}
