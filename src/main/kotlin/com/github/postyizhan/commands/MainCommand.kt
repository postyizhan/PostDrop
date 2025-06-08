package com.github.postyizhan.commands

import com.github.postyizhan.PostDrop
import com.github.postyizhan.util.MessageUtil
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
            "update" -> {
                return handleUpdate(sender)
            }
            else -> {
                MessageUtil.sendMessage(sender, MessageUtil.getMessage("messages.invalid-command"))
                return true
            }
        }
    }
    
    /**
     * 显示帮助信息
     */
    private fun showHelp(sender: CommandSender) {
        val helpMessages = MessageUtil.getMessageList("messages.help")
        MessageUtil.sendMessages(sender, helpMessages)
    }
    
    /**
     * 处理重载命令
     */
    private fun handleReload(sender: CommandSender): Boolean {
        // 检查权限
        if (!sender.hasPermission("postdrop.reload")) {
            MessageUtil.sendMessage(sender, MessageUtil.getMessage("messages.no-permission"))
            return true
        }
        
        // 重载插件
        if (plugin.reload()) {
            MessageUtil.sendMessage(sender, MessageUtil.getMessage("messages.reload"))
        } else {
            MessageUtil.sendMessage(sender, "§cReload failed! Check console for errors.")
        }
        
        return true
    }
    
    /**
     * 显示版本信息
     */
    private fun showVersion(sender: CommandSender) {
        MessageUtil.sendMessage(sender, "§aPostDrop v${plugin.description.version}")
        MessageUtil.sendMessage(sender, "§7By postyizhan")
    }
    
    /**
     * 处理切换保护命令
     */
    private fun handleToggle(sender: CommandSender): Boolean {
        // 只有玩家可以使用此命令
        if (sender !is Player) {
            MessageUtil.sendMessage(sender, "§cThis command can only be used by players!")
            return true
        }
        
        // 检查权限
        if (!sender.hasPermission("postdrop.toggle")) {
            MessageUtil.sendMessage(sender, MessageUtil.getMessage("messages.no-permission"))
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
            MessageUtil.sendMessage(sender, MessageUtil.getMessage("messages.item-drop.protection-enabled"))
        } else {
            MessageUtil.sendMessage(sender, MessageUtil.getMessage("messages.item-drop.protection-disabled"))
        }
        
        return true
    }
    
    /**
     * 处理更新检查命令
     */
    private fun handleUpdate(sender: CommandSender): Boolean {
        // 检查权限
        if (!sender.hasPermission("postdrop.update")) {
            MessageUtil.sendMessage(sender, MessageUtil.getMessage("messages.no-permission"))
            return true
        }
        
        // 发送正在检查更新的消息
        MessageUtil.sendMessage(sender, MessageUtil.getMessage("system.updater.update_checking"))
        
        // 执行更新检查
        if (sender is Player) {
            plugin.sendUpdateInfo(sender)
        } else {
            plugin.getUpdateChecker().checkForUpdates { isUpdateAvailable, newVersion ->
                if (isUpdateAvailable) {
                    MessageUtil.sendMessage(sender, MessageUtil.getMessage("system.updater.update_available")
                        .replace("{current_version}", plugin.description.version)
                        .replace("{latest_version}", newVersion))
                    MessageUtil.sendMessage(sender, MessageUtil.getMessage("system.updater.update_url")
                        .replace("{current_version}", plugin.description.version)
                        .replace("{latest_version}", newVersion))
                } else {
                    MessageUtil.sendMessage(sender, MessageUtil.getMessage("system.updater.up_to_date"))
                }
            }
        }
        
        return true
    }
    
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>? {
        val completions = ArrayList<String>()
        
        if (args.size == 1) {
            // 第一个参数的补全
            val subCommands = listOf("help", "reload", "version", "toggle", "update")
            
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
