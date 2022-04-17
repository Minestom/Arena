package net.minestom.arena.team;

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
}
