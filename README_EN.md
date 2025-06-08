[简体中文](README.md) | English

# PostDrop
![Version](https://img.shields.io/github/v/release/postyizhan/PostDrop?color=blue&label=Version)
![Minecraft](https://img.shields.io/badge/Minecraft-1.13+-green)
![Language](https://img.shields.io/badge/Language-简体中文|English-orange)

PostDrop is a simple and useful Minecraft plugin that protects dropped items from being picked up by other players.

## 📚 Features

- **🔒 Item Protection**: Items dropped by players are protected by default and can only be picked up by the player who dropped them
- **✨ Item Highlighting**: Protected items have a glow effect, making them easy to identify
- **👁️ Item Visibility Control**: Configure whether protected items are visible to other players (requires ProtocolLib)
- **🔄 Player Control**: Players can toggle item protection on or off
- **🔔 Automatic Update Checking**: Automatically checks for and notifies about new versions
- **🌐 Multi-language Support**: Built-in Chinese and English language packs
- **📊 Placeholder Support**: Supports PlaceholderAPI variables for use in scoreboards, holograms, etc.

## 💻 Installation

1. Download the latest version of `PostDrop-xxx.jar`
2. Place the JAR file in your server's `plugins` folder
3. Restart your server or load the plugin using a plugin manager
4. Optional: Install [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997) for advanced item visibility control
5. Optional: Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245) to use placeholders

## 🔧 Commands

| Command | Shorthand | Description |
|---------|-----------|-------------|
| `/postdrop help` | `/pd help` | Display help information |
| `/postdrop toggle` | `/pd toggle` | Toggle item drop protection status |
| `/postdrop reload` | `/pd reload` | Reload plugin configuration |
| `/postdrop version` | `/pd version` | Show plugin version information |
| `/postdrop update` | `/pd update` | Check for updates |

## 🔒 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `postdrop.toggle` | Allows players to toggle item protection status | Everyone |
| `postdrop.reload` | Allows players to reload the plugin | OP |
| `postdrop.update` | Allows players to check for updates | OP |
| `postdrop.*` | Includes all permissions | OP |

## 📊 PlaceholderAPI Variables

After installing PlaceholderAPI, you can use the following placeholders:

| Placeholder | Description | Returns |
|-------------|-------------|---------|
| `%postdrop_toggle%` | Shows whether the player has enabled item drop protection | "开启" (Enabled) or "关闭" (Disabled) |

## 📫 Support

- [![](https://img.shields.io/badge/QQ群-611076407-54B4EF?logo=data:image/png;base64,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)](https://8aka.cn/qq) <-点击加入

- [![](https://img.shields.io/discord/1342805340839870514.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://discord.com/invite/jN4Br8uhSS) <-点击加入

## 📜 License

This plugin is licensed under the [GNU General Public License v2.0](LICENSE). 
