package net.minestom.arena.group;

import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDisconnectEvent;

public class GroupEvent {
    public static void hook(GlobalEventHandler eventHandler) {
        eventHandler.addListener(PlayerDisconnectEvent.class, event -> {
            GroupManager.removePlayer(event.getPlayer());

            Player player = event.getPlayer();
            if (GroupManager.getGroup(player) != null) {
                GroupManager.transferOwnership(player);
            }
        });
    }
}
