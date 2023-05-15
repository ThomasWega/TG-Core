package net.trustgames.core.managers.skin;

import org.jetbrains.annotations.NotNull;

/**
 * Stores the texture and signature data of given skin
 */
public record SkinData(@NotNull String texture, @NotNull String signature) {
}