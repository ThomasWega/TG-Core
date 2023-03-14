package net.trustgames.core.utils;

import java.util.Base64;

public final class Base64Utils {

    /**
     * @param id Plain String
     * @return Encoded String
     */
    public static String encode(String id) {
        return Base64.getEncoder().encodeToString(id.getBytes());
    }

    /**
     * @param encodedId Encoded String
     * @return Decoded String or Null
     */
    public static String decode(String encodedId) {
        try {
            return new String(Base64.getDecoder().decode(encodedId));
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }
}
