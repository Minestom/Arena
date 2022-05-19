package net.minestom.arena.game.mob;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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

    // TODO: Probably a better way to make the whole component gray
    @Override
    protected Sidebar.ScoreboardLine createPlayerLine(Player player, Group group) {
        final ArenaClass arenaClass = arena.playerClass(player);
        final boolean dead = !arena.instance().getPlayers().contains(player);

        Component icon = Component.text(arenaClass.icon(), arenaClass.color());
        if (!arena.stageInProgress()) {
            icon = arena.hasContinued(player)
                ? Component.text(Icons.CHECKMARK, dead ? NamedTextColor.GRAY : NamedTextColor.GREEN)
                : Component.text(Icons.CROSS, dead ? NamedTextColor.GRAY : NamedTextColor.RED);
        }

        Component line = icon.append(Component.text(" "))
                .append(player.getName().color(dead ? NamedTextColor.GRAY : Messenger.ORANGE_COLOR));

        // Strikethrough if player is dead
        if (dead) line = line.decorate(TextDecoration.STRIKETHROUGH);

        return new Sidebar.ScoreboardLine(
                player.getUuid().toString(),
                line,
                0
        );
    }
}
