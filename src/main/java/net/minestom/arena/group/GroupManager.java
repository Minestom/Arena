package net.minestom.arena.group;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class GroupManager {
    private static final Map<Player, GroupImpl> groups = new HashMap<>();

    public static @NotNull GroupImpl getGroup(@NotNull Player player) {
        GroupImpl group = groups.get(player);
        if (group == null) group = createGroup(player);
        return group;
    }

    public static @NotNull GroupImpl createGroup(@NotNull Player player) {
        GroupImpl group = new GroupImpl(player);
        groups.put(player, group);
        return group;
    }

    public static void transferOwnership(@NotNull GroupImpl group, @NotNull Player newLeader) {
        groups.remove(group.leader());
        group.setLeader(newLeader);
        group.members().forEach(member ->
                member.sendMessage(Component.text("Group ownership has been transferred to ").append(newLeader.getName())));
        groups.put(newLeader, group);
    }

    public static void removePlayer(@NotNull Player player) {
        groups.values().forEach(group -> group.removePlayer(player));
        
        if (groups.containsKey(player)) {
            Optional<Player> newLeader = groups.get(player).members().stream().findFirst();

            if (newLeader.isPresent()) {
                transferOwnership(groups.get(player), newLeader.get());
                player.sendMessage("You have left your group and ownership has been transferred");
            } else {
                player.sendMessage("Your group has been disbanded");
                groups.remove(player);
            }
        } else {
            player.sendMessage("You have left your group");
        }
    }
}
