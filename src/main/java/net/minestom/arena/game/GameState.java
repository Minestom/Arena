package net.minestom.arena.game;

public enum GameState {
    CREATED(0), INITIALIZING(1), STARTING(2), STARTED(3), ENDING(4), SHUTTING_DOWN(4), ENDED(5), KILLED(5);

    private final int sequence;

    GameState(int sequence) {
        this.sequence = sequence;
    }

    public boolean isBefore(GameState state) {
        return this.sequence < state.sequence;
    }

    public boolean isOrBefore(GameState state) {
        return this.sequence <= state.sequence;
    }

    public boolean isAfter(GameState state) {
        return this.sequence > state.sequence;
    }

    public boolean isOrAfter(GameState state) {
        return this.sequence >= state.sequence;
    }
}
