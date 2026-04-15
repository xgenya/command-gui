# Placeholders

[English](#english) | [中文](#中文)

---

## English

**Placeholders** are special tokens you embed in a command string. When you click the command button, the mod detects placeholders and presents the appropriate input screen for each one — in order — before the command is finally executed.

### How Placeholders Work

1. You save a command like `/tp @s {player}`
2. When you click the button, a **Player Selector** screen appears
3. You pick a player (e.g. `Alex`)
4. The mod replaces `{player}` with `Alex` and sends `/tp @s Alex`

Multiple placeholders in a single command are resolved **sequentially** — each input screen appears one after the other.

### Placeholder Reference

#### `{player_all}` — All Players

Opens a player-selector screen listing **all online players** (including yourself).

| | |
|--|--|
| **Input Screen** | Player list |
| **Selection** | One player at a time |
| **Example** | `/tp {player_all} ~ ~ ~` |
| **Replaces** | First occurrence of `{player_all}` |

---

#### `{player}` — Other Players

Opens a player-selector screen listing **all online players except yourself**.

| | |
|--|--|
| **Input Screen** | Player list (self excluded) |
| **Example** | `/tp @s {player}` (teleport to another player) |
| **Replaces** | First occurrence of `{player}` |

---

#### `{player_fake}` — Fake Players Only

Opens a player-selector screen listing **only Carpet fake players** (bots).

| | |
|--|--|
| **Input Screen** | Player list (fake players only) |
| **Example** | `/player {player_fake} kill` |
| **Replaces** | First occurrence of `{player_fake}` |

> **Note:** Requires Carpet Mod and at least one active fake player.

---

#### `{name}` — Free Text

Opens a text input screen where you can type any string.

| | |
|--|--|
| **Input Screen** | Single text field |
| **Default hint** | `Steve` |
| **Example** | `/player {name} spawn` |
| **Replaces** | First occurrence of `{name}` |

---

#### `{number}` — Numeric Input

Opens a number input screen.

| | |
|--|--|
| **Input Screen** | Number field with optional quick-select buttons |
| **Example** | `/tick rate {number}` |
| **Replaces** | First occurrence of `{number}` |

For preset commands, the JSON can specify `minValue`, `maxValue`, and `quickValues` to control the range and quick buttons. Custom commands use the default range with no quick buttons.

**Entering the value:**
- Type a number and press **Enter** to confirm
- Click one of the quick-select buttons (if available) to instantly pick a value

---

#### `{time}` — Time Input

Opens a time input screen that supports Minecraft time units.

| | |
|--|--|
| **Input Screen** | Time field with optional quick-select buttons |
| **Supported units** | `t` (ticks), `s` (seconds), `d` (game days) |
| **Example** | `/tick step {time}` → type `5s` for 5 seconds |
| **Replaces** | First occurrence of `{time}` |

For preset commands, `quickStrValues` in the JSON provides preset time values as quick buttons.

**Examples of valid time values:**
| Input | Meaning |
|-------|---------|
| `20t` | 20 ticks (1 second) |
| `5s` | 5 seconds |
| `1d` | 1 game day (24000 ticks) |
| `100` | 100 ticks (no unit = ticks) |

---

#### `{coords}` — Coordinate Input (Combined)

Opens a coordinate input screen with **X**, **Y**, and **Z** fields. The three values are concatenated with spaces and replace the single `{coords}` token.

| | |
|--|--|
| **Input Screen** | Three number fields (X, Y, Z) |
| **Example** | `/tp @s {coords}` → fills as `/tp @s 100 64 200` |
| **Replaces** | `{coords}` with `"X Y Z"` |

**Convenience buttons:**
- **Current Position** — fills X/Y/Z with your current floating-point position
- **Current Block** — fills X/Y/Z with your current block position (integer)

---

#### `{x}`, `{y}`, `{z}` — Coordinate Input (Separate)

Same coordinate input screen as `{coords}`, but each variable is replaced individually.

| | |
|--|--|
| **Input Screen** | Three number fields (X, Y, Z) — one screen for all three |
| **Example** | `/setblock {x} {y} {z} minecraft:stone` |
| **Replaces** | `{x}` → X value, `{y}` → Y value, `{z}` → Z value |

> **Note:** `{coords}` and `{x}/{y}/{z}` trigger the same input screen. If a command contains both styles, only **one** screen is shown and both styles are replaced simultaneously.

---

### Using Multiple Placeholders

A command can contain multiple different placeholders. They are resolved **left to right**:

```
/tp {player_all} {coords}
```

1. Player selector screen appears → pick `Alex`
2. Coordinate input screen appears → enter `100 64 200`
3. Final command: `/tp Alex 100 64 200`

---

### Inserting Placeholders When Editing

In the Add/Edit Command screen, use the **Placeholder** selector row to insert a placeholder at the cursor position. The selector shows abbreviated type labels:

| Button | Full Name | Inserted Token |
|--------|-----------|---------------|
| All | All Players | `{player_all}` |
| Other | Other Players | `{player}` |
| Fake | Fake Player | `{player_fake}` |
| Text | Text Input | `{name}` |
| Num | Number Input | `{number}` |
| Time | Time Input | `{time}` |
| Coord | Coord Input | `{coords}` |

---

## 中文

**占位符**是嵌入指令字符串中的特殊标记。点击指令按钮时，模组会检测占位符，并按顺序逐一弹出对应的输入界面 — 最终再执行指令。

### 占位符的工作原理

1. 你保存了一条指令，如 `/tp @s {player}`
2. 点击按钮后，弹出**玩家选择**界面
3. 你选择一个玩家（例如 `Alex`）
4. 模组将 `{player}` 替换为 `Alex`，发送 `/tp @s Alex`

一条指令中的多个占位符会**从左到右依次**解析 — 每个输入界面依次弹出。

### 占位符参考

#### `{player_all}` — 所有玩家

打开玩家选择界面，列出**所有在线玩家**（包括自己）。

| | |
|--|--|
| **输入界面** | 玩家列表 |
| **选择** | 每次选择一个玩家 |
| **示例** | `/tp {player_all} ~ ~ ~` |
| **替换** | `{player_all}` 的第一个出现 |

---

#### `{player}` — 其他玩家

打开玩家选择界面，列出**除自己以外的所有在线玩家**。

| | |
|--|--|
| **输入界面** | 玩家列表（排除自己） |
| **示例** | `/tp @s {player}`（传送到另一个玩家） |
| **替换** | `{player}` 的第一个出现 |

---

#### `{player_fake}` — 仅假人

打开玩家选择界面，仅列出 **Carpet 假人**（机器人）。

| | |
|--|--|
| **输入界面** | 玩家列表（仅假人） |
| **示例** | `/player {player_fake} kill` |
| **替换** | `{player_fake}` 的第一个出现 |

> **注意：** 需要安装 Carpet Mod 且至少有一个活动的假人。

---

#### `{name}` — 自由文本

打开文本输入界面，可以输入任意字符串。

| | |
|--|--|
| **输入界面** | 单个文本字段 |
| **默认提示** | `Steve` |
| **示例** | `/player {name} spawn` |
| **替换** | `{name}` 的第一个出现 |

---

#### `{number}` — 数字输入

打开数字输入界面。

| | |
|--|--|
| **输入界面** | 数字字段，可选快速选择按钮 |
| **示例** | `/tick rate {number}` |
| **替换** | `{number}` 的第一个出现 |

对于预设指令，JSON 中可以指定 `minValue`、`maxValue` 和 `quickValues` 来控制范围和快速按钮。自定义指令使用默认范围，不带快速按钮。

**输入方式：**
- 输入数字并按 **Enter** 确认
- 点击快速选择按钮（如果有）可快速选择值

---

#### `{time}` — 时间输入

打开支持 Minecraft 时间单位的时间输入界面。

| | |
|--|--|
| **输入界面** | 时间字段，可选快速选择按钮 |
| **支持单位** | `t`（游戏刻）、`s`（秒）、`d`（游戏天） |
| **示例** | `/tick step {time}` → 输入 `5s` 表示 5 秒 |
| **替换** | `{time}` 的第一个出现 |

对于预设指令，JSON 中的 `quickStrValues` 提供预设时间值作为快速按钮。

**有效时间值示例：**
| 输入 | 含义 |
|------|------|
| `20t` | 20 游戏刻（1 秒） |
| `5s` | 5 秒 |
| `1d` | 1 游戏天（24000 游戏刻） |
| `100` | 100 游戏刻（无单位 = 游戏刻） |

---

#### `{coords}` — 坐标输入（合并形式）

打开坐标输入界面，包含 **X**、**Y**、**Z** 三个字段。三个值用空格拼接后替换单个 `{coords}` 标记。

| | |
|--|--|
| **输入界面** | 三个数字字段（X、Y、Z） |
| **示例** | `/tp @s {coords}` → 填充为 `/tp @s 100 64 200` |
| **替换** | `{coords}` 替换为 `"X Y Z"` |

**便捷按钮：**
- **当前位置** — 用你当前的浮点坐标填充 X/Y/Z
- **当前方块** — 用你当前的方块坐标（整数）填充 X/Y/Z

---

#### `{x}`、`{y}`、`{z}` — 坐标输入（独立形式）

与 `{coords}` 使用相同的坐标输入界面，但每个变量单独替换。

| | |
|--|--|
| **输入界面** | 三个数字字段（X、Y、Z）— 一个界面同时填写三个 |
| **示例** | `/setblock {x} {y} {z} minecraft:stone` |
| **替换** | `{x}` → X 值、`{y}` → Y 值、`{z}` → Z 值 |

> **注意：** `{coords}` 和 `{x}/{y}/{z}` 触发相同的输入界面。如果指令同时包含两种写法，只显示**一个**界面，两种写法同时被替换。

---

### 使用多个占位符

一条指令可以包含多个不同的占位符。它们从**左到右**依次解析：

```
/tp {player_all} {coords}
```

1. 弹出玩家选择界面 → 选择 `Alex`
2. 弹出坐标输入界面 → 输入 `100 64 200`
3. 最终指令：`/tp Alex 100 64 200`

---

### 编辑时插入占位符

在添加/编辑指令界面中，使用**占位符**选择器行在光标位置插入占位符。选择器显示缩写类型标签：

| 按钮 | 完整名称 | 插入标记 |
|------|----------|----------|
| 所有 | 所有玩家 | `{player_all}` |
| 其他 | 其他玩家 | `{player}` |
| 假人 | 假人 | `{player_fake}` |
| 文本 | 文本输入 | `{name}` |
| 数字 | 数字输入 | `{number}` |
| 时间 | 时间输入 | `{time}` |
| 坐标 | 坐标输入 | `{coords}` |
