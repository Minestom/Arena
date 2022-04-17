package net.minestom.arena.team;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

        Optional<Player> newOwner = team.getPlayers().stream().findFirst();

        if (newOwner.isPresent()) {
            team.setOwner(newOwner.get());

            team.getPlayers().forEach(member -> {
                member.sendMessage(Component.text("Team ownership has been transferred to ").append(newOwner.get().getName()));
            });

            teams.remove(player);
            teams.put(newOwner.get(), team);
        }
    }

    public static void removePlayer(Player player) {
        teams.values().forEach(team -> team.removePlayer(player));
    }
}
