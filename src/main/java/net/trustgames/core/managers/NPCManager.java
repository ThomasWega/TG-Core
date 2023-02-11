package net.trustgames.core.managers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
import net.trustgames.core.cache.EntityCache;
import net.trustgames.core.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class NPCManager {

    private final Core core;

    private final ProtocolManager manager;
    private static CooldownManager cooldownManager;

    public NPCManager(Core core) {
        this.core = core;
        manager = core.getProtocolManager();
        cooldownManager = new CooldownManager();
    }

    /**
     * Create a new npc using NMS Packets. This method won't spawn it!
     *
     * @param location Location of the npc
     * @param name     Name of the npc
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
     * @param npc    Npc to add
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
     * @param npc    NPC to remove
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
     * @param npc        NPC to set the location to
     * @param player     Player to set the NPC to
     * @param yaw        Location yaw
     * @param pitch      Location pitch
     * @param straighten Try to make the npc body point the same direction as the head
     */
    public void lookAtPosition(Entity npc, Player player, float yaw, float pitch, boolean straighten) {
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

        // horizontal head movement
        connection.send(new ClientboundRotateHeadPacket(npc, (byte) (yaw * 256 / 360)));
        // body movement and vertical head movement
        connection.send(new ClientboundMoveEntityPacket.Rot(npc.getId(), (byte) angle, (byte) (pitch * 256 / 360), true));
    }

    /**
     * NPC will look at the players position and follow him
     *
     * @param npc         NPC to make look at the player
     * @param player      Player NPC will be looking at
     * @param npcLocation Location of the NPC
     */
    public void lookAtPlayer(ServerPlayer npc, Player player, Location npcLocation) {

        Location loc = npcLocation.clone();
        //Calculate a new direction by subtracting the location of the player vector from the location vector of the npc
        loc.setDirection(player.getLocation().subtract(loc).toVector());

        // if the player is in larger distance than 10 blocks, the npcs will stop looking at him
        if (loc.distance(player.getLocation()) > 10) {
            lookAtPosition(npc, player, npcLocation.getYaw(), npcLocation.getPitch(), false);
            return;
        }

        float yaw = loc.getYaw();
        float pitch = loc.getPitch();

        ServerGamePacketListenerImpl ps = ((CraftPlayer) player).getHandle().connection;

        // horizontal head movement
        ps.send(new ClientboundRotateHeadPacket(npc, (byte) ((yaw % 360) * 256 / 360)));
        // body movement and vertical head movement
        ps.send(new ClientboundMoveEntityPacket.Rot(npc.getBukkitEntity().getEntityId(), (byte) ((yaw % 360.) * 256 / 360), (byte) ((pitch % 360.) * 256 / 360), false));
    }

    // NOTE: not sure if this works
    public static void move(ServerPlayer npc, Player player, double x, double y, double z) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundMoveEntityPacket.Pos(
                npc.getId(), (short) (x * 4096), (short) (y * 4096), (short) (z * 4096), true));
    }

    /**
     * Apply the skin to the given NPC
     *
     * @param npc       NPC to apply skin to
     * @param player    Player to set the NPC to
     * @param texture   Texture of the skin
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
        setTeamValues(team);
    }

    /**
     * Hide the NPC from the TAB
     *
     * @param npc    NPC to hide
     * @param player Player to hide NPC from
     */
    public void hideTab(ServerPlayer npc, Player player) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc));
    }

    /**
     * Sets the equipment for the npc from the item list
     *
     * @param npc        NPC to set equipments for
     * @param player     Player who will see the NPC with equipment
     * @param equipments What items to set as equipment
     */
    public void equipment(ServerPlayer npc, Player player, List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipments) {
        PacketContainer packet = manager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packet.getIntegers().write(0, npc.getId());
        packet.getSlotStackPairLists().write(0, equipments);

        try {
            manager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private enum ActionType {
        COMMAND, MESSAGE
    }

    /**
     * Listens for the entity use packet. Handles what action
     * should be taken on entity click by getting the actions
     * from the config
     *
     * @param npcs   List of NPCs to run actions on
     * @param config Config where the NPCs are specified
     */
    public void interact(List<ServerPlayer> npcs, YamlConfiguration config) {
        ProtocolManager manager = core.getProtocolManager();

        manager.addPacketListener(new PacketAdapter(core, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                UUID uuid = EntityCache.getUUID(player);
                int entityId = packet.getIntegers().read(0);

                for (ServerPlayer npc : npcs) {

                    if (npc.getId() == entityId) {

                        boolean isPresent = Objects.requireNonNull(
                                config.getConfigurationSection("npcs")).getKeys(false).contains(npc.displayName);
                        if (!isPresent) return;

                        double cooldown = config.getDouble("npcs." + npc.displayName + ".action.cooldown");
                        if (cooldownManager.commandCooldown(uuid, cooldown)) return;

                        String action = config.getString("npcs." + npc.displayName + ".action.type");
                        List<String> value = config.getStringList("npcs." + npc.displayName + ".action.value");
                        ActionType actionType = ActionType.valueOf(action);

                        switch (actionType) {
                            case COMMAND:
                                core.getServer().getScheduler().runTask(core, () -> value.forEach(player::performCommand));
                            case MESSAGE:
                                for (String s : value) {
                                    player.sendMessage(MiniMessage.miniMessage().deserialize(s));
                                }
                        }
                        break;
                    }
                }
            }
        });
    }

    public void glow(ServerPlayer npc, TextColor color) {
        Scoreboard scoreboard = core.getPlayerListScoreboard();

        CraftPlayer npcEntity = npc.getBukkitEntity();
        npcEntity.setGlowing(true);
        Team team = scoreboard.getTeam(npc.displayName);

        if (team == null) {
            team = scoreboard.registerNewTeam(npc.displayName);
            team.color(NamedTextColor.nearestTo(color));
            setTeamValues(team);
        }

        if (!team.hasEntity(npcEntity))
            team.addEntity(npcEntity);
    }

    private void setTeamValues(Team team){
        team.prefix(ColorUtils.color("&8[NPC] "));
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
    }
}
