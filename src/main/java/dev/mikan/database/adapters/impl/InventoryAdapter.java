package dev.mikan.database.adapters.impl;


import dev.mikan.database.adapters.ObjectAdapter;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;

public class InventoryAdapter implements ObjectAdapter<ItemStack[]> {

    @Override
    public String serialize(ItemStack[] obj) {
        if (obj == null) return null;

//        final byte[] serialized = ItemStack.serializeItemsAsBytes(obj);
//        return Base64.getEncoder().encodeToString(serialized);
        return null;
    }

    @Override
    public ItemStack[] deserialize(String data) {
        if (data == null) return null;

        final byte[] bytes = Base64.getDecoder().decode(data);
//        return ItemStack.deserializeItemsFromBytes(bytes);
        return null;
    }

}
