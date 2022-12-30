package net.trustgames.core.tablist;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.trustgames.core.Core;
import net.trustgames.core.managers.LuckPermsManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TabPrefix implements Listener {

    private final Core core;

    public TabPrefix(Core core) {
        this.core = core;
        EventBus eventBus = Core.getLuckPerms().getEventBus();
        eventBus.subscribe(core, GroupDataRecalculateEvent.class, this::onGroupChange);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event){

        TablistConfig tablistConfig = new TablistConfig(core);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(tablistConfig.getTablistFile());

        Player player = event.getPlayer();

        String text = config.getString("tablist.prefix." + LuckPermsManager.getPlayerGroup(player));
        System.out.println(text);
        text = PlaceholderAPI.setPlaceholders(player, text);
        System.out.println(text);
        player.playerListName(Component.text(ChatColor.translateAlternateColorCodes('&', text)));
    }

    private void onGroupChange(GroupDataRecalculateEvent event){
        System.out.println("CHANGE");
    }
}
