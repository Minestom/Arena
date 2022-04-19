package net.minestom.arena.group;

import net.minestom.arena.game.Arena;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public sealed interface Group permits GroupImpl {
    static Group findGroup(@NotNull Player player) {
        return GroupManager.getGroup(player);
    }

    void play(Arena arena);

    @NotNull Player leader();
    @NotNull Set<@NotNull Player> members();
    @Nullable Arena arena();
}
