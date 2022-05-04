package net.minestom.arena.utils;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class VoidFuture extends CompletableFuture<Void> {
    private static final VoidFuture COMPLETED = new VoidFuture() {{
        complete();
    }};

    public static VoidFuture completedFuture() {
        return COMPLETED;
    }

    public void complete() {
        this.complete(null);
    }

    public CompletableFuture<Boolean> thenRun(Duration timeout, BooleanConsumer action) {
        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        CompletableFuture.delayedExecutor(timeout.toNanos(), TimeUnit.NANOSECONDS).execute(() -> future.complete(true));
        this.thenRun(() -> future.complete(false));
        future.thenAccept(action);
        return future;
    }
}