package net.minestom.arena.feature;

import net.kyori.adventure.text.Component;
import net.minestom.arena.Lobby;
import net.minestom.arena.event.PreDeathEvent;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class DeathFeature implements Feature {

    private final Supplier<Component> endMessage;

    public DeathFeature(@NotNull Supplier<Component> endMessage) {
        this.endMessage = endMessage;
    }

    @Override
    public void hook(@NotNull EventNode<Event> node) {
        node.addListener(PreDeathEvent.class, event -> {
            if (event.getEntity() instanceof Player player) {
                event.setCancelled(true);
                player.setInstance(Lobby.INSTANCE);
                player.sendMessage(endMessage.get());
            }
        });
    }
}
