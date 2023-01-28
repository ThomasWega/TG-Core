package net.trustgames.core.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

public class NPC {

    private final Core core;

    public NPC(Core core) {
        this.core = core;
    }

    public ServerPlayer create(Location location, String name) {
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel nmsWorld = ((CraftWorld) Bukkit.getWorld("world")).getHandle(); // Change "world" to the world the NPC should be spawned in.
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name); // Change "playername" to the name the NPC should have, max 16 characters.
        ServerPlayer npc = new ServerPlayer(nmsServer, nmsWorld, gameProfile, null); // This will be the EntityPlayer (NPC) we send with the sendNPCPacket method.
        npc.setPos(location.getX(), location.getY(), location.getZ());

        return npc;
    }

    public void add(ServerPlayer npc, Player player) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, npc));
        connection.send(new ClientboundAddPlayerPacket(npc)); // Spawns the NPC for the player client.
        connection.send(new ClientboundRotateHeadPacket(npc, (byte) (npc.getBukkitYaw() * 256 / 360))); // Correct head rotation when spawned in player look direction.
    }

    public void remove(ServerPlayer npc, Player player) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundRemoveEntitiesPacket(npc.getId()));
        connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc));
    }

    public void look(Entity npc, Player player, float yaw, float pitch, boolean straighten) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;

        float angle = (yaw * 256.0f / 360.0f);
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

    // TODO NOTE: not sure this works
    public static void move(Entity entity, Player player, double x, double y, double z) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundMoveEntityPacket.Pos(
                entity.getId(), (short) (x * 4096), (short) (y * 4096), (short) (z * 4096), true));
    }

    public void skin(ServerPlayer npc, Player player, String texture, String signature) { // The username is the name for the player that has the skin.
        remove(npc, player);


        npc.getGameProfile().getProperties().put("textures", new Property("textures", texture, signature));

        add(npc, player);

        // Second layer on skin
        SynchedEntityData dataWatcher = npc.getEntityData();
        dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 126);
        ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(npc.getId(), dataWatcher, true);
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    public void hideName(ServerPlayer npc) {
        Team team = core.getPlayerListScoreboard().getTeam("9999NPC");
        if (team == null) {
            team = core.getPlayerListScoreboard().registerNewTeam("9999NPC");
        }

        team.addEntity(npc.getBukkitEntity());
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
    }

    public void hideTab(ServerPlayer npc, Player player) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc));
    }
}
