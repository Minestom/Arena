package net.minestom.arena.game;

import net.minestom.arena.group.Group;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public abstract class Arena extends Game {
    @NotNull
    protected abstract Group group();
}
