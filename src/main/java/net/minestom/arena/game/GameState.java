package net.minestom.arena.game;

public enum GameState {
    CREATED(0), INITIALIZING(1), STARTING(2), STARTED(2), ENDING(3), SHUTTINGDOWN(3), ENDED(4), KILLED(4);

    private final int sequence;

    GameState(int sequence) {
        this.sequence = sequence;
    }

    public boolean isAfter(GameState state) {
        return this.sequence > state.sequence;
    }

    public boolean isOrAfter(GameState state) {
        return this.sequence >= state.sequence;
    }
}
