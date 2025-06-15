package com.github.postyizhan.visibility

import com.github.postyizhan.PostDrop
import org.bukkit.Bukkit
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 处理物品可见性的类
 * 注：当ProtocolLib或PacketEvents可用时，会使用相应的处理器
 * 此类作为默认的后备机制
 */
class ItemVisibilityHandler(private val plugin: PostDrop) : Listener {
    
    // 存储受保护物品的映射
    private val protectedItems = ConcurrentHashMap<UUID, UUID>() // 物品UUID -> 所有者UUID
    
    init {
        // 注册事件
        plugin.server.pluginManager.registerEvents(this, plugin)
        
        // 启动可见性检查任务
        startVisibilityTask()
    }
    
    /**
     * 注册受保护的物品
     * 注：当ProtocolLib或PacketEvents可用时，不会执行此类的处理逻辑
     */
    fun registerProtectedItem(item: Item, ownerUUID: UUID) {
        // 如果ProtocolLib或PacketEvents可用，则不执行此处理
        if (plugin.isProtocolLibAvailable() || plugin.isPacketEventsAvailable()) {
            return
        }
        
        // 存储物品信息
        protectedItems[item.uniqueId] = ownerUUID
        
        // 如果配置为不对其他玩家可见，则隐藏物品
        if (!plugin.configManager.isVisibleToOthers()) {
            hideItemFromNonOwners(item, ownerUUID)
        }
        
        if (plugin.configManager.isDebugEnabled()) {
            plugin.logger.info("Registered protected item in default handler: ${item.uniqueId}, owner: ${ownerUUID}")
        }
    }
    
    /**
     * 移除受保护的物品
     */
    fun unregisterProtectedItem(item: Item) {
        protectedItems.remove(item.uniqueId)
        
        if (plugin.configManager.isDebugEnabled()) {
            plugin.logger.info("Unregistered protected item: ${item.uniqueId}")
        }
    }
    
    /**
     * 向非所有者隐藏物品
     * 注：这是一个基本实现，对于没有ProtocolLib或PacketEvents的服务器，效果有限
     */
    private fun hideItemFromNonOwners(item: Item, ownerUUID: UUID) {
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.uniqueId != ownerUUID) {
                // 在1.13版本，如果没有ProtocolLib或PacketEvents，很难有效隐藏物品
                // 只能做一些基本的处理
                if (plugin.configManager.isDebugEnabled()) {
                    plugin.logger.info("Cannot fully hide item ${item.uniqueId} - ProtocolLib or PacketEvents needed")
                }
            }
        }
    }
    
    /**
     * 玩家加入服务器时，隐藏受保护的物品
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        // 如果ProtocolLib或PacketEvents可用，则不执行此处理
        if (plugin.isProtocolLibAvailable() || plugin.isPacketEventsAvailable()) {
            return
        }
        
        if (!plugin.configManager.isVisibleToOthers()) {
            val player = event.player
            
            // 延迟执行，确保玩家完全加载
            object : BukkitRunnable() {
                override fun run() {
                    for ((itemUUID, ownerUUID) in protectedItems) {
                        if (player.uniqueId != ownerUUID) {
                            // 查找物品
                            for (entity in player.world.entities) {
                                if (entity is Item && entity.uniqueId == itemUUID) {
                                    // 隐藏物品（实际效果有限）
                                    if (plugin.configManager.isDebugEnabled()) {
                                        plugin.logger.info("Cannot fully hide item ${itemUUID} - ProtocolLib or PacketEvents needed")
                                    }
                                    break
                                }
                            }
                        }
                    }
                }
            }.runTaskLater(plugin, 20L)
        }
    }
    
    /**
     * 启动可见性检查任务
     */
    private fun startVisibilityTask() {
        object : BukkitRunnable() {
            override fun run() {
                // 如果ProtocolLib或PacketEvents可用，则不执行清理
                if (plugin.isProtocolLibAvailable() || plugin.isPacketEventsAvailable()) {
                    return
                }
                
                // 清理不存在的物品
                val toRemove = ArrayList<UUID>()
                
                for (itemUUID in protectedItems.keys) {
                    var found = false
                    
                    // 检查物品是否还存在
                    for (world in Bukkit.getWorlds()) {
                        for (entity in world.entities) {
                            if (entity is Item && entity.uniqueId == itemUUID) {
                                found = true
                                break
                            }
                        }
                        if (found) break
                    }
                    
                    if (!found) {
                        toRemove.add(itemUUID)
                    }
                }
                
                // 移除不存在的物品
                for (itemUUID in toRemove) {
                    protectedItems.remove(itemUUID)
                    
                    if (plugin.configManager.isDebugEnabled()) {
                        plugin.logger.info("Removed non-existent protected item: $itemUUID")
                    }
                }
            }
        }.runTaskTimer(plugin, 100L, 100L) // 5秒检查一次
    }
}
