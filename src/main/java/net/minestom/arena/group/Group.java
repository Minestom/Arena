package net.minestom.arena.group;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public sealed interface Group permits GroupImpl {
    static Group findGroup(@NotNull Player player) {
        return GroupManager.getGroup(player);
    }

    @NotNull Player leader();
    @NotNull Set<@NotNull Player> members();
}
