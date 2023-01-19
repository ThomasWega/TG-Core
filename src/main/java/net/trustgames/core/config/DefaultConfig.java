package net.trustgames.core.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Sets the config defaults for the default config (config.yml)
 */
public class DefaultConfig {

    /**
     * Create the defalts for the default config (config.yml)
     *
     * @param defaultConfig plugin.getConfig()
     */
    public static void create(@NotNull FileConfiguration defaultConfig) {

        // placeholders
        defaultConfig.addDefault("placeholders.ip", "play.trustgames.net");
        defaultConfig.addDefault("placeholders.discord", "discord.trustgames.net");
        defaultConfig.addDefault("placeholders.website", "www.trustgames.net");
        defaultConfig.addDefault("placeholders.store", "store.trustgames.net");

        // prefixes
        String prefix_chat = "&3Chat | ";

        // messages
        defaultConfig.addDefault("messages.server-restart", "&eServer is restarting...");
        defaultConfig.addDefault("messages.command-spam", prefix_chat + "&8Please don't spam the command!");
        defaultConfig.addDefault("messages.no-permission", prefix_chat + "&8You don't have permission to perform this command!");
        defaultConfig.addDefault("messages.chat-cooldown", prefix_chat + "&8Wait another %s seconds before using chat again!");
        defaultConfig.addDefault("messages.same-chat-cooldown", prefix_chat + "&8Don't write the same message! (wait %s seconds)");
        defaultConfig.addDefault("messages.only-in-game-command", "This command can be executed by in-game players only!");
        defaultConfig.addDefault("messages.buy.rank", "&fBuy a rank for better experience.");
        defaultConfig.addDefault("messages.buy.higher-rank", "&fBuy a higher rank for even better experience.");
        defaultConfig.addDefault("messages.mariadb-disabled", prefix_chat + "&8Database is disabled!");
        defaultConfig.addDefault("messages.command-invalid-argument", prefix_chat + "&8You need to specify a valid argument!");
        defaultConfig.addDefault("messages.command-invalid-player", prefix_chat + "&8The player %s doesn't exist!");
        defaultConfig.addDefault("messages.command-no-player-activity", prefix_chat + "&8No activity data for player %s!");
        defaultConfig.addDefault("messages.command-no-id-activity", prefix_chat + "&8No activity data for ID %s!");

        // cooldowns
        defaultConfig.addDefault("cooldowns.max-commands-per-second", 5d);
        defaultConfig.addDefault("cooldowns.cooldown-warn-messages-limit-in-seconds", 0.5d);
        defaultConfig.addDefault("cooldowns.chat-limit-in-seconds.default", 15d);
        defaultConfig.addDefault("cooldowns.chat-limit-in-seconds.prime", 10d);
        defaultConfig.addDefault("cooldowns.chat-limit-in-seconds.knight", 5d);
        defaultConfig.addDefault("cooldowns.chat-limit-in-seconds.lord", 3d);
        defaultConfig.addDefault("cooldowns.chat-limit-in-seconds.titan", 0.1d);
        defaultConfig.addDefault("cooldowns.same-message-limit-in-seconds.default", 120d);
        defaultConfig.addDefault("cooldowns.same-message-limit-in-seconds.prime", 60d);
        defaultConfig.addDefault("cooldowns.same-message-limit-in-seconds.knight", 45d);
        defaultConfig.addDefault("cooldowns.same-message-limit-in-seconds.lord", 25d);
        defaultConfig.addDefault("cooldowns.same-message-limit-in-seconds.titan", 10d);

        // tablist
        defaultConfig.addDefault("tablist.header", List.of("&e&lTRUSTGAMES &f- &7Chillin' on the hub"));
        defaultConfig.addDefault("tablist.footer", List.of("&astore.trustgames.net"));

        // chat
        defaultConfig.addDefault("chat.allow-colors-permission", "core.knight");
    }
}
