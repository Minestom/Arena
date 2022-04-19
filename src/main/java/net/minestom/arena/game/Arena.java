package net.minestom.arena.game;

import net.minestom.arena.group.Group;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface Arena {
    @NotNull Group group();

    @NotNull CompletableFuture<Void> init();

    int stage();
}
