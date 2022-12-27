package net.minestom.arena.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.arena.Items;
import net.minestom.arena.lobby.Lobby;
import net.minestom.arena.Messenger;
import net.minestom.arena.group.Group;
import net.minestom.arena.utils.CommandUtils;
import net.minestom.arena.utils.ItemUtils;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ArenaCommand extends Command {
    public ArenaCommand() {
        super("arena", "play", "join", "game");
        setCondition(CommandUtils::lobbyOnly);

        setDefaultExecutor((sender, context) -> open((Player) sender));
        addSyntax((sender, context) ->
                play((Player) sender, context.get("type"), Set.of()),
        ArgumentType.Enum("type", ArenaType.class).setFormat(ArgumentEnum.Format.LOWER_CASED));
    }

    public static void open(@NotNull Player player) {
        player.openInventory(new ArenaInventory());
    }

    private static void play(@NotNull Player player, @NotNull ArenaType type, @NotNull Set<ArenaOption> options) {
        if (player.getInstance() != Lobby.INSTANCE) {
            Messenger.warn(player, "You are not in the lobby! Join the lobby first.");
            return;
        }
        final Group group = Group.findGroup(player);
        if (group.leader() != player) {
            Messenger.warn(player, "You are not the leader of your group!");
            return;
        }

        Arena arena = type.createInstance(group, options);
        ArenaManager.register(arena);
        arena.init().thenRun(() -> group.members().forEach(Player::refreshCommands));
    }

    private static final class ArenaInventory extends Inventory {
        private static final Tag<Integer> ARENA_TAG = Tag.Integer("arena").defaultValue(-1);
        private static final ItemStack HEADER = ItemUtils.stripItalics(ItemStack.builder(Material.IRON_BARS)
                .displayName(Component.text("Arena", NamedTextColor.RED))
                .lore(Component.text("Select an arena to play in", NamedTextColor.GRAY))
                .build());

        ArenaInventory() {
            super(InventoryType.CHEST_4_ROW, Component.text("Arena"));

            setItemStack(4, HEADER);
            setItemStack(31, Items.CLOSE);

            final ArenaType[] arenaTypes = ArenaType.values();
            int index = 13 - arenaTypes.length / 2;
            for (ArenaType arenaType : ArenaType.values())
                setItemStack(index++, ItemUtils.stripItalics(arenaType.item()
                        .withLore(List.of(Component.text(
                                "Left click to play or right click to configure",
                                NamedTextColor.GRAY
                        )))
                        .withTag(ARENA_TAG, arenaType.ordinal())));

            addInventoryCondition((player, slot, clickType, result) -> {
                result.setCancel(true);

                if (slot == 31) { // Close button
                    player.closeInventory();
                    return;
                }

                final int arena = result.getClickedItem().getTag(ARENA_TAG);
                if (arena == -1) return;
                final ArenaType type = ArenaType.values()[arena];

                if (clickType == ClickType.RIGHT_CLICK) {
                    player.openInventory(new ArenaOptionInventory(this, type));
                } else{
                    player.closeInventory();
                    play(player, type, Set.of());
                }
            });
        }
    }

    private static final class ArenaOptionInventory extends Inventory {
        private static final ItemStack PLAY_ITEM = ItemUtils.stripItalics(ItemStack.builder(Material.NOTE_BLOCK)
                .displayName(Component.text("Play", NamedTextColor.GREEN))
                .lore(Component.text("Play this arena", NamedTextColor.GRAY))
                .build());
        private static final Tag<Integer> OPTION_TAG = Tag.Integer("option").defaultValue(-1);

        private final ArenaType type;
        private final List<ArenaOption> availableOptions;
        private final Set<ArenaOption> selectedOptions = new HashSet<>();

        ArenaOptionInventory(@NotNull Inventory parent, @NotNull ArenaType type) {
            super(InventoryType.CHEST_4_ROW, Component.text("Arena Options"));
            this.type = type;
            availableOptions = type.availableOptions();

            draw();

            addInventoryCondition((player, slot, c, result) -> {
                result.setCancel(true);

                if (slot == 30) { // Play button
                    player.closeInventory();
                    play(player, type, selectedOptions);
                    return;
                }

                if (slot == 32) { // Back button
                    player.openInventory(parent);
                    return;
                }

                final int index = result.getClickedItem().getTag(OPTION_TAG);
                if (index == -1) return;
                final ArenaOption option = availableOptions.get(index);

                if (!selectedOptions.add(option)) {
                    selectedOptions.remove(option);
                }

                draw();
            });
        }

        private void draw() {
            setItemStack(4, type.item());
            setItemStack(30, PLAY_ITEM);
            setItemStack(32, Items.BACK);

            final int start = 13 - availableOptions.size() / 2;
            int index = 0;
            for (ArenaOption option : availableOptions)
                setItemStack(start + index, option.item().withTag(OPTION_TAG, index++).withMeta(builder -> {
                    if (selectedOptions.contains(option)) builder.enchantment(Enchantment.PROTECTION, (short) 1);
                }));
        }
    }
}
