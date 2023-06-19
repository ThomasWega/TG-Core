package net.trustgames.core.cosmetics;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class CosmeticBalloon {
    private final Plugin plugin;
    @Getter
    private final Player holder;
    @Getter
    private final ItemStack balloonItem;
    private final Bee bee;
    private final ArmorStand armorStand;
    @Nullable
    private BukkitTask animationTask;

    /**
     * Balloon that player can hold in hand and run with
     *
     * @param plugin Plugin instance (used for scheduling)
     * @param holder Holder of the balloon
     * @param balloonItem Item the Balloon will have
     */
    public CosmeticBalloon(Plugin plugin, Player holder, ItemStack balloonItem) {
        this.plugin = plugin;
        this.holder = holder;
        this.balloonItem = balloonItem;
        this.bee = createBee();
        this.armorStand = createArmorStand();
    }

    /**
     * Spawn bee
     * @return new Bee with set attributes
     */
    private Bee createBee() {
        return holder.getWorld().spawn(holder.getLocation(), Bee.class, bee -> {
            bee.setAware(false);
            bee.setCanPickupItems(false);
            bee.setAI(false);
            bee.setCollidable(false);
            bee.setGravity(false);
            bee.setInvulnerable(true);
            bee.setInvisible(true);
            bee.setSilent(true);
            bee.setBaby();
            bee.setAgeLock(true);
            bee.setLeashHolder(holder);
        });
    }

    /**
     * Spawn armor stand
     * @return new ArmorStand with set attributes
     */
    private ArmorStand createArmorStand() {
        return holder.getWorld().spawn(holder.getLocation(), ArmorStand.class, as -> {
            as.setSmall(true);
            as.setAI(false);
            as.setCollidable(false);
            as.setGravity(false);
            as.setInvulnerable(true);
            as.setInvisible(true);
            as.setSilent(true);
            as.setLeashHolder(holder);
            as.getEquipment().setHelmet(balloonItem);
        });
    }

    /**
     * Create a {@link BukkitTask} to run animation every tick
     */
    public void startAnimation() {
        this.animationTask = plugin.getServer().getScheduler().runTaskTimer(plugin,
                this::animation,
                1L, 1L
        );
    }

    /**
     * Stop the {@link BukkitTask} responsible for running the animation
     */
    public void stopAnimation() {
        if (animationTask == null) return;
        animationTask.cancel();
    }

    /**
     * Animation calculation and handling of changing the positions
     * */
    private void animation() {
        if (!holder.isOnline()) {
            stopAnimation();
            return;
        }

        Location holderLoc = holder.getLocation();
        Location standMoveLoc = this.armorStand.getLocation().subtract(0.0D, 2.0D, 0.0D).clone();
        Vector vector = holderLoc.toVector().subtract(standMoveLoc.toVector());
        vector.multiply(0.3D);
        standMoveLoc.add(vector);
        double value1 = vector.getX() * 50.0D;
        double value2 = vector.getZ() * 50.0D * -1.0D;

        this.armorStand.setHeadPose(new EulerAngle(Math.toRadians(value1),
                Math.toRadians(0), Math.toRadians(value2)));
        this.armorStand.teleport(standMoveLoc.add(0, 2, 0));
        this.bee.teleport(standMoveLoc.add(0, 0.65, 0));
    }

    /**
     * Stop the animation (if any)
     * and remove the mob as well as the armor stand
     */
    public void remove() {
        stopAnimation();
        this.bee.remove();
        this.armorStand.remove();
    }
}
