package com.github.postyizhan.listeners

import com.github.postyizhan.PostDrop
import com.github.postyizhan.util.MessageUtil
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent

/**
 * 物品丢弃监听器
 */
class ItemDropListener(private val plugin: PostDrop) : Listener {
    
    @EventHandler(priority = EventPriority.NORMAL)
    fun onItemDrop(event: PlayerDropItemEvent) {
        val player = event.player
        val item = event.itemDrop
        
        // 调试日志
        if (plugin.configManager.isDebugEnabled()) {
            plugin.logger.info("Player ${player.name} dropped item ${item.itemStack.type}")
        }
        
        // 检查玩家是否启用了保护
        if (plugin.protectionManager.isProtectionEnabled(player)) {
            // 标记物品为受保护状态
            plugin.protectionManager.markItemAsProtected(item, player)
            
            // 如果配置了通知，发送消息给玩家
            if (plugin.configManager.isNotifyOnDrop()) {
                MessageUtil.sendMessage(player, MessageUtil.getMessage("messages.item-drop.item-protected"))
            }
            
            if (plugin.configManager.isDebugEnabled()) {
                plugin.logger.info("Item protection applied for ${player.name}'s dropped item")
            }
        } else {
            if (plugin.configManager.isDebugEnabled()) {
                plugin.logger.info("Item protection not applied for ${player.name}'s dropped item (protection disabled)")
            }
        }
    }
}
