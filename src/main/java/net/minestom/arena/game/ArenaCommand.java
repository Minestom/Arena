package net.minestom.arena.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.arena.Items;
import net.minestom.arena.Lobby;
import net.minestom.arena.Messenger;
import net.minestom.arena.game.mob.MobArena;
import net.minestom.arena.game.procedural.ProceduralArena;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ArenaCommand extends Command {

    // In order to add a new arena, add it to this section:
    public static final ArenaType MOB = new ArenaType("mob", ItemStack.of(Material.ZOMBIE_HEAD),
            builder -> builder.displayName(Component.text("Mob Arena", NamedTextColor.GREEN)),
            MobArena::new);
    public static final ArenaType PROCEDURAL = new ArenaType("procedural", ItemStack.of(Material.BONE_BLOCK),
            builder -> builder.displayName(Component.text("Procedural", NamedTextColor.RED)),
            ProceduralArena::new);
    public static final Map<String, ArenaType> ARENAS = Map.of(
            "mob", MOB,
            "procedural", PROCEDURAL
    );

    private record ArenaType(String name, ItemStack menuItem, Consumer<ItemStack.Builder> builderConsumer,
                             Function<Group, Arena> arenaCreator) {

        public Arena startNew(Group group) {
            return arenaCreator.apply(group);
        }
    }

    public ArenaCommand() {
        super("arena");
        setCondition(CommandUtils::lobbyOnly);

        setDefaultExecutor((sender, context) ->
                ((Player) sender).openInventory(new ArenaInventory()));

        addSyntax(
                (sender, context) -> play((Player) sender, context.get("type")),
                ArgumentType.Word("type").from(ARENAS.keySet().toArray(String[]::new))
        );
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
        Arena arena = ARENAS.get(type).startNew(group);
        arena.init().thenRun(() -> group.members().forEach(Player::refreshCommands));
    }

    private static class ArenaInventory extends Inventory {
        private static final Tag<String> ARENA_TAG = Tag.String("arena");
        private static final ItemStack HEADER = ItemUtils.stripItalics(ItemStack.builder(Material.IRON_BARS)
                .displayName(Component.text("Arena", NamedTextColor.RED))
                .lore(Component.text("Select an arena to play in", NamedTextColor.GRAY))
                .build());

        public ArenaInventory() {
            super(InventoryType.CHEST_4_ROW, Component.text("Arena"));

            setItemStack(4, HEADER);
            setItemStack(31, Items.CLOSE);

            int i = 13 - ARENAS.size() / 2;
            for (ArenaType type : ARENAS.values()) {
                ItemStack item = type.menuItem();
                setItemStack(
                        i++,
                        ItemUtils.stripItalics(
                                item.withLore(List.of(Component.text("Click to play in the " + type.name() + " arena", NamedTextColor.GRAY)))
                                        .withTag(ARENA_TAG, type.name())
                        )
                );
            }

            addInventoryCondition((player, slot, c, result) -> {
                result.setCancel(true);

                if (slot == 31) { // Close button
                    player.closeInventory();
                    return;
                }

                String arena = result.getClickedItem().getTag(ARENA_TAG);

                if (arena != null) {
                    player.closeInventory();
                    play(player, arena);
                }
            });
        }
    }
}
