package net.minestom.arena.game.mobdrops;

import net.minestom.server.entity.ItemEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.Random;

public class RandomDrop {

    public static ItemEntity getDrop() {
        ArrayList<ItemEntity> itemList = new ArrayList<>();
        itemList.add(new ItemEntity(healItem));
        itemList.add(new ItemEntity(speedItem));
        itemList.add(new ItemEntity(lightningItem));
        Random random = new Random();
        return itemList.get(random.nextInt(itemList.size()));
    }

    public static ItemStack speedItem = ItemStack.builder(Material.FEATHER).build();
    public static ItemStack healItem = ItemStack.builder(Material.GOLDEN_APPLE).build();
    public static ItemStack lightningItem = ItemStack.builder(Material.BLAZE_ROD).build();
}
