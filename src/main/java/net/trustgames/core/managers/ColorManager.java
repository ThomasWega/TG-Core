package net.trustgames.core.managers;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the converting of colors
 */
public class ColorManager {

    /**
     * Translate color code of & to ChatColor
     *
     * @param text Text to translate colors on
     * @return Text with translated colors
     */
    public static String color(@NotNull String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
