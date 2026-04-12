# Command-GUI

[English](#english) | [中文](#中文)

---

## English

A Fabric mod for quickly executing commands through a GUI interface, with powerful fake player management.

### Features

#### Custom Commands
- Create your own commands with optional placeholders
- Organize commands into categories
- Search commands by name or content
- Right-click to edit or delete

#### Placeholder Support
Dynamic command execution with various input types:
- `{player_all}` - Select from all players
- `{player}` - Select from other players (excluding self)
- `{player_fake}` - Select from fake players (Carpet bots)
- `{name}` - Text input
- `{number}` - Number input with quick select buttons
- `{time}` - Time input (supports t/s/d units)
- `{coords}` or `{x} {y} {z}` - Coordinate input

#### Preset Commands
Built-in command groups (can be toggled in settings):
- **Vanilla**: Game mode, time, weather, difficulty, teleport, tick control, gamerules
- **Carpet**: Performance profiling, fake player actions, logging, tracking

#### Fake Player Control (Carpet Mod)
Comprehensive fake player management tab:

**Player List (Single Column)**
- Shows online fake players with face icons
- Shows pending spawn tasks with "+" icon
- Displays countdown timer for timed tasks

**Batch Operations**
- **Batch Spawn**: Spawn multiple fake players at once
  - Numbered naming (prefix + number)
  - English names (Alex, Ben, Carl...)
- **Kill All**: Remove all fake players instantly

**Timed Tasks**
- **Timed Add**: Schedule fake player spawn
  - Set player name and delay time (H:M:S)
  - Pending players appear in list with green countdown
  - Click to cancel before execution
- **Timed Kill**: Schedule fake player removal
  - Select existing fake player
  - Set delay time (H:M:S)
  - Shows red countdown on player entry
  - Click "Cancel Timer" to abort

**Individual Actions**
Select a fake player to access:
- Stop / Kill
- Attack (continuous/once)
- Use (continuous/once)
- Jump / Sneak / Sprint
- Drop / Drop Stack

### Installation

1. Install [Fabric Loader](https://fabricmc.net/) and [Fabric API](https://modrinth.com/mod/fabric-api)
2. Download the mod and place it in the `mods` folder
3. Launch Minecraft

### Usage

- Press `C` (default) to open the GUI
- Press `C` again or `Esc` to close
- Use the checkbox to keep GUI open after executing commands

### Tabs

| Tab | Description |
|-----|-------------|
| Custom | Your personal commands |
| Fake Player | Carpet fake player management |
| Vanilla | Common vanilla commands (requires OP) |
| Carpet | Carpet mod commands |

### Settings

Click the gear icon to configure:
- Show/hide Vanilla commands tab
- Show/hide Carpet commands tab
- Show/hide Fake Player tab

### Configuration

- Custom commands: `config/command-gui/commands.json`
- Settings: `config/command-gui/settings.json`
- Custom presets: `config/command-gui/presets/`

### Requirements

- Minecraft 1.21.1
- Fabric Loader ≥ 0.18.6
- Fabric API
- Carpet Mod (optional, for fake player features)

### License

GPL-3.0

---

## 中文

一个 Fabric 模组，通过 GUI 界面快速执行指令，并提供强大的假人管理功能。

### 功能特性

#### 自定义指令
- 创建带有占位符的自定义指令
- 使用分类整理指令
- 按名称或内容搜索指令
- 右键编辑或删除

#### 占位符支持
支持多种输入类型的动态指令：
- `{player_all}` - 选择所有玩家
- `{player}` - 选择其他玩家（排除自己）
- `{player_fake}` - 选择假人（Carpet 机器人）
- `{name}` - 文本输入
- `{number}` - 数字输入，带快速选择按钮
- `{time}` - 时间输入（支持 t/s/d 单位）
- `{coords}` 或 `{x} {y} {z}` - 坐标输入

#### 预设指令
内置指令组（可在设置中开关）：
- **原版**：游戏模式、时间、天气、难度、传送、游戏刻控制、游戏规则
- **Carpet**：性能分析、假人动作、日志订阅、追踪功能

#### 假人控制（需要 Carpet Mod）
完整的假人管理标签页：

**假人列表（单列显示）**
- 显示在线假人及其头像
- 显示待生成假人（带 "+" 图标）
- 显示定时任务的倒计时

**批量操作**
- **批量生成**：一次生成多个假人
  - 编号命名（前缀 + 数字）
  - 英文名（Alex、Ben、Carl...）
- **移除全部**：立即移除所有假人

**定时任务**
- **定时添加**：预约生成假人
  - 设置假人名称和延迟时间（时:分:秒）
  - 待生成假人显示在列表中，带绿色倒计时
  - 点击可取消
- **定时移除**：预约移除假人
  - 选择已存在的假人
  - 设置延迟时间（时:分:秒）
  - 假人条目显示红色倒计时
  - 点击"取消定时"可中止

**单个假人操作**
选中假人后可执行：
- 停止 / 移除
- 攻击（持续/单次）
- 使用（持续/单次）
- 跳跃 / 潜行 / 疾跑
- 丢弃 / 丢弃整组

### 安装方法

1. 安装 [Fabric Loader](https://fabricmc.net/) 和 [Fabric API](https://modrinth.com/mod/fabric-api)
2. 下载模组并放入 `mods` 文件夹
3. 启动 Minecraft

### 使用方法

- 按 `C` 键（默认）打开 GUI
- 再次按 `C` 或 `Esc` 关闭
- 勾选复选框可在执行指令后保持界面打开

### 标签页

| 标签页 | 说明 |
|--------|------|
| 自定义 | 你的个人指令 |
| 假人控制 | Carpet 假人管理 |
| 原版 | 常用原版指令（需要管理员权限） |
| Carpet | Carpet 模组指令 |

### 设置

点击齿轮图标可配置：
- 显示/隐藏原版指令标签
- 显示/隐藏 Carpet 指令标签
- 显示/隐藏假人控制标签

### 配置文件

- 自定义指令：`config/command-gui/commands.json`
- 设置：`config/command-gui/settings.json`
- 自定义预设：`config/command-gui/presets/`

### 运行要求

- Minecraft 1.21.1
- Fabric Loader ≥ 0.18.6
- Fabric API
- Carpet Mod（可选，用于假人功能）

### 许可证

GPL-3.0
