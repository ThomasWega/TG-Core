package net.trustgames.core.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.trustgames.core.cache.UUIDCache;
import net.trustgames.core.chat.config.ChatConfig;
import net.trustgames.core.chat.config.ChatLimitConfig;
import net.trustgames.core.config.CooldownConfig;
import net.trustgames.core.config.CorePermissionsConfig;
import net.trustgames.core.managers.LuckPermsManager;
import net.trustgames.core.utils.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

/**
 * Limit the time player can send the next message. Each permission can have different times set.
 * If the message is the same as the last one, the timeout can be longer. It also makes sure to not send
 * too many cooldown messages to not spam the player's chat.
 */

public final class ChatLimiter implements Listener {
    private final HashMap<UUID, Long> cooldownTime = new HashMap<>();
    private final Map<String, Double> ranksChatCooldown = new HashMap<>();
    private final Map<String, Double> ranksSameChatCooldown = new HashMap<>();
    private final HashMap<UUID, String> lastPlayerMessage = new HashMap<>();
    private final HashMap<UUID, Long> lastWaitMessage = new HashMap<>();

    /**
     * It gets the configured keys and puts them
     * to a hashmap with their corresponding values. Then it runs the checks method
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void limit(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String playerMessage = ColorUtils.stripColor(event.originalMessage());
        if (player.hasPermission(CorePermissionsConfig.STAFF.permission)) return;

        /*
        These for loops will loop through all the configured keys (the normal cooldown
        and the same message cooldown) and save them with the values (time) into hashmaps
        which are later used
         */

        for (ChatLimitConfig limitEnum : ChatLimitConfig.values()) {
            ranksChatCooldown.put(limitEnum.name().toLowerCase(), limitEnum.chatLimitSec);
        }

        for (ChatLimitConfig limitEnum : ChatLimitConfig.values()) {
            ranksSameChatCooldown.put(limitEnum.name().toLowerCase(), limitEnum.chatLimitSameSec);
        }

