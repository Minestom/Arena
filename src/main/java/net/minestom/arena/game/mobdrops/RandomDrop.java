package net.minestom.arena.game.mobdrops;

import net.minestom.server.entity.ItemEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.Random;

public class RandomDrop {

    public ItemEntity getDrop() {
        ArrayList<ItemEntity> itemList = new ArrayList<>();
        itemList.add(new ItemEntity(ItemStack.builder(Material.GOLDEN_APPLE).build()));
        itemList.add(new ItemEntity(ItemStack.builder(Material.FEATHER).build()));
        itemList.add(new ItemEntity(ItemStack.builder(Material.BLAZE_ROD).build()));
        Random random = new Random();
        return itemList.get(random.nextInt(itemList.size()));
    }

}
