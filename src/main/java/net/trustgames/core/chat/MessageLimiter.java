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

/*
NOTE: this class is coded so badly, that it shouldn't even exist. If anyone needs to go through this
at anytime, I am deeply sorry, as you will probably want to grab a huge gun, put it right in your
ass and press it to end this sacrifice. At least I tried to comment it good. Still shitty to understand tho.
- by Wega
 */

public class MessageLimiter implements Listener {

    private final Core core;

    public MessageLimiter(Core core) {
        this.core = core;
    }

    private final HashMap<UUID, Long> cooldownTime = new HashMap<>();
    private final Map<String, Double> ranksChatCooldown = new TreeMap<>();
    private final Map<String, Double> ranksSameChatCooldown = new TreeMap<>();
    private final HashMap<UUID, String> lastPlayerMessage = new HashMap<>();
    private final HashMap<UUID, Long> lastWaitMessage = new HashMap<>();

    /*
    on player chat event (this doesn't include writing commands), it gets the keys from the config and puts them
    to a hashmap with their corresponding values. Then it runs the checks method
     */
    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String playerMessage = event.message().toString();
        FileConfiguration config = core.getConfig();

        /*
        These for loops will loop through all the keys in the config (the normal cooldown
        and the same message cooldown) and save them with the values (time) into hashmaps
        which are later used
         */

