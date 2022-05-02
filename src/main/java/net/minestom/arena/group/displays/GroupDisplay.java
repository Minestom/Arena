package net.minestom.arena.group.displays;

public interface GroupDisplay {
    void update();
    default void clean() {}
}
