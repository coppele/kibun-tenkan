import com.destroystokyo.paper.Namespaced;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Consumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <h2>アイテムを簡単に作成できます！</h2>
 * {@link ItemStack} や {@link ItemMeta} のややこしさに頭を悩まさずに済むはずです(*´ω`*)<br>
 * さらにgiveやdropなどが直接できます！٩('ω')و <br>
 * <br>
 * ですがやはり {@link AttributeModifier} や {@link PersistentDataContainer} は慣れが必要ですね...(´･ω･`)
 *
 * @author coppele
 * @version 1.0
 */
public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;
    private Boolean result;

    public ItemBuilder(Material type) {
        item = new ItemStack(type);
        meta = item.getItemMeta();
    }
    /**
     * @param type {@link Material} を入れてください
     * @param amount アイテムの数を入れてください
     */
    public ItemBuilder(Material type, int amount) {
        this(type);
        setAmount(amount);
    }
    public ItemBuilder(Material type,String display) {
        this(type);
        setDisplayName(display);
    }
    /**
     * @param type {@link Material} を入れてください
     * @param display アイテムの名前を入れてください
     * @param lore アイテムの説明文を入れてください
     */
    public ItemBuilder(Material type,String display,List<String> lore) {
        this(type, display);
        setLore(lore);
    }
    public ItemBuilder(Material type,String display,String... lore) {
        this(type, display, Arrays.asList(lore));
    }
    private ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = this.item.getItemMeta();
    }

    /**
     * <h2>{@link ItemMeta} を継承したクラスにキャストした状態で指定の処理をします</h2>
     *
     * _使用例_<br>
     * ItemBuilder build = new ItemBuilder(Material.PLAYER_HEAD);<br>
     * build.setCast(SkullMeta.class, meta -> {<br>
     * 　　meta.setOwningPlayer(player);<br>
     * });<br>
     * ItemStack item = build.toItemStack();
     * @param cast {@link ItemMeta} を継承したクラスを入れてください
     * @param function キャストしたクラスに対する処理をしてください
     */
    public <M extends ItemMeta> ItemBuilder setCast(Class<M> cast, Consumer<M> function) {
        function.accept(cast.cast(meta));
        return setMeta();
    }

    /**
     * <h2>{@link Material}を指定します</h2>
     * @param type {@link Material} を入れてください
     */
    public ItemBuilder setType(Material type) {
        item.setType(type);
        return setMeta();
    }

    /**
     * <h2>{@link Material}を取得します</h2>
     * @return {@link Material} が帰ってきます
     */
    public Material getType() {
        return item.getType();
    }

    /**
     * <h2>アイテムの個数 を指定します</h2>
     * @param amount 個数 を入れてください
     */
    public ItemBuilder setAmount(int amount) {
        item.setAmount(amount);
        return setMeta();
    }

    /**
     * <h2>アイテムの個数 を取得します</h2>
     * @return 個数 が帰ってきます
     */
    public int getAmount() {
        return item.getAmount();
    }

    /**
     * <h2>{@link MaterialData} を代入します</h2>
     * @param data {@link MaterialData} を入れてください
     */
    public ItemBuilder setData(MaterialData data) {
        item.setData(data);
        return setMeta();
    }

    /**
     * <h2>{@link MaterialData} を取得します</h2>
     * @return {@link MaterialData} が帰ってきます
     */
    public MaterialData getData() {
        return item.getData();
    }

    // paper start
    /**
     * <h2>{@link String 名前} を代入します</h2>
     * @param name {@link String 名前} を入れてください
     */
    public ItemBuilder setDisplayName(String name) {
        meta.displayName(Component.text(name));
        return setMeta();
    }
    // paper end

    /**
     * <h2>{@link String 名前}があるかどうか を確認します</h2>
     * @return {@link String 名前}があるかどうか が帰ってきます
     */
    public boolean hasDisplayName() {
        return setResult(meta.hasDisplayName());
    }

    // paper start
    /**
     * <h2>{@link String 名前} を取得します</h2>
     * @return {@link String 名前} が帰ってきます
     */
    public String getDisplayName() {
        if (!hasDisplayName()) return null;
        TextComponent text = (TextComponent) meta.displayName();
        return text == null ? null : text.content();
    }

    /**
     * <h2>{@link String 説明} を代入します</h2>
     * @param lore {@link String 説明} を入れてください
     */
    public ItemBuilder setLore(String... lore) {
        return setLore(Arrays.asList(lore));
    }

    /**
     * <h2>{@link List<String> 説明} を代入します</h2>
     * @param lore {@link List<String> 説明} を入れてください
     */
    public ItemBuilder setLore(List<String> lore) {
        meta.lore(lore.stream().map(Component::text).collect(Collectors.toList()));
        return setMeta();
    }

    /**
     * <h2>{@link String 説明}があるかどうか を確認します</h2>
     * @return {@link String 説明}があるかどうか が帰ってきます
     */
    public boolean hasLore() {
        return setResult(meta.hasLore());
    }

    /**
     * <h2>{@link String 説明} を取得します</h2>
     * @return {@link String 説明} が帰ってきます
     */
    public List<String> getLore() {
        List<Component> lore = meta.lore();
        if (lore == null) return new ArrayList<>();
        return lore.stream().map(Component::insertion).collect(Collectors.toList());
    }
    // paper end

    /**
     * <h2>CMD を代入します</h2>
     * @param data CMD を入れてください
     */
    public ItemBuilder setCustomModelData(int data) {
        meta.setCustomModelData(data);
        return setMeta();
    }

    /**
     * <h2>CMD を取得します</h2>
     * @return CMD が帰ってきます
     */
    public int getCustomModelData() {
        return meta.getCustomModelData();
    }

    /**
     * <h2>ダメージ値 を代入します</h2>
     * @param damage ダメージ値 を入れてください
     */
    public ItemBuilder setDamage(int damage) {
        ((Damageable) meta).setDamage(damage);
        return setMeta();
    }

    /**
     * <h2>ダメージ値があるかどうか を確認します</h2>
     * @return ダメージ値があるかどうか が帰ってきます
     */
    public boolean hasDamage() {
        return setResult(((Damageable) meta).hasDamage());
    }

    /**
     * <h2>ダメージ値 を取得します</h2>
     * @return ダメージ値 が帰ってきます
     */
    public int getDamage() {
        return ((Damageable) meta).getDamage();
    }

    /**
     * <h2>不可壊かどうか を代入します</h2>
     * @param unbreakable 不可壊かどうか を入れてください
     */
    public ItemBuilder setUnbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        return setMeta();
    }
    /**
     * <h2>不可壊かどうか を取得します</h2>
     * @return 不可壊かどうか が帰ってきます
     */
    public boolean isUnbreakable() {
        return setResult(meta.isUnbreakable());
    }

    /**
     * <h2>エンチャント を追加します</h2>
     * @param enchantment エンチャント を入れてください
     * @param level エンチャントレベル を入れてください
     */
    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        return addEnchant(enchantment, level, true);
    }

    /**
     * <h2>エンチャント を追加します</h2>
     * @param enchantment エンチャント を入れてください
     * @param level エンチャントレベル を入れてください
     * @param ignoreLevelRestriction 仕様で出るエンチャントレベルを超えていても追加するかどうか を入れてください
     */
    public ItemBuilder addEnchant(Enchantment enchantment, int level, boolean ignoreLevelRestriction) {
        setResult(meta.addEnchant(enchantment, level, ignoreLevelRestriction));
        return setMeta();
    }

    /**
     * <h2>指定のエンチャントがあるかどうか を確認します</h2>
     * @param enchantment エンチャント を入れてください
     * @return 指定のエンチャントがあるかどうか が帰ってきます
     */
    public boolean hasEnchant(Enchantment enchantment) {
        return setResult(meta.hasEnchant(enchantment));
    }

    /**
     * <h2>追加されたエンチャントがあるかどうか を確認します</h2>
     * @return 追加されたエンチャントがあるかどうか が帰ってきます
     */
    public boolean hasEnchants() {
        return setResult(meta.hasEnchants());
    }

    /**
     * <h2>エンチャントレベル を取得します</h2>
     * @param enchantment エンチャント を入れてください
     * @return エンチャントレベル が帰ってきます (なければ 0 )
     */
    public int getEnchantLevel(Enchantment enchantment) {
        return meta.getEnchantLevel(enchantment);
    }

    /**
     * <h2>追加された全てのエンチャント を取得します</h2>
     * @return 追加された全てのエンチャント が帰ってきます
     */
    public Map<Enchantment, Integer> getEnchants() {
        return meta.getEnchants();
    }

    /**
     * <h2>エンチャント を削除します</h2>
     * @param enchantment エンチャント を入れてください
     */
    public ItemBuilder removeEnchant(Enchantment enchantment) {
        setResult(meta.removeEnchant(enchantment));
        return setMeta();
    }

    /**
     * <h2>アイテムフラグ を追加します</h2>
     * @param itemFlags アイテムフラグ を入れてください<br>
     *                  アイテムフラグ - エンチャントなどを隠す事ができます
     */
    public ItemBuilder addItemFlags(ItemFlag... itemFlags) {
        meta.addItemFlags(itemFlags);
        return setMeta();
    }

    /**
     * <h2>追加されたアイテムフラグがあるかどうか を確認します</h2>
     * @return 追加されたアイテムフラグがあるかどうか が帰ってきます
     */
    public boolean hasItemFlag(ItemFlag itemFlag) {
        return setResult(meta.hasItemFlag(itemFlag));
    }

    /**
     * <h2>追加された全てのアイテムフラグ を取得します</h2>
     * @return 追加された全てのアイテムフラグ が帰ってきます
     */
    public Set<ItemFlag> getItemFlags() {
        return meta.getItemFlags();
    }

    /**
     * <h2>アイテムフラグ を削除します</h2>
     * @param itemFlags アイテムフラグ を入れてください
     */
    public ItemBuilder removeItemFlags(ItemFlag... itemFlags) {
        meta.removeItemFlags(itemFlags);
        return setMeta();
    }

    /**
     * <h2>修飾子 を追加します</h2>
     * @param attribute 属性 を入れてください
     * @param amount どれだけ上昇するか を入れてください
     * @param operation どのような方法で追加するか を入れてください (加算したり、乗算したり...)<br>
     *                  修飾子 - プレイヤーに直接作用する細かなステータスです (体力を増やしたり、移動速度をあげたり...)
     */
    public ItemBuilder addAttributeModifier(Attribute attribute, double amount, AttributeModifier.Operation operation) {
        return addAttributeModifier(attribute, amount, operation, null);
    }

    /**
     * <h2>修飾子 を追加します</h2>
     * @param attribute 属性 を入れてください
     * @param amount どれだけ上昇するか を入れてください
     * @param operation どのような方法で追加するか を入れてください
     * @param slot どの装備スロットでのみ作用するか を入れてください (利き手のみ、頭のみ、全部、...)
     */
    public ItemBuilder addAttributeModifier(Attribute attribute, double amount, AttributeModifier.Operation operation, EquipmentSlot slot) {
        return addAttributeModifier(attribute, new AttributeModifier(UUID.randomUUID(), attribute.getKey().getKey(), amount, operation, slot));
    }

    /**
     * <h2>修飾子 を追加します</h2>
     * @param attribute 属性 を入れてください
     * @param modifier どのような方法で追加され、特定のスロットでのみ、どれだけ上昇するか を入れてください
     */
    public ItemBuilder addAttributeModifier(Attribute attribute, AttributeModifier modifier) {
        setResult(meta.addAttributeModifier(attribute, modifier));
        return setMeta();
    }

    /**
     * <h2>追加された修飾子があるかどうか を確認します</h2>
     * @return 追加された修飾子があるかどうか が帰ってきます
     */
    public boolean hasAttributeModifiers() {
        return setResult(meta.hasAttributeModifiers());
    }

    /**
     * <h2>追加された全ての修飾子 を取得します</h2>
     * @return 追加された全ての修飾子 が帰ってきます
     */
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers() {
        Multimap<Attribute, AttributeModifier> Modifiers = meta.getAttributeModifiers();
        if (Modifiers == null) Modifiers = ArrayListMultimap.create();
        return Modifiers;
    }

    /**
     * <h2>追加された全ての修飾子 を取得します</h2>
     * @return 追加された全ての修飾子 が帰ってきます
     */
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return meta.getAttributeModifiers(slot);
    }

    /**
     * <h2>修飾子 を取得します</h2>
     * @param attribute 属性を入れてください
     * @return 指定の修飾子 が帰ってきます
     */
    public Collection<AttributeModifier> getAttributeModifiers(Attribute attribute) {
        return meta.getAttributeModifiers(attribute);
    }

    /**
     * <h2>修飾子 を削除します</h2>
     * @param slot 削除したい修飾子の装備スロット を入れてください<br>
     *             - すごい説明し辛いので {@link ItemMeta#removeAttributeModifier(EquipmentSlot)}<br>
     *             　のJavaDocを参考にしていただけるとありがたいです
     */
    public ItemBuilder removeAttributeModifier(EquipmentSlot slot) {
        setResult(meta.removeAttributeModifier(slot));
        return setMeta();
    }

    /**
     * <h2>修飾子 を削除します</h2>
     * @param attribute 削除したい修飾子の属性 を入れてください
     */
    public ItemBuilder removeAttributeModifier(Attribute attribute) {
        setResult(meta.removeAttributeModifier(attribute));
        return setMeta();
    }

    /**
     * <h2>修飾子 を削除します</h2>
     * @param attribute 削除したい修飾子の属性 を入れてください
     * @param modifier 削除したい修飾子のヽ(´･ω･`)ﾉｳｪ- を入れてください(語彙力低下)
     */
    public ItemBuilder removeAttributeModifier(Attribute attribute,AttributeModifier modifier) {
        setResult(meta.removeAttributeModifier(attribute, modifier));
        return setMeta();
    }

    // Paper start

    /**
     * <h2>設置可能なブロック を追加します</h2>
     * @param type {@link Material} を入れてください<br>
     *                             設置可能なブロック - アドベンチャーモードの場合、指定されたブロックの方面にしか設置する事ができません
     */
    public ItemBuilder addCanPlaceKey(Material type) {
        Set<Namespaced> places = getCanPlaceKeys();
        if (setResult(places.add(type.getKey()))) meta.setPlaceableKeys(places);
        return setMeta();
    }

    /**
     * <h2>設置可能なブロック を代入します</h2>
     * @param types {@link Set<Material>} を入れてください
     */
    public ItemBuilder setCanPlaceKeys(Set<Material> types) {
        meta.setPlaceableKeys(types.stream().map(Material::getKey).collect(Collectors.toList()));
        return setMeta();
    }

    /**
     * <h2>指定の設置可能なブロックがあるかどうか を確認します</h2>
     * @param type {@link Material} を入れてください
     * @return  指定の設置可能なブロックがあるかどうか が帰ってきます
     */
    public boolean hasCanPlaceKeys(Material type) {
        return setResult(getCanDestroyKeys().contains(type.getKey()));
    }

    /**
     * <h2>設置可能なブロックがあるかどうか を確認します</h2>
     * @return  設置可能なブロックがあるかどうか が帰ってきます
     */
    public boolean hasCanPlaceKeys() {
        return setResult(meta.hasPlaceableKeys());
    }

    /**
     * <h2>{@link Set<Namespaced> 追加された全ての設置可能なブロック} を取得します</h2>
     * @return  {@link Set<Namespaced> 追加された全ての設置可能なブロック} が帰ってきます
     */
    public Set<Namespaced> getCanPlaceKeys() {
        return meta.getPlaceableKeys();
    }

    /**
     * <h2>設置可能なブロック を削除します</h2>
     * @param type {@link Material} を入れてください
     */
    public ItemBuilder removeCanPlaceKey(Material type) {
        Set<Namespaced> places = getCanPlaceKeys();
        if (setResult(places.remove(type.getKey()))) meta.setPlaceableKeys(places);
        return setMeta();
    }

    /**
     * <h2>破壊可能なブロック を追加します</h2>
     * @param type {@link Material} を入れてください<br>
     *                             破壊可能なブロック - アドベンチャーモードの場合、指定されたブロックのみ破壊できます
     */
    public ItemBuilder addCanDestroyKeys(Material type) {
        Set<Namespaced> destroys = getCanDestroyKeys();
        if (setResult(destroys.add(type.getKey()))) meta.setDestroyableKeys(destroys);
        return setMeta();
    }

    /**
     * <h2>{@link Set<Material> 破壊可能なブロック} を代入します</h2>
     * @param types {@link Set<Material> 破壊可能なブロック} を入れてください
     */
    public ItemBuilder setCanDestroyKeys(Set<Material> types) {
        meta.setDestroyableKeys(types.stream().map(Material::getKey).collect(Collectors.toList()));
        return setMeta();
    }

    /**
     * <h2>指定の破壊可能なブロックがあるかどうか を確認します</h2>
     * @param type {@link Material} を入れてください
     * @return 指定の破壊可能なブロックがあるかどうか が帰ってきます
     */
    public boolean hasCanDestroyKey(Material type) {
        return setResult(getCanDestroyKeys().contains(type.getKey()));
    }

    /**
     * <h2>破壊可能なブロックがあるかどうか を確認します</h2>
     * @return  破壊可能なブロックがあるかどうか が帰ってきます
     */
    public boolean hasCanDestroyKeys() {
        return setResult(meta.hasDestroyableKeys());
    }

    /**
     * <h2>{@link Set<Namespaced> 追加された全ての破壊可能なブロック} を取得します</h2>
     * @return  {@link Set<Namespaced> 追加された全ての破壊可能なブロック} が帰ってきます
     */
    public Set<Namespaced> getCanDestroyKeys() {
        return meta.getDestroyableKeys();
    }

    /**
     * <h2>設置可能なブロック を削除します</h2>
     * @param type {@link Material} を入れてください
     */
    public ItemBuilder removeCanDestroyKey(Material type) {
        Set<Namespaced> destroys = getCanDestroyKeys();
        if (setResult(destroys.remove(type.getKey()))) meta.setDestroyableKeys(destroys);
        return setMeta();
    }
    // paper end

    /**
     * <h2>{@link PersistentDataContainer} を代入します</h2>
     * @param key {@link NamespacedKey} を入れてください
     * @param value {@link String} を入れてください<br>
     *                            PersistentDataContainer - サーバーが閉じられてもデータを永続的に保持し続けます
     */
    public ItemBuilder setPersistentDataContainer(NamespacedKey key, String value) {
        getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
        return setMeta();
    }

    /**
     * <h2>{@link PersistentDataContainer} を代入します</h2>
     * @param key {@link NamespacedKey} を入れてください
     * @param type {@link PersistentDataType} を入れてください
     * @param value {@link String} を入れてください
     */
    public <T, Z> ItemBuilder setPersistentDataContainer(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        getPersistentDataContainer().set(key, type, value);
        return setMeta();
    }

    /**
     * <h2>指定の{@link PersistentDataContainer}があるかどうか を確認します</h2>
     * @param key {@link NamespacedKey} を入れてください
     * @param type {@link PersistentDataType} を入れてください
     * @return 指定の{@link PersistentDataContainer}があるかどうか が帰ってきます
     */
    public <T, Z> boolean hasPersistentDataContainer(NamespacedKey key, PersistentDataType<T, Z> type) {
        return setResult(getPersistentDataContainer().has(key, type));
    }

    /**
     * <h2>{@link PersistentDataContainer} を取得します</h2>
     * @return  {@link PersistentDataContainer} が帰ってきます
     */
    public PersistentDataContainer getPersistentDataContainer() {
        return meta.getPersistentDataContainer();
    }

    /**
     * <h2>{@link String PersistentDataContainerの値} を取得します</h2>
     * @param key {@link NamespacedKey} を入れてください
     * @return  {@link String PersistentDataContainerの値} が帰ってきます
     */
    public String getPersistentDataContainer(NamespacedKey key) {
        return getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    /**
     * <h2>{@link Z PersistentDataContainerの値} を取得します</h2>
     * @param key {@link NamespacedKey} を入れてください
     * @param type {@link PersistentDataType} を入れてください
     * @return  {@link Z PersistentDataContainerの値} が帰ってきます
     */
    public <T, Z> Z getPersistentDataContainer(NamespacedKey key, PersistentDataType<T, Z> type) {
        return getPersistentDataContainer().get(key, type);
    }



    // せっとあいてむめた (´･ω･`)b
    private ItemBuilder setMeta() {
        item.setItemMeta(meta);
        return this;
    }
    private boolean setResult(boolean bool) {
        if (!bool) result = false;
        return bool;
    }
    
    /**
     * <h2>実行結果が成功したかどうか を確認します</h2>
     * @return 実行結果が成功したかどうか が帰ってきます<br>
     *  - 一度でも失敗した場合falseが帰ってきます
     */
    public Boolean result() {
        return result;
    }

    /**
     * <h2>{@link ItemStack} を取得します</h2>
     * @return {@link ItemStack} が帰ってきます
     */
    public ItemStack toItemStack() {
        return setMeta().item;
    }

    /**
     * <h2>{@link ItemStack} を {@link ItemBuilder} にします</h2>
     * @return {@link ItemBuilder} が帰ってきます
     */
    public static ItemBuilder fromItemStack(ItemStack oldItem) {
        return new ItemBuilder(oldItem);
    }

    /**
     * <h2>{@link ItemBuilder 分身}します</h2>
     * @return {@link ItemBuilder 分身} が帰ってきます
     */
    @Override
    public ItemBuilder clone() {
        try {
            return (ItemBuilder) super.clone();
        } catch (CloneNotSupportedException | ClassCastException ignored) {
        }
        ItemBuilder builder = new ItemBuilder(toItemStack().clone());
        builder.setResult(result);
        return builder;
    }

    /**
     * <h2>{@link String 文字} にします</h2>
     * @return {@link String 文字} が帰ってきます
     */
    @Override
    public String toString() {
        return toItemStack().toString();
    }

    /**
     * <h2>比較します</h2>
     * @param obj {@link Object} を入れてください
     * @return 中身が一語一句一緒であれば良さげな結果が帰ってきます
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ItemBuilder)) {
            return setResult(false);
        }
        ItemBuilder builder = (ItemBuilder) obj;
        return setResult(builder.item.equals(item) && builder.result == result);
    }

    /**
     * <h2>指定の {@link Location} に {@link Item} をドロップします</h2>
     * @param location {@link Location} を入れてください
     * @return {@link Item} が帰ってきます
     */
    public Item drop(Location location) {
        return location.getWorld().dropItem(location, toItemStack());
    }

    /**
     * <h2>指定の {@link Player} に空きスペースがあれば {@link ItemStack} を渡します</h2>
     * @param player {@link Player} を入れてください
     * @return {@link ItemStack} が帰ってきます
     */
    public ItemStack give(Player player) {
        Inventory inv = player.getInventory();
        ItemStack item = toItemStack();
        if (setResult((inv.contains(item) || inv.firstEmpty() != -1))) inv.addItem(item);
        return item;
    }

    /**
     * <h2>指定の {@link Player} に空きスペースがなければその場所にドロップします</h2>
     * @param player {@link Player} を入れてください
     * @return {@link ItemStack} が帰ってきます
     */
    public ItemStack giveOrDrop(Player player) {
        return giveOrDrop(player, player.getLocation());
    }

    /**
     * <h2>指定の {@link Player} に空きスペースがなければ指定の {@link Location} にドロップします</h2>
     * @param player {@link Player} を入れてください
     * @param location {@link Location} を入れてください
     * @return {@link ItemStack} が帰ってきます
     */
    public ItemStack giveOrDrop(Player player, Location location) {
        Inventory inv = player.getInventory();
        ItemStack item = toItemStack();
        if (inv.contains(item) || inv.firstEmpty() != -1) inv.addItem(item);
        else drop(location);
        return item;
    }

    /**
     * <h2>指定の {@link Location} に {@link Block} を設置します</h2>
     * @param location {@link Location} を入れてください
     * @param replace 指定された {@link Location} がブロックでも置き換えるかどうか を入れてください
     * @return {@link Block} が帰ってきます
     */
    public Block place(Location location, boolean replace) {
        return place(location, replace, null);
    }

    /**
     * <h2>指定の {@link Location} に {@link Block} を設置します</h2>
     * @param location {@link Location} を入れてください
     * @param replace 指定された {@link Location} がブロックでも置き換えるかどうか を入れてください
     * @param biome {@link Biome} を入れてください
     * @return {@link Block} が帰ってきます
     */
    public Block place(Location location, boolean replace, Biome biome) {
        ItemStack item = toItemStack();
        if (!item.getType().isBlock()) return null;

        Block block = location.getWorld().getBlockAt(location);
        if (block.getType().isAir() || replace) block.setType(item.getType());
        if (biome != null) block.setBiome(biome);

        return block;
    }
}
