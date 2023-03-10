package net.trustgames.core.managers.packets;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.trustgames.core.utils.ColorUtils;
import net.trustgames.core.utils.ComponentUtils;
import net.trustgames.core.utils.JsonUtils;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class HoloManager {

    /**
     * Spawns the hologram with the text for the player
     * on the given location
     *
     * @param player   Player to spawn the hologram for
     * @param location Location to spawn the hologram
     * @param text     Content of the hologram
     * @return List of created armorstands
     */
    public static List<ArmorStand> spawn(Player player, Location location, List<String> text) {
        List<ArmorStand> armorStands = new ArrayList<>(text.size());
        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        for (int i = -1; ++i < text.size(); ) {
            ArmorStand armorStand = new ArmorStand(craftWorld.getHandle(), location.getX(), location.getY(), location.getZ());

            armorStand.setInvisible(true);
            armorStand.setCustomNameVisible(true);

            armorStand.setCustomName(JsonUtils.convertFromJSON(ComponentUtils.toJson(
                    ColorUtils.color(text.get(i)))));

            ClientboundAddEntityPacket addPacket = new ClientboundAddEntityPacket(armorStand);
            ClientboundSetEntityDataPacket metaPacket = new ClientboundSetEntityDataPacket(armorStand.getId(), armorStand.getEntityData(), true);
            ((CraftPlayer) player).getHandle().connection.send(addPacket);
            ((CraftPlayer) player).getHandle().connection.send(metaPacket);
            location = location.subtract(0, 0.265, 0);

            armorStands.add(armorStand);
        }

        return armorStands;
    }

    /**
     * Remove the Hologram armorstand entity
     *
     * @param armorStand ArmorStand to remove
     * @param player     Player to remove armorstand from
     */
    public static void remove(ArmorStand armorStand, Player player) {
        ClientboundRemoveEntitiesPacket removePacket = new ClientboundRemoveEntitiesPacket(armorStand.getId());
        ((CraftPlayer) player).getHandle().connection.send(removePacket);
    }
}
