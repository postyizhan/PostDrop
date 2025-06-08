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

## 📜 License

This plugin is licensed under the [GNU General Public License v2.0](LICENSE). 
