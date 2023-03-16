package net.trustgames.core.player.activity;

import org.bukkit.Material;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum PlayerActivityType {
    JOIN("JOIN", Material.GREEN_BED),
    LEAVE("LEAVE", Material.BLACK_BED);

    public final String action;
    public final Material icon;

    PlayerActivityType(String action, Material icon) {
        this.action = action;
        this.icon = icon;
    }

    public static final Set<String> ALL_ACTIONS = EnumSet.allOf(PlayerActivityType.class)
            .stream()
            .map(playerActivityType -> playerActivityType.action)
            .collect(Collectors.toSet());

    public static final Set<Material> ALL_MATERIALS = EnumSet.allOf(PlayerActivityType.class)
            .stream()
            .map(playerActivityType -> playerActivityType.icon)
            .collect(Collectors.toSet());
}
