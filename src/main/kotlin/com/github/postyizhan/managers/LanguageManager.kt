package com.github.postyizhan.managers

import com.github.postyizhan.PostDrop
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * 语言管理器类，负责处理插件多语言支持
 */
class LanguageManager(private val plugin: PostDrop) {
    private lateinit var langConfig: YamlConfiguration
    private var prefix: String = ""
    
    init {
        reload()
    }
    
    /**
     * 重载语言配置
     */
    fun reload() {
        val language = plugin.configManager.getLanguage()
        val langFile = File(plugin.dataFolder, "lang/$language.yml")
        
        if (!langFile.exists()) {
            plugin.logger.warning("Language file $language.yml not found, using default language")
            // 尝试使用英文
            val enFile = File(plugin.dataFolder, "lang/en_US.yml")
            if (enFile.exists()) {
                langConfig = YamlConfiguration.loadConfiguration(enFile)
            } else {
                // 如果英文也不存在，创建一个空配置
                langConfig = YamlConfiguration()
            }
        } else {
            langConfig = YamlConfiguration.loadConfiguration(langFile)
        }
        
        // 读取前缀
        prefix = langConfig.getString("prefix", "&8[&3Post&bDrop&8] ")
        
        if (plugin.configManager.isDebugEnabled()) {
            plugin.logger.info("Language loaded: $language")
        }
    }
    
    /**
     * 获取消息
     */
    fun getMessage(path: String, vararg args: String): String {
        var message = langConfig.getString(path, "Missing message: $path")
        
        // 替换前缀
        message = message?.replace("{prefix}", prefix)
        
        // 替换参数
        for (i in args.indices) {
            message = message?.replace("{$i}", args[i])
        }
        
        return message ?: "Missing message: $path"
    }
    
    /**
     * 获取消息列表
     */
    fun getMessageList(path: String): List<String> {
        val list = langConfig.getStringList(path)
        if (list.isEmpty()) {
            return listOf("Missing message list: $path")
        }
        
        return list.map { it.replace("{prefix}", prefix) }
    }
    
    /**
     * 获取前缀
     */
    fun getPrefix(): String {
        return prefix
    }
}
