package net.trustgames.core.utils;

import java.util.UUID;

public final class UUIDUtils {

    public static boolean isValidUUID(String uuidString) {
        try {
            //noinspection ResultOfMethodCallIgnored
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