        doChecks(player, event, playerMessage);
    }

    /**
     * Checks if the player is on cooldown, and if the message is same as the last time,
     * and makes sure to cancel the event, send the proper message to the player
     * or put the player's message to the correct map.
     *
     * @param player        Player who wrote the message
     * @param event         the main AsyncChatEvent
     * @param playerMessage Player's chat message
     */
    private void doChecks(Player player, AsyncChatEvent event, String playerMessage) {
        UUID uuid = UUIDCache.get(player.getName());

        // gets the highest rank player has permission to
        String rank = getPermission(player);

        /*
         IF player isn't in the hashmap or if he is in the hashmap but doesn't have cooldown,
         put him in the cooldown hashmap.
         ELSE IF the player is in the hashmap, but he has a cooldown on him, send him the wait message
         and cancel the event.
         */

        /*
        if the message is same, it will use these if check. It will add the player's last message
        to the hashmap. If he is already in the hashmap and at the same time is on the cooldown, it
        will send him the wait message and cancel the event.
         */
        if (!lastPlayerMessage.containsKey(uuid)) {
            lastPlayerMessage.put(uuid, playerMessage);
        } else if (isSameMessage(player, playerMessage) && isOnCooldown(player, rank, true)) {
            sendMessage(player, rank, true);
            event.setCancelled(true);
            return;
        }

        /*
        if the message is not the same as the last one, it will use this IF check. It will first put the player
        in the hashmap (if he isn't there already) or if he isn't on cooldown anymore. If he is in on the cooldown
        however, it will send him a wait message and cancel the event.
         */
        if (!cooldownTime.containsKey(uuid) || !isOnCooldown(player, rank, false)) {
            cooldownTime.put(uuid, System.currentTimeMillis());
        } else if (isOnCooldown(player, rank, false)) {
            sendMessage(player, rank, false);
            event.setCancelled(true);
        }
    }

    /**
     * check which is the highest permission (group) the player has access to.
     * Then return that permission
     *
     * @param player Player who wrote the message
     * @return Player's highest permission
     */
    private String getPermission(Player player) {
        List<String> possibleRanks = new ArrayList<>();
        for (ChatLimitConfig limitEnum : ChatLimitConfig.values()) {
            possibleRanks.add(limitEnum.name().toLowerCase());
        }
        String rank = LuckPermsManager.getPlayerGroupFromList(player, possibleRanks);

        if (rank == null)
            rank = "default";

        return rank;
    }


    /**
     * checks if the player has a cooldown. If he does have a cooldown, it returns true.
     * If he doesn't have a cooldown, it returns false
     *
     * @param player      Player who wrote the message
     * @param rank        Player's closest highest rank present in configuration
     * @param sameMessage if the message same as the last time
     * @return is Player on Cooldown
     */
    private boolean isOnCooldown(Player player, String rank, boolean sameMessage) {
        UUID uuid = UUIDCache.get(player.getName());

        /*
        Check if the message is same as the last time. In case it is, it will use a completely different
        cooldown time and return true if still in cooldown, otherwise return false.
         Else, if the message isn't the same as the last time, it will use
        the normal cooldown time and return true if he is still in the cooldown
         */
        if (sameMessage) {
            return !(ranksSameChatCooldown.get(rank) <= (System.currentTimeMillis() - cooldownTime.get(uuid)) / 1000d);
        } else {
            /*
         check if the cooldown time of the given rank is smaller than the
         (current time - the time the player last wrote a message) divided by 1000
         (to convert it to seconds to match the configured time)
         */
            return !(ranksChatCooldown.get(rank) <= (System.currentTimeMillis() - cooldownTime.get(uuid)) / 1000d);
        }
    }

    /**
     * Will check if his current messages matches the last message
     * in the hashmap. It will remove all non-alphanumeric characters
     * from the message (using regex) and compare the current one
     * with the one from the hashmap. If they are the same, it returns true,
     * otherwise if they are different, it returns false.
     *
     * @param player        Player who wrote the message
     * @param playerMessage The message the player wrote
     * @return is the same message as the last time
     */
    private boolean isSameMessage(Player player, String playerMessage) {
        UUID uuid = UUIDCache.get(player.getName());
        if (playerMessage.replaceAll("[^\\p{Alnum}]", "").equalsIgnoreCase(lastPlayerMessage.get(uuid).replaceAll("[^\\p{Alnum}]", ""))) {
            return true;
        } else {
            lastPlayerMessage.put(uuid, playerMessage);
            return false;
        }
    }

    /**
     * sends the wait message to the player
     *
     * @param player      Player who wrote the message
     * @param rank        Player's closest highest rank present in configuration
     * @param sameMessage is it the same message as the last time
     */
    private void sendMessage(Player player, String rank, boolean sameMessage) {
        UUID uuid = UUIDCache.get(player.getName());
        /*
         checks if the message print wouldn't be too spammy. Meaning, if the player used
         the chat 10 times a second, he would get the wait message only sometimes. The message
         would be cancelled always.
         */
        if (isSpam(player)) return;

        //Check if the message is the same as the last time. It is given earlier by the boolean in the method.
        if (sameMessage) {
            player.sendMessage(ChatConfig.ON_SAME_COOLDOWN.addSeconds(getWaitTime(player, ranksSameChatCooldown.get(rank))));
        } else {
            player.sendMessage(ChatConfig.ON_COOLDOWN.addSeconds(getWaitTime(player, ranksChatCooldown.get(rank))));
        }
        // log the last time player got the wait message (used in the anti-spam method)
        lastWaitMessage.put(uuid, System.currentTimeMillis());
    }

    /**
     * get the time player needs to wait until using the chat again
     *
     * @param player Player who wrote the message
     * @param time   Time of the cooldown
     * @return The time remaining until the player can write again
     */
    private double getWaitTime(Player player, double time) {
        UUID uuid = UUIDCache.get(player.getName());
        return (time - ((System.currentTimeMillis() - cooldownTime.get(uuid)) / 1000d));
    }

    /**
     * check if the wait message isn't being sent too often to avoid it being too spammy
     *
     * @param player Player who wrote the message
     * @return is the cooldown message being sent too often
     */
    private boolean isSpam(Player player) {
        UUID uuid = UUIDCache.get(player.getName());
        /*
         if he has any last wait message, get the time and make sure the
         current time - the last time of wait message is larger than the min value configured
        */
        if (lastWaitMessage.containsKey(uuid)) {
            return CooldownConfig.WARN_MESSAGES_LIMIT_SEC.value > (System.currentTimeMillis() - lastWaitMessage.get(uuid)) / 1000d;
            // if the last message doesn't contain the player (meaning he probably didn't receive any wait messages, put him in the map and return false
        } else {
            lastWaitMessage.put(uuid, System.currentTimeMillis());
            return false;
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = UUIDCache.get(player.getName());

        lastWaitMessage.remove(uuid);
        cooldownTime.remove(uuid);
        lastPlayerMessage.remove(uuid);
    }
}
