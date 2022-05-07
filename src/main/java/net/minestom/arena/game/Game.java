package net.minestom.arena.game;

import net.minestom.arena.utils.ConcurrentUtils;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Game {
    private final CompletableFuture<Void> gameFuture = new CompletableFuture<>();
    private State state = State.CREATED;
    private Date start;
    private Date end;
    private final static Duration END_TIMEOUT = Duration.ofMinutes(10);
    public enum State {
        CREATED(0), INITIALIZING(1), STARTING(2), STARTED(2), ENDING(3), SHUTTINGDOWN(3), ENDED(4), KILLED(4);

        private final int sequence;
        State(int sequence) {
            this.sequence = sequence;
        }

        public boolean isAfter(State state) {
            return this.sequence > state.sequence;
        }

        public boolean isOrAfter(State state) {
            return this.sequence >= state.sequence;
        }
    }
    private final ReentrantLock stateLock = new ReentrantLock();

    /**
     * Used to get a future that represents this game life
     *
     * @return a future that is completed when the game state is either {@link State#ENDED} or {@link State#KILLED}
     */
    public final CompletableFuture<Void> getGameFuture() {
        return this.gameFuture;
    }

    public final Date getStartDate() {
        return start;
    }

    public final Date getStopDate() {
        return end;
    }

    public final State getState() {
        return this.state;
    }

    /**
     * Used to prepare the game for players e.g. generate the world, summon entities, register listeners, etc.
     * Players SHOULD NOT be altered in this state
     *
     * @return a future that completes when the game can be started with {@link #start()}
     */
    protected abstract CompletableFuture<Void> init();

    /**
     * Used to start the game, here you can change the players' instance, etc.
     *
     * @return a future that completes when the actual gameplay begins
     */
    protected abstract CompletableFuture<Void> onStart();

    /**
     * Used to start the game, the start sequence is the following (note that a shutdown will interrupt this flow):
     * <ol>
     *     <li>Set state to {@link State#INITIALIZING}</li>
     *     <li>Execute {@link #init()}</li>
     *     <li>Set state to {@link State#STARTING}</li>
     *     <li>Execute {@link #onStart()}</li>
     *     <li>Set state to {@link State#STARTED}</li>
     * </ol>
     *
     * @return {@link #getGameFuture()}
     * @throws RuntimeException if called when the state isn't {@link State#CREATED}
     */
    public final CompletableFuture<Void> start() {
        this.stateLock.lock();
        if (this.state.isAfter(State.CREATED)) {
            throw new RuntimeException("Cannot start a Game twice!");
        }
        this.state = State.INITIALIZING;
        this.stateLock.unlock();
        init().thenRun(() -> {
            this.stateLock.lock();
            if (this.state != State.INITIALIZING) {
                // A shutdown has been initiated during initialization, don't start the game
                this.stateLock.unlock();
                return;
            }
            this.state = State.STARTING;
            this.stateLock.unlock();
            onStart().thenRun(() -> {
                this.stateLock.lock();
                if (this.state != State.STARTING) {
                    // A shutdown has been initiated during game start, don't change state
                    this.stateLock.unlock();
                    return;
                }
                this.state = State.STARTED;
                this.stateLock.unlock();
                this.start = new Date();
            });
        });
        return getGameFuture();
    }

    /**
     * Used to reset the players after the game
     *
     * @return a future that completes when all players have been reset, this SHOULD NOT wait on gameplay
     */
    protected abstract CompletableFuture<Void> onEnd();

    /**
     * Used to prepare the game for ending within the specified timeout
     *
     * @param shutdownTimeout duration in which the game should end
     * @return a future which is completed when the internal state of the game allows the call of {@link #end()}
     */
    protected abstract CompletableFuture<Void> onShutdown(Duration shutdownTimeout);

    /**
     * Used to shut down the game gracefully, shutdown process id the following:
     * <ol>
     *     <li>Call {@link #onShutdown(Duration)} with the timeout</li>
     *     <li>Wait for the returned future to complete or the timeout to be reached</li>
     *     <li>If <b>(A)</b> the timeout wasn't reached continue with the normal ending procedure by calling {@link #end()}
     *     or if it was reached, but <b>(B)</b> the game already ended then return otherwise <b>(C)</b> kill the game</li>
     * </ol>
     *
     * @return {@link #getGameFuture()}
     */
    public final CompletableFuture<Void> shutdown() {
        this.stateLock.lock();
        if (this.state.isAfter(State.ENDING)) {
            this.stateLock.unlock();
            return getGameFuture();
        }
        this.state = State.SHUTTINGDOWN;
        this.stateLock.unlock();
        ConcurrentUtils.thenRunOrTimeout(onShutdown(END_TIMEOUT), END_TIMEOUT, (timeoutReached) -> {
            if (timeoutReached) {
                this.stateLock.lock();
                if (this.state.isAfter(State.ENDING)) {
                    // The game ended already, we can safely return
                    this.stateLock.unlock();
                    return;
                }
                // Kill game
                this.state = State.KILLED;
                this.stateLock.unlock();
                this.end = new Date();
                this.gameFuture.complete(null);
                this.kill();
            } else {
                // Execute normal end procedure
                end();
            }
        });
        return getGameFuture();
    }

    protected void kill() {}

    /**
     * Used to end the game normally, only the first call will execute {@link #onEnd()}
     * multiple calls to this method will be ignored
     *
     * @return {@link #getGameFuture()}
     */
    public final CompletableFuture<Void> end() {
        this.stateLock.lock();
        if (this.state.isOrAfter(State.ENDING)) {
            this.stateLock.unlock();
            return getGameFuture();
        }
        this.state = State.ENDING;
        this.stateLock.unlock();
        onEnd().thenRun(() -> {
            this.stateLock.lock();
            if (this.state == State.KILLED) {
                // Game was killed, don't alter the state
                this.stateLock.unlock();
                return;
            }
            this.state = State.ENDED;
            this.stateLock.unlock();
            this.end = new Date();
            this.gameFuture.complete(null);
        });
        return getGameFuture();
    }
}
