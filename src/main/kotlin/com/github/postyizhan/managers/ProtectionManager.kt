package com.github.postyizhan.managers

import com.github.postyizhan.PostDrop
import org.bukkit.ChatColor
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scoreboard.Team
import java.util.*

/**
 * 保护管理器类，负责处理物品保护状态
 */
class ProtectionManager(private val plugin: PostDrop) {
    // 存储已明确关闭保护的玩家UUID
    private val protectionDisabledPlayers = HashSet<UUID>()
    
    // 元数据和持久化数据键
    private val PROTECTION_METADATA_KEY = "postdrop_protected"
    private val OWNER_METADATA_KEY = "postdrop_owner"
    
    // 用于保存物品的颜色团队
    private var glowTeam: Team? = null
    
    init {
        reload()
        setupGlowTeam()
    }
    
    /**
     * 设置发光团队
     */
    private fun setupGlowTeam() {
        try {
            // 获取记分板
            val scoreboard = plugin.server.scoreboardManager?.mainScoreboard
            
            // 删除旧团队（如果存在）
            scoreboard?.getTeam("postdropGlow")?.unregister()
            
            // 创建新团队
            glowTeam = scoreboard?.registerNewTeam("postdropGlow")
            
            // 设置团队颜色（从配置获取）
            val colorName = plugin.configManager.getGlowColor()
            try {
                val chatColor = ChatColor.valueOf(colorName)
                glowTeam?.color = chatColor
                
                if (plugin.configManager.isDebugEnabled()) {
                    plugin.logger.info("Glow team color set to $colorName")
                }
            } catch (e: Exception) {
                plugin.logger.warning("Invalid glow color in config: $colorName, using WHITE")
                glowTeam?.color = ChatColor.WHITE
            }
        } catch (e: Exception) {
            plugin.logger.warning("Failed to setup glow team: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * 重载保护管理器
     */
    fun reload() {
        // 清空保护列表
        protectionDisabledPlayers.clear()
        
        // 重新设置发光团队
        setupGlowTeam()
        
        if (plugin.configManager.isDebugEnabled()) {
            plugin.logger.info("Protection manager reloaded")
        }
    }
    
    /**
     * 重新初始化保护管理器
     * 在配置更改后调用，确保所有组件都使用最新配置
     */
    fun reinitialize() {
        // 重载
        reload()
        
        // 重新初始化可见性处理器
        if (plugin.isPacketEventsAvailable()) {
            plugin.getPacketEventsHandler()?.reinitialize()
            
            // 记录日志
            if (plugin.configManager.isDebugEnabled()) {
                val visibleToOthers = if (plugin.configManager.isVisibleToOthers()) "enabled" else "disabled"
                plugin.logger.info("Reinitializing PacketEvents handler with visible-to-others: $visibleToOthers")
            }
        }
        
        if (plugin.configManager.isDebugEnabled()) {
            plugin.logger.info("Protection manager reinitialized with new configuration")
        }
    }
    
    /**
     * 设置玩家的保护状态
     * @param player 玩家
     * @param enabled 是否启用保护
     */
    fun setProtectionEnabled(player: Player, enabled: Boolean) {
        if (enabled) {
            // 如果启用保护，从禁用列表中移除
            protectionDisabledPlayers.remove(player.uniqueId)
            if (plugin.configManager.isDebugEnabled()) {
                plugin.logger.info("Enabled protection for player ${player.name}")
            }
        } else {
            // 如果禁用保护，添加到禁用列表
            protectionDisabledPlayers.add(player.uniqueId)
            if (plugin.configManager.isDebugEnabled()) {
                plugin.logger.info("Disabled protection for player ${player.name}")
            }
        }
    }
    
    /**
     * 检查玩家是否启用了保护
     * @return true如果玩家应该保护丢弃的物品，false如果不应该
     */
    fun isProtectionEnabled(player: Player): Boolean {
        // 如果玩家特别设置了关闭保护（在禁用列表中），返回false
        if (protectionDisabledPlayers.contains(player.uniqueId)) {
            return false
        }
        // 否则使用默认设置
        return plugin.configManager.isDefaultProtectionEnabled()
    }
    
    /**
     * 选择合适的可见性处理器
     * 优先级：PacketEvents > ProtocolLib > 默认处理器
     */
    private fun handleItemVisibility(item: Item, player: Player) {
        // 尝试使用PacketEvents
        if (plugin.isPacketEventsAvailable()) {
            plugin.getPacketEventsHandler()?.registerProtectedItem(item, player.uniqueId)
            if (plugin.configManager.isDebugEnabled()) {
                plugin.logger.info("Using PacketEvents for item visibility")
            }
        }
        // 如果PacketEvents不可用，尝试使用ProtocolLib
        else if (plugin.isProtocolLibAvailable()) {
            plugin.getProtocolLibHandler()?.registerProtectedItem(item, player.uniqueId)
            if (plugin.configManager.isDebugEnabled()) {
                plugin.logger.info("Using ProtocolLib for item visibility")
            }
        }
        // 如果两者都不可用，使用默认处理器
        else {
            plugin.visibilityHandler.registerProtectedItem(item, player.uniqueId)
            if (plugin.configManager.isDebugEnabled()) {
                plugin.logger.info("Using default handler for item visibility")
            }
        }
    }
    
    /**
     * 标记物品为受保护状态
     */
    fun markItemAsProtected(item: Item, player: Player) {
        // 设置元数据
        item.setMetadata(PROTECTION_METADATA_KEY, FixedMetadataValue(plugin, true))
        item.setMetadata(OWNER_METADATA_KEY, FixedMetadataValue(plugin, player.uniqueId.toString()))
        
        // 设置物品描边（如果启用）
        if (plugin.configManager.isGlowEnabled()) {
            applyGlowEffect(item)
        }
        
        // 处理物品可见性
        handleItemVisibility(item, player)
        
        if (plugin.configManager.isDebugEnabled()) {
            plugin.logger.info("Item marked as protected for player ${player.name}")
        }
    }
    
    /**
     * 检查物品是否受保护
     */
    fun isItemProtected(item: Item): Boolean {
        return item.hasMetadata(PROTECTION_METADATA_KEY)
    }
    
    /**
     * 获取物品所有者
     */
    fun getItemOwner(item: Item): UUID? {
        if (!item.hasMetadata(OWNER_METADATA_KEY)) {
            return null
        }
        
        val ownerString = item.getMetadata(OWNER_METADATA_KEY).firstOrNull()?.asString() ?: return null
        return try {
            UUID.fromString(ownerString)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
    
    /**
     * 应用发光效果到物品
     */
    private fun applyGlowEffect(item: Item) {
        try {
            // 直接使用实体的发光效果
            item.isGlowing = true
            
            // 将物品添加到团队以应用颜色
            glowTeam?.addEntry(item.uniqueId.toString())
            
            if (plugin.configManager.isDebugEnabled()) {
                plugin.logger.info("Applied glow effect to item using entity glowing property")
            }
        } catch (e: Exception) {
            plugin.logger.warning("Failed to apply glow effect: ${e.message}")
            if (plugin.configManager.isDebugEnabled()) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 检查玩家是否可以拾取物品
     */
    fun canPickupItem(player: Player, item: Item): Boolean {
        // 如果物品不受保护，任何人都可以拾取
        if (!isItemProtected(item)) {
            return true
        }
        
        // 获取物品所有者
        val owner = getItemOwner(item)
        
        // 如果所有者是当前玩家，允许拾取
        if (owner == player.uniqueId) {
            return true
        }
        
        // 其他玩家不能拾取受保护的物品
        return false
    }
}
