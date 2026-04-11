# Command-GUI

[English](#english) | [中文](#中文)

---

## English

A simple Fabric mod for quickly executing commands through a GUI interface.

### Features

- **Custom Commands**: Create your own commands with optional placeholders
- **Preset Commands**: Built-in command groups for Vanilla and Carpet Mod
- **Placeholder Support**: Dynamic command execution with various input types
  - `{player_all}` - Select from all players
  - `{player}` - Select from other players (excluding self)
  - `{player_fake}` - Select from fake players (Carpet bots)
  - `{name}` - Text input
  - `{number}` - Number input with quick select buttons
  - `{coords}` - Coordinate input (X, Y, Z)
- **Chained Input**: Commands can have multiple placeholders, executed in sequence
- **Categories**: Organize commands into groups for easy navigation
- **Search**: Quickly find commands by name or content

### Installation

1. Install [Fabric Loader](https://fabricmc.net/) and [Fabric API](https://modrinth.com/mod/fabric-api)
2. Download the mod and place it in the `mods` folder
3. Launch Minecraft

### Usage

- Press `C` (default) to open the GUI
- Press `C` again or `Esc` to close
- **Custom Tab**: Add, edit, or delete your own commands (right-click for context menu)
- **Vanilla Tab**: Quick access to common vanilla commands (requires operator/creative mode)
- **Carpet Tab**: Commands for Carpet Mod (if installed)

### Configuration

Custom commands are saved in: `config/command-gui/presets/custom.json`

You can also add custom preset files in the same directory.

### Examples

| Name | Command | Description |
|------|---------|-------------|
| TP to Player | `/tp {player}` | Teleport to selected player |
| Spawn Bot | `/player {name} spawn` | Spawn a Carpet bot with input name |
| TP Player to Coords | `/tp {player} {coords}` | Teleport a player to coordinates |
| Set Time | `/time set {number}` | Set world time |

### Requirements

- Minecraft 1.21.11
- Fabric Loader ≥ 0.18.6
- Fabric API

### License

MIT

---

## 中文

一个简单的 Fabric 模组，通过 GUI 界面快速执行指令。

### 功能特性

- **自定义指令**：创建带有占位符的自定义指令
- **预设指令**：内置原版和 Carpet Mod 的常用指令
- **占位符支持**：支持多种输入类型的动态指令
  - `{player_all}` - 选择所有玩家
  - `{player}` - 选择其他玩家（排除自己）
  - `{player_fake}` - 选择假人（Carpet 机器人）
  - `{name}` - 文本输入
  - `{number}` - 数字输入，带快速选择按钮
  - `{coords}` - 坐标输入（X, Y, Z）
- **链式输入**：指令可包含多个占位符，依次输入
- **分类管理**：将指令整理到不同分组中
- **搜索功能**：按名称或内容快速查找指令

### 安装方法

1. 安装 [Fabric Loader](https://fabricmc.net/) 和 [Fabric API](https://modrinth.com/mod/fabric-api)
2. 下载模组并放入 `mods` 文件夹
3. 启动 Minecraft

### 使用方法

- 按 `C` 键（默认）打开 GUI
- 再次按 `C` 或 `Esc` 关闭
- **自定义标签**：添加、编辑或删除自定义指令（右键打开菜单）
- **原版标签**：快速访问常用原版指令（需要管理员/创造模式权限）
- **Carpet 标签**：Carpet Mod 相关指令

### 配置文件

自定义指令保存在：`config/command-gui/presets/custom.json`

你也可以在同一目录下添加自定义预设文件。

### 示例

| 名称 | 指令 | 说明 |
|------|------|------|
| 传送到玩家 | `/tp {player}` | 传送到选择的玩家 |
| 生成假人 | `/player {name} spawn` | 生成指定名称的假人 |
| 传送玩家到坐标 | `/tp {player} {coords}` | 将玩家传送到指定坐标 |
| 设置时间 | `/time set {number}` | 设置世界时间 |

### 运行要求

- Minecraft 1.21.11
- Fabric Loader ≥ 0.18.6
- Fabric API

### 许可证

MIT
