package com.github.postyizhan.listeners

import com.github.postyizhan.PostDrop
import com.github.postyizhan.util.MessageUtil
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

/**
 * 物品拾取监听器
 */
class ItemPickupListener(private val plugin: PostDrop) : Listener {
    // 用于记录已经提示过的玩家和物品，防止刷屏
    private val notifiedPickups = ConcurrentHashMap<UUID, Long>()
    private val NOTIFICATION_COOLDOWN = 2000L // 2秒冷却时间

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onItemPickup(event: EntityPickupItemEvent) {
        // 只处理玩家拾取物品的情况
        if (event.entity !is Player) {
            return
        }
        
        val player = event.entity as Player
        val item = event.item
        
        // 调试日志
        if (plugin.configManager.isDebugEnabled()) {
            plugin.logger.info("Player ${player.name} attempting to pickup item ${item.itemStack.type}")
        }
        
        // 检查物品是否受保护
        if (plugin.protectionManager.isItemProtected(item)) {
            val owner = plugin.protectionManager.getItemOwner(item)
            
            // 检查物品对该玩家是否可见
            // 如果物品设置为不可见且玩家不是所有者，直接取消事件且不发送任何消息
            if (!plugin.configManager.isVisibleToOthers() && player.uniqueId != owner) {
                event.isCancelled = true
                return
            }
            
            // 如果玩家不是所有者且物品可见，检查是否可以拾取
            if (!plugin.protectionManager.canPickupItem(player, item)) {
                // 取消拾取
                event.isCancelled = true
                
                // 检查是否应该发送消息（防止刷屏）
                val now = System.currentTimeMillis()
                val lastNotified = notifiedPickups.getOrDefault(player.uniqueId, 0L)
                if (now - lastNotified > NOTIFICATION_COOLDOWN) {
                    // 发送消息
                    MessageUtil.sendMessage(player, MessageUtil.getMessage("messages.item-drop.pickup-denied"))
                    // 更新通知时间
                    notifiedPickups[player.uniqueId] = now
                }
                
                if (plugin.configManager.isDebugEnabled()) {
                    plugin.logger.info("Pickup denied for ${player.name} - item is protected")
                }
            } else {
                // 玩家是物品所有者，可以拾取
                // 在拾取前确保物品发光效果不会保留到物品栏中
                if (plugin.configManager.isDebugEnabled()) {
                    plugin.logger.info("Owner ${player.name} picking up protected item - removing glow effects")
                }
                
                // 从团队中移除
                try {
                    val scoreboard = plugin.server.scoreboardManager?.mainScoreboard
                    val team = scoreboard?.getTeam("postdropGlow")
                    team?.removeEntry(item.uniqueId.toString())
                } catch (e: Exception) {
                    if (plugin.configManager.isDebugEnabled()) {
                        plugin.logger.info("Failed to remove item from team: ${e.message}")
                    }
                }
            }
        }
    }
}
