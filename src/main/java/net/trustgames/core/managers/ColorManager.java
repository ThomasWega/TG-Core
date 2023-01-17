package net.trustgames.core.managers;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

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
    public static String translateColors(@NotNull String text){
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
