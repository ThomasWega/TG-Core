package net.trustgames.core.managers;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Handles the creation of the ItemStack,
 * their flags and ItemMeta
 */
public final class ItemManager {

    /**
     * @param material ItemStack Material
     * @param count    What amount
     * @return created ItemStack
     */
    public static ItemStack createItemStack(@NotNull Material material, int count) {
        return new ItemStack(material, count);
    }

    /**
     * @param itemStack ItemStack to create ItemMeta to
     * @param name      Display name of the ItemStack
     * @param itemFlags What items flags to put on the ItemStack. Can be null
     * @return created ItemMeta of the given ItemStack
     */
    public static ItemMeta createItemMeta(@NotNull ItemStack itemStack,
                                          @NotNull Component name,
                                          @Nullable ItemFlag[] itemFlags) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(name);

        if (itemFlags != null) {
            itemMeta.addItemFlags(itemFlags);
        }

        itemStack.setItemMeta(itemMeta);
        return itemMeta;
    }

    /**
     * gets the player skull by his url. Use mineskin.org for url
     * Using paper api method for this. (will not work on spigot)
     *
     * @param value     Value of the Texture of the Skull
     * @param signature Signature of the texture of the Skull
     * @return ItemStack with the Skull texture
     */
    public static ItemStack getSkull(@NotNull String value, @NotNull String signature) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID(), null);
        playerProfile.getProperties().add(new ProfileProperty("textures", value, signature));
        skullMeta.setPlayerProfile(playerProfile);

        skull.setItemMeta(skullMeta);

        return skull;
    }
}

/*      OLD METHOD (spigot way)
https://www.spigotmc.org/threads/tutorial-skulls.135083/#post-1432132

     ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);

        // check if the skinurl is null or empty, in case it is, return skull (null)
        if (skinURL == null || skinURL.isEmpty())
            return skull;

        ItemMeta skullMeta = skull.getItemMeta();
        // generate a new gameprofile with random uuid and name null
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        // get the decoded data and encodes it back
        byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", skinURL).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;

        try {
            profileField = skullMeta.getClass().getDeclaredField("profile");
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }

        Objects.requireNonNull(profileField).setAccessible(true);

        try {
            profileField.set(skullMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

        skull.setItemMeta(skullMeta);

    */
