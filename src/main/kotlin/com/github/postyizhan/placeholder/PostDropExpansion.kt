package com.github.postyizhan.placeholder

import com.github.postyizhan.PostDrop
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

/**
 * PostDrop的PlaceholderAPI扩展类
 * 提供以下占位符:
 * %postdrop_toggle% - 返回玩家是否启用了物品丢弃保护
 */
class PostDropExpansion(private val plugin: PostDrop) : PlaceholderExpansion() {

    /**
     * 返回扩展的标识符
     * 这是占位符的前缀
     */
    override fun getIdentifier(): String {
        return "postdrop"
    }

    /**
     * 返回扩展的作者
     */
    override fun getAuthor(): String {
        return plugin.description.authors.joinToString()
    }

    /**
     * 返回扩展的版本
     */
    override fun getVersion(): String {
        return plugin.description.version
    }

    /**
     * 这是PlaceholderAPI调用处理占位符的方法
     * 我们使用这个方法来实现自定义占位符
     * @param player 玩家对象
     * @param identifier 占位符标识符（去掉前缀后的部分）
     * @return 替换后的字符串，或者null如果无法替换
     */
    override fun onPlaceholderRequest(player: Player?, identifier: String): String? {
        if (player == null) {
            return null
        }

        // %postdrop_toggle%
        if (identifier.equals("toggle", ignoreCase = true)) {
            // 检查玩家是否启用了物品丢弃保护
            val isEnabled = plugin.protectionManager.isProtectionEnabled(player)
            return if (isEnabled) "开启" else "关闭"
        }

        return null
    }

    /**
     * 该扩展是否持久存在
     * 如果为true，则扩展将在服务器启动时注册，
     * 如果为false，则需要手动注册
     */
    override fun persist(): Boolean {
        return true
    }
}
