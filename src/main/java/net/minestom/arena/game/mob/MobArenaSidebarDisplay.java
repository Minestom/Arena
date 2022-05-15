package net.minestom.arena.game.mob;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.arena.Icons;
import net.minestom.arena.Messenger;
import net.minestom.arena.group.Group;
import net.minestom.arena.group.displays.GroupSidebarDisplay;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;

final class MobArenaSidebarDisplay extends GroupSidebarDisplay {
    private final MobArena arena;

    public MobArenaSidebarDisplay(MobArena arena) {
        super(arena.group());
        this.arena = arena;
    }

    @Override
    protected Sidebar.ScoreboardLine createPlayerLine(Player player, Group group) {
        final ArenaClass arenaClass = arena.playerClass(player);
        Component icon = Component.text(arenaClass.icon(), arenaClass.color());

        if (!arena.stageInProgress()) {
            icon = arena.hasContinued(player)
                ? Component.text(Icons.CHECKMARK, NamedTextColor.GREEN)
                : Component.text(Icons.CROSS, NamedTextColor.RED);
        }

        return new Sidebar.ScoreboardLine(
                player.getUuid().toString(),
                icon.append(Component.text(" ")).append(player.getName().color(Messenger.ORANGE_COLOR)),
                3
        );
    }
}
