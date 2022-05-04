package net.minestom.arena.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class CountDownFuture extends CountDownLatch {
    private final int initialCount;

    public CountDownFuture(int count) {
        super(count);
        this.initialCount = count;
    }

    public int getInitialCount() {
        return initialCount;
    }

    public CompletableFuture<Void> thenRun(Runnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                this.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).thenRun(runnable);
    }
}
