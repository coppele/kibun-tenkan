import co.aikar.timings.TimedEventExecutor;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Consumer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * {@link Event イベント}を {@link Consumer} でさくっと登録できます(´･ω･`)b<br>
 * 基本的なシステムは {@link org.bukkit.plugin.SimplePluginManager#registerEvent} などを参考にさせていただいています。
 */
public final class EventExecutor extends JavaPlugin {

    private final Listener listener = new Listener() {};

    @Override
    public void onEnable() {

        register(listener, PlayerJoinEvent.class, event -> {

            var player = event.getPlayer();
            player.sendMessage("いらっしゃいませこんにちは！マイクに向かってこんにちは！");

        }, EventPriority.NORMAL, false);

    }

    /**
     * {@link Event}を登録します(´･ω･`)
     * @param listener {@link HandlerList#unregister(Listener)} などで解除する際に使う {@link Listener} を決めてください！
     * @param clazz どのイベントにするかを選んでください！
     * @param consumer 実行内容を決めてください！
     * @param priority {@link EventPriority} を参考に設定してください。
     * @param ignoreCancelled すでにキャンセルされていた場合実行されなくなります。優先度については{@link EventPriority}を参考に...
     * @throws IllegalPluginAccessException clazz に Event.getHandlerList() が無い場合にスローされます。
     */
    public <E extends Event> void register(Listener listener, Class<E> clazz, Consumer<E> consumer, EventPriority priority, boolean ignoreCancelled) {
        try {

            var method = getHandlerList(clazz);
            method.setAccessible(true);
            var handler = (HandlerList) method.invoke(null);

            var executor = new TimedEventExecutor((also, event) -> consumer.accept(clazz.cast(event)), this, null, clazz);
            handler.register(new RegisteredListener(listener, executor, priority, this, ignoreCancelled));

        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new IllegalPluginAccessException(e.toString());
        }
    }

    // HandlerListを取るためのものです(´･ω･`)
    private static Method getHandlerList(Class<? extends Event> clazz) {
        try {

            return clazz.getDeclaredMethod("getHandlerList");

        } catch (NoSuchMethodException e) {

            var superclass = clazz.getSuperclass();
            if (superclass != null && !superclass.equals(Event.class) && Event.class.isAssignableFrom(superclass)) {
                return getHandlerList(superclass.asSubclass(Event.class));
            }

            throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName() + ". Static getHandlerList method required!");
        }
    }
}
