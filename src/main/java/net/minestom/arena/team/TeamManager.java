package net.minestom.arena.team;

import net.minestom.server.entity.Player;

import java.util.HashMap;

public class TeamManager {
    private static final HashMap<Player, Team> teams = new HashMap<>();

    public static Team getTeam(Player player) {
        return teams.get(player);
    }

    public static void createTeam(Player player) {
        teams.put(player, new Team(player));
    }
}
