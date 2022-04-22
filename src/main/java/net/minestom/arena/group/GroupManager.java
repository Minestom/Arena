package net.minestom.arena.group;

import net.minestom.arena.Messenger;
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
        Messenger.info(group.audience(), "Group ownership has been transferred to " + newLeader.getUsername());
        groups.put(newLeader, group);
    }

    public static void removePlayer(@NotNull Player player) {
        GroupImpl foundGroup = getMemberGroup(player);
        if (foundGroup == null) return;

        foundGroup.removeMember(player);

        // If the leader is removed, change the leader
        if (groups.containsKey(player)) {
            Optional<Player> newLeader = groups.get(player).members().stream().findFirst();

            if (newLeader.isPresent()) {
                transferOwnership(groups.get(player), newLeader.get());
                Messenger.info(player, "You have left your group and ownership has been transferred");
            } else {
                Messenger.info(player, "Your group has been disbanded");
                groups.remove(player);
            }
        } else {
            Messenger.info(player, "You have left your group");
        }
    }

    public static GroupImpl getMemberGroup(@NotNull Player player) {
        for (GroupImpl group : groups.values())
            if (group.members().contains(player))
                return group;
        return null;
    }
}
