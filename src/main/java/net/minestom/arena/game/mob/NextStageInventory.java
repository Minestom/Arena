package net.minestom.arena.game.mob;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.arena.Items;
import net.minestom.arena.Messenger;
import net.minestom.arena.utils.ItemUtils;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

final class NextStageInventory extends Inventory {
    private static final ItemStack HEADER = ItemUtils.stripItalics(ItemStack.builder(Material.PAPER)
            .displayName(Component.text("Next Stage", NamedTextColor.GOLD))
            .lore(Component.text("Buy a different class, team upgrades or just continue to the next stage", NamedTextColor.GRAY))
            .build());
    private static final ItemStack CLASS_SELECTION = ItemUtils.stripItalics(ItemStack.builder(Material.SHIELD)
            .displayName(Component.text("Class Selection", NamedTextColor.GREEN))
            .lore(Component.text("Buy a different class", NamedTextColor.GRAY))
            .build());
    private static final ItemStack TEAM_UPGRADES = ItemUtils.stripItalics(ItemStack.builder(Material.ANVIL)
            .displayName(Component.text("Team Upgrades", NamedTextColor.LIGHT_PURPLE))
            .lore(Component.text("Buy upgrades for the whole team", NamedTextColor.GRAY))
            .build());

    private final Player player;
    private final MobArena arena;

    NextStageInventory(Player player, MobArena arena) {
        super(InventoryType.CHEST_4_ROW, Component.text("Next Stage"));
        this.player = player;
        this.arena = arena;

        setItemStack(4, HEADER);

        setItemStack(12, CLASS_SELECTION);
        setItemStack(14, TEAM_UPGRADES);

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

            draw();

            setItemStack(31, Items.BACK);

            addInventoryCondition((p, s, c, result) -> result.setCancel(true));
            addInventoryCondition((p, slot, c, r) -> {
                if (slot == 31) player.openInventory(parent);
                else {
                    final int length = MobArena.CLASSES.size();
                    for (int i = 0; i < length; i++) {
                        ArenaClass arenaClass = MobArena.CLASSES.get(i);

                        if (slot == 13 - length / 2 + i) {
                            switchClass(arenaClass);
                            return;
                        }
                    }
                }
            });
        }

        private void draw() {
            final int length = MobArena.CLASSES.size();
            for (int i = 0; i < length; i++) {
                ArenaClass arenaClass = MobArena.CLASSES.get(i);

                setItemStack(13 - length / 2 + i, arenaClass.itemStack().withMeta(
                        builder -> arena.playerClass(player).equals(arenaClass)
                                ? builder.enchantment(Enchantment.PROTECTION, (short) 1)
                                : builder
                ));
            }
        }

        private void switchClass(ArenaClass arenaClass) {
            if (arena.playerClass(player).equals(arenaClass)) {
                Messenger.warn(player, "You can't switch to your selected class");
                return;
            }

            if (arena.takeCoins(arenaClass.cost())) {
                Messenger.info(player, "You switched your class to " + arenaClass.name());
                arena.setPlayerClass(player, arenaClass);
                draw();
                player.playSound(Sound.sound(SoundEvent.ENTITY_VILLAGER_YES, Sound.Source.NEUTRAL, 1, 1), Sound.Emitter.self());
            } else {
                Messenger.warn(player, "You can't afford that");
                player.playSound(Sound.sound(SoundEvent.ENTITY_VILLAGER_NO, Sound.Source.NEUTRAL, 1, 1), Sound.Emitter.self());
            }
        }
    }

    private final class TeamUpgradeInventory extends Inventory {
        TeamUpgradeInventory(Inventory parent) {
            super(InventoryType.CHEST_4_ROW, Component.text("Team Upgrades"));

            setItemStack(4, HEADER);

            draw();

            setItemStack(31, Items.BACK);

            addInventoryCondition((p, s, c, result) -> result.setCancel(true));
            addInventoryCondition((p, slot, c, r) -> {
                if (slot == 31) player.openInventory(parent);
                else {
                    final int length = MobArena.UPGRADES.size();
                    for (int i = 0; i < length; i++) {
                        ArenaUpgrade upgrade = MobArena.UPGRADES.get(i);

                        if (slot == 13 - length / 2 + i) {
                            buyUpgrade(upgrade);
                            return;
                        }
                    }
                }
            });
        }

        private void draw() {
            final int length = MobArena.UPGRADES.size();
            for (int i = 0; i < length; i++) {
                ArenaUpgrade upgrade = MobArena.UPGRADES.get(i);

                setItemStack(13 - length / 2 + i, upgrade.itemStack().withMeta(
                        builder -> arena.getUpgrade(upgrade) != 0
                                ? builder.enchantment(Enchantment.PROTECTION, (short) 1)
                                : builder
                ));
            }
        }

        private void buyUpgrade(ArenaUpgrade upgrade) {
            if (arena.takeCoins(upgrade.cost())) {
                Messenger.info(player, "You bought the " + upgrade.name() + " upgrade for your team");
                arena.addUpgrade(upgrade);
                draw();
                player.playSound(Sound.sound(SoundEvent.ENTITY_VILLAGER_YES, Sound.Source.NEUTRAL, 1, 1), Sound.Emitter.self());
            } else {
                Messenger.warn(player, "You can't afford that");
                player.playSound(Sound.sound(SoundEvent.ENTITY_VILLAGER_NO, Sound.Source.NEUTRAL, 1, 1), Sound.Emitter.self());
            }
        }
    }
}
