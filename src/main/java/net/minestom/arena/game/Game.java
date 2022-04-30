package net.minestom.arena.game;

import java.util.concurrent.CompletableFuture;

public abstract class Game {

    private final CompletableFuture<Void> gameFuture;
    private State state;

    enum State {
        STARTING,
        STARTED,
        ENDING,
        ENDED
    }

    public Game() {
        this.gameFuture = new CompletableFuture<>();
        this.state = State.STARTING;
    }

    /**
     * Used to wait for game end
     * @return a future that's completed when the game ended and results are saved
     */
    public CompletableFuture<Void> getGameFuture() {
        return this.gameFuture;
    }

    public State getState() {
        return this.state;
    }

    public void start() {
        this.state = State.STARTED;
    }

    /**
     * Gracefully stop game as soon as possible and save results
     */
    protected void stop() {}

    /**
     * Calling this method will initiate the ending of this game by calling
     * {@link #stop()} to gracefully end the game and save results
     */
    public final void end() {
        switch (this.state) {
            case STARTING, STARTED -> {
                this.state = State.ENDING;
                stop();
                this.state = State.ENDED;
                this.gameFuture.complete(null);
            }
        }
    }
}
