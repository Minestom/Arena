package net.minestom.arena.utils;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;

public final class ConcurrentUtils {
    private ConcurrentUtils() {
        //no instance
    }

    /**
     * Used to add timeout for CompletableFutures
     *
     * @param future the future which has to complete
     * @param timeout duration to wait for the future to complete
     * @param action Action to run after the future completes or the timeout is reached.<br>
     *               Parameter means:
     *               <ul>
     *               <li><b>true</b> - the timeout is reached</li>
     *               <li><b>false</b> - future completed before timeout</li>
     *               </ul>
     * @return the new CompletionStage
     */
    public static CompletableFuture<Void> thenRunOrTimeout(CompletableFuture<?> future, Duration timeout, BooleanConsumer action) {
        final CompletableFuture<Boolean> f = new CompletableFuture<>();
        CompletableFuture.delayedExecutor(timeout.toNanos(), TimeUnit.NANOSECONDS).execute(() -> f.complete(true));
        future.thenRun(() -> f.complete(false));
        return f.thenAccept(action);
    }

    /**
     * Create a future from a CountDownLatch
     *
     * @return a future that completes when the countdown reaches zero
     */
    public static CompletableFuture<Void> futureFromCountdown(CountDownLatch countDownLatch) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try {
                countDownLatch.await();
                future.complete(null);
            } catch (InterruptedException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public static <V> boolean testAndSet(AtomicReference<V> reference, BiPredicate<V, V> predicate, V testValue, V newValue) {
        for (;;) {
            V prev = reference.get();
            if (predicate.test(prev, testValue)) {
                if (reference.compareAndSet(prev, newValue)) return true;
            } else {
                return false;
            }
        }
    }

    public static <V> boolean testAndSet(AtomicReference<V> reference, BiPredicate<V, V> predicate, V newValue) {
        return testAndSet(reference, predicate, newValue, newValue);
    }
}
