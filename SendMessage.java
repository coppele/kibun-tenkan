import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * このClassはわかりやすくメッセージが送れます。(多分ですが)<br>
 * <br>
 * 全て静的メソッドの為、インスタンスを作る必要性はありません！<br>
 * ですが使う際は最初に PREFIX と PLUGIN を設定しておくと良さげです( ´･ω･`)b<br>
 * ( なのでユーティリティというわけではなさそうです... )<br>
 * <br>
 * 注意: {@link SendMessage#sendHoverMessage(CommandSender, String, String)} や、<br>
 *      {@link SendMessage#sendHoverClickMessage(CommandSender, String, String, String)}<br>
 *      {@link SendMessage#sendActionbar(CommandSender, String)}<br>
 *      {@link SendMessage#format(String)}<br>
 *      {@link SendMessage#parse(Component)}<br>
 *      はPaper用となっています、ご了承くださいませ...(´･ω･`)
 * 
 * @author coppele
 * @version 1.0
 */
public class SendMessage {
    public static String PREFIX = "";
    public static JavaPlugin PLUGIN = null;

    private SendMessage() {}

    ///////////////////////////////////////////////////////////////////////////
    //            自動でセットします。
    //　手動でもこちらのメソッドでも大丈夫です。
    public static void setUp(JavaPlugin plugin, boolean replacePrefix) {
        PLUGIN = plugin;
        if (replacePrefix) PREFIX = "[" + plugin.getName() + "]";
    }

    ///////////////////////////////////////////////////////////////////////////
    //            メッセージをそのまま送ります。　(PREFIX色)
    public static void sendMessage(CommandSender sender, String text) {
        sender.sendMessage(PREFIX + text);
    }

    ///////////////////////////////////////////////////////////////////////////
    //            コマンドが成功した時に送るメッセージです。　(緑色)
    public static void sendSuccessMessage(CommandSender sender, String text) {
        sender.sendMessage(PREFIX + ChatColor.GREEN + text);
    }

    ///////////////////////////////////////////////////////////////////////////
    //            一般的な情報を送る時のメッセージです。　　　(白色)
    public static void sendInfoMessage(CommandSender sender, String text) {
        sender.sendMessage(PREFIX + ChatColor.WHITE + text);
    }

    ///////////////////////////////////////////////////////////////////////////
    //            ヒントを送る時のメッセージです。　　　　　　(灰色)
    public static void sendAdviceMessage(CommandSender sender, String text) {
        sender.sendMessage(PREFIX + ChatColor.GRAY + text);
    }

    ///////////////////////////////////////////////////////////////////////////
    //            コマンドが失敗した時に送るメッセージです。　(赤色)
    public static void sendFailedMessage(CommandSender sender, String text) {
        sender.sendMessage(PREFIX + ChatColor.RED + text);
    }

    ///////////////////////////////////////////////////////////////////////////
    //            コマンドの入力が失敗した時に送るメッセージです。
    public static void sendErrorMessage(CommandSender sender, String text) {
        sendFailedMessage(sender, text);
        sendAdviceMessage(sender, "\"/help\" からコマンドを確認できます");
    }

    ///////////////////////////////////////////////////////////////////////////
    //            HoverTextのみ送ります。
    public static Component sendHoverMessage(CommandSender sender, String mainText, String subText) {
        return sendHoverClickMessage(sender, mainText, subText, null);
    }

    ///////////////////////////////////////////////////////////////////////////
    //            HoverTextとClickEventを送ります。
    public static Component sendHoverClickMessage(CommandSender sender, String mainText, String subText, String command) {
        Component component = Component.text(PREFIX + mainText);

        if (subText != null) {
            HoverEvent<Component> hoverEvent = HoverEvent.showText(Component.text(subText));
            component = component.hoverEvent(hoverEvent);
        }
        if (command != null) {
            ClickEvent clickEvent = ClickEvent.runCommand(command);
            component = component.clickEvent(clickEvent);
        }

        sender.sendMessage(component);
        return component;
    }

    ///////////////////////////////////////////////////////////////////////////
    //            Actionbarを送ります。
    public static void sendActionbar(CommandSender sender, String text) {
        sender.sendActionBar(Component.text(text));
    }

    ///////////////////////////////////////////////////////////////////////////
    //            Titleを送ります。
    public static void sendTitle(Player player, String mainText, String subText, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(mainText, subText, fadeIn, stay, fadeOut);
    }

    ///////////////////////////////////////////////////////////////////////////
    //            String を Component に変えます。
    public static Component format(String str) {
        return Component.text(str);
    }

    ///////////////////////////////////////////////////////////////////////////
    //            Component を String に変えます。
    public static String parse(Component compo) {
        TextComponent text = (TextComponent) compo;
        return text == null ? null : text.content();
    }
}
