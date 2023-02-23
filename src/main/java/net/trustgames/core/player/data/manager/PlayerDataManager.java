package net.trustgames.core.player.data.manager;

import net.trustgames.core.command.TrustCommand;
import net.trustgames.core.config.CorePermissionsConfig;
import net.trustgames.core.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class PlayerDataManager extends TrustCommand {

    public PlayerDataManager() {
        super(CorePermissionsConfig.STAFF.permission);
    }

    @Override
    @AllowConsole
    public void execute(CommandSender sender, String[] args) {
        String statsType = args[0];
        String statsAction = args[1];
        OfflinePlayer target = PlayerUtils.getOfflinePlayer(args[2]);

        switch (statsType){
            case "level":
        }
    }
}
