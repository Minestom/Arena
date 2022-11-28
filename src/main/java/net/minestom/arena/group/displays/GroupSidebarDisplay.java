package net.minestom.arena.group.displays;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.arena.Messenger;
import net.minestom.arena.group.Group;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class GroupSidebarDisplay implements GroupDisplay {
    private static final int MAX_SCOREBOARD_LINES = 15;

    private final Sidebar sidebar = new Sidebar(Component.text("Group"));
    private final Group group;

    public GroupSidebarDisplay(Group group) {
        this.group = group;
    }

    private List<Sidebar.ScoreboardLine> createLines() {
        List<Sidebar.ScoreboardLine> lines = new ArrayList<>();

        List<Player> groupMembers = group.members();
        // separate check is required to prevent "1 more..." from occurring when the player could just be displayed.
        if (groupMembers.size() <= MAX_SCOREBOARD_LINES) {
            for (Player player : groupMembers) {
                lines.add(createPlayerLine(player, group));
            }
        } else {
            for (int i = 0; i < groupMembers.size() && i < 14; i++) {
                lines.add(createPlayerLine(groupMembers.get(i), group));
            }
            lines.add(new Sidebar.ScoreboardLine(
                    "more",
                    Component.text(groupMembers.size() - 14 + " more...", NamedTextColor.DARK_GREEN),
                    -1
            ));
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

    @Override
    public void clean() {
        for (Player viewer : sidebar.getViewers()) {
            sidebar.removeViewer(viewer);
        }
    }
}
