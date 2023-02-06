package net.trustgames.core.commands.messages_commands;

import net.trustgames.core.Core;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sets the config defaults for CoreSettings (commands.yml)
 */
public class MessagesConfig {
    
    private final Core core;

    public MessagesConfig(Core core) {
        this.core = core;
    }

    /**
     * create the config defaults for commands.yml
     */
    public void createDefaults(){
        YamlConfiguration config = YamlConfiguration.loadConfiguration(getMessagesFile());
        
        List<String> discordMessage = new ArrayList<>();
        discordMessage.add("&r");
        discordMessage.add("&9You can join our discord server by clicking the link below.");
        discordMessage.add("  &9⋱ &bdiscord.trustgames.net &9⋰");
        discordMessage.add("&r");
        config.addDefault("messages.discord", discordMessage);

        List<String> websiteMessage = new ArrayList<>();
        websiteMessage.add("&r");
        websiteMessage.add("&fYou can visit our website by clicking the link below.");
        websiteMessage.add("  &f⋱ &7www.trustgames.net &f⋰");
        websiteMessage.add("&r");
        config.addDefault("messages.website", websiteMessage);
        
        List<String> storeMessage = new ArrayList<>();
        storeMessage.add("&r");
        storeMessage.add("&6You can buy benefits on our store by clicking below.");
        storeMessage.add("  &6⋱ &estore.trustgames.net &6⋰");
        storeMessage.add("&r");
        config.addDefault("messages.store", storeMessage);
        try {
            config.options().copyDefaults(true);
            config.save(getMessagesFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getMessagesFile() {
        return new File(core.getDataFolder(), "commands.yml");
    }
}
