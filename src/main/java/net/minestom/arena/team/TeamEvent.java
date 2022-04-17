package net.minestom.arena.team;

import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDisconnectEvent;

public class TeamEvent {
    public static void hook(GlobalEventHandler eventHandler) {
        eventHandler.addListener(PlayerDisconnectEvent.class, event -> {
            TeamManager.removePlayer(event.getPlayer());

            Player player = event.getPlayer();
            if (TeamManager.getTeam(player) != null) {
                TeamManager.transferOwnership(player);
            }
        });
    }
}
