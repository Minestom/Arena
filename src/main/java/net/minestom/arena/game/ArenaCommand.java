package net.minestom.arena.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.arena.Items;
import net.minestom.arena.Lobby;
import net.minestom.arena.Messenger;
import net.minestom.arena.game.mob.MobArena;
import net.minestom.arena.group.Group;
import net.minestom.arena.utils.CommandUtils;
import net.minestom.arena.utils.ItemUtils;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public final class ArenaCommand extends Command {
    private static final Map<String, Function<Group, Arena>> ARENAS = Map.of(
            "mob", MobArena::new);

    public ArenaCommand() {
        super("arena");
        setCondition(CommandUtils::lobbyOnly);

        setDefaultExecutor((sender, context) ->
                ((Player) sender).openInventory(new ArenaInventory()));

        addSyntax((sender, context) ->
                play((Player) sender, context.get("type")),
        ArgumentType.Word("type").from(ARENAS.keySet().toArray(String[]::new)));
    }

    private static void play(Player player, String type) {
        if (player.getInstance() != Lobby.INSTANCE) {
            Messenger.warn(player, "You are not in the lobby! Join the lobby first.");
            return;
        }
        final Group group = Group.findGroup(player);
        if (group.leader() != player) {
            Messenger.warn(player, "You are not the leader of your group!");
            return;
        }

        if (GameManager.canStartGame(group.audience())) {
            Arena arena = ARENAS.get(type).apply(group);
            if (arena instanceof Game game) {
                GameManager.registerGame(game);
            } else {
                LOGGER.warn("Arena with type '"+type+"' doesn't extend Game, graceful shutdown of this arena isn't possible!");
            }

            arena.init().thenRun(() -> group.members().forEach(Player::refreshCommands));
        }
    }

    private static class ArenaInventory extends Inventory {
        private static final Tag<String> ARENA_TAG = Tag.String("arena");
        private static final ItemStack HEADER = ItemUtils.stripItalics(ItemStack.builder(Material.IRON_BARS)
                .displayName(Component.text("Arena", NamedTextColor.RED))
                .lore(Component.text("Select an arena to play in", NamedTextColor.GRAY))
                .build());
        private static final Map<ItemStack, String> ARENA_ITEM = Map.of(
                ItemStack.builder(Material.ZOMBIE_HEAD)
                        .displayName(Component.text("Mob Arena", NamedTextColor.GREEN))
                        .build(), "mob");

        public ArenaInventory() {
            super(InventoryType.CHEST_4_ROW, Component.text("Arena"));

            setItemStack(4, HEADER);
            setItemStack(31, Items.CLOSE);

            AtomicInteger i = new AtomicInteger(13 - ARENA_ITEM.size() / 2);
            ARENA_ITEM.forEach((item, arena) ->
                    setItemStack(i.getAndIncrement(), ItemUtils.stripItalics(item.withLore(List.of(
                            Component.text("Click to play in the " + arena + " arena", NamedTextColor.GRAY))
                    ).withTag(ARENA_TAG, arena)))
            );

            addInventoryCondition((player, slot, c, result) -> {
                result.setCancel(true);

                if (slot == 31) { // Close button
                    player.closeInventory();
                    return;
                }

                final String arena = result.getClickedItem().getTag(ARENA_TAG);

                if (arena != null) {
                    player.closeInventory();
                    play(player, arena);
                }
            });
        }
    }
}
