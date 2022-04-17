package net.minestom.arena.game;

import net.minestom.arena.Lobby;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;

public class ArenaCommand extends Command {

    public ArenaCommand() {
        super("arena");
        setCondition(Conditions::playerOnly);

        setDefaultExecutor((sender, context) -> {
            Player player = (Player) sender;

            if (player.getInstance() != Lobby.INSTANCE) {
                player.sendMessage("You are not in the lobby! Join the lobby first.");
                return;
            }

            Arena arena = new Arena();

            arena.join(player);

        });


    }

}
