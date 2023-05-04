package net.trustgames.core.player.activity.config;

import lombok.Getter;
import net.trustgames.toolkit.database.player.activity.config.PlayerActivityType;
import org.bukkit.Material;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Config where a new Action and its Material can be set.
 * The action String is then used to find the action by using contains()
 */
public enum PlayerActivityMaterials {
    JOIN(PlayerActivityType.JOIN, Material.GREEN_BED),
    LEAVE(PlayerActivityType.LEAVE, Material.BLACK_BED);


    /**
     * List of all Action Strings for the action types
     */
    public static final Set<String> ALL_ACTIONS = PlayerActivityType.ALL_ACTIONS;

    /**
     * List of all Materials for the action types
     */
    public static final Set<Material> ALL_MATERIALS = EnumSet.allOf(PlayerActivityMaterials.class)
            .stream()
            .map(playerActivityMaterials -> playerActivityMaterials.icon)
            .collect(Collectors.toSet());

    @Getter
    private final PlayerActivityType activityType;
    @Getter
    private final Material icon;

    PlayerActivityMaterials(PlayerActivityType activityType, Material icon) {
        this.activityType = activityType;
        this.icon = icon;
    }
}
