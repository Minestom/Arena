package net.minestom.arena.group;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;

public class GroupEvent {
    public static void hook(EventNode<Event> eventHandler) {
        eventHandler.addListener(PlayerDisconnectEvent.class, event -> GroupManager.removePlayer(event.getPlayer()));
    }
}
