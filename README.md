# Command-GUI

[English](#english) | [中文](#中文)

---

## English

A Fabric client-side mod for Minecraft 1.21.1 that lets you quickly execute commands through a clean GUI interface, with powerful fake player (Carpet bot) management.

> **Wiki / Detailed Docs →** see the [`docs/`](docs/) folder

### Features at a Glance

| Feature | Summary |
|---------|---------|
| 🗂 Custom Commands | Create, edit, delete and categorize your own commands |
| 🔤 Placeholder System | Dynamic inputs: player selector, text, number, time, coords |
| 📋 Multi-Command Chains | Execute multiple commands in sequence with a single click |
| 🏷 Categories | Organize commands into named categories; move commands between them |
| 🔍 Search | Filter commands by name, description, or command text |
| ⚙ Preset: Vanilla | Built-in groups for gamemode, time, weather, tick control, gamerules, etc. |
| 🟩 Preset: Carpet | Built-in groups for profiling, fake player control, logging, tracking, etc. |
| 🤖 Fake Player Tab | Full fake player management panel (requires Carpet Mod) |
| ⏱ Timed Tasks | Schedule spawn or kill of fake players with countdown display |
| 👥 Batch Spawn | Spawn many fake players at once with numbered or English names |
| 🔧 Settings | Toggle visibility of each tab; persisted to JSON |
| 📁 JSON Config | All data stored in human-readable JSON files |

---

### Installation

1. Install [Fabric Loader](https://fabricmc.net/) (≥ 0.18.6) and [Fabric API](https://modrinth.com/mod/fabric-api)
2. *(Optional)* Install [Carpet Mod](https://github.com/gnembon/fabric-carpet) for fake player features
3. Download the latest `command-gui-*.jar` from [Releases](../../releases)
4. Place the `.jar` in your `.minecraft/mods/` folder
5. Launch Minecraft 1.21.1 with the Fabric profile

### Requirements

| Dependency | Version |
|-----------|---------|
| Minecraft | 1.21.1 |
| Java | ≥ 21 |
| Fabric Loader | ≥ 0.18.6 |
| Fabric API | any |
| Carpet Mod | optional – for fake player features |

---

### Usage

#### Opening the GUI

- Press **`C`** (default keybind) to open the Command-GUI
- Press **`C`** again or **`Esc`** to close
- Check **"Keep open after execute"** to keep the GUI open after running a command

#### Tabs

| Tab | Description |
|-----|-------------|
| **Custom** | Your personal commands, organized by category |
| **Fake Player** | Carpet fake player management panel |
| **Vanilla** | Common vanilla commands (requires OP / single-player) |
| **Carpet** | Carpet mod utility commands |

> The **Fake Player**, **Vanilla**, and **Carpet** tabs can be hidden in Settings.

#### Custom Tab — Adding a Command

1. Click **`+`** (Add Command) in the footer
2. Enter a **Name**, the **Command(s)**, and an optional **Description**
3. Use placeholders like `{player}`, `{number}`, `{coords}` in the command text for dynamic input at runtime
4. Click **Save**

To add a **Fake Player Command** with a visual builder, click **"Add Fake Player Command"** from the `+` button.

#### Custom Tab — Action Buttons

Each command entry shows three icon buttons on the right:

| Icon | Action |
|------|--------|
| 📖 (Book) | **Edit** the command |
| 🪣 (Lava Bucket) | **Delete** the command |
| 📦 (Shulker Box) | **Move** to a different category |

#### Category Management

- Click **"All"** to view all commands across categories
- Click a category name to filter by that category
- Click **`+`** (next to categories) to create a new category
- Commands in the **Default** category cannot be deleted; the Default category itself cannot be removed

#### Settings

Click the **⚙ gear icon** in the footer to open Settings:

- ☑ **Show Vanilla Commands** – toggle the Vanilla tab
- ☑ **Show Carpet Commands** – toggle the Carpet tab
- ☑ **Show Fake Player Tab** – toggle the Fake Player tab

---

### Placeholder System

When a command contains a placeholder, a helper screen appears at execution time:

| Placeholder | Input Screen | Example Use |
|-------------|-------------|-------------|
| `{player_all}` | Player list (all online) | `/tp @s {player_all}` |
| `{player}` | Player list (others only) | `/tp {player} ~ ~ ~` |
| `{player_fake}` | Player list (fake players only) | `/player {player_fake} kill` |
| `{name}` | Free text input | `/player {name} spawn` |
| `{number}` | Numeric input with quick buttons | `/tick rate {number}` |
| `{time}` | Time input (t / s / d) | `/tick step {time}` |
| `{coords}` | XYZ coordinate input | `/tp @s {coords}` |
| `{x}` `{y}` `{z}` | XYZ coordinate input (separate) | `/tp @s {x} {y} {z}` |

Multiple placeholders in one command are resolved **sequentially**.

---

### Preset Commands — Vanilla Tab

> Requires OP permissions or single-player

| Group | Commands |
|-------|---------|
| **Gamemode** | Survival, Creative, Adventure, Spectator |
| **Time** | Day, Noon, Night, Midnight |
| **Weather** | Clear, Rain, Thunder |
| **Difficulty** | Peaceful, Easy, Normal, Hard |
| **Tick Control** | Freeze, Unfreeze, Step `{time}`, Stop Step, Rate `{number}`, Sprint `{time}`, Stop Sprint |
| **Teleport** | To Player `{player}`, To Coordinates `{coords}`, To Spawn (0,~,0) |
| **Player** | Kill Self, Full Health, Full Hunger, Clear Effects |
| **World** | Kill Entities, KeepInventory ON/OFF, MobGriefing ON/OFF, DaylightCycle ON/OFF |

---

### Preset Commands — Carpet Tab

> Requires [Carpet Mod](https://github.com/gnembon/fabric-carpet)

| Group | Commands |
|-------|---------|
| **Profile** | Health profile `{number}`, Entities profile `{number}` |
| **Fake Player** | Spawn `{name}`, Spawn At `{name}` `{coords}`, Kill `{player_fake}`, Stop `{player_fake}` |
| **Fake Player Actions** | Attack / Attack Once / Use / Use Once / Jump / Drop / Drop Stack / Swap Hands |
| **Fake Player Movement** | Sneak / Unsneak / Sprint / Unsprint / Mount / Dismount / Look At `{coords}` / Turn |
| **Log** | List, Clear All, TPS, Mobcaps, Counter |
| **Log Tracking** | TNT, Projectiles, Falling Blocks |
| **Info** | Carpet List, Ping, Block Info `{coords}` / Here, Perimeter Info `{coords}` / Here |
| **Distance** | From, To |
| **Track** | Villager Breeding, Iron Golem Spawning |
| **Script** | List Scarpet Apps |

---

### Fake Player Tab

The **Fake Player** tab (requires Carpet Mod) provides a full management panel:

#### Player List
- Online fake players are listed with their **face icon**
- Pending timed-spawn entries appear with a **`+`** icon and a green countdown
- Fake players with an active timed-kill show a red countdown

#### Batch Spawn
Click **Batch Spawn** to open the Batch Spawn screen:
- **Prefix + Number** mode: `Bot_1`, `Bot_2`, … (configure prefix and starting number)
- **English Names** mode: Alex, Ben, Carl, … (up to 26 names)

#### Kill All
Click **Kill All** (requires Shift + Click confirmation) to remove all fake players.

#### Timed Spawn
Click **Timed Add** to schedule a fake player spawn:
1. Enter the fake player name
2. Choose position: **Current Position** or **Custom Coordinates**
3. Set delay time (hours : minutes : seconds)
4. The player appears in the list with a green countdown; click to **cancel** before execution

#### Timed Kill
Select a fake player and click **Timed Kill**:
1. The player is pre-filled
2. Set delay time (hours : minutes : seconds)
3. The entry shows a red countdown; click **"Cancel Timer"** to abort

#### Individual Actions
Click a fake player in the list to select it, then use the action buttons:

| Action | Description |
|--------|-------------|
| Stop | Stop the current action |
| Kill | Remove the fake player |
| Attack | Attack continuously |
| Attack Once | Attack a single time |
| Use | Use item / interact continuously |
| Use Once | Use item / interact once |
| Jump | Jump continuously |
| Sneak | Start sneaking |
| Unsneak | Stop sneaking |
| Sprint | Start sprinting |
| Stop Sprint | Stop sprinting |
| Drop | Drop held item (single) |
| Drop Stack | Drop held item (full stack) |

---

### Configuration Files

All configuration is stored in `<minecraft>/config/command-gui/`:

| File | Description |
|------|-------------|
| `presets/custom.json` | Your custom commands and categories |
| `settings.json` | Tab visibility settings |
| `presets/vanilla.json` | Vanilla preset commands (auto-generated) |
| `presets/carpet.json` | Carpet preset commands (auto-generated) |

The preset files (`vanilla.json`, `carpet.json`) are copied from the mod's built-in resources on first load and can be customized.

---

### Development

#### Requirements

- JDK 21+
- Gradle (wrapper included)

#### Build

```bash
git clone https://github.com/remrin/command-gui
cd command-gui
./gradlew build
```

The output `.jar` is placed in `build/libs/`.

#### Project Structure

```
src/
├── main/java/com/remrin/          # Server-side entry point (minimal)
├── main/resources/
│   └── assets/command-gui/
│       ├── lang/                  # en_us.json, zh_cn.json
│       └── presets/               # vanilla.json, carpet.json
└── client/java/com/remrin/client/
    ├── CommandGUIClient.java      # Client entry point, key binding, tick events
    ├── config/                    # CommandConfig, PresetConfig, SettingsConfig
    └── gui/                       # All GUI screens and tabs
```

#### Adding a Custom Preset Tab

1. Create a JSON file in `src/main/resources/assets/command-gui/presets/` (see `vanilla.json` as a template)
2. Add translation keys to `en_us.json` and `zh_cn.json`
3. The preset is automatically loaded and shown as a new tab

#### Preset JSON Format

```json
{
  "id": "my_preset",
  "nameKey": "screen.command-gui.tab.my_preset",
  "groups": [
    {
      "nameKey": "screen.command-gui.my_preset.group1",
      "commands": [
        {
          "nameKey": "screen.command-gui.my_preset.cmd1",
          "command": "/my command {number}",
          "description": "screen.command-gui.my_preset.cmd1.desc",
          "minValue": 1,
          "maxValue": 100,
          "quickValues": [1, 10, 50, 100]
        }
      ]
    }
  ]
}
```

Supported optional fields per command:
- `minValue` / `maxValue` – numeric range for `{number}` placeholder
- `quickValues` – array of `int` quick-select buttons for `{number}`
- `quickStrValues` – array of `String` quick-select values for `{time}`

---

### License

MIT

---

## 中文

一个适用于 Minecraft 1.21.1 的 Fabric 客户端模组，通过简洁的 GUI 界面快速执行指令，并提供强大的假人（Carpet 机器人）管理功能。

> **Wiki / 详细文档 →** 请查看 [`docs/`](docs/) 文件夹

### 功能概览

| 功能 | 说明 |
|------|------|
| 🗂 自定义指令 | 创建、编辑、删除并分类管理你的指令 |
| 🔤 占位符系统 | 动态输入：玩家选择器、文本、数字、时间、坐标 |
| 📋 多指令链式执行 | 一键依次执行多条指令 |
| 🏷 分类管理 | 将指令整理到命名分类中，支持在分类间移动 |
| 🔍 搜索 | 按名称、描述或指令内容筛选 |
| ⚙ 预设：原版 | 内置游戏模式、时间、天气、游戏刻控制、游戏规则等指令组 |
| 🟩 预设：Carpet | 内置性能分析、假人控制、日志订阅、追踪等指令组 |
| 🤖 假人标签页 | 完整的假人管理面板（需要 Carpet Mod） |
| ⏱ 定时任务 | 预约生成或移除假人，带倒计时显示 |
| 👥 批量生成 | 一次生成多个假人，支持编号命名或英文名 |
| 🔧 设置 | 控制各标签页的显示/隐藏，持久化保存 |
| 📁 JSON 配置 | 所有数据以易读的 JSON 文件存储 |

---

### 安装方法

1. 安装 [Fabric Loader](https://fabricmc.net/)（≥ 0.18.6）和 [Fabric API](https://modrinth.com/mod/fabric-api)
2. *（可选）* 安装 [Carpet Mod](https://github.com/gnembon/fabric-carpet) 以使用假人功能
3. 在 [Releases](../../releases) 页面下载最新的 `command-gui-*.jar`
4. 将 `.jar` 放入 `.minecraft/mods/` 文件夹
5. 使用 Fabric 配置启动 Minecraft 1.21.1

### 运行要求

| 依赖 | 版本 |
|------|------|
| Minecraft | 1.21.1 |
| Java | ≥ 21 |
| Fabric Loader | ≥ 0.18.6 |
| Fabric API | 任意 |
| Carpet Mod | 可选 – 用于假人功能 |

---

### 使用方法

#### 打开 GUI

- 按 **`C`** 键（默认快捷键）打开 Command-GUI
- 再次按 **`C`** 或 **`Esc`** 关闭
- 勾选 **"执行后不关闭界面"** 可在执行指令后保持界面打开

#### 标签页

| 标签页 | 说明 |
|--------|------|
| **自定义** | 你的个人指令，按分类整理 |
| **假人控制** | Carpet 假人管理面板 |
| **原版** | 常用原版指令（需要管理员权限或单人游戏） |
| **Carpet** | Carpet 模组实用指令 |

> **假人控制**、**原版** 和 **Carpet** 标签页可在设置中隐藏。

#### 自定义标签页 — 添加指令

1. 点击底栏的 **`+`**（添加指令）
2. 输入**名称**、**指令内容**和可选的**描述**
3. 在指令文本中使用占位符，如 `{player}`、`{number}`、`{coords}`，执行时会弹出输入界面
4. 点击**保存**

若要使用可视化构建器添加**假人指令**，点击 `+` 按钮中的"添加假人指令"。

#### 自定义标签页 — 操作按钮

每个指令条目右侧有三个图标按钮：

| 图标 | 操作 |
|------|------|
| 📖（书） | **编辑**指令 |
| 🪣（熔岩桶） | **删除**指令 |
| 📦（潜影盒） | **移动**到其他分类 |

#### 分类管理

- 点击**"全部"**查看所有分类的指令
- 点击分类名称按该分类筛选
- 点击分类列表末尾的 **`+`** 创建新分类
- **默认**分类中的指令无法直接删除默认分类本身

#### 设置

点击底栏的 **⚙ 齿轮图标** 打开设置界面：

- ☑ **显示原版指令** – 控制原版标签页显示
- ☑ **显示 Carpet 指令** – 控制 Carpet 标签页显示
- ☑ **显示假人控制** – 控制假人控制标签页显示

---

### 占位符系统

当指令包含占位符时，执行时会弹出对应的辅助输入界面：

| 占位符 | 输入界面 | 使用示例 |
|--------|----------|----------|
| `{player_all}` | 玩家列表（所有在线玩家） | `/tp @s {player_all}` |
| `{player}` | 玩家列表（仅其他玩家） | `/tp {player} ~ ~ ~` |
| `{player_fake}` | 玩家列表（仅假人） | `/player {player_fake} kill` |
| `{name}` | 自由文本输入 | `/player {name} spawn` |
| `{number}` | 数字输入，带快速选择按钮 | `/tick rate {number}` |
| `{time}` | 时间输入（t / s / d） | `/tick step {time}` |
| `{coords}` | XYZ 坐标输入 | `/tp @s {coords}` |
| `{x}` `{y}` `{z}` | XYZ 坐标输入（独立变量） | `/tp @s {x} {y} {z}` |

一条指令中的多个占位符会**按顺序**依次解析。

---

### 预设指令 — 原版标签页

> 需要管理员权限或单人游戏

| 指令组 | 包含指令 |
|--------|----------|
| **游戏模式** | 生存、创造、冒险、旁观 |
| **时间** | 白天、正午、夜晚、午夜 |
| **天气** | 晴天、雨天、雷暴 |
| **难度** | 和平、简单、普通、困难 |
| **游戏刻控制** | 冻结、解冻、步进 `{time}`、停止步进、速率 `{number}`、快进 `{time}`、停止快进 |
| **传送** | 传送到玩家 `{player}`、传送到坐标 `{coords}`、回到出生点 (0,~,0) |
| **玩家** | 自杀、满血、满饱食度、清除效果 |
| **世界** | 清除实体、死亡保留开/关、生物破坏开/关、日夜循环开/关 |

---

### 预设指令 — Carpet 标签页

> 需要 [Carpet Mod](https://github.com/gnembon/fabric-carpet)

| 指令组 | 包含指令 |
|--------|----------|
| **性能分析** | 性能监控 `{number}` 刻、实体耗时 `{number}` 刻 |
| **假人管理** | 生成 `{name}`、生成在 `{name}` `{coords}`、移除 `{player_fake}`、停止 `{player_fake}` |
| **假人动作** | 攻击/攻击一次/使用/使用一次/跳跃/丢弃/丢弃整组/交换双手 |
| **假人移动** | 潜行/取消潜行/疾跑/停止疾跑/骑乘/下马/看向 `{coords}`/转身 |
| **日志** | 列表、清除全部、TPS、生物上限、计数器 |
| **追踪日志** | TNT、抛射物、下落方块 |
| **信息** | 规则列表、延迟、方块信息 `{coords}` / 当前、范围分析 `{coords}` / 当前 |
| **距离** | 起点、终点 |
| **追踪** | 村民繁殖、铁傀儡生成 |
| **脚本** | 列出 Scarpet 应用 |

---

### 假人标签页

**假人控制**标签页（需要 Carpet Mod）提供完整的假人管理面板：

#### 假人列表
- 在线假人以其**头像**列出
- 待生成的定时任务条目显示 **`+`** 图标和绿色倒计时
- 有定时移除任务的假人显示红色倒计时

#### 批量生成
点击**批量生成**打开批量生成界面：
- **前缀+编号**模式：`Bot_1`、`Bot_2`……（可配置前缀和起始编号）
- **英文名**模式：Alex、Ben、Carl……（最多 26 个名字）

#### 移除全部
点击**移除全部**（需要 Shift + 点击确认）一次移除所有假人。

#### 定时添加
点击**定时添加**预约生成假人：
1. 输入假人名称
2. 选择生成位置：**当前位置**或**自定义坐标**
3. 设置延迟时间（时 : 分 : 秒）
4. 假人出现在列表中，显示绿色倒计时；执行前点击可**取消**

#### 定时移除
选中一个假人并点击**定时移除**：
1. 假人名称已预填
2. 设置延迟时间（时 : 分 : 秒）
3. 条目显示红色倒计时；点击**"取消定时"**可中止

#### 单个假人操作
在列表中点击假人将其选中，然后使用操作按钮：

| 操作 | 说明 |
|------|------|
| 停止 | 停止当前动作 |
| 移除 | 移除假人 |
| 持续攻击 | 持续攻击 |
| 攻击一次 | 攻击一次 |
| 持续使用 | 持续使用物品/交互 |
| 使用一次 | 使用物品/交互一次 |
| 持续跳跃 | 持续跳跃 |
| 潜行 | 开始潜行 |
| 取消潜行 | 停止潜行 |
| 疾跑 | 开始疾跑 |
| 停止疾跑 | 停止疾跑 |
| 丢弃 | 丢弃手持物品（单个） |
| 丢弃整组 | 丢弃手持物品（整组） |

---

### 配置文件

所有配置存储在 `<minecraft>/config/command-gui/` 目录下：

| 文件 | 说明 |
|------|------|
| `presets/custom.json` | 你的自定义指令和分类 |
| `settings.json` | 标签页显示设置 |
| `presets/vanilla.json` | 原版预设指令（自动生成） |
| `presets/carpet.json` | Carpet 预设指令（自动生成） |

预设文件（`vanilla.json`、`carpet.json`）在首次加载时从模组内置资源复制，可以自定义修改。

---

### 开发

#### 环境要求

- JDK 21+
- Gradle（已包含 Wrapper）

#### 构建

```bash
git clone https://github.com/remrin/command-gui
cd command-gui
./gradlew build
```

输出的 `.jar` 文件位于 `build/libs/`。

#### 项目结构

```
src/
├── main/java/com/remrin/          # 服务端入口（功能极少）
├── main/resources/
│   └── assets/command-gui/
│       ├── lang/                  # en_us.json, zh_cn.json
│       └── presets/               # vanilla.json, carpet.json
└── client/java/com/remrin/client/
    ├── CommandGUIClient.java      # 客户端入口、快捷键、Tick 事件
    ├── config/                    # CommandConfig, PresetConfig, SettingsConfig
    └── gui/                       # 所有 GUI 界面和标签页
```

#### 添加自定义预设标签页

1. 在 `src/main/resources/assets/command-gui/presets/` 中创建 JSON 文件（参考 `vanilla.json` 格式）
2. 在 `en_us.json` 和 `zh_cn.json` 中添加翻译键
3. 预设将自动加载并显示为新标签页

#### 预设 JSON 格式

```json
{
  "id": "my_preset",
  "nameKey": "screen.command-gui.tab.my_preset",
  "groups": [
    {
      "nameKey": "screen.command-gui.my_preset.group1",
      "commands": [
        {
          "nameKey": "screen.command-gui.my_preset.cmd1",
          "command": "/my command {number}",
          "description": "screen.command-gui.my_preset.cmd1.desc",
          "minValue": 1,
          "maxValue": 100,
          "quickValues": [1, 10, 50, 100]
        }
      ]
    }
  ]
}
```

每条指令支持的可选字段：
- `minValue` / `maxValue` – `{number}` 占位符的数值范围
- `quickValues` – `{number}` 的快速选择按钮（`int` 数组）
- `quickStrValues` – `{time}` 的快速选择值（`String` 数组）

---

### 许可证

MIT
