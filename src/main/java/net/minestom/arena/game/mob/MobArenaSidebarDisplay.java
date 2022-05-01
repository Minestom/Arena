package net.minestom.arena.game.mob;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.arena.group.Group;
import net.minestom.arena.group.displays.GroupSidebarDisplay;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;

import java.util.List;

final class MobArenaSidebarDisplay extends GroupSidebarDisplay {
    private final MobArena arena;

    public MobArenaSidebarDisplay(MobArena arena) {
        super(arena.group());
        this.arena = arena;
    }

    @Override
    protected Sidebar.ScoreboardLine createPlayerLine(Player player, Group group) {
        return new Sidebar.ScoreboardLine(player.getUuid().toString(), player.getName(), 2);
    }

    @Override
    protected List<Sidebar.ScoreboardLine> createAdditionalLines() {
        return List.of(
                new Sidebar.ScoreboardLine("empty", Component.empty(), 1),
                new Sidebar.ScoreboardLine("coins", Component.text("Coins: " + arena.coins(), NamedTextColor.GOLD), 0)
        );
    }
}
