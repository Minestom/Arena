package net.minestom.arena.team;

import net.kyori.adventure.text.Component;
import net.minestom.arena.MessageUtils;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TeamManager {
    private static final Map<Player, Team> teams = new HashMap<>();

    public static @Nullable Team getTeam(@NotNull Player player) {
        return teams.get(player);
    }

    public static void createTeam(@NotNull Player player) {
        teams.put(player, new Team(player));
    }

    public static void removeTeam(@NotNull Player player) {
        Team team = teams.get(player);

        if (team != null) {
            team.disband();
            teams.remove(player);
        }
    }

    public static boolean transferOwnership(Player player) {
        Team team = teams.get(player);
        Optional<Player> newOwner = team.getPlayers().stream().findFirst();

        if (newOwner.isPresent()) {
            team.setOwner(newOwner.get());

            team.getPlayersAsAudience().sendMessage(Component.text("Team ownership has been transferred to ").append(newOwner.get().getName()));

            teams.remove(player);
            teams.put(newOwner.get(), team);

            return true;
        }

        return false;
    }

    public static void removePlayer(Player player) {
        teams.values().forEach(team -> team.removePlayer(player));

        if (teams.containsKey(player)) {
            if (transferOwnership(player)) {
                MessageUtils.sendInfoMessage(player, "You have left your team and ownership has been transferred");
            } else {
                MessageUtils.sendInfoMessage(player, "Your team has been disbanded");
            }
        } else {
            MessageUtils.sendInfoMessage(player, "You have left your team");
        }
    }
}
