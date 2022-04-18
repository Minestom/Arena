package net.minestom.arena.group;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GroupManager {
    private static final Map<Player, Group> groups = new HashMap<>();

    public static Group getGroup(Player player) {
        return groups.get(player);
    }

    public static void createGroup(Player player) {
        groups.put(player, new Group(player));
    }

    public static void removeGroup(Player player) {
        Group group = groups.get(player);

        if (group != null) {
            group.disband();
            groups.remove(player);
        }
    }

    public static boolean transferOwnership(Player player) {
        Group group = groups.get(player);
        Optional<Player> newOwner = group.getPlayers().stream().filter(p -> p != player).findFirst();

        if (newOwner.isPresent()) {
            group.setOwner(newOwner.get());

            group.getPlayers().forEach(member -> {
                member.sendMessage(Component.text("Group ownership has been transferred to ").append(newOwner.get().getName()));
            });

            groups.remove(player);
            groups.put(newOwner.get(), group);

            return true;
        }

        return false;
    }

    public static void removePlayer(Player player) {
        groups.values().forEach(group -> group.removePlayer(player));

        if (groups.containsKey(player)) {
            if (transferOwnership(player)) {
                player.sendMessage("You have left your group and ownership has been transferred");
            } else {
                player.sendMessage("Your group has been disbanded");
            }
        } else {
            player.sendMessage("You have left your group");
        }
    }
}
