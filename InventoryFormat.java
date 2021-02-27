import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * このClassはインベントリ作成を直感的に作成することができるプラグインです
 * 
 * formatは {@link InventoryFormat#format(String title, String format, Item... items)} として使います。<br>
 * <br>
 * {@link String title} はGUIのタイトルを決めてください。<br>
 * <br>
 * {@link String format} は<br>
 * |o |b |o |b |o |b |o |b |o |<br>
 * |b |n |n |n |n |n |n |n |b |<br>
 * |o |n |n |n |n |n |n |n |o |<br>
 * |b |n |n |n |n |n |n |n |b |<br>
 * |o |n |n |n |n |n |n |n |o |<br>
 * |b |o |n |o |b |o |b |o |b |<br>
 * こんな感じのものを決めてください(語彙力低下)。<br>
 * 注意: "A~Z"と"a~z"と"1~9" <i>以外</i> は削除されます。<br>
 * 　　: 二文字以上の名前(brownやbr)はエラーの発生を招きます。<br>
 * <br>
 * {@link Item items}... は上記で出した、"o"や"b"に 対応するアイテムを決めてください<br>
 * <br>
 * つまるところ使うとすればこんな感じです(´･ω･`)<br>
 * <br>
 * InventoryFormat.<i>format</i>("空っぽのインベントリ",<br>
 * 　　"|o |b |o |b |o |b |o |b |o |" +<br>
 * 　　"|b |n |n |n |n |n |n |n |b |" +<br>
 * 　　"|o |n |n |n |n |n |n |n |o |" +<br>
 * 　　"|b |n |n |n |n |n |n |n |b |" +<br>
 * 　　"|o |n |n |n |n |n |n |n |o |" +<br>
 * 　　"|b |o |n |o |b |o |b |o |b |",<br>
 * 　　new InventoryFormat.Item('o', new ItemStack(Material.<i>ORANGE_STAINED_GLASS_PANE</i>)),<br>
 * 　　new InventoryFormat.Item('b', new ItemStack(Material.<i>BROWN_STAINED_GLASS_PANE</i>)),<br>
 * 　　new InventoryFormat.Item('n', new ItemStack(Material.<i>AIR</i>))<br>
 * );<br>
 * <br>
 * 最悪|o |b |o...としなくとも<br>
 * obobobobobnnnnnnnbonnnnnnnobnnnnnnnbonnnnnnnobobobobob<br>
 * でも動作します(´･ω･`)
 *
 * @author coppele
 * @version 1.0
 */
public class InventoryFormat {
    public static Inventory format(String title, String s, Item... items) {
        //    all は s の中にある A~Z,a~z のアルファベットが全て入っています。
        // format は s の中にある A~Z,a~z のアルファベットが、被りが一切ない状態で入っています。
        List<Character> all = new ArrayList<>();
        Set<Character> format = new HashSet<>();


        ///////////////////////////////////////////////////////////////////////////
        // 　　　　ここでは s として代入されたStringに例外がないかどうかを確認します。
        //
        // 　最初に s をchar型の配列(c)にし、それが A~Z,a~z,1~9 かどうかを確認します。
        // 次に format に c と一致するものがなければ format に追加します。
        // そして最後に結果に関わらず all に追加します。
        for (char c : s.toCharArray()) {
            if (Pattern.matches("[\\W]", String.valueOf(c))) continue;
            format.add(c);
            all.add(c);
        }

        ///////////////////////////////////////////////////////////////////////////
        // 　　　　　　ここでは format と items が均等であるかどうかを確認します
        //
        // 　まず format のサイズと items のサイズを比べます。
        // そして format 全てに割り振れる items があるかどうかを確認します。
        // どちらも条件を満たせなかった場合 InventoryFormatException がスローされるので注意です。
        if (format.size() != items.length) throw new InventoryFormatException(format.size(), items.length);
        for (Item item : items) {
            if (format.contains(item.getChar())) continue;
            throw new InventoryFormatException(item.getChar() + "がformatの中に存在しません");
        }

        ///////////////////////////////////////////////////////////////////////////
        // 　　　　　　　　　　ここでは Inventory にアイテムを設置します
        //
        // 　まず最初に all のサイズを利用し、インベントリを作成します。
        // ここで、all のサイズが9の段で無かった場合 IllegalArgumentException がスローされるので注意です。
        // 次に all を利用し、Inventory に設置していきます。
        // 最後に、設定した Inventory を返せば、メソッドの処理は完了です。
        Inventory inv = Bukkit.createInventory(null, all.size(), title);
        for (int i = 0; i < all.size(); i++)
            for (Item item : items) {
                if (item.getChar() != all.get(i)) continue;
                ItemStack setItem = item.getItem();
                inv.setItem(i, setItem);
                break;
            }
        return inv;
    }

    ///////////////////////////////////////////////////////////////////////////
    // 　　　　　　　　ここでは ItemStack に char をつけたものを返します。
    //
    // new Inventory.Item('A~Zまたはa~z', new ItemStack(Material));
    // で利用できます。(''<- なのが重要です。)
    public static class Item {
        private final char c;
        private final ItemStack item;

        public Item(char c, ItemStack item) {
            this.c = c;
            this.item = item;
        }

        public char getChar() {
            return c;
        }

        public ItemStack getItem() {
            return item;
        }
    }

    private static class InventoryFormatException extends IllegalArgumentException {
        // これランダムで割り振ってくれるのですね…(´･ω･`)
        static final long serialVersionUID = -5828357610703876919L;

        protected InventoryFormatException() {
            super();
        }

        protected InventoryFormatException(String s) {
            super(s);
        }

        protected InventoryFormatException(int want, int over) {
            super("ItemStackがFormatで必要な数よりも長いまたは短いです: " + want + " != " + over);
        }
    }
}
