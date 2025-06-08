package com.github.postyizhan.managers

import com.github.postyizhan.PostDrop
import com.github.postyizhan.placeholder.PostDropExpansion
import com.github.postyizhan.util.MessageUtil
import org.bukkit.Bukkit

/**
 * PlaceholderAPI 挂钩管理器
 */
class PlaceholderManager(private val plugin: PostDrop) {

    private var placeholderAPIHooked = false
    private var expansion: PostDropExpansion? = null
    
    /**
     * 注册 PlaceholderAPI 扩展
     */
    fun register() {
        // 检查 PlaceholderAPI 是否存在
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                // 创建并注册扩展
                expansion = PostDropExpansion(plugin)
                expansion!!.register()
                
                placeholderAPIHooked = true
                plugin.server.consoleSender.sendMessage(MessageUtil.color(
                    MessageUtil.getMessage("system.hooks.enabled").replace("{0}", "PlaceholderAPI")
                ))
            } catch (e: Exception) {
                plugin.logger.warning("PlaceholderAPI hook failed: ${e.message}")
                if (plugin.configManager.isDebugEnabled()) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    /**
     * 注销 PlaceholderAPI 扩展
     */
    fun unregister() {
        if (placeholderAPIHooked && expansion != null) {
            try {
                expansion!!.unregister()
                placeholderAPIHooked = false
            } catch (e: Exception) {
                plugin.logger.warning("PlaceholderAPI hook failed: ${e.message}")
                if (plugin.configManager.isDebugEnabled()) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    /**
     * 检查 PlaceholderAPI 是否已挂钩
     */
    fun isHooked(): Boolean {
        return placeholderAPIHooked
    }
}
