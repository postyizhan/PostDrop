package com.github.postyizhan.util

import com.github.postyizhan.PostDrop
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * MiniMessage工具类，用于处理MiniMessage格式的消息
 */
object MiniMessageUtil {
    // MiniMessage解析器
    private val miniMessage = MiniMessage.miniMessage()
    
    // Legacy格式解析器
    private val legacySerializer = LegacyComponentSerializer.legacySection()
    
    // BukkitAudiences实例
    private lateinit var audiences: BukkitAudiences
    
    /**
     * 初始化MiniMessage工具
     * @param plugin 插件实例
     */
    fun init(plugin: PostDrop) {
        audiences = BukkitAudiences.create(plugin)
    }
    
    /**
     * 关闭MiniMessage工具
     */
    fun shutdown() {
        if (::audiences.isInitialized) {
            audiences.close()
        }
    }

    /**
     * 将MiniMessage格式文本转换为Component
     * @param miniMessageText MiniMessage格式文本
     * @return Component对象
     */
    fun parse(miniMessageText: String): Component {
        return miniMessage.deserialize(miniMessageText)
    }
    
    /**
     * 将传统颜色代码(&)转换为MiniMessage格式
     * @param legacyText 包含传统颜色代码的文本
     * @return MiniMessage格式的文本
     */
    fun legacyToMiniMessage(legacyText: String): String {
        val component = legacySerializer.deserialize(MessageUtil.color(legacyText))
        return miniMessage.serialize(component)
    }
    
    /**
     * 将Component转换为传统颜色代码格式
     * @param component Component对象
     * @return 包含传统颜色代码的文本
     */
    fun componentToLegacy(component: Component): String {
        return legacySerializer.serialize(component)
    }
    
    /**
     * 发送MiniMessage格式消息给接收者
     * @param receiver 消息接收者
     * @param miniMessageText MiniMessage格式文本
     */
    fun sendMessage(receiver: CommandSender, miniMessageText: String) {
        val component = parse(miniMessageText)
        audiences.sender(receiver).sendMessage(component)
    }
    
    /**
     * 发送MiniMessage格式消息列表给接收者
     * @param receiver 消息接收者
     * @param miniMessageTexts MiniMessage格式文本列表
     */
    fun sendMessages(receiver: CommandSender, miniMessageTexts: List<String>) {
        miniMessageTexts.forEach { sendMessage(receiver, it) }
    }
    
    /**
     * 在MiniMessage格式文本中替换占位符
     * @param miniMessageText MiniMessage格式文本
     * @param replacements 替换对（键值对形式的替换内容）
     * @return 替换后的MiniMessage格式文本
     */
    fun replace(miniMessageText: String, vararg replacements: Pair<String, String>): String {
        var result = miniMessageText
        for ((key, value) in replacements) {
            result = result.replace(key, value)
        }
        return result
    }
    
    /**
     * 判断文本是否包含MiniMessage格式
     * @param text 要检查的文本
     * @return 是否包含MiniMessage格式
     */
    fun containsMiniMessageFormat(text: String): Boolean {
        return text.contains("<") && text.contains(">")
    }
}