        // cooldown for normal messages
        for (String rankCooldown : Objects.requireNonNull(config.getConfigurationSection("settings.chat-limit-in-seconds")).getKeys(false)) {
            ranksChatCooldown.put(rankCooldown, config.getDouble("settings.chat-limit-in-seconds." + rankCooldown));
        }
        // cooldown if the message is same as the last one
        for (String rankSameCooldown : Objects.requireNonNull(config.getConfigurationSection("settings.same-message-limit-in-seconds")).getKeys(false)) {
            ranksSameChatCooldown.put(rankSameCooldown, config.getDouble("settings.same-message-limit-in-seconds." + rankSameCooldown));
        }
        doChecks(player, event, playerMessage);
    }

    // run the checks and the methods
    public void doChecks(Player player, AsyncChatEvent event, String playerMessage) {
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
        if (!lastPlayerMessage.containsKey(player.getUniqueId())){
            lastPlayerMessage.put(player.getUniqueId(), playerMessage);
        } else if (isSameMessage(player, playerMessage) && isOnCooldown(player, rank, true)){
            sendMessage(player, rank, true);
            event.setCancelled(true);
            return;
        }

        /*
        if the message is not the same as the last one, it will use this IF check. It will first put the player
        in the hashmap (if he isn't there already) or if he isn't on cooldown anymore. If he is in on the cooldown
        however, it will send him a wait message and cancel the event.
         */
        if (!cooldownTime.containsKey(player.getUniqueId()) || !isOnCooldown(player, rank, false)) {
            cooldownTime.put(player.getUniqueId(), System.currentTimeMillis());
        } else if (isOnCooldown(player, rank, false)) {
            sendMessage(player, rank, false);
            event.setCancelled(true);
        }
    }

    /*
    check which is the highest permission (group) the player has access to.
    Then return that permission

    IMPORTANT NOTE: in the config.yml. There always needs to be the trust+ rank the highest and
    at the same time, in spam or normal message cooldown times there needs to be the same ranks specified!
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
        for (String x : ranksChatCooldown.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey).toList()) {
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
    public boolean isOnCooldown(Player player, String rank, boolean sameMessage) {

        /*
        Check if the message is same as the last time. In case it is, it will use a completely different
        cooldown time and return true if still in cooldown, otherwise return false.
         Else, if the message isn't the same as the last time, it will use
        the normal cooldown time and return true if he is still in the cooldown
         */
        if (sameMessage){
            return !(ranksSameChatCooldown.get(rank) <= (System.currentTimeMillis() - cooldownTime.get(player.getUniqueId())) / 1000d);
        }
        else{
            /*
         check if the cooldown time of the given rank is smaller than the
         (current time - the time the player last wrote a message) divided by 1000
         (to convert it to seconds to match the config time)
         */
            return !(ranksChatCooldown.get(rank) <= (System.currentTimeMillis() - cooldownTime.get(player.getUniqueId())) / 1000d);
        }
    }

    /*
    This will check if his current messages matches the last message
    in the hashmap. It will remove all non-alphanumeric characters
    from the message (using regex) and compare the current one
    with the one from the hashmap. If they are the same, it return true,
    otherwise if they are different, it returns false.
     */
    public boolean isSameMessage(Player player, String playerMessage){
        if (playerMessage.replaceAll("[^\\p{Alnum}]", "").equalsIgnoreCase(lastPlayerMessage.get(player.getUniqueId()).replaceAll("[^\\p{Alnum}]", ""))){
            return true;
        }
        else{
            lastPlayerMessage.put(player.getUniqueId(), playerMessage);
            return false;
        }
    }

    // sends the wait message to the player
    public void sendMessage(Player player, String rank, boolean sameMessage) {
        FileConfiguration config = core.getConfig();

        /*
         checks if the message print wouldn't be too spammy. Meaning, if the player used
         the chat 10 times a second, he would get the wait message only sometimes. The message
         would be cancelled always.
         */
        if (isSpam(player)) return;


        /*
        Check if the message is the same as the last time. It is given earlier by the boolean in the method.
        THE HIGHEST RANK IN THE CONFIG.YML NEEDS TO BE TRUST+ FOR THIS TO WORK. As with the highest or lowest ranks,
        different wait messages are being sent.
         */
        if (sameMessage){
            switch (rank) {
                case "default" ->
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join("\n","\n" + String.format(Objects.requireNonNull(config.getString("messages.same-chat-cooldown")), String.format("%.1f", getWaitTime(player, ranksSameChatCooldown.get(rank)))) + "\n" + config.getString("messages.buy.rank") + "\n&r")));
                case "trust+" ->
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join("\n","\n" + String.format(Objects.requireNonNull(config.getString("messages.same-chat-cooldown")), String.format("%.1f", getWaitTime(player, ranksSameChatCooldown.get(rank)))) + "\n&r")));
                default ->
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join("\n","\n" + String.format(Objects.requireNonNull(config.getString("messages.same-chat-cooldown")), String.format("%.1f", getWaitTime(player, ranksSameChatCooldown.get(rank)))) + "\n" + config.getString("messages.buy.higher-rank") + "\n&r")));
            }
        }
        else{
         /*
          If the rank is default, send the player a wait message and message to buy a vip
          If the rank is trust (the lowest possible cooldown) send player only the wait message
          If the rank is any other (there is a rank with lower cooldown, but the player already has bought a rank)
          send the wait message and buy a better rank message
         */
            switch (rank) {
                case "default" ->
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join("\n","\n" + String.format(Objects.requireNonNull(config.getString("messages.chat-cooldown")), String.format("%.1f", getWaitTime(player, ranksChatCooldown.get(rank)))) + "\n" + config.getString("messages.buy.rank") + "\n&r")));
                case "trust+" ->
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join("\n","\n" + String.format(Objects.requireNonNull(config.getString("messages.chat-cooldown")), String.format("%.1f", getWaitTime(player, ranksChatCooldown.get(rank)))) + "\n&r")));
                default ->
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join("\n","\n" + String.format(Objects.requireNonNull(config.getString("messages.chat-cooldown")), String.format("%.1f", getWaitTime(player, ranksChatCooldown.get(rank)))) + "\n" + config.getString("messages.buy.higher-rank") + "\n&r")));
            }
        }
        // log the last time player got the wait message (used in the anti-spam method)
        lastWaitMessage.put(player.getUniqueId(), System.currentTimeMillis());
    }

    // get the time player needs to wait until using the chat again
    public double getWaitTime(Player player, double time) {
        return (time - ((System.currentTimeMillis() - cooldownTime.get(player.getUniqueId())) / 1000d));
    }

    // check if the wait message isn't being sent too often to avoid it being too spammy
    public boolean isSpam(Player player) {
        FileConfiguration config = core.getConfig();

        /*
         if he has any last wait message, get the time and make sure the
         current time - the last time of wait message is larger than the min value in config
        */
        if (lastWaitMessage.containsKey(player.getUniqueId())) {
            return config.getDouble("settings.chat-cooldown-max-warn-messages-per-second") > (System.currentTimeMillis() - lastWaitMessage.get(player.getUniqueId())) / 1000d;
        // if the last message doesn't contain the player (meaning he probably didn't receive any wait messages, put him in the map and return false
        } else {
            lastWaitMessage.put(player.getUniqueId(), System.currentTimeMillis());
            return false;
        }
    }

    /*
    when the player leaves, make sure that he isn't no longer in the maps
    - remove him from the last wait message (lastWaitMessage) map
    - remove him from the cooldown time message (cooldownTime) map
    - remove him from the same message (lastPlayerMessage) map
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        lastWaitMessage.remove(player.getUniqueId());
        cooldownTime.remove(player.getUniqueId());
        lastPlayerMessage.remove(player.getUniqueId());
    }
}
