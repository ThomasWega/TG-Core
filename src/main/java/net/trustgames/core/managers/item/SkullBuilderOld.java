package net.trustgames.core.managers.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.trustgames.core.Core;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the creation of Skulls ItemStacks
 */
public final class SkullBuilderOld {

    // TODO Convert to be similar to ItemBuilder
    // TODO return ItemBuilder instead of ItemStack

    private static final Logger logger = Core.LOGGER;

    /**
     * Gets the player skull by his url. Use mineskin.org for url
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

    /*
      OLD METHOD (spigot way)
      https://www.spigotmc.org/threads/tutorial-skulls.135083/#post-1432132
    */

    /**
     * Spigot way of retrieving Skull
     *
     * @param skinUrl URL of the skin
     * @return new ItemStack with the Skull
     * @see SkullBuilderOld#getSkull(String, String)
     * @deprecated In favor of paper-api way
     */
    public static ItemStack getSkullSpigot(String skinUrl) {

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);

        // check if the skinurl is null or empty, in case it is, return skull (null)
        if (skinUrl == null || skinUrl.isEmpty())
            return skull;

        ItemMeta skullMeta = skull.getItemMeta();
        // generate a new gameprofile with random uuid and name null
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        // get the decoded data and encodes it back
        byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", skinUrl).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField;

        try {
            profileField = skullMeta.getClass().getDeclaredField("profile");
        } catch (NoSuchFieldException | SecurityException e) {
            logger.log(Level.SEVERE, "Unable to get declared profile field for skull with url " + skinUrl);
            return skull;
        }
        Objects.requireNonNull(profileField).setAccessible(true);

        try {
            profileField.set(skullMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            logger.log(Level.SEVERE, "Unable to set meta to declared skull profile field", e);
        }

        skull.setItemMeta(skullMeta);
        return skull;
    }
}