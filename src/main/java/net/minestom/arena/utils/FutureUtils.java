package net.minestom.arena.utils;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class FutureUtils {

    public static CompletableFuture<Void> completeAfter(Duration duration) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        CompletableFuture.delayedExecutor(duration.toNanos(), TimeUnit.NANOSECONDS).execute(() -> future.complete(null));
        return future;
    }
}
