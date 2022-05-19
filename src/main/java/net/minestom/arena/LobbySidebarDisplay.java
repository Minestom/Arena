package net.minestom.arena;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.arena.group.Group;
import net.minestom.arena.group.displays.GroupSidebarDisplay;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;

public final class LobbySidebarDisplay extends GroupSidebarDisplay {
    public LobbySidebarDisplay(Group group) {
        super(group);
    }

    @Override
    protected Sidebar.ScoreboardLine createPlayerLine(Player player, Group group) {
        if (player.equals(group.leader())) {
            return new Sidebar.ScoreboardLine(
                    player.getUuid().toString(),
                    Component.text(Icons.STAR + " ").color(NamedTextColor.WHITE).append(player.getName().color(Messenger.ORANGE_COLOR)),
                    1
            );
        } else {
            return new Sidebar.ScoreboardLine(
                    player.getUuid().toString(),
                    player.getName(),
                    0
            );
        }
    }
}
