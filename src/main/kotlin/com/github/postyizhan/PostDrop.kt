package com.github.postyizhan

import com.github.postyizhan.commands.MainCommand
import com.github.postyizhan.config.ConfigManager
import com.github.postyizhan.listeners.ItemDropListener
import com.github.postyizhan.listeners.ItemPickupListener
import com.github.postyizhan.listeners.PlayerJoinListener
import com.github.postyizhan.managers.LanguageManager
import com.github.postyizhan.managers.PlaceholderManager
import com.github.postyizhan.managers.ProtectionManager
import com.github.postyizhan.util.MessageUtil
import com.github.postyizhan.util.UpdateChecker
import com.github.postyizhan.visibility.ItemVisibilityHandler
import com.github.postyizhan.visibility.ProtocolLibHandler
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class PostDrop : JavaPlugin() {
    // 单例实例
    companion object {
        lateinit var instance: PostDrop
            private set
    }

    // 管理器
    lateinit var configManager: ConfigManager
    lateinit var languageManager: LanguageManager
    lateinit var protectionManager: ProtectionManager
    lateinit var visibilityHandler: ItemVisibilityHandler
    lateinit var placeholderManager: PlaceholderManager
    
    // 更新检查器
    private lateinit var updateChecker: UpdateChecker
    
    // ProtocolLib处理器
    private var protocolLibHandler: ProtocolLibHandler? = null
    private var protocolLibAvailable = false
    
    override fun onEnable() {
        // 设置实例
        instance = this
        
        // 初始化配置
        saveDefaultConfig()
        
        // 确保语言文件夹存在
        val langFolder = File(dataFolder, "lang")
        if (!langFolder.exists()) {
            langFolder.mkdirs()
        }
        
        // 保存默认语言文件
        saveResource("lang/zh_CN.yml", false)
        saveResource("lang/en_US.yml", false)
        
        // 初始化管理器
        configManager = ConfigManager(this)
        languageManager = LanguageManager(this)
        protectionManager = ProtectionManager(this)
        visibilityHandler = ItemVisibilityHandler(this)
        placeholderManager = PlaceholderManager(this)
        
        // 初始化消息工具
        MessageUtil.init(this)
        
        // 初始化更新检查器
        updateChecker = UpdateChecker(this, "postyizhan/PostDrop")
        
        // 检查并初始化ProtocolLib
        setupProtocolLib()
        
        // 注册 PlaceholderAPI 挂钩
        placeholderManager.register()
        
        // 注册命令
        getCommand("postdrop")?.setExecutor(MainCommand(this))
        
        // 注册监听器
        server.pluginManager.registerEvents(ItemDropListener(this), this)
        server.pluginManager.registerEvents(ItemPickupListener(this), this)
        server.pluginManager.registerEvents(PlayerJoinListener(this), this)
        
        // 检查更新
        if (configManager.isUpdateCheckerEnabled()) {
            updateChecker.checkForUpdates { isUpdateAvailable, newVersion ->
                if (isUpdateAvailable) {
                    server.consoleSender.sendMessage(MessageUtil.color(
                        MessageUtil.getMessage("system.updater.update_available")
                            .replace("{current_version}", description.version)
                            .replace("{latest_version}", newVersion)
                    ))
                    server.consoleSender.sendMessage(MessageUtil.color(
                        MessageUtil.getMessage("system.updater.update_url")
                            .replace("{current_version}", description.version)
                            .replace("{latest_version}", newVersion)
                    ))
                } else {
                    server.consoleSender.sendMessage(MessageUtil.color(
                        MessageUtil.getMessage("system.updater.up_to_date")
                    ))
                }
            }
        }
        
        // 调试日志
        if (configManager.isDebugEnabled()) {
            logger.info("PostDrop plugin started in debug mode")
        }
        
        // 发送启动消息
        server.consoleSender.sendMessage(MessageUtil.color(MessageUtil.getMessage("messages.enabled")))
    }

    /**
     * 设置ProtocolLib处理器
     */
    private fun setupProtocolLib() {
        if (server.pluginManager.getPlugin("ProtocolLib") != null) {
            try {
                protocolLibHandler = ProtocolLibHandler(this)
                protocolLibAvailable = true
                logger.info("ProtocolLib found and hooked successfully!")
            } catch (e: Exception) {
                logger.warning("Failed to hook into ProtocolLib: ${e.message}")
                if (configManager.isDebugEnabled()) {
                    e.printStackTrace()
                }
            }
        } else {
            logger.warning("ProtocolLib not found. Item visibility to other players cannot be controlled.")
            logger.warning("Install ProtocolLib for better item visibility control.")
        }
    }
    
    /**
     * 获取ProtocolLib处理器
     */
    fun getProtocolLibHandler(): ProtocolLibHandler? {
        return protocolLibHandler
    }
    
    /**
     * 检查ProtocolLib是否可用
     */
    fun isProtocolLibAvailable(): Boolean {
        return protocolLibAvailable
    }

    override fun onDisable() {
        // 注销 PlaceholderAPI 挂钩
        placeholderManager.unregister()
        
        // 关闭消息工具
        MessageUtil.shutdown()
        
        // 调试日志
        if (configManager.isDebugEnabled()) {
            logger.info("PostDrop plugin shutdown")
        }
        
        // 发送关闭消息
        server.consoleSender.sendMessage(MessageUtil.color(MessageUtil.getMessage("messages.disabled")))
    }
    
    // 重载插件方法
    fun reload(): Boolean {
        try {
            // 重载配置
            reloadConfig()
            configManager.reload()
            languageManager.reload()
            protectionManager.reload()
            
            // 重新初始化消息工具
            MessageUtil.init(this)
            
            // 重新注册 PlaceholderAPI 挂钩
            placeholderManager.unregister()
            placeholderManager.register()
            
            // 调试日志
            if (configManager.isDebugEnabled()) {
                logger.info("PostDrop plugin reloaded")
            }
            
            return true
        } catch (e: Exception) {
            logger.severe("Failed to reload plugin: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * 向玩家发送更新检查信息
     */
    fun sendUpdateInfo(player: Player) {
        updateChecker.checkForUpdates { isUpdateAvailable, newVersion ->
            if (isUpdateAvailable) {
                val updateAvailableMsg = MessageUtil.getMessage("system.updater.update_available")
                    .replace("{current_version}", description.version)
                    .replace("{latest_version}", newVersion)
                
                val updateUrlMsg = MessageUtil.getMessage("system.updater.update_url")
                    .replace("{current_version}", description.version)
                    .replace("{latest_version}", newVersion)
                
                MessageUtil.sendMessage(player, updateAvailableMsg)
                MessageUtil.sendMessage(player, updateUrlMsg)
            } else {
                val upToDateMsg = MessageUtil.getMessage("system.updater.up_to_date")
                MessageUtil.sendMessage(player, upToDateMsg)
            }
        }
    }
    
    /**
     * 获取更新检查器
     */
    fun getUpdateChecker(): UpdateChecker {
        return updateChecker
    }
}
