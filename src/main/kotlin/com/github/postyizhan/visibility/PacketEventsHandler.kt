package com.github.postyizhan.visibility

import com.github.postyizhan.PostDrop
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import org.bukkit.Bukkit
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 使用PacketEvents处理物品可见性
 * 注意：本类实现简化了，只提供基本注册功能
 * 具体的数据包拦截在运行时由PacketEvents处理
 */
class PacketEventsHandler(private val plugin: PostDrop) {
    // 存储受保护物品的映射
    private val protectedItems = ConcurrentHashMap<Int, UUID>() // 物品实体ID -> 所有者UUID
    
    // 监听器实例
    private var packetListener: PacketListenerAbstract? = null
    
    init {
        // 确保PacketEvents已初始化
        if (!PacketEvents.getAPI().isInitialized) {
            plugin.logger.warning("PacketEvents API not initialized. Attempting to initialize...")
            try {
                // 尝试初始化PacketEvents
                PacketEvents.getAPI().settings
                    .checkForUpdates(false)
                    .bStats(false)
                
                PacketEvents.getAPI().init()
                plugin.logger.info("Successfully initialized PacketEvents API")
            } catch (e: Exception) {
                plugin.logger.severe("Failed to initialize PacketEvents: ${e.message}")
                if (plugin.configManager.isDebugEnabled()) {
                    e.printStackTrace()
                }
                // 不能在init块中使用return，所以我们不做任何操作，继续执行
            }
        }
        
        // 注册数据包监听器
        registerPacketListener()
        
        // 注册清理任务
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            cleanupItems()
        }, 200L, 200L) // 每10秒执行一次
        
        if (isDebugEnabled()) {
            plugin.logger.info("PacketEvents handler initialized")
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
        try {
            // 如果已经有监听器，先注销
            if (packetListener != null) {
                PacketEvents.getAPI().eventManager.unregisterListener(packetListener!!)
                packetListener = null
                
                if (isDebugEnabled()) {
                    plugin.logger.info("Unregistered previous PacketEvents listener")
                }
            }
            
            // 创建并注册新监听器
            packetListener = object : PacketListenerAbstract(PacketListenerPriority.NORMAL) {
                override fun onPacketSend(event: PacketSendEvent) {
                    // 如果设置为对其他玩家可见，则不拦截任何数据包
                    if (isVisibleToOthers()) {
                        return
                    }
                    
                    try {
                        // 检查数据包类型，只处理游戏状态下的实体相关数据包
                        val packetType = event.packetType
                        
                        // 快速过滤：只关心实体生成和元数据数据包
                        if (packetType != PacketType.Play.Server.SPAWN_ENTITY && 
                            packetType != PacketType.Play.Server.ENTITY_METADATA) {
                            return
                        }
                        
                        // 检查是否是状态数据包
                        val packetName = packetType.name
                        if (packetName.contains("STATUS") || packetName.contains("Status")) {
                            return
                        }
                        
                        // 获取用户，安全检查
                        val user = try {
                            event.getUser()
                        } catch (e: Exception) {
                            if (isDebugEnabled()) {
                                plugin.logger.warning("Failed to get user from packet: ${e.message}")
                            }
                            return
                        }
                        
                        // 检查UUID是否有效
                        val uuid = try {
                            user.getUUID()
                        } catch (e: Exception) {
                            if (isDebugEnabled()) {
                                plugin.logger.warning("Failed to get UUID from user: ${e.message}")
                            }
                            return
                        }
                        
                        // UUID必须有效
                        if (uuid == null) {
                            return
                        }
                        
                        // 特殊检查：如果是服务器状态请求，UUID可能是有效的，但不是真实玩家
                        // 检查UUID是否为服务器状态请求的特殊UUID
                        if (uuid.toString() == "00000000-0000-0000-0000-000000000000") {
                            return
                        }
                        
                        // 获取玩家实例
                        val bukkitPlayer = try {
                            Bukkit.getPlayer(uuid)
                        } catch (e: Exception) {
                            if (isDebugEnabled()) {
                                plugin.logger.warning("Failed to get player for UUID $uuid: ${e.message}")
                            }
                            return
                        }
                        
                        // 玩家必须在线
                        if (bukkitPlayer == null || !bukkitPlayer.isOnline) {
                            return
                        }
                        
                        // 处理数据包
                        when (packetType) {
                            PacketType.Play.Server.SPAWN_ENTITY -> {
                                // 处理生成实体数据包
                                try {
                                    val wrapper = WrapperPlayServerSpawnEntity(event)
                                    val entityId = wrapper.getEntityId()
                                    
                                    // 只对物品实体进行处理
                                    val entityType = wrapper.getEntityType()
                                    if (entityType == EntityTypes.ITEM) {
                                        // 检查是否是受保护的物品
                                        val ownerUUID = protectedItems[entityId]
                                        if (ownerUUID != null && bukkitPlayer.uniqueId != ownerUUID) {
                                            // 如果不是物品所有者，取消数据包发送
                                            event.isCancelled = true
                                            
                                            if (isDebugEnabled()) {
                                                plugin.logger.info("Blocked spawn entity packet for protected item $entityId to non-owner ${bukkitPlayer.name}")
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    if (isDebugEnabled()) {
                                        plugin.logger.warning("Error processing spawn entity packet: ${e.message}")
                                    }
                                }
                            }
                            PacketType.Play.Server.ENTITY_METADATA -> {
                                // 处理实体元数据数据包
                                try {
                                    val wrapper = WrapperPlayServerEntityMetadata(event)
                                    val entityId = wrapper.getEntityId()
                                    
                                    // 检查是否是受保护的物品
                                    val ownerUUID = protectedItems[entityId]
                                    if (ownerUUID != null && bukkitPlayer.uniqueId != ownerUUID) {
                                        // 如果不是物品所有者，取消数据包发送
                                        event.isCancelled = true
                                        
                                        if (isDebugEnabled()) {
                                            plugin.logger.info("Blocked metadata packet for protected item $entityId to non-owner ${bukkitPlayer.name}")
                                        }
                                    }
                                } catch (e: Exception) {
                                    if (isDebugEnabled()) {
                                        plugin.logger.warning("Error processing metadata packet: ${e.message}")
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        if (isDebugEnabled()) {
                            plugin.logger.warning("Error in packet listener: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                }
            }
            
            // 注册监听器
            PacketEvents.getAPI().eventManager.registerListener(packetListener!!)
            
            if (isDebugEnabled()) {
                plugin.logger.info("PacketEvents listener registered successfully")
            }
        } catch (e: Exception) {
            plugin.logger.warning("Failed to register PacketEvents listener: ${e.message}")
            if (isDebugEnabled()) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 注册受保护的物品
     */
    fun registerProtectedItem(item: Item, ownerUUID: UUID) {
        protectedItems[item.entityId] = ownerUUID
        
        if (isDebugEnabled()) {
            plugin.logger.info("Registered protected item ${item.entityId} owned by ${ownerUUID}")
            
            // 如果设置为不对其他玩家可见，记录额外调试信息
            if (!isVisibleToOthers()) {
                plugin.logger.info("Item ${item.entityId} will be hidden from non-owners")
            }
        }
    }
    
    /**
     * 判断是否可以拾取物品
     */
    fun canPickupItem(player: Player, entityId: Int): Boolean {
        val ownerUUID = protectedItems[entityId] ?: return true // 未注册的物品任何人可拾取
        return player.uniqueId == ownerUUID // 只有所有者可拾取
    }
    
    /**
     * 清理不存在的物品
     */
    private fun cleanupItems() {
        val toRemove = ArrayList<Int>()
        
        for (entityId in protectedItems.keys) {
            var found = false
            
            // 检查物品是否还存在
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
    
    /**
     * 重新初始化处理器
     * 在配置更改后调用
     */
    fun reinitialize() {
        // 重新注册监听器
        registerPacketListener()
        
        if (isDebugEnabled()) {
            plugin.logger.info("PacketEvents handler reinitialized")
            plugin.logger.info("Visibility setting: visible-to-others=${isVisibleToOthers()}")
        }
    }
}
