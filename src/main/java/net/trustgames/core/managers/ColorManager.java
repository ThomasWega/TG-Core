package net.trustgames.core.managers;

import org.bukkit.ChatColor;

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
    public static String translateColors(@Nullable Object text){

        if (text == null){
            return ChatColor.RED + "ERROR: Text was null when translating color codes!";
        }
        return ChatColor.translateAlternateColorCodes('&', text.toString());
    }
}
