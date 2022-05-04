package net.minestom.arena.game;

import net.minestom.arena.utils.VoidFuture;

import java.time.Duration;
import java.util.Date;

public abstract class Game {
    private final VoidFuture gameFuture = new VoidFuture();
    private State state = State.CREATED;
    private Date start;
    private Date stop;
    private final static Duration END_TIMEOUT = Duration.ofMinutes(10);
    public enum State { CREATED, INITIALIZING, STARTING, STARTED, ENDING, ENDED }

    public final VoidFuture getGameFuture() {
        return this.gameFuture;
    }

    public final Date getStartDate() {
        return start;
    }

    public final Date getStopDate() {
        return stop;
    }

    public final State getState() {
        return this.state;
    }

    public final Duration getEndTimeout() {
        return END_TIMEOUT;
    }

    protected abstract VoidFuture init();
    protected abstract VoidFuture onStart();

    public final void start() {
        this.state = State.INITIALIZING;
        init().thenRun(() -> {
            this.state = State.STARTING;
            onStart().thenRun(() -> {
                this.state = State.STARTED;
                this.start = new Date();
            });
        });
    }

    protected abstract VoidFuture onStop(boolean normalEnding);

    protected void kill() {}

    public final VoidFuture stop(boolean normalEnding) {
        if (this.state == State.ENDING || this.state == State.ENDED) return this.gameFuture;
        this.state = State.ENDING;
        onStop(normalEnding).thenRun(END_TIMEOUT, (timeoutReached) -> {
            this.state = State.ENDED;
            this.stop = new Date();
            this.gameFuture.complete(null);
            if (timeoutReached) {
                this.kill();
            }
        });
        return gameFuture;
    }
}
