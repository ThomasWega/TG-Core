package net.trustgames.core.managers;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.trustgames.core.Core;
import net.trustgames.core.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

public class NPCManager {

    private final Core core;

    public NPCManager(Core core) {
        this.core = core;
    }

    /**
     * Create a new npc using NMS Packets. This method won't spawn it!
     *
     * @param location Location of the npc
     * @param name Name of the npc
     * @return new create npc
     */
    public ServerPlayer create(Location location, String name) {
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name);
        ServerPlayer npc = new ServerPlayer(nmsServer, nmsWorld, gameProfile, null);
        npc.setPos(location.getX(), location.getY(), location.getZ());

        return npc;
    }

    /**
     * Add the npc for the given player
     *
     * @param npc Npc to add
     * @param player Player to add the NPC to
     */
    public void add(ServerPlayer npc, Player player) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, npc));
        connection.send(new ClientboundAddPlayerPacket(npc)); // Spawns the NPC for the player client.
        connection.send(new ClientboundRotateHeadPacket(npc, (byte) (npc.getBukkitYaw() * 256 / 360))); // Correct head rotation when spawned in player look direction.
    }

    /**
     * Remove the given NPC
     *
     * @param npc NPC to remove
     * @param player Player to remove the NPC from
     */
    public void remove(ServerPlayer npc, Player player) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundRemoveEntitiesPacket(npc.getId()));
        connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc));
    }

    /**
     * Set where the npc should be looking
     *
     * @param npc NPC to set the location to
     * @param player Player to set the NPC to
     * @param yaw Location yaw
     * @param pitch Location pitch
     * @param straighten Try to make the npc body point the same direction as the head
     */
    public void look(Entity npc, Player player, float yaw, float pitch, boolean straighten) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;

        float angle = (yaw * 256.0f / 360.0f);

        // add 35 to the angle to try to make the npc body stand straight
        if (straighten) {
            float str8n = 35;
            if (angle < 0) {
                if (angle - str8n > -128)
                    angle = angle - str8n;
                else
                    angle = -1;
            } else if (angle + str8n >= 0) {
                if (angle + str8n < 128)
                    angle = angle + str8n;
                else
                    angle = 127;
            }
        }

        connection.send(new ClientboundRotateHeadPacket(npc, (byte) (yaw * 256 / 360)));
        connection.send(new ClientboundMoveEntityPacket.Rot(npc.getId(), (byte) angle, (byte) (pitch * 256 / 360), true));
    }

    // NOTE: not sure if this works
    public static void move(Entity entity, Player player, double x, double y, double z) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundMoveEntityPacket.Pos(
                entity.getId(), (short) (x * 4096), (short) (y * 4096), (short) (z * 4096), true));
    }

    /**
     * Apply the skin to the given NPC
     *
     * @param npc NPC to apply skin to
     * @param player Player to set the NPC to
     * @param texture Texture of the skin
     * @param signature Signature of the skin
     */
    public void skin(ServerPlayer npc, Player player, String texture, String signature) {
        remove(npc, player);

        npc.getGameProfile().getProperties().put("textures", new Property("textures", texture, signature));

        add(npc, player);

        // Second layer on skin
        SynchedEntityData dataWatcher = npc.getEntityData();
        dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 126);
        ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(npc.getId(), dataWatcher, true);
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    /**
     * Hide the name on NPC's head
     *
     * @param npc NPC to hide the name of
     */
    public void hideName(ServerPlayer npc) {
        Team team = core.getPlayerListScoreboard().getTeam("9999NPC");
        if (team == null) {
            team = core.getPlayerListScoreboard().registerNewTeam("9999NPC");
        }

        team.addEntity(npc.getBukkitEntity());
        team.prefix(ColorUtils.color("&8[NPC] "));
        team.color(NamedTextColor.DARK_GRAY);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
    }

    /**
     * Hide the NPC from the TAB
     *
     * @param npc NPC to hide
     * @param player Player to hide NPC from
     */
    public void hideTab(ServerPlayer npc, Player player) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc));
    }

  /*  public void interact(){
        ProtocolManager manager = core.getProtocolManager();

        manager.addPacketListener(new PacketAdapter(core, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Bukkit.getServer().getConsoleSender().sendMessage("Used Entity" + packet.getIntegers().read(0));
                Bukkit.getLogger().severe("USED ENTITY " + packet.getIntegers().read(0));
            }
        });
    }

   */
}
