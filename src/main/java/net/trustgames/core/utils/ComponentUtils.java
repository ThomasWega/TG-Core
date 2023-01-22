package net.trustgames.core.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ComponentUtils {

    public static String convertToString(Component component){
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
