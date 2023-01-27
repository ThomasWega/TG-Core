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

        float add = 32;
        float angle = (yaw * 256.0f / 360.0f);
        if (angle - add > -128 && angle < 0){
            System.out.println("1");
            angle = angle - add;
        }
        else if (angle + add >= 0){
            System.out.println("2");
            angle = angle + add;
        }


        connection.send(new ClientboundRotateHeadPacket(npc, (byte)(yaw * 256 / 360)));
        connection.send(new ClientboundMoveEntityPacket.Rot(npc.getId(), (byte)angle, (byte)(pitch * 256 / 360), true));

        System.out.println(angle);
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
           //     String skin = reply.substring(indexOfValue + 10, reply.indexOf("\"", indexOfValue + 10));
            //    String signature = reply.substring(indexOfSignature + 14, reply.indexOf("\"", indexOfSignature + 14));

                String skin = "ewogICJ0aW1lc3RhbXAiIDogMTY3NDgyODU2NDg2MCwKICAicHJvZmlsZUlkIiA6ICIwNTVhOTk2NTk2M2E0YjRmOGMwMjRmMTJmNDFkMmNmMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVWb3hlbGxlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzI3ZDBjODk2MGY4N2RiZTc2OGQ2YTE0ZDQ2NDc1NDBlM2Y1NzJjZTYwMjEyNTgyYWQ1OGU3NjU4YWY0NGVhYjEiCiAgICB9CiAgfQp9";
                String signature = "trjc49KCGED81QrSAiNxLhMnwGa+W4vjpujTyG9iTHc8JMi9h5eG5E2BkVv8B76wJlWz261FKEIZA9XYFcJtkfBeZRpswUL79gO7SN41lembKK4ajz5mLbkP7j2eeG0A0VjbfonD7EhGyKJzIGEhLFXLZplgERd3OfDnqR51Y0lQA7HvYgnO9+NjEHyd0POTCsp0XtMGWxFUskcxa/5Vv5yc11EJpbpFrhXgLg0kjh9DfKTM3f+7KZN+NImHeAAuJ/2N66EUsZPidSkhMAorDF1T1hrCZCARR3lxMi3zcpAmekyVLXH9oGLQESPDjGCeqifZJtxnBqubDjTuTmdNU6muXE4QS2qaSdq1X/jvNK1mVnQkqImI/ZJjqOVTzDG8w5DnkSeGOECNSdHJDbuhjisnQvg+V8/HEuPzGqlkXSYDa7bj5tkHjV8GlHp15TIxMagPocrMdJhbqm1xNMmPCwUDtUxtD35UPirSFCV2WXq7zN89pmpb6M4ustcLWf0TLPEc5J9RzUaSqkJI/3SBGZyrurwcfDJAl3hY4rJfdofubQV57HGoxf5sJOo+TIuKdb7P+9XooJl1aaejVKlZB5tq15FiIC9QKtkje5krLWlhrGFKNcr+LWcBp4V7ZvmdzAl/je9IflxK/PraokJYuKbDrRYSZ7XiNUGQja2RFmQ=";

                npc.getGameProfile().getProperties().put("textures", new Property("textures", skin, signature));
            }

            else {
                Bukkit.getConsoleSender().sendMessage("Connection could not be opened when fetching player skin (Response code " + connection.getResponseCode() + ", " + connection.getResponseMessage() + ")");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        addNPCPacket(npc, player);

        // Second layer on skin
        SynchedEntityData dataWatcher = npc.getEntityData();
        dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 126);
        ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(npc.getId(), dataWatcher, true);
        ((CraftPlayer) player).getHandle().connection.send(packet);
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
