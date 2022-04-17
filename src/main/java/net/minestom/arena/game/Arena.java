package net.minestom.arena.game;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Arena {

    @NotNull Tag<UUID> arenaTag = Tag.UUID("arena");

    @NotNull Instance getArenaInstance();

    CompletableFuture<Void> join(@NotNull Player player);
    void start();

}
