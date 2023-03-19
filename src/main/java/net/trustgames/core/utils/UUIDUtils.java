package net.trustgames.core.utils;

import jline.internal.Nullable;

import java.util.UUID;

public final class UUIDUtils {

    public static boolean isValidUUID(@Nullable String uuidString) {
        try {
            //noinspection ResultOfMethodCallIgnored
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
