package net.minestom.arena.event;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

public class PreDeathEvent implements EntityInstanceEvent, CancellableEvent {

    private final Entity entity;
    private boolean cancelled;

    public PreDeathEvent(@NotNull Entity entity) {
        this.entity = entity;
    }


    @Override
    public @NotNull Entity getEntity() {
        return entity;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
