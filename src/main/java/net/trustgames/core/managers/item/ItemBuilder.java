package net.trustgames.core.managers.item;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * <p><b>Please do not remove these lines !!</b></p>
 * <p>ItemBuilder is a tiny Java API for Spigot and Paper that helps you to create ItemStack instance way much easier.</p>
 * <p>You're free to use this class and contribute to the project.</p>
 * <p>For any question, info or bug, please contact me from my GtiHub profile down below.</p>
 *
 * @author RusterX16
 * @link <a href="https://github.com/RusterX16/ItemBuilder">GitHub</a>
 */
@ToString
@EqualsAndHashCode
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ItemBuilder {

    @Getter
    private final ItemStack itemStack;
    @Getter
    private final ItemMeta meta;
    private final Damageable damageable;
    @Getter
    private List<Component> lore = new ArrayList<>();
    @Getter
    private final Set<ItemFlag> flags = new HashSet<>();
    @Getter
    private final Map<Enchantment, Integer> enchantments = new HashMap<>();
    @Getter
    private Material material;
    @Getter
    private Component displayName;
    @Getter
    private int amount;
    @Getter
    private short durability;
    @Getter
    private boolean unbreakable;
    private boolean save = false;

    /**
     * Create a new ItemBuilder
     *
     * @param itemStack The ItemStack to build from
     */
    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = new ItemStack(itemStack);
        material = itemStack.getType();
        amount = itemStack.getAmount();
        enchantments.putAll(itemStack.getEnchantments());
        meta = itemStack.getItemMeta();
        damageable = (Damageable) meta;

        if (itemStack.hasItemMeta()) {
            if (meta.hasDisplayName()) {
                displayName = meta.displayName();
            }
            if (meta.hasLore()) {
                lore.addAll(Objects.requireNonNull(meta.lore()));
            }
            flags.addAll(meta.getItemFlags());
            durability = (short) ((Damageable) meta).getDamage();
        }
    }

    /**
     * Create a new ItemBuilder
     *
     * @param material The material of the item
     * @param amount   The amount of item
     */
    public ItemBuilder(Material material, int amount) {
        itemStack = new ItemStack(material, amount);
        meta = itemStack.getItemMeta();
        damageable = (Damageable) meta;
        this.material = material;
        this.amount = amount;
        durability = material.getMaxDurability();
        unbreakable = false;
    }

    /**
     * Create a new ItemBuilder
     *
     * @param material The material of the item
     */
    public ItemBuilder(Material material) {
        this(material, 1);
    }

    /**
     * Create a new ItemBuilder
     *
     * @param builder The ItemBuilder to copy from
     */
    public ItemBuilder(@NotNull ItemBuilder builder) {
        itemStack = new ItemStack(builder.itemStack);
        meta = itemStack.getItemMeta();
        damageable = (Damageable) meta;
        material = builder.material;
        displayName = builder.displayName;
        amount = builder.amount;
        durability = builder.durability;
        unbreakable = builder.unbreakable;
        lore.addAll(builder.lore);
        flags.addAll(builder.flags);
        enchantments.putAll(builder.enchantments);
        save = builder.save;
        itemStack.setItemMeta(meta);
    }

    /**
     * Change the material of the item
     *
     * @param material The material to set
     * @return The ItemBuilder
     */
    public ItemBuilder material(Material material) {
        this.material = material;
        itemStack.setType(material);
        return this;
    }

    /**
     * Set a new display name to the item
     *
     * @param displayName The name to display
     * @return The ItemBuilder
     */
    public ItemBuilder displayName(Component displayName) {
        this.displayName = displayName
                // in vanilla minecraft italic may be set on some items
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        meta.displayName(this.displayName);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Define an item lore
     * overrides existing lore
     *
     * @param lore A collection of lines to set as lore
     * @return The ItemBuilder
     */
    public ItemBuilder lore(@NotNull List<Component> lore) {
        // handle if the list is immutable
        List<Component> newLore = new ArrayList<>(lore);
        // in vanilla minecraft italic may be set on some items
        newLore.replaceAll(component -> component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        this.lore = newLore;
        meta.lore(this.lore);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Define an item lore
     * overrides existing lore
     *
     * @param lines the lines set as lore
     * @return The ItemBuilder
     */
    public ItemBuilder lore(Component... lines) {
        return lore(List.of(lines));
    }

    /**
     * Add a new line to the item lore
     *
     * @param line The text to append
     * @return The ItemBuilder
     */
    public ItemBuilder appendLoreLine(Component line) {
        lore.add(line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(lore);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Define a new line in the lore on the given index.<br>
     *
     * @param index    The index where to set the line
     * @param line     The text to display on lore
     * @param override <br><b>true</b> : replace existing line<br><b>false</b> : shifts the next elements
     * @return The ItemBuilder
     */
    public ItemBuilder setLoreLine(int index, Component line, boolean override) {
        line = line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        if (override) {
            lore.set(index, line);
        } else {
            lore.add(index, line);
        }
        meta.lore(lore);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Define a new line in the lore on the given index.<br>
     * Will override the current line if existing
     *
     * @param index The index where to set the line
     * @param line  The text to display on lore
     * @return The ItemBuilder
     */
    public ItemBuilder setLoreLine(int index, Component line) {
        return setLoreLine(
                index,
                line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                true
        );
    }

    /**
     * Remove a line of the lore by the index of where the line is
     *
     * @param index The index of the line to delete
     * @return The ItemBuilder
     */
    public ItemBuilder removeLoreLine(int index) {
        lore.remove(index);
        meta.lore(lore);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Remove the given line of the lore
     *
     * @param line The line to delete
     * @return The ItemBuilder
     */
    public ItemBuilder removeLoreLine(Component line) {
        line = line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        lore.removeAll(Collections.singleton(line));
        meta.lore(lore);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Clear the lore
     *
     * @return The ItemBuilder
     */
    @SuppressWarnings("UnusedReturnValue")
    public ItemBuilder clearLore() {
        lore.clear();
        meta.lore(lore);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * remove ItemFlag from the item
     *
     * @param flags The flag to hide
     * @return The ItemBuilder
     */
    public ItemBuilder hideFlag(ItemFlag @NotNull ... flags) {
        this.flags.addAll(List.of(flags));
        meta.addItemFlags(flags);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Hide all ItemFlags from the item
     *
     * @return The ItemBuilder
     */
    public ItemBuilder hideFlags() {
        return hideFlag(ItemFlag.values());
    }

    /**
     * Add ItemFlag to the item
     *
     * @param flags The flag to show
     * @return The ItemBuilder
     */
    public ItemBuilder showFlag(ItemFlag @NotNull ... flags) {
        List.of(flags).forEach(this.flags::remove);
        meta.removeItemFlags(flags);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Show all ItemFlags on the item
     *
     * @return The ItemBuilder
     */
    public ItemBuilder showFlags() {
        return showFlag(ItemFlag.values());
    }

    /**
     * Add some enchantments to the item by passing a Map in parameter
     *
     * @param enchantments The Map of enchantements
     * @return The ItemBuilder
     */
    public ItemBuilder enchantment(Map<Enchantment, Integer> enchantments) {
        this.enchantments.putAll(enchantments);
        enchantments.forEach((e, i) -> meta.addEnchant(e, i, true));
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Add a single enchantment to the item
     *
     * @param enchantment The enchantement to add
     * @param level       The level of the enchantment
     * @return The ItemBuilder
     */
    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        enchantments.put(enchantment, level);
        meta.addEnchant(enchantment, level, true);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Remove all enchantment from the item that match with the enchantement and the given level
     *
     * @param enchantment The enchantment to remove
     * @param level       The level of the enchantment to remove
     * @return The ItemBuilder
     */
    public ItemBuilder removeEnchantment(Enchantment enchantment, int level) {
        enchantments.entrySet().stream().filter(e -> e.getValue() == level).forEach(e -> {
            enchantments.remove(enchantment, level);
            meta.removeEnchant(e.getKey());
        });
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Remove some enchantments from the item by a collection of enchantments given in parameter
     *
     * @param enchantments The enchantements to remove
     * @return The ItemBuilder
     */
    public ItemBuilder removeEnchantments(Enchantment... enchantments) {
        List.of(enchantments).forEach(e -> {
            this.enchantments.remove(e);
            meta.removeEnchant(e);
        });
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Remove some enchantments from the item by a collection of level that match the enchantments given in parameter
     *
     * @param levels The levels of enchantments to remove
     * @return The ItemBuilder
     */
    public ItemBuilder removeEnchantments(int @NotNull ... levels) {
        enchantments.forEach((key, value) -> Arrays.stream(levels).filter(l -> value == l).forEach(l -> {
            enchantments.remove(key);
            meta.removeEnchant(key);
        }));
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Check if the item has some enchantments
     *
     * @return true if the item has some enchantments
     */
    public boolean hasEnchantments() {
        return !enchantments.isEmpty();
    }

    /**
     * Change the amount of the item
     *
     * @param amount The amount to set
     */
    public ItemBuilder amount(int amount) {
        this.amount = amount;
        itemStack.setAmount(amount);
        return this;
    }

    /**
     * Set a durability on the item.
     *
     * @param durability The new durability of the item
     * @return The ItemBuilder
     */
    public ItemBuilder damage(short durability) {
        this.durability = durability;
        damageable.setDamage(durability);
        itemStack.setItemMeta(damageable);
        return this;
    }

    /**
     * Damage the item. The value given will be subtracted to the current item life.
     *
     * @param damage The damage to deal
     * @return The ItemBuilder
     */
    public ItemBuilder durability(short damage) {
        return damage((short) (itemStack.getMaxItemUseDuration() - damage));
    }

    /**
     * Set the item unbreakable
     *
     * @param unbreakable <br><b>true</b> : The item will never take damage<br><b>false</b> : Basic behavior of an item
     * @return the ItemBuilder
     */
    public ItemBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        meta.setUnbreakable(true);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Add a specified key to the items DataContainer.
     * Similar to NBT tags
     *
     * @param key   The key instance with plugin and tag name
     * @param type  Type of the value stored
     * @param value Value to be stored
     * @param <T>   the primary object type that is stored in the given ta
     * @param <Z>   the retrieved object type when applying this tag typ
     * @return the ItemBuilder
     * @see PersistentDataContainer
     * @see PersistentDataType
     */
    public <T, Z> ItemBuilder container(@NotNull NamespacedKey key,
                                        @NotNull PersistentDataType<T, Z> type,
                                        @NotNull Z value) {
        meta.getPersistentDataContainer().set(key, type, value);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Build the item
     *
     * @return The ItemStack
     */
    public ItemStack build() {
        return itemStack;
    }


    public boolean hasFlags() {
        return !flags.isEmpty();
    }

    public PersistentDataContainer getContainer() {
        return this.meta.getPersistentDataContainer();
    }
}
