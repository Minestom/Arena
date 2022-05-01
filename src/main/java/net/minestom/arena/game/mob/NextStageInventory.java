package net.minestom.arena.game.mob;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.arena.Items;
import net.minestom.arena.utils.ItemUtils;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

final class NextStageInventory extends Inventory {
    private static final ItemStack HEADER = ItemUtils.stripItalics(ItemStack.builder(Material.ANVIL)
            .displayName(Component.text("Next Stage", NamedTextColor.GOLD))
            .lore(Component.text("Buy a different class, team upgrades or just continue to the next stage", NamedTextColor.GRAY))
            .build());

    private final Player player;
    private final MobArena arena;

    NextStageInventory(Player player, MobArena arena) {
        super(InventoryType.CHEST_4_ROW, Component.text("Next Stage"));
        this.player = player;
        this.arena = arena;

        setItemStack(4, HEADER);

        setItemStack(12, ItemStack.of(Material.DIRT));
        setItemStack(14, ItemStack.of(Material.STONE));

        setItemStack(30, Items.CLOSE);
        setItemStack(32, Items.CONTINUE);

        addInventoryCondition((p, s, c, result) -> result.setCancel(true));
        addInventoryCondition((p, slot, c, r) -> {
            switch (slot) {
                case 12 -> player.openInventory(new ClassSelectionInventory(this));
                case 14 -> player.openInventory(new TeamUpgradeInventory(this));
                case 30 -> player.closeInventory();
                case 32 -> {
                    player.closeInventory();
                    arena.continueToNextStage(player);
                }
            }
        });
    }

    private final class ClassSelectionInventory extends Inventory {
        ClassSelectionInventory(Inventory parent) {
            super(InventoryType.CHEST_4_ROW, Component.text("Class Selection"));

            setItemStack(4, HEADER);

            setItemStack(31, Items.BACK);

            addInventoryCondition((p, s, c, result) -> result.setCancel(true));
            addInventoryCondition((p, slot, c, r) -> {
                switch (slot) {
                    case 31 -> player.openInventory(parent);
                }
            });
        }
    }

    private final class TeamUpgradeInventory extends Inventory {
        TeamUpgradeInventory(Inventory parent) {
            super(InventoryType.CHEST_4_ROW, Component.text("Team Upgrades"));

            setItemStack(4, HEADER);

            setItemStack(31, Items.BACK);

            addInventoryCondition((p, s, c, result) -> result.setCancel(true));
            addInventoryCondition((p, slot, c, r) -> {
                switch (slot) {
                    case 31 -> player.openInventory(parent);
                }
            });
        }
    }
}
