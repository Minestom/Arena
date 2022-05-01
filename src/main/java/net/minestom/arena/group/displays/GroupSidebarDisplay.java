package net.minestom.arena.group.displays;

import net.kyori.adventure.text.Component;
import net.minestom.arena.group.Group;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class GroupSidebarDisplay implements GroupDisplay {
    private final Sidebar sidebar = new Sidebar(Component.text("Group"));
    private final Group group;

    public GroupSidebarDisplay(Group group) {
        this.group = group;
    }

    private List<Sidebar.ScoreboardLine> createLines() {
        List<Sidebar.ScoreboardLine> lines = new java.util.ArrayList<>();
        for (Player player : group.members()) {
            lines.add(createPlayerLine(player, group));
        }

        lines.addAll(createAdditionalLines());

        return lines;
    }

    protected abstract Sidebar.ScoreboardLine createPlayerLine(Player player, Group group);

    protected List<Sidebar.ScoreboardLine> createAdditionalLines() {
        return List.of();
    }

    @Override
    public final void update() {
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

        for (Sidebar.ScoreboardLine line : createLines())
            sidebar.createLine(line);

        Set<Player> toAdd = new HashSet<>(group.members());
        toAdd.removeAll(sidebar.getPlayers());
        for (Player player : toAdd) {
            sidebar.addViewer(player);
        }
    }
}
