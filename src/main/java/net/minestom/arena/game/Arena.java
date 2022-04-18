package net.minestom.arena.game;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Arena {
    void join(@NotNull Player player);
}
