package net.trustgames.core.skin;

/**
 * Stores the texture and signature data of given string
 */
public class SkinData {
    private final String texture;
    private final String signature;

    public SkinData(String texture, String signature) {
        this.texture = texture;
        this.signature = signature;
    }

    public String getTexture() {
        return texture;
    }

    public String getSignature() {
        return signature;
    }
}