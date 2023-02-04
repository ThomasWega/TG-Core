package net.trustgames.core.managers;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

/**
 * Handles the Skull ItemStack creation and texture
 */
public class SkullManager {


    /**
     gets the player skull by his url. Use mineskin.org for url
     Using paper api method for this. (will not work on spigot)
     * @param textureValue Value of the Texture of the Skull
     * @param textureSignature Signature of the texture of the Skull
     * @return ItemStack with the Skull texture
    */
    public static ItemStack getSkull(String textureValue, String textureSignature) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID(), null);
        playerProfile.getProperties().add(new ProfileProperty("textures", textureValue, textureSignature));
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
