package org.ast.bedwarspro.been;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class MarketItem {
    private final String seller;
    private final String serializedItem; // Store serialized ItemStack as Base64
    private final double price;

    public MarketItem(String seller, ItemStack itemStack, double price) {
        this.seller = seller;
        this.serializedItem = serializeItemStack(itemStack); // Serialize ItemStack to Base64
        this.price = price;
    }

    public String getSeller() {
        return seller;
    }

    public String getSerializedItem() {
        return serializedItem;
    }

    public double getPrice() {
        return price;
    }

    public ItemStack toItemStack() {
        return deserializeItemStack(serializedItem); // Deserialize back to ItemStack
    }

    private String serializeItemStack(ItemStack itemStack) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeObject(itemStack);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to serialize ItemStack", e);
        }
    }

    private ItemStack deserializeItemStack(String serializedItem) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(serializedItem));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            return (ItemStack) dataInput.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Unable to deserialize ItemStack", e);
        }
    }
}