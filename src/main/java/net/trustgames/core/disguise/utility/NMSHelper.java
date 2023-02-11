package net.trustgames.core.disguise.utility;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class NMSHelper {
  @Getter private static final NMSHelper instance = new NMSHelper();
  private NMSHelper() {}

  public GameProfile getGameProfile(Player player) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    return ((CraftPlayer) player).getHandle().getGameProfile();
  }

  public Property getTexturesProperty(GameProfile profile) {
    Optional<Property> texturesProperty = profile.getProperties().get("textures").stream().findFirst();
    return texturesProperty.orElse(null);
  }
}
