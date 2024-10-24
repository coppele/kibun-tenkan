//////////////////////////////////////////////////////////////////////////////
// 失敗作的なものです(´･ω･`)
//////////////////////////////////////////////////////////////////////////////

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.joml.Vector3f
import java.util.*
import com.comphenix.protocol.PacketType.Play.Server as ServerPacketType
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry as WrappedRegistry

/**
 * よくあるHUDは、アイテムを持ったエンティティを毎Tickプレイヤーの目の前にテレポートすることでそれっぽくしています。
 *
 * ですが、高速で移動した時やサーバーが重い時はエンティティが置いて行かれちゃいます。
 *
 * なので、プレイヤーの頭の上にItemDisplayを乗せてあげれば完璧では！と思って試したものの、
 * 目の位置よりも高い場所(頭上)にItemDisplayがいるので、頭を回転させるとちょっとずれちゃいます。
 * `transformation`では原点が変わらないので余計にずれてだめでした。
 *
 * あと、スニークすると反応がちょっと遅れちゃいます。
 *
 * 結局解決策としては使えません｡ﾟ(ﾟ´ω｀ﾟ)ﾟ｡
 *
 * `/hud`で手に入るアイテムは鉄塊のCMD10番を指定しているので、`iron_nugget.json`の`overrides`でモデルを指定してから、
 * そのモデルの`firstperson_righthand`をこんな感じに変更すれば、それっぽくなると思います。
 * ```
 * "display": {
 *     "firstperson_righthand": {
 *         "translation": [-1E-3, -5E-4, 0],
 *         "rotation": [0, -45, 25],
 *         "scale": [1E-4, 1E-4, 1E-4]
 *     }
 * }
 * ```
 *
 * @since Kotlin@2.0.20, Minecraft@1.20.4
 * @see <a href="https://i.gyazo.com/999bedb900d1a0bb8a39b52dc8b1e4f1.mp4">実際のやつです…</a>
 * @see <a href="https://wiki.vg/Protocol">参考①…</a>
 * @see <a href="https://qiita.com/Hirobao1/items/9cf8805c7a5d311ed0e7">参考②…</a>
 */
class Hud : JavaPlugin(), Listener {

    private val typeKey = NamespacedKey(this, "type") to PersistentDataType.STRING

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
        server.commandMap.register("hud", object : Command("hud") {
            override fun execute(sender: CommandSender, label: String, vararg args: String): Boolean {
                if (sender is Player) sender.inventory.addItem(ItemStack(Material.IRON_NUGGET).apply {
                    editMeta {
                        it.setCustomModelData(10)
                        it.persistentDataContainer.set(typeKey.first, typeKey.second, "(´･ω･`)")
                    }
                })
                return true
            }
        })
    }

    @EventHandler(ignoreCancelled = true)
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val entityId = -player.entityId
        spawn(player, entityId)

        val item = player.inventory.itemInMainHand
        val meta = item.itemMeta ?: return
        if (meta.persistentDataContainer.has(typeKey.first)) show(player, entityId, item)
    }

    @EventHandler(ignoreCancelled = true)
    fun onHeld(event: PlayerItemHeldEvent) {
        val player = event.player
        val entityId = -player.entityId
        val item = player.inventory.getItem(event.newSlot) ?: return hide(player, entityId)
        val meta = item.itemMeta ?: return hide(player, entityId)

        if (meta.persistentDataContainer.has(typeKey.first)) show(player, entityId, item)
        else hide(player, entityId)
    }

    private fun PacketType.send(player: Player, initializer: (PacketContainer) -> Unit) {
        ProtocolLibrary.getProtocolManager().let {
            it.sendServerPacket(player, it.createPacket(this).also(initializer))
        }
    }

    private inline fun <reified V> value(
        index: Int, value: V, serializer: Serializer = WrappedRegistry.get(V::class.java)
    ) = WrappedDataValue.fromWrappedValue(index, serializer, value)

    private fun spawn(player: Player, entityId: Int) {
        ServerPacketType.SPAWN_ENTITY.send(player) {
            val location = player.eyeLocation
            it.integers.write(0, entityId)
            it.uuiDs.write(0, UUID.randomUUID())
            it.entityTypeModifier.write(0, EntityType.ITEM_DISPLAY)
            it.doubles.write(0, location.x).write(1, location.y).write(2, location.z)
        }
        ServerPacketType.ENTITY_METADATA.send(player) {
            it.integers.write(0, entityId)
            it.dataValueCollectionModifier.write(0, listOf(
                value(11, Vector3f(0F, player.eyeHeight.toFloat() - player.height.toFloat(), 0F)),
                value(12, Vector3f(1E2F)),
                value(15, 3.toByte(), WrappedRegistry.get(java.lang.Byte::class.java)),
//                value(17, 3E-4F), // 描画範囲です。本来であれば三人称からは見えなくなります。
                value(24, 4.toByte(), WrappedRegistry.get(java.lang.Byte::class.java)),
            ))
        }
        ServerPacketType.MOUNT.send(player) {
            it.integers.write(0, player.entityId)
            it.integerArrays.write(0, intArrayOf(entityId))
        }
    }

    private fun show(player: Player, entityId: Int, item: ItemStack) {
        ServerPacketType.ENTITY_METADATA.send(player) {
            it.integers.write(0, entityId)
            it.dataValueCollectionModifier.write(0, listOf(value(23, item, WrappedRegistry.getItemStackSerializer(false))))
        }
    }

    private fun hide(player: Player, entityId: Int) = show(player, entityId, ItemStack.empty())
}
