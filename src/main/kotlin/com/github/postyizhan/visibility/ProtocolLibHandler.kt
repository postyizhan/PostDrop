package com.github.postyizhan.visibility

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.github.postyizhan.PostDrop
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 使用ProtocolLib处理物品可见性
 */
class ProtocolLibHandler(private val plugin: PostDrop) {
    private val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()
    private val protectedItems = ConcurrentHashMap<Int, UUID>() // 物品实体ID -> 所有者UUID
    
    init {
        registerPacketListener()
        
        // 注册任务清理不存在的物品
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            cleanupItems()
        }, 200L, 200L) // 每10秒执行一次
        
        if (isDebugEnabled()) {
            plugin.logger.info("ProtocolLib handler initialized")
        }
    }
    
    /**
     * 获取调试模式设置
     */
    private fun isDebugEnabled(): Boolean {
        return plugin.configManager.isDebugEnabled()
    }
    
    /**
     * 获取可见性设置
     */
    private fun isVisibleToOthers(): Boolean {
        return plugin.configManager.isVisibleToOthers()
    }
    
    /**
     * 注册数据包监听器
     */
    private fun registerPacketListener() {
        // 监听实体生成数据包
        protocolManager.addPacketListener(object : PacketAdapter(plugin, PacketType.Play.Server.SPAWN_ENTITY) {
            override fun onPacketSending(event: PacketEvent) {
                try {
                    // 检查物品可见性配置
                    if (isVisibleToOthers()) {
                        return
                    }
                    
                    val packet = event.packet
                    val entityID = packet.integers.read(0)
                    
                    // 检查是否是受保护的物品
                    val ownerUUID = protectedItems[entityID] ?: return
                    
                    // 不向非所有者发送生成数据包
                    if (event.player.uniqueId != ownerUUID) {
                        event.isCancelled = true
                        
                        if (isDebugEnabled()) {
                            plugin.logger.info("Blocked spawn entity packet for protected item to non-owner ${event.player.name}")
                        }
                    }
                } catch (e: Exception) {
                    if (isDebugEnabled()) {
                        plugin.logger.warning("Error in spawn entity packet listener: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
        })
        
        // 监听实体元数据更新数据包
        protocolManager.addPacketListener(object : PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA) {
            override fun onPacketSending(event: PacketEvent) {
                try {
                    // 检查物品可见性配置
                    if (isVisibleToOthers()) {
                        return
                    }
                    
                    val packet = event.packet
                    val entityID = packet.integers.read(0)
                    
                    // 检查是否是受保护的物品
                    val ownerUUID = protectedItems[entityID] ?: return
                    
                    // 不向非所有者发送元数据更新数据包
                    if (event.player.uniqueId != ownerUUID) {
                        event.isCancelled = true
                        
                        if (isDebugEnabled()) {
                            plugin.logger.info("Blocked metadata packet for protected item to non-owner ${event.player.name}")
                        }
                    }
                } catch (e: Exception) {
                    if (isDebugEnabled()) {
                        plugin.logger.warning("Error in metadata packet listener: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
        })
    }
    
    /**
     * 注册受保护的物品
     */
    fun registerProtectedItem(item: Item, ownerUUID: UUID) {
        protectedItems[item.entityId] = ownerUUID
        
        // 如果配置为不对其他玩家可见，则需要对所有在线玩家隐藏此物品
        if (!isVisibleToOthers()) {
            hideItemFromNonOwners(item, ownerUUID)
        }
        
        if (isDebugEnabled()) {
            plugin.logger.info("Registered protected item ${item.entityId} owned by ${ownerUUID}")
        }
    }
    
    /**
     * 从非所有者玩家处隐藏物品
     */
    private fun hideItemFromNonOwners(item: Item, ownerUUID: UUID) {
        try {
            // 获取销毁实体数据包
            val destroyPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY)
            destroyPacket.integerArrays.write(0, intArrayOf(item.entityId))
            
            // 向非所有者发送销毁数据包
            for (player in Bukkit.getOnlinePlayers()) {
                if (player.uniqueId != ownerUUID) {
                    protocolManager.sendServerPacket(player, destroyPacket)
                    
                    if (isDebugEnabled()) {
                        plugin.logger.info("Sent destroy packet for item ${item.entityId} to non-owner ${player.name}")
                    }
                }
            }
        } catch (e: Exception) {
            if (isDebugEnabled()) {
                plugin.logger.warning("Error hiding item from non-owners: ${e.message}")
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
            
            if (isDebugEnabled()) {
                plugin.logger.info("Removed non-existent protected item: $entityId")
            }
        }
    }
}
