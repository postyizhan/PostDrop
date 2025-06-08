package com.github.postyizhan.commands

import com.github.postyizhan.PostDrop
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

/**
 * 主命令处理类
 */
class MainCommand(private val plugin: PostDrop) : CommandExecutor, TabCompleter {
    
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // 调试日志
        if (plugin.configManager.isDebugEnabled()) {
            plugin.logger.info("Command executed: /$label ${args.joinToString(" ")}")
        }
        
        // 检查参数
        if (args.isEmpty()) {
            // 显示帮助
            showHelp(sender)
            return true
        }
        
        // 处理子命令
        when (args[0].lowercase()) {
            "help" -> {
                showHelp(sender)
                return true
            }
            "reload" -> {
                return handleReload(sender)
            }
            "version" -> {
                showVersion(sender)
                return true
            }
            "toggle" -> {
                return handleToggle(sender)
            }
            else -> {
                sender.sendMessage(plugin.languageManager.getColoredMessage("messages.invalid-command"))
                return true
            }
        }
    }
    
    /**
     * 显示帮助信息
     */
    private fun showHelp(sender: CommandSender) {
        val helpMessages = plugin.languageManager.getMessageList("messages.help")
        helpMessages.forEach { sender.sendMessage(it) }
    }
    
    /**
     * 处理重载命令
     */
    private fun handleReload(sender: CommandSender): Boolean {
        // 检查权限
        if (!sender.hasPermission("postdrop.reload")) {
            sender.sendMessage(plugin.languageManager.getColoredMessage("messages.no-permission"))
            return true
        }
        
        // 重载插件
        if (plugin.reload()) {
            sender.sendMessage(plugin.languageManager.getColoredMessage("messages.reload"))
        } else {
            sender.sendMessage("§cReload failed! Check console for errors.")
        }
        
        return true
    }
    
    /**
     * 显示版本信息
     */
    private fun showVersion(sender: CommandSender) {
        sender.sendMessage("§aPostDrop v${plugin.description.version}")
        sender.sendMessage("§7By postyizhan")
    }
    
    /**
     * 处理切换保护命令
     */
    private fun handleToggle(sender: CommandSender): Boolean {
        // 只有玩家可以使用此命令
        if (sender !is Player) {
            sender.sendMessage("§cThis command can only be used by players!")
            return true
        }
        
        // 检查权限
        if (!sender.hasPermission("postdrop.toggle")) {
            sender.sendMessage(plugin.languageManager.getColoredMessage("messages.no-permission"))
            return true
        }
        
        // 获取当前状态
        val currentStatus = plugin.protectionManager.isProtectionEnabled(sender)
        
        // 切换到相反状态
        val newStatus = !currentStatus
        
        // 调试日志
        if (plugin.configManager.isDebugEnabled()) {
            plugin.logger.info("Player ${sender.name} toggled protection from $currentStatus to $newStatus")
        }
        
        // 应用新状态
        plugin.protectionManager.setProtectionEnabled(sender, newStatus)
        
        // 发送消息
        if (newStatus) {
            sender.sendMessage(plugin.languageManager.getColoredMessage("messages.item-drop.protection-enabled"))
        } else {
            sender.sendMessage(plugin.languageManager.getColoredMessage("messages.item-drop.protection-disabled"))
        }
        
        return true
    }
    
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>? {
        val completions = ArrayList<String>()
        
        if (args.size == 1) {
            // 第一个参数的补全
            val subCommands = listOf("help", "reload", "version", "toggle")
            
            // 过滤匹配的命令
            for (subCommand in subCommands) {
                if (subCommand.startsWith(args[0].lowercase())) {
                    completions.add(subCommand)
                }
            }
            
            return completions
        }
        
        return null
    }
}
