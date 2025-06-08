简体中文 | [English](README_EN.md)

# PostDrop
![版本](https://img.shields.io/github/v/release/postyizhan/PostDrop?color=blue&label=版本)
![Minecraft](https://img.shields.io/badge/Minecraft-1.13+-green)
![语言](https://img.shields.io/badge/语言-简体中文|English-orange)

PostDrop 是一个简单而实用的 Minecraft 物品丢弃保护插件，可以防止其他玩家拾取您丢弃的物品。

## 📚 功能特性

- **🔒 物品保护**：玩家丢弃的物品默认受到保护，只有丢弃者可以拾取
- **✨ 物品高亮**：受保护的物品会有发光效果，方便玩家识别
- **👁️ 物品可见性控制**：可以配置受保护的物品对其他玩家是否可见（需要ProtocolLib）
- **🔄 玩家自主控制**：玩家可以自行切换是否保护丢弃的物品
- **🔔 自动更新检查**：自动检查并提示新版本
- **🌐 多语言支持**：内置中文和英文语言包
- **📊 变量支持**：支持PlaceholderAPI变量，可用于计分板、全息图等

## 💻 安装

1. 下载最新版本的`PostDrop-xxx.jar`
2. 将JAR文件放入服务器的`plugins`文件夹
3. 重启服务器或使用插件管理器加载插件
4. 可选：安装 [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997) 以启用高级物品可见性控制
5. 可选：安装 [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245) 以使用变量功能

## 🔧 命令

| 命令 | 简写 | 描述 |
|------|------|------|
| `/postdrop help` | `/pd help` | 显示帮助信息 |
| `/postdrop toggle` | `/pd toggle` | 切换物品丢弃保护状态 |
| `/postdrop reload` | `/pd reload` | 重载插件配置 |
| `/postdrop version` | `/pd version` | 显示插件版本信息 |
| `/postdrop update` | `/pd update` | 检查插件更新 |

## 🔒 权限

| 权限 | 描述 | 默认值 |
|------|------|--------|
| `postdrop.toggle` | 允许玩家切换物品保护状态 | 所有玩家 |
| `postdrop.reload` | 允许玩家重载插件 | OP |
| `postdrop.update` | 允许玩家检查更新 | OP |
| `postdrop.*` | 包含所有权限 | OP |

## 📊 PlaceholderAPI 变量

安装 PlaceholderAPI 后，可以使用以下变量：

| 变量 | 描述 | 返回值 |
|------|------|--------|
| `%postdrop_toggle%` | 显示玩家是否启用了物品丢弃保护 | "开启" 或 "关闭" |

## 📜 许可证

本插件采用 [GNU General Public License v2.0](LICENSE) 许可证。
