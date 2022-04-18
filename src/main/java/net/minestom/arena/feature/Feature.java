package net.minestom.arena.feature;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;

public interface Feature {
    void hook(@NotNull EventNode<Event> node);
}
