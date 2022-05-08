package net.minestom.arena.game.mob;

import net.minestom.arena.utils.CommandUtils;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class MobTestCommand extends Command {
    public MobTestCommand() {
        super("test");
        setCondition((sender, commandString) -> CommandUtils.arenaOnly(sender, commandString) &&
                sender instanceof Player player && player.getPermissionLevel() == 4);

        final ArgumentLiteral coins = ArgumentType.Literal("coins");
        final ArgumentLiteral stage = ArgumentType.Literal("stage");
        final ArgumentLiteral clear = ArgumentType.Literal("clear");
        final ArgumentLiteral clazz = ArgumentType.Literal("class");
        final ArgumentLiteral spawn = ArgumentType.Literal("spawn");
        final ArgumentLiteral immortal = ArgumentType.Literal("immortal");
        final ArgumentLiteral strong = ArgumentType.Literal("strong");

        final ArgumentInteger coinsAmount = ArgumentType.Integer("amount");
        final ArgumentInteger classId = ArgumentType.Integer("id");
        final ArgumentInteger mobType = ArgumentType.Integer("type");

        addSyntax((sender, context) -> arena(sender)
                .ifPresent(arena -> arena.addCoins(context.get(coinsAmount))), coins, coinsAmount);
        addSyntax((sender, context) -> arena(sender)
                .ifPresent(arena -> arena.addCoins(10)), coins);

        addSyntax((sender, context) -> arena(sender)
                .ifPresent(MobArena::nextStage), stage);

        addSyntax((sender, context) -> arena(sender).ifPresent(arena -> {
            for (Entity entity : arena.instance().getEntities()) {
                if (entity instanceof ArenaMob arenaMob)
                    arenaMob.kill();
            }
        }), clear);

        addSyntax((sender, context) -> arena(sender).ifPresent(arena ->
            arena.setPlayerClass(
                    (Player) sender,
                    MobArena.CLASSES.get(MathUtils.clamp(
                            context.get(classId),
                            0, MobArena.CLASSES.size() - 1
                    ))
            )
        ), clazz, classId);

        addSyntax((sender, context) -> arena(sender).ifPresent(arena ->
            MobArena.MOB_GENERATORS.get(MathUtils.clamp(
                    context.get(mobType),
                    0, MobArena.MOB_GENERATORS.size() - 1
            )).generate(new MobGenerationContext(arena)).ifPresent(entity ->
                    entity.setInstance(arena.instance(), ((Player) sender).getPosition()))), spawn, mobType);

        addSyntax((sender, context) -> {
            final Player player = (Player) sender;
            player.setInvulnerable(!player.isInvulnerable());
        }, immortal);

        addSyntax((sender, context) ->
                ((Player) sender).getInventory().addItemStack(ItemStack.builder(Material.COOKED_CHICKEN)
                        .set(MobArena.MELEE_TAG, 10000).build()
        ), strong);
    }

    // TODO: Replace with game API once merged
    private static @NotNull Optional<MobArena> arena(CommandSender sender) {
        if (!(sender instanceof Player player)) return Optional.empty();
        if (!(player.getInstance() instanceof MobArena.MobArenaInstance instance)) return Optional.empty();

        return Optional.of(instance.arena);
    }
}
