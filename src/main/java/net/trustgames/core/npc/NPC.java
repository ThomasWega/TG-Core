package net.trustgames.core.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.protocol.game.*;
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

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

public class NPC {

    private final Core core;

    public NPC(Core core) {
        this.core = core;
    }

    public ServerPlayer create(Location location, String name){
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel nmsWorld = ((CraftWorld) Bukkit.getWorld("world")).getHandle(); // Change "world" to the world the NPC should be spawned in.
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name); // Change "playername" to the name the NPC should have, max 16 characters.
        ServerPlayer npc = new ServerPlayer(nmsServer, nmsWorld, gameProfile, null); // This will be the EntityPlayer (NPC) we send with the sendNPCPacket method.
        npc.setPos(location.getX(), location.getY(), location.getZ());

        return npc;
    }

    public void addNPCPacket(ServerPlayer npc, Player player) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, npc));
        connection.send(new ClientboundAddPlayerPacket(npc)); // Spawns the NPC for the player client.
        connection.send(new ClientboundRotateHeadPacket(npc, (byte) (npc.getBukkitYaw() * 256 / 360))); // Correct head rotation when spawned in player look direction.
    }

    public void removeNPCPacket(ServerPlayer npc, Player player) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundRemoveEntitiesPacket(npc.getId()));
        connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc));
    }

    public void lookNPCPacket(Entity npc, Player player, float yaw, float pitch) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundRotateHeadPacket(npc, (byte)(yaw * 256 / 360)));
        connection.send(new ClientboundMoveEntityPacket.Rot(npc.getId(), (byte)(yaw * 256 / 360), (byte)(pitch * 256 / 360), true));
    }

    // TODO NOTE: not sure this works
    public static void sendMoveEntityPacket(Entity entity, Player player, double x, double y, double z) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundMoveEntityPacket.Pos(
                entity.getId(), (short)(x * 4096), (short)(y * 4096), (short)(z * 4096), true));
    }

    public void sendSetNPCSkinPacket(ServerPlayer npc, Player player, String username) { // The username is the name for the player that has the skin.
        removeNPCPacket(npc, player);

        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(String.format("https://api.ashcon.app/mojang/v2/user/%s", username)).openConnection();
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                ArrayList<String> lines = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                reader.lines().forEach(lines::add);

                String reply = String.join(" ",lines);
                int indexOfValue = reply.indexOf("\"value\": \"");
                int indexOfSignature = reply.indexOf("\"signature\": \"");
                String skin = reply.substring(indexOfValue + 10, reply.indexOf("\"", indexOfValue + 10));
                String signature = reply.substring(indexOfSignature + 14, reply.indexOf("\"", indexOfSignature + 14));

                npc.getGameProfile().getProperties().put("textures", new Property("textures", skin, signature));
            }

            else {
                Bukkit.getConsoleSender().sendMessage("Connection could not be opened when fetching player skin (Response code " + connection.getResponseCode() + ", " + connection.getResponseMessage() + ")");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        addNPCPacket(npc, player);
    }

    public void hideName(ServerPlayer npc){
        Team team = core.getPlayerListScoreboard().getTeam("9999NPC");
        if (team == null){
            team = core.getPlayerListScoreboard().registerNewTeam("9999NPC");
        }

        team.addEntity(npc.getBukkitEntity());
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
    }

    public void hideFromTab(ServerPlayer npc, Player player){
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc));
    }
}
