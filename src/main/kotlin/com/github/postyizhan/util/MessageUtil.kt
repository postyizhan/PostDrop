package com.github.postyizhan.util

import com.github.postyizhan.PostDrop
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * 消息工具类，负责处理消息格式化和发送
 */
object MessageUtil {
    private lateinit var plugin: PostDrop
    
    // 是否启用MiniMessage格式
    private var useMiniMessage = false
    
    /**
     * 初始化消息工具
     * @param plugin 插件实例
     */
    fun init(plugin: PostDrop) {
        this.plugin = plugin
        this.useMiniMessage = plugin.configManager.isMiniMessageEnabled()
        
        // 初始化MiniMessage工具类
        if (useMiniMessage) {
            MiniMessageUtil.init(plugin)
        }
    }
    
    /**
     * 关闭消息工具
     */
    fun shutdown() {
        if (useMiniMessage) {
            MiniMessageUtil.shutdown()
        }
    }
    
    /**
     * 获取消息
     * @param path 消息路径
     * @return 消息内容
     */
    fun getMessage(path: String): String {
        return plugin.languageManager.getMessage(path)
    }
    
    /**
     * 获取消息列表
     * @param path 消息路径
     * @return 消息列表
     */
    fun getMessageList(path: String): List<String> {
        return plugin.languageManager.getMessageList(path)
    }
    
    /**
     * 为消息添加颜色
     * @param message 原始消息
     * @return 添加颜色后的消息
     */
    fun color(message: String): String {
        return ChatColor.translateAlternateColorCodes('&', message)
    }
    
    /**
     * 发送消息给接收者
     * @param receiver 消息接收者
     * @param message 消息内容
     */
    fun sendMessage(receiver: CommandSender, message: String) {
        // 检查消息格式
        if (useMiniMessage && MiniMessageUtil.containsMiniMessageFormat(message)) {
            // 使用MiniMessage格式发送
            MiniMessageUtil.sendMessage(receiver, message)
        } else {
            // 使用传统颜色代码发送
            receiver.sendMessage(color(message))
        }
    }
    
    /**
     * 发送消息列表给接收者
     * @param receiver 消息接收者
     * @param messages 消息列表
     */
    fun sendMessages(receiver: CommandSender, messages: List<String>) {
        messages.forEach { sendMessage(receiver, it) }
    }
    
    /**
     * 替换消息中的占位符
     * @param message 原始消息
     * @param replacements 替换对（键值对形式的替换内容）
     * @return 替换后的消息
     */
    fun replace(message: String, vararg replacements: Pair<String, String>): String {
        var result = message
        for ((key, value) in replacements) {
            result = result.replace(key, value)
        }
        return result
    }
    
    /**
     * 将传统格式消息转换为MiniMessage格式
     * @param legacyMessage 传统格式消息
     * @return MiniMessage格式消息
     */
    fun legacyToMiniMessage(legacyMessage: String): String {
        return MiniMessageUtil.legacyToMiniMessage(legacyMessage)
    }
    
    /**
     * 检查MiniMessage格式是否启用
     * @return 是否启用MiniMessage格式
     */
    fun isMiniMessageEnabled(): Boolean {
        return useMiniMessage
    }
}
