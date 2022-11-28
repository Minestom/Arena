package net.minestom.arena.group;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.minestom.arena.group.displays.GroupDisplay;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public sealed interface Group extends ForwardingAudience permits GroupImpl {
    static Group findGroup(@NotNull Player player) {
        return GroupManager.getGroup(player);
    }

    @NotNull Player leader();

    @NotNull List<@NotNull Player> members();

    @NotNull GroupDisplay display();

    void setDisplay(@NotNull GroupDisplay display);

    @Override
    default @NotNull Iterable<? extends Audience> audiences() {
        return members();
    }
}
