package com.github.postyizhan.visibility

import com.github.postyizhan.PostDrop
import org.bukkit.Bukkit
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 使用PacketEvents处理物品可见性
 * 这是一个简化版本，使用反射获取PacketEvents实例
 * 避免直接导入可能不存在的类造成编译错误
 */
class PacketEventsHandler(private val plugin: PostDrop) {
    // 存储受保护物品的映射
    private val protectedItems = ConcurrentHashMap<Int, UUID>() // 物品实体ID -> 所有者UUID
    
    // PacketEvents API对象 (通过反射获取，避免编译时依赖)
    private var packetEventsAPI: Any? = null
    
    init {
        try {
            // 尝试通过反射获取PacketEvents API实例
            val packetEventsClass = Class.forName("io.github.retrooper.packetevents.PacketEvents")
            val getAPIMethod = packetEventsClass.getMethod("getAPI")
            packetEventsAPI = getAPIMethod.invoke(null)
            
            if (plugin.configManager.isDebugEnabled()) {
                plugin.logger.info("PacketEvents handler initialized successfully")
            }
            
            // 注册任务清理不存在的物品
            Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
                cleanupItems()
            }, 200L, 200L) // 每10秒执行一次
        } catch (e: Exception) {
            plugin.logger.warning("Failed to initialize PacketEvents handler: ${e.message}")
            if (plugin.configManager.isDebugEnabled()) {
                e.printStackTrace()
            }
            packetEventsAPI = null
        }
    }
    
    /**
     * 注册受保护的物品
     */
    fun registerProtectedItem(item: Item, ownerUUID: UUID) {
        protectedItems[item.entityId] = ownerUUID
        
        // 如果配置为不对其他玩家可见，则隐藏物品
        if (!plugin.configManager.isVisibleToOthers()) {
            hideItemFromNonOwners(item, ownerUUID)
        }
        
        if (plugin.configManager.isDebugEnabled()) {
            plugin.logger.info("(PacketEvents) Registered protected item ${item.entityId} owned by ${ownerUUID}")
        }
    }
    
    /**
     * 从非所有者玩家处隐藏物品
     */
    private fun hideItemFromNonOwners(item: Item, ownerUUID: UUID) {
        if (packetEventsAPI == null) {
            if (plugin.configManager.isDebugEnabled()) {
                plugin.logger.warning("Cannot hide item: PacketEvents API not available")
            }
            return
        }
        
        try {
            for (player in Bukkit.getOnlinePlayers()) {
                if (player.uniqueId != ownerUUID) {
                    // 使用反射调用PacketEvents API发送销毁实体数据包
                    sendDestroyEntityPacket(player, item.entityId)
                    
                    if (plugin.configManager.isDebugEnabled()) {
                        plugin.logger.info("(PacketEvents) Sent destroy packet for item ${item.entityId} to non-owner ${player.name}")
                    }
                }
            }
        } catch (e: Exception) {
            plugin.logger.warning("Error hiding item using PacketEvents: ${e.message}")
            if (plugin.configManager.isDebugEnabled()) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 使用反射发送销毁实体数据包
     */
    private fun sendDestroyEntityPacket(player: Player, entityId: Int) {
        try {
            // 获取PlayerManager
            val playerManagerMethod = packetEventsAPI!!.javaClass.getMethod("getPlayerManager")
            val playerManager = playerManagerMethod.invoke(packetEventsAPI)
            
            // 调用sendDestroyEntities方法
            val sendDestroyEntitiesMethod = playerManager.javaClass.getMethod("sendDestroyEntities", Player::class.java, IntArray::class.java)
            sendDestroyEntitiesMethod.invoke(playerManager, player, intArrayOf(entityId))
        } catch (e: Exception) {
            if (plugin.configManager.isDebugEnabled()) {
                plugin.logger.warning("Failed to send destroy packet: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 清理不存在的物品
     */
    private fun cleanupItems() {
        val toRemove = ArrayList<Int>()
        
        for (entityId in protectedItems.keys) {
            var found = false
            
            // 查找物品是否还存在
            for (world in Bukkit.getWorlds()) {
                for (entity in world.entities) {
                    if (entity.entityId == entityId) {
                        found = true
                        break
                    }
                }
                if (found) break
            }
            
            if (!found) {
                toRemove.add(entityId)
            }
        }
        
        // 移除不存在的物品
        for (entityId in toRemove) {
            protectedItems.remove(entityId)
            
            if (plugin.configManager.isDebugEnabled()) {
                plugin.logger.info("(PacketEvents) Removed non-existent protected item: $entityId")
            }
        }
    }
}
