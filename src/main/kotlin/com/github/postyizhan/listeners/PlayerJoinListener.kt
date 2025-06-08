package com.github.postyizhan.listeners

import com.github.postyizhan.PostDrop
import com.github.postyizhan.util.MessageUtil
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * 玩家加入事件监听器
 */
class PlayerJoinListener(private val plugin: PostDrop) : Listener {

    /**
     * 处理玩家加入事件
     */
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        
        // 如果玩家是OP且配置中启用了更新检查，发送更新检查信息
        if (player.isOp && plugin.configManager.isUpdateCheckerEnabled()) {
            // 在玩家加入后2秒发送更新信息，避免消息过多
            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                if (player.isOnline) {
                    plugin.sendUpdateInfo(player)
                }
            }, 40L)  // 40 ticks = 2 seconds
        }
    }
}
 