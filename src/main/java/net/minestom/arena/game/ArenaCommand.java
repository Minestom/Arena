package net.minestom.arena.game;

import net.minestom.arena.Lobby;
import net.minestom.arena.MessageUtils;
import net.minestom.arena.game.mob.MobArena;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;

import java.util.Map;
import java.util.function.Supplier;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

public class ArenaCommand extends Command {

    private final Map<String, Supplier<Arena>> arenas = Map.of(
            "mob", MobArena::new
    );

    public ArenaCommand() {
        super("arena");
        setCondition(Conditions::playerOnly);

        setDefaultExecutor((sender, context) -> {
            MessageUtils.sendWarnMessage(sender, "Usage: /arena <name>. Choices are: " + String.join(", ", arenas.keySet()));
        });

        arenas.forEach((name, arena) -> {
            addSyntax((sender, context) -> {
                Player player = (Player) sender;

                if (player.getInstance() != Lobby.INSTANCE) {
                    player.sendMessage("You are not in the lobby! Join the lobby first.");
                    return;
                }

                Arena suppliedArena = arena.get();

                suppliedArena.join(player).thenRun(suppliedArena::start);

            }, Literal(name));
        });

    }

}
