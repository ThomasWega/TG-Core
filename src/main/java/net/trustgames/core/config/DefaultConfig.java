package net.trustgames.core.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class DefaultConfig {

    public static void create(@NotNull FileConfiguration defaultConfig) {

        // placeholders
        defaultConfig.addDefault("placeholders.ip", "play.trustgames.net");
        defaultConfig.addDefault("placeholders.discord", "discord.trustgames.net");
        defaultConfig.addDefault("placeholders.website", "www.trustgames.net");
        defaultConfig.addDefault("placeholders.store", "store.trustgames.net");

        // messages
        defaultConfig.addDefault("messages.server-restart", "&eServer is restarting...");
        defaultConfig.addDefault("messages.command-spam", "&cPlease don't spam the command!");
        defaultConfig.addDefault("messages.no-permission", "&cYou don't have permission to perform this command!");
        defaultConfig.addDefault("messages.chat-cooldown", "&cYou need to wait for another %s seconds before using the chat again!");
        defaultConfig.addDefault("messages.same-chat-cooldown", "&cYou can't write the same message for another %s seconds!");
        defaultConfig.addDefault("messages.only-in-game-command", "&cThis command can be executed by in-game players only!");
        defaultConfig.addDefault("messages.buy.rank", "&fBuy rank on &e" + defaultConfig.getString("placeholders.store") + " &ffor a better experience.");
        defaultConfig.addDefault("messages.buy.higher-rank", "&fBuy a higher rank on &e" + defaultConfig.getString("placeholders.store") + " &ffor a better experience.");

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
    }
}
