package net.minestom.arena.game;

public interface GenerationContext {
    int generated();

    void setGenerated(int generated);

    default void incrementGenerated() {
        setGenerated(generated() + 1);
    }
}
