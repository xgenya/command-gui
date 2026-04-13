# Fake Player Management

[English](#english) | [中文](#中文)

---

## English

The **Fake Player** tab provides a dedicated management panel for [Carpet Mod](https://github.com/gnembon/fabric-carpet) fake players (bots). It is only shown when:
- Carpet Mod is present on the server, **and**
- The tab is enabled in Settings (default: enabled)

### Player List

The left panel shows all current fake players and pending timed tasks in a scrollable list.

#### List Entry States

| Icon / Color | Meaning |
|-------------|---------|
| Player face icon | Active fake player |
| **`+`** icon + green countdown | Pending timed-spawn task |
| Red countdown on name | Active timed-kill task |

- Click an **active fake player** to select it and reveal the action buttons
- Click a **pending spawn** entry to cancel the timed spawn
- Click the **countdown** on a selected fake player to cancel the timed kill

### Batch Spawn

Click the **Batch Spawn** button to open the Batch Spawn screen, which lets you spawn multiple fake players with one action.

#### Naming Modes

**Prefix + Number mode** (default):
- Configure a **Prefix** (e.g. `Bot_`) and a **Start Number** (e.g. `1`)
- Set the **Count** (1–50)
- Spawns: `Bot_1`, `Bot_2`, `Bot_3`, …

**English Names mode**:
- Toggle the **Type** button to switch to English Names
- Set the **Count** (1–26)
- Spawns: `Alex`, `Ben`, `Carl`, `David`, … (in alphabetical order)

A **Preview** at the bottom shows the first and last names that will be spawned.

Click **Spawn** to execute all spawn commands.

### Kill All

Click the **Kill All** button to remove all fake players at once.

> ⚠ **Safety:** The button requires a **Shift + Click** to confirm. On first click (without Shift), the button label changes to show a confirmation prompt. Shift+click again to proceed.

### Timed Spawn

Click **Timed Add** to schedule a fake player to spawn after a delay.

**Setup:**
1. Enter the **Player Name**
2. Select the spawn **Position**:
   - **At Current Position** — spawns at your current coordinates when the task executes
   - **At Custom Coords** — enter specific XYZ coordinates
3. Set the **Delay Time** in hours, minutes, and seconds
4. Click **Confirm**

**While waiting:**
- The player appears in the list with a **`+`** icon and a green countdown timer
- Click the entry at any time to **cancel** the task before it executes

**Execution:**
- When the countdown reaches zero, `/player <name> spawn [at X Y Z]` is sent automatically

### Timed Kill

Select an active fake player in the list, then click **Timed Kill** to schedule its removal.

**Setup:**
1. The **Player Name** is pre-filled with the selected player
2. Set the **Delay Time** in hours, minutes, and seconds
3. Click **Confirm**

**While waiting:**
- The fake player entry shows a **red countdown** timer
- Click **"Cancel Timer"** on the entry to abort the task

**Execution:**
- When the countdown reaches zero, `/player <name> kill` is sent automatically

### Individual Actions

After selecting a fake player from the list, action buttons appear on the right.

#### Basic

| Button | Command Sent | Description |
|--------|-------------|-------------|
| **Stop** | `player <name> stop` | Stop the current action |
| **Kill** | `player <name> kill` | Remove the fake player permanently |

#### Combat Actions

| Button | Command Sent | Description |
|--------|-------------|-------------|
| **Attack** | `player <name> attack continuous` | Attack repeatedly until stopped |
| **Attack Once** | `player <name> attack once` | Perform a single attack |
| **Use** | `player <name> use continuous` | Use item / interact repeatedly |
| **Use Once** | `player <name> use once` | Use item / interact once |

#### Movement Actions

| Button | Command Sent | Description |
|--------|-------------|-------------|
| **Jump** | `player <name> jump continuous` | Jump repeatedly |
| **Sneak** | `player <name> sneak` | Start sneaking |
| **Unsneak** | `player <name> unsneak` | Stop sneaking |
| **Sprint** | `player <name> sprint` | Start sprinting |
| **Stop Sprint** | `player <name> unsprint` | Stop sprinting |

#### Item Actions

| Button | Command Sent | Description |
|--------|-------------|-------------|
| **Drop** | `player <name> drop` | Drop the currently held item (one at a time) |
| **Drop Stack** | `player <name> dropStack` | Drop the entire held stack |

---

## 中文

**假人控制**标签页提供了专用的 [Carpet Mod](https://github.com/gnembon/fabric-carpet) 假人（机器人）管理面板。只有在以下条件满足时才会显示：
- 服务器上安装了 Carpet Mod，**且**
- 在设置中启用了该标签页（默认：启用）

### 假人列表

左侧面板在可滚动列表中显示所有当前假人和待执行的定时任务。

#### 列表条目状态

| 图标 / 颜色 | 含义 |
|------------|------|
| 玩家头像图标 | 活跃的假人 |
| **`+`** 图标 + 绿色倒计时 | 待执行的定时生成任务 |
| 名称上的红色倒计时 | 活跃的定时移除任务 |

- 点击**活跃假人**可选中它并显示操作按钮
- 点击**待生成**条目可取消定时生成任务
- 点击选中假人上的**倒计时**可取消定时移除任务

### 批量生成

点击**批量生成**按钮打开批量生成界面，可以一次生成多个假人。

#### 命名模式

**前缀+编号模式**（默认）：
- 配置**前缀**（例如 `Bot_`）和**起始编号**（例如 `1`）
- 设置**数量**（1–50）
- 生成：`Bot_1`、`Bot_2`、`Bot_3`……

**英文名模式**：
- 切换**类型**按钮切换到英文名模式
- 设置**数量**（1–26）
- 生成：`Alex`、`Ben`、`Carl`、`David`……（按字母顺序）

底部的**预览**显示将要生成的第一个和最后一个名字。

点击**生成**执行所有生成指令。

### 移除全部

点击**移除全部**按钮一次移除所有假人。

> ⚠ **安全确认：** 该按钮需要 **Shift + 点击**才能确认。首次点击（不按 Shift）时，按钮标签会变为确认提示。再次 Shift+点击以继续。

### 定时添加

点击**定时添加**预约在延迟后生成假人。

**设置：**
1. 输入**假人名称**
2. 选择生成**位置**：
   - **在当前位置** — 任务执行时在你当前的坐标生成
   - **在指定坐标** — 输入特定的 XYZ 坐标
3. 设置延迟时间（时、分、秒）
4. 点击**确认**

**等待中：**
- 假人显示在列表中，带有 **`+`** 图标和绿色倒计时
- 在任意时间点击条目可**取消**任务（在执行前）

**执行时：**
- 倒计时归零时，自动发送 `/player <名称> spawn [at X Y Z]`

### 定时移除

在列表中选中一个活跃假人，然后点击**定时移除**以预约其移除。

**设置：**
1. **假人名称**已预填为选中的假人
2. 设置延迟时间（时、分、秒）
3. 点击**确认**

**等待中：**
- 假人条目显示**红色倒计时**
- 点击条目上的**"取消定时"**可中止任务

**执行时：**
- 倒计时归零时，自动发送 `/player <名称> kill`

### 单个假人操作

在列表中选中假人后，右侧会显示操作按钮。

#### 基本操作

| 按钮 | 发送的指令 | 说明 |
|------|------------|------|
| **停止** | `player <名称> stop` | 停止当前动作 |
| **移除** | `player <名称> kill` | 永久移除假人 |

#### 战斗动作

| 按钮 | 发送的指令 | 说明 |
|------|------------|------|
| **持续攻击** | `player <名称> attack continuous` | 持续攻击直到停止 |
| **攻击一次** | `player <名称> attack once` | 执行单次攻击 |
| **持续使用** | `player <名称> use continuous` | 持续使用物品/交互 |
| **使用一次** | `player <名称> use once` | 使用物品/交互一次 |

#### 移动动作

| 按钮 | 发送的指令 | 说明 |
|------|------------|------|
| **持续跳跃** | `player <名称> jump continuous` | 持续跳跃 |
| **潜行** | `player <名称> sneak` | 开始潜行 |
| **取消潜行** | `player <名称> unsneak` | 停止潜行 |
| **疾跑** | `player <名称> sprint` | 开始疾跑 |
| **停止疾跑** | `player <名称> unsprint` | 停止疾跑 |

#### 物品动作

| 按钮 | 发送的指令 | 说明 |
|------|------------|------|
| **丢弃** | `player <名称> drop` | 丢弃当前持有的物品（单个） |
| **丢弃整组** | `player <名称> dropStack` | 丢弃整组持有物品 |
