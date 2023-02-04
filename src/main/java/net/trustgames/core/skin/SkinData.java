package net.trustgames.core.skin;

import lombok.Data;

/**
 * Stores the texture and signature data of given skin
 */
@Data
public class SkinData {
    private final String texture;
    private final String signature;
}