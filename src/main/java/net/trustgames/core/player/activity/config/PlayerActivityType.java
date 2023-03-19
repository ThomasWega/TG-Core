package net.trustgames.core.player.activity.config;

import org.bukkit.Material;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Config where a new Action and its Material can be set.
 * The action String is then used to find the action by using contains()
 */
public enum PlayerActivityType {
    JOIN("JOIN", Material.GREEN_BED),
    LEAVE("LEAVE", Material.BLACK_BED);


    public final String action;
    public final Material icon;

    PlayerActivityType(String action, Material icon) {
        this.action = action;
        this.icon = icon;
    }

    /**
     * List of all Action Strings for the action types
     */
    public static final Set<String> ALL_ACTIONS = EnumSet.allOf(PlayerActivityType.class)
            .stream()
            .map(playerActivityType -> playerActivityType.action)
            .collect(Collectors.toSet());

    /**
     * List of all Materials for the action types
     */
    public static final Set<Material> ALL_MATERIALS = EnumSet.allOf(PlayerActivityType.class)
            .stream()
            .map(playerActivityType -> playerActivityType.icon)
            .collect(Collectors.toSet());
}
