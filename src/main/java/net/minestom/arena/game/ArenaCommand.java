package net.minestom.arena.game;

import net.minestom.arena.Lobby;
import net.minestom.arena.MessageUtils;
import net.minestom.arena.game.mob.MobArena;
import net.minestom.arena.group.Group;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;

import java.util.Map;
import java.util.function.Supplier;

public final class ArenaCommand extends Command {
    private static final Map<String, Supplier<Arena>> ARENAS = Map.of(
            "mob", MobArena::new);

    public ArenaCommand() {
        super("arena");
        setCondition(Conditions::playerOnly);

        setDefaultExecutor((sender, context) ->
                MessageUtils.sendWarnMessage(sender, "Usage: /arena <name>. Choices are: " + String.join(", ", ARENAS.keySet())));

        addSyntax((sender, context) -> {
            final Player player = (Player) sender;
            if (player.getInstance() != Lobby.INSTANCE) {
                player.sendMessage("You are not in the lobby! Join the lobby first.");
                return;
            }
            final String type = context.get("type");
            final Group group = Group.findGroup(player);
            if (group.leader() != player) {
                player.sendMessage("You are not the leader of your group!");
                return;
            }
            Arena arena = ARENAS.get(type).get();
            arena.join(group);
            group.members().forEach(Player::refreshCommands);
        }, ArgumentType.Word("type").from(ARENAS.keySet().toArray(new String[0])));
    }
}
