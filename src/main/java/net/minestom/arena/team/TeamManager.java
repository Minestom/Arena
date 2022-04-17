package net.minestom.arena.team;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TeamManager {
    private static final Map<Player, Team> teams = new HashMap<>();

    public static Team getTeam(Player player) {
        return teams.get(player);
    }

    public static void createTeam(Player player) {
        teams.put(player, new Team(player));
    }

    public static void removeTeam(Player player) {
        Team team = teams.get(player);

        if (team != null) {
            team.disband();
            teams.remove(player);
        }
    }

    public static void transferOwnership(Player player) {
        Team team = teams.get(player);
        team.removePlayer(player);

        if (team.getPlayers().size() != 0) {
            Player newOwner = team.getPlayers().get(0);
            team.setOwner(newOwner);

            team.getPlayers().forEach(member -> {
                member.sendMessage(Component.text("Team ownership has been transferred to ").append(newOwner.getName()));
            });
            
            teams.remove(player);
            teams.put(newOwner, team);
        }
    }
}
