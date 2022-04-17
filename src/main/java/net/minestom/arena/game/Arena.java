package net.minestom.arena.game;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public abstract class Arena {

    protected final @NotNull Instance arenaInstance;

    protected Arena(@NotNull Instance arenaInstance) {
        this.arenaInstance = arenaInstance;
    }

    abstract public CompletableFuture<Void> join(@NotNull Player player);
    abstract public void start();

}
