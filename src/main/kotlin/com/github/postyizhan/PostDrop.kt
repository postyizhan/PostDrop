package com.github.postyizhan

import com.github.postyizhan.commands.MainCommand
import com.github.postyizhan.config.ConfigManager
import com.github.postyizhan.listeners.ItemDropListener
import com.github.postyizhan.listeners.ItemPickupListener
import com.github.postyizhan.managers.LanguageManager
import com.github.postyizhan.managers.ProtectionManager
import com.github.postyizhan.visibility.ItemVisibilityHandler
import com.github.postyizhan.visibility.ProtocolLibHandler
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
        
        // 检查并初始化ProtocolLib
        setupProtocolLib()
        
        // 注册命令
        getCommand("postdrop")?.setExecutor(MainCommand(this))
        
        // 注册监听器
        server.pluginManager.registerEvents(ItemDropListener(this), this)
        server.pluginManager.registerEvents(ItemPickupListener(this), this)
        
        // 调试日志
        if (configManager.isDebugEnabled()) {
            logger.info("PostDrop plugin started in debug mode")
        }
        
        // 发送启动消息
        logger.info(languageManager.getColoredMessage("system.startup.enabled"))
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
        // 调试日志
        if (configManager.isDebugEnabled()) {
            logger.info("PostDrop plugin shutdown")
        }
        
        // 发送关闭消息
        logger.info(languageManager.getColoredMessage("messages.disabled"))
    }
    
    // 重载插件方法
    fun reload(): Boolean {
        try {
            // 重载配置
            reloadConfig()
            configManager.reload()
            languageManager.reload()
            protectionManager.reload()
            
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
}
