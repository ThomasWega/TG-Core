package net.trustgames.core.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.trustgames.core.Core;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class MessageLimiter implements Listener {

    private final Core core;

    public MessageLimiter(Core core) {
        this.core = core;
    }

    private final HashMap<UUID, Long> cooldown = new HashMap<>();

    private final Map<String, Double> ranks = new TreeMap<>();

    private final HashMap<UUID, Long> lastMessage = new HashMap<>();


    /*
    TODO - if the message is same, have a larger delay
     */

    /*
    on player chat event (this doesn't include writing commands), it gets the keys from the config and puts them
    to a hashmap with their corresponding values. Then it runs the checks method
     */
    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = core.getConfig();

        for (String rank : Objects.requireNonNull(config.getConfigurationSection("settings.chat-limit-in-seconds")).getKeys(false)) {
            ranks.put(rank, config.getDouble("settings.chat-limit-in-seconds." + rank));
        }
        doChecks(player, event);
    }

    // run the checks and the methods
    public void doChecks(Player player, AsyncChatEvent event) {
        // gets the highest rank player has permission to
        String rank = getPermission(player);

        /*
         IF player isn't in the hashmap or if he is in the hashmap but doesn't have cooldown,
         put him in the cooldown hashmap.
         ELSE IF the player is in the hashmap, but he has a cooldown on him, send him the wait message
         and cancel the event.
         */
        if (!cooldown.containsKey(player.getUniqueId()) || !isOnCooldown(player, rank)) {
            cooldown.put(player.getUniqueId(), System.currentTimeMillis());
        } else if (isOnCooldown(player, rank)) {
            sendMessage(player, rank);
            event.setCancelled(true);
        }
    }

    /*
    check which is the highest permission (group) the player has access to.
    Then return that permission
     */
    public String getPermission(Player player) {
        FileConfiguration config = core.getConfig();

        // default rank's cooldown time is named default in the config
        String rank = "default";
        /*
         goes through all the keys (ranks) in the cooldown time config.
         The keys (ranks) are sorted by lowest cooldown values. For each one, it checks
         if the player has the permission of the rank. If he does, it assigns it to the
         string rank (which is later returned), and breaks the loop. If he doesn't have the permission,
         it goes back and tries the next rank. This continues until finished (meaning the rank is "default")
         or until a rank that player has permission to is found.
         */
        for (String x : ranks.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey).toList()) {
            if (x.equalsIgnoreCase("default")) break;
            if (player.hasPermission(Objects.requireNonNull(config.getString("permissions." + x)))) {
                rank = x;
                break;
            }
        }
        return rank;
    }


    /*
     checks if the player has a cooldown. If he does have a cooldown, it returns true.
     If he doesn't have a cooldown, it returns false
     */
    public boolean isOnCooldown(Player player, String rank) {
        /*
         check if the cooldown time of the given rank is smaller than the
         (current time - the time the player last wrote a message) divided by 1000
         (to convert it to seconds to match the config time)
         */
        return !(ranks.get(rank) <= (System.currentTimeMillis() - cooldown.get(player.getUniqueId())) / 1000d);
    }

    // sends the wait message to the player
    public void sendMessage(Player player, String rank) {
        FileConfiguration config = core.getConfig();

        /*
         checks if the message print wouldn't be too spammy. Meaning, if the player used
         the chat 10 times a second, he would get the wait message only sometimes. The message
         would be cancelled always.
         */
        if (isSpam(player)) return;

        /*
        If the rank is default, send the player a wait message and message to buy a vip
        If the rank is trust (the lowest possible cooldown) send player only the wait message
        If the rank is any other (there is a rank with lower cooldown, but the player already has bought a rank)
        send the wait message and buy a better rank message
         */
        switch (rank) {
            case "default" ->
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join("\n","\n" + String.format(Objects.requireNonNull(config.getString("messages.chat-cooldown")), String.format("%.1f", getWaitTime(player, ranks.get(rank)))) + "\n" + config.getString("messages.buy.rank") + "\n&r")));
            case "trust" ->
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join("\n","\n" + String.format(Objects.requireNonNull(config.getString("messages.chat-cooldown")), String.format("%.1f", getWaitTime(player, ranks.get(rank)))) + "\n&r")));
            default ->
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join("\n","\n" + String.format(Objects.requireNonNull(config.getString("messages.chat-cooldown")), String.format("%.1f", getWaitTime(player, ranks.get(rank)))) + "\n" + config.getString("messages.buy.higher-rank") + "\n&r")));
        }
        // log the last time player got the wait message (used in the anti-spam method)
        lastMessage.put(player.getUniqueId(), System.currentTimeMillis());
    }

    // get the time player needs to wait until using the chat again
    public double getWaitTime(Player player, double time) {
        return (time - ((System.currentTimeMillis() - cooldown.get(player.getUniqueId())) / 1000d));
    }

    // check if the wait message isn't being sent too often to avoid it being too spammy
    public boolean isSpam(Player player) {
        FileConfiguration config = core.getConfig();

        /*
         if he has any last wait message, get the time and make sure the
         current time - the last time of wait message is larger than the min value in config
        */
        if (lastMessage.containsKey(player.getUniqueId())) {
            return config.getDouble("settings.spam-time-in-seconds") > (System.currentTimeMillis() - lastMessage.get(player.getUniqueId())) / 1000d;
        // if the last message doesn't contain the player (meaning he probably didn't receive any wait messages, put him in the map and return false
        } else {
            lastMessage.put(player.getUniqueId(), System.currentTimeMillis());
            return false;
        }
    }

    /*
    when the player leaves, make sure that he isn't no longer in the maps
    - remove him from the last wait message (lastMessage) map
    - remove him from the cooldown time message (cooldown) map
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        lastMessage.remove(player.getUniqueId());
        cooldown.remove(player.getUniqueId());
    }
}
