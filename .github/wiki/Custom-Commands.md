# Custom Commands

[English](#english) | [中文](#中文)

---

## English

The **Custom** tab is where you manage all of your personal commands.

### Overview

Commands are organized into **categories** (shown as column tabs on the left side of the command list). Click a category to filter commands, or click **All** to see everything.

### Adding a Command

1. Click the **`+`** button in the footer
2. The **Add Command** screen opens. Choose between:
   - **Regular command** — for any `/command` or free text
   - **Add Fake Player Command** — opens a specialized visual builder (see [Fake Player Commands](#fake-player-commands))
3. Fill in the fields:
   - **Name** — a short label shown on the button (required)
   - **Command** — the full command to send, e.g. `/gamemode creative` (required)
   - **Description** — optional tooltip text shown on hover
4. To add **multiple commands** in one entry (chain), click **"Add to List"** to add more command lines. All commands execute sequentially when the button is clicked.
5. Use the **Placeholder** selector to insert a placeholder token into the command field.
6. Click **Save**

> **Tip:** Commands that start with `/` are sent as commands. Commands that do **not** start with `/` are sent as chat messages.

### Editing a Command

Each command button shows three small icon buttons to its right:

| Button | Icon | Action |
|--------|------|--------|
| Edit | 📖 (Writable Book) | Open the Edit screen for this command |
| Delete | 🪣 (Lava Bucket) | Permanently delete this command |
| Move | 📦 (Shulker Box) | Move to a different category |

Alternatively, **right-click** a command button to access edit/delete options.

### Searching Commands

Type in the **Search** box in the footer to filter commands in the Custom tab:
- Filters by command **name**
- Filters by **description**
- Filters by the raw **command text** of any command in the entry

### Category Management

#### Viewing Categories

Categories appear as buttons on the left side of the command list:
- **All** — shows commands from all categories
- Individual category buttons — filter to that category only
- **`+`** — create a new category

#### Adding a Category

1. Click the **`+`** button after the last category button
2. Enter a category name
3. Click **Save**

The new category appears in the category list. By default, new commands are added to the last selected category.

#### Deleting a Category

Currently, categories other than **Default** can be removed only by manually editing the config file (`config/command-gui/presets/custom.json`). The **Default** category cannot be removed.

#### Moving Commands Between Categories

1. Click the **📦 Move** button on a command entry
2. A screen lists all available categories
3. Click the target category to move the command

### Fake Player Commands

The **Add Fake Player Command** builder provides a visual form for constructing `player … spawn` commands with optional configuration:

| Field | Description |
|-------|-------------|
| **Player Name** | The name for the fake player |
| **Spawn At** | Current position, or custom XYZ coordinates |
| **Dimension** | Overworld / Nether / The End |
| **Gamemode** | Survival / Creative / Adventure / Spectator |
| **Actions** | Actions to execute immediately after spawning (attack, use, jump, etc.) |
| **Config Commands** | Extra commands to run after the fake player spawns (e.g. `/player {name} equip`) |

The screen shows a **Command Preview** at the bottom displaying exactly what will be saved.

### Multi-Command Entries

A single command entry can contain **multiple commands** executed in order:

1. In the Add/Edit screen, enter the first command
2. Click **"Add to List"** to add another line
3. Repeat as needed
4. Use **✗** to remove a line

When the entry button is clicked:
- Commands execute one after another
- If the first command is a fake player spawn (`/player … spawn …`), subsequent commands are automatically delayed by ~1 second to ensure the bot is fully loaded before running actions

---

## 中文

**自定义**标签页是管理所有个人指令的地方。

### 概览

指令按**分类**整理（显示为指令列表左侧的列标签）。点击分类筛选指令，或点击**全部**查看所有指令。

### 添加指令

1. 点击底栏的 **`+`** 按钮
2. **添加指令**界面打开，可选择：
   - **普通指令** — 适用于任何 `/指令` 或自由文本
   - **添加假人指令** — 打开专用的可视化构建器（参见[假人指令](#假人指令)）
3. 填写字段：
   - **名称** — 显示在按钮上的简短标签（必填）
   - **指令** — 要发送的完整指令，例如 `/gamemode creative`（必填）
   - **描述** — 可选的悬停提示文字
4. 若要在一个条目中添加**多条指令**（链式执行），点击**"添加到列表"**追加更多指令行。点击按钮时所有指令按顺序执行。
5. 使用**占位符**选择器将占位符标记插入指令字段。
6. 点击**保存**

> **提示：** 以 `/` 开头的指令作为命令发送。**不以** `/` 开头的指令作为聊天消息发送。

### 编辑指令

每个指令按钮右侧有三个小图标按钮：

| 按钮 | 图标 | 操作 |
|------|------|------|
| 编辑 | 📖（可写书） | 打开该指令的编辑界面 |
| 删除 | 🪣（熔岩桶） | 永久删除该指令 |
| 移动 | 📦（潜影盒） | 移动到其他分类 |

也可以**右键单击**指令按钮访问编辑/删除选项。

### 搜索指令

在底栏的**搜索**框中输入文字，以筛选自定义标签页中的指令：
- 按指令**名称**筛选
- 按**描述**筛选
- 按条目中任意指令的原始**指令文本**筛选

### 分类管理

#### 查看分类

分类显示为指令列表左侧的按钮：
- **全部** — 显示所有分类的指令
- 各分类按钮 — 仅显示该分类的指令
- **`+`** — 创建新分类

#### 添加分类

1. 点击最后一个分类按钮后面的 **`+`** 按钮
2. 输入分类名称
3. 点击**保存**

新分类出现在分类列表中。默认情况下，新指令被添加到最后选中的分类。

#### 删除分类

目前，除**默认**分类外，其他分类只能通过手动编辑配置文件（`config/command-gui/presets/custom.json`）来删除。**默认**分类不能被删除。

#### 在分类间移动指令

1. 点击指令条目上的 **📦 移动**按钮
2. 界面列出所有可用分类
3. 点击目标分类以移动指令

### 假人指令

**添加假人指令**构建器提供了可视化表单，用于构建带有可选配置的 `player … spawn` 指令：

| 字段 | 说明 |
|------|------|
| **假人名称** | 假人的名称 |
| **生成在** | 当前位置，或自定义 XYZ 坐标 |
| **维度** | 主世界 / 下界 / 末地 |
| **游戏模式** | 生存 / 创造 / 冒险 / 旁观 |
| **动作** | 生成后立即执行的动作（攻击、使用、跳跃等） |
| **配置指令** | 假人生成后执行的额外指令（例如 `/player {name} equip`） |

界面底部显示**命令预览**，展示将被保存的完整指令。

### 多指令条目

单个指令条目可以包含**多条按顺序执行的指令**：

1. 在添加/编辑界面中输入第一条指令
2. 点击**"添加到列表"**追加更多行
3. 根据需要重复
4. 使用 **✗** 删除某行

点击条目按钮时：
- 指令依次执行
- 如果第一条指令是假人生成指令（`/player … spawn …`），后续指令会自动延迟约 1 秒，以确保机器人完全加载后再执行动作
