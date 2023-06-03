package net.trustgames.core.player.activity.config;

import lombok.Getter;
import net.trustgames.toolkit.database.player.activity.config.PlayerAction;
import org.bukkit.Material;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Config where a new Action and its Material can be set.
 * The action String is then used to find the action by using contains()
 */
public enum PlayerActionMaterials {
    JOIN(PlayerAction.JOIN, Material.GREEN_BED),
    LEAVE(PlayerAction.LEAVE, Material.BLACK_BED);


    /**
     * List of all Action Strings for the action types
     */
    public static final Set<String> ALL_ACTIONS = PlayerAction.ALL_ACTION_STRINGS;

    /**
     * List of all Materials for the action types
     */
    public static final Set<Material> ALL_MATERIALS = EnumSet.allOf(PlayerActionMaterials.class)
            .stream()
            .map(playerActionMaterials -> playerActionMaterials.icon)
            .collect(Collectors.toSet());

    @Getter
    private final PlayerAction action;
    @Getter
    private final Material icon;

    PlayerActionMaterials(PlayerAction action, Material icon) {
        this.action = action;
        this.icon = icon;
    }
}
