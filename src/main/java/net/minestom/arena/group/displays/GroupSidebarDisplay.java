package net.minestom.arena.group.displays;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.arena.group.Group;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;

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
                lines.add(new Sidebar.ScoreboardLine(player.getUuid().toString(), Component.text("☆ ").color(NamedTextColor.WHITE).append(player.getName().color(NamedTextColor.LIGHT_PURPLE)), 0));
            } else {
                lines.add(new Sidebar.ScoreboardLine(player.getUuid().toString(), player.getName(), 1));
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

        Set<Player> toUpdate = new java.util.HashSet<>(group.members());
        toUpdate.retainAll(sidebar.getPlayers());

        Set<Player> toRemove = new java.util.HashSet<>(sidebar.getPlayers());
        toRemove.removeAll(toUpdate);
        for (Player player : toRemove) {
            sidebar.removeViewer(player);
        }

        for (Sidebar.ScoreboardLine line : createLines(group.members(), group.leader()))
            sidebar.createLine(line);

        Set<Player> toAdd = new java.util.HashSet<>(group.members());
        toAdd.removeAll(sidebar.getPlayers());
        for (Player player : toAdd) {
            sidebar.addViewer(player);
        }
    }
}
