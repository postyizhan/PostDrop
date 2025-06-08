package com.github.postyizhan.config

import com.github.postyizhan.PostDrop
import org.bukkit.ChatColor
import org.bukkit.configuration.file.FileConfiguration

/**
 * 配置管理器类，负责处理插件配置
 */
class ConfigManager(private val plugin: PostDrop) {
    private var config: FileConfiguration = plugin.config
    
    // 语言设置
    private var language: String = "zh_CN"
    
    // 更新检查设置
    private var updateCheckerEnabled: Boolean = true
    private var updateCheckerInterval: Int = 1
    
    // 保护设置
    private var defaultProtectionEnabled: Boolean = true
    private var glowEnabled: Boolean = true
    private var glowColor: String = "WHITE"
    private var visibleToOthers: Boolean = false
    
    // 通知设置
    private var notifyOnDrop: Boolean = true
    
    // 调试模式
    private var debug: Boolean = false
    
    init {
        reload()
    }
    
    /**
     * 重载配置
     */
    fun reload() {
        plugin.reloadConfig()
        config = plugin.config
        
        // 加载配置
        language = config.getString("language", "zh_CN")!!
        
        // 加载更新检查设置
        updateCheckerEnabled = config.getBoolean("update-checker.enabled", true)
        updateCheckerInterval = config.getInt("update-checker.check-interval-days", 1)
        
        defaultProtectionEnabled = config.getBoolean("protection.default-enabled", true)
        glowEnabled = config.getBoolean("protection.glow.enabled", true)
        glowColor = config.getString("protection.glow.color", "WHITE")!!
        visibleToOthers = config.getBoolean("protection.visibility.visible-to-others", false)
        
        notifyOnDrop = config.getBoolean("notifications.notify-on-drop", true)
        
        debug = config.getBoolean("debug", false)
        
        if (debug) {
            plugin.logger.info("Configuration loaded")
        }
    }
    
    // Getter方法
    fun getLanguage(): String = language
    
    fun isUpdateCheckerEnabled(): Boolean = updateCheckerEnabled
    
    fun getUpdateCheckerInterval(): Int = updateCheckerInterval
    
    fun isDefaultProtectionEnabled(): Boolean = defaultProtectionEnabled
    
    fun isGlowEnabled(): Boolean = glowEnabled
    
    fun getGlowColor(): String = glowColor
    
    fun isVisibleToOthers(): Boolean = visibleToOthers
    
    fun isNotifyOnDrop(): Boolean = notifyOnDrop
    
    fun isDebugEnabled(): Boolean = debug
}
