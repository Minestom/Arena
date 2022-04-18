package net.minestom.arena.group;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public sealed interface Group permits GroupImpl {
    static Group findGroup(@NotNull Player player) {
        return GroupManager.getGroup(player);
    }

    @NotNull Player leader();

    @NotNull List<@NotNull Player> members();
}
