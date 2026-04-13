# Configuration Files

[English](#english) | [中文](#中文)

---

## English

All configuration files are stored in:

```
<minecraft directory>/config/command-gui/
```

### File Overview

| File | Created By | Purpose |
|------|-----------|---------|
| `settings.json` | Auto on first launch | Tab visibility settings |
| `presets/custom.json` | Auto on first save | Your custom commands and categories |
| `presets/vanilla.json` | Copied from mod on first launch | Vanilla preset commands (editable) |
| `presets/carpet.json` | Copied from mod on first launch | Carpet preset commands (editable) |

---

### `settings.json`

Stores the three boolean settings from the Settings screen.

**Default content:**
```json
{
  "show_vanilla_commands": true,
  "show_carpet_commands": true,
  "show_fakeplayer_tab": true
}
```

| Key | Type | Description |
|-----|------|-------------|
| `show_vanilla_commands` | boolean | Whether the Vanilla tab is shown |
| `show_carpet_commands` | boolean | Whether the Carpet tab is shown |
| `show_fakeplayer_tab` | boolean | Whether the Fake Player tab is shown |

---

### `presets/custom.json`

Stores all your custom commands and their categories.

**Example structure:**
```json
{
  "categories": [
    {
      "id": "default",
      "nameKey": "screen.command-gui.category.default",
      "commands": {
        "Creative Mode": {
          "command": "/gamemode creative",
          "description": "Switch to creative"
        },
        "Teleport to Player": {
          "command": "/tp @s {player}",
          "description": "Teleport to another player"
        },
        "Multi-step": {
          "commands": [
            "/gamemode creative",
            "/effect give @s minecraft:night_vision infinite 1"
          ],
          "description": "Creative + night vision"
        }
      }
    },
    {
      "id": "farming",
      "nameKey": "",
      "displayName": "Farming",
      "commands": {
        "Time Day": {
          "command": "/time set day",
          "description": ""
        }
      }
    }
  ]
}
```

#### Category Object

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | Unique category identifier (do not use `default`) |
| `nameKey` | string | Translation key for the category name (leave empty for custom display names) |
| `displayName` | string | Direct display name (overrides `nameKey` if set) |
| `commands` | object | Map of command name → CommandEntry |

#### CommandEntry Object

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `command` | string | One of `command`/`commands` | Single command string |
| `commands` | array of strings | One of `command`/`commands` | Multiple commands (executed in sequence) |
| `description` | string | No | Tooltip description shown on hover |

---

### `presets/vanilla.json` and `presets/carpet.json`

These files are copied from the mod's built-in resources on first launch. You can edit them to customize the preset tabs, or add new groups and commands.

**Schema:**
```json
{
  "id": "vanilla",
  "nameKey": "screen.command-gui.tab.vanilla",
  "groups": [
    {
      "nameKey": "screen.command-gui.vanilla.gamemode",
      "commands": [
        {
          "nameKey": "screen.command-gui.vanilla.gamemode.creative",
          "command": "/gamemode creative",
          "description": "screen.command-gui.vanilla.gamemode.creative.desc"
        },
        {
          "nameKey": "screen.command-gui.vanilla.tick.rate",
          "command": "/tick rate {number}",
          "description": "screen.command-gui.vanilla.tick.rate.desc",
          "minValue": 1,
          "maxValue": 10000,
          "quickValues": [1, 5, 10, 20, 40, 60, 100, 200]
        },
        {
          "nameKey": "screen.command-gui.vanilla.tick.step",
          "command": "/tick step {time}",
          "description": "screen.command-gui.vanilla.tick.step.desc",
          "quickStrValues": ["1t", "5t", "10t", "20t", "1s", "5s", "10s", "30s", "1d"]
        }
      ]
    }
  ]
}
```

#### Preset Object

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | Unique preset identifier (used for lookup) |
| `nameKey` | string | Translation key for the tab name |
| `groups` | array | List of command groups |

#### CommandGroup Object

| Field | Type | Description |
|-------|------|-------------|
| `nameKey` | string | Translation key for the group heading |
| `commands` | array | List of preset commands |

#### PresetCommand Object

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `nameKey` | string | Yes | Translation key for the button label |
| `command` | string | Yes | Command string (may contain placeholders) |
| `description` | string | No | Translation key for the tooltip |
| `minValue` | integer | No | Min value for `{number}` placeholder |
| `maxValue` | integer | No | Max value for `{number}` placeholder |
| `quickValues` | int[] | No | Quick-select integers for `{number}` |
| `quickStrValues` | string[] | No | Quick-select strings for `{time}` |

---

### Adding a Custom Preset File

You can create additional preset JSON files in the `presets/` folder. Place the file at:

```
config/command-gui/presets/my_preset.json
```

The file is loaded automatically on next game launch (or resource pack reload). The `id` field in the JSON is used to identify the preset — make sure it is unique.

> **Tip:** Preset display names require translation keys in a resource pack. Alternatively, use Minecraft's plain-text component approach and add the display text directly as the `nameKey` value — but this only works for custom user files, not the in-game translation system.

---

## 中文

所有配置文件存储在：

```
<Minecraft 目录>/config/command-gui/
```

### 文件概览

| 文件 | 创建方式 | 用途 |
|------|----------|------|
| `settings.json` | 首次启动时自动创建 | 标签页显示设置 |
| `presets/custom.json` | 首次保存时自动创建 | 你的自定义指令和分类 |
| `presets/vanilla.json` | 首次启动时从模组复制 | 原版预设指令（可编辑） |
| `presets/carpet.json` | 首次启动时从模组复制 | Carpet 预设指令（可编辑） |

---

### `settings.json`

存储设置界面中的三个布尔值设置。

**默认内容：**
```json
{
  "show_vanilla_commands": true,
  "show_carpet_commands": true,
  "show_fakeplayer_tab": true
}
```

| 键 | 类型 | 说明 |
|----|------|------|
| `show_vanilla_commands` | boolean | 是否显示原版标签页 |
| `show_carpet_commands` | boolean | 是否显示 Carpet 标签页 |
| `show_fakeplayer_tab` | boolean | 是否显示假人控制标签页 |

---

### `presets/custom.json`

存储所有自定义指令及其分类。

**示例结构：**
```json
{
  "categories": [
    {
      "id": "default",
      "nameKey": "screen.command-gui.category.default",
      "commands": {
        "创造模式": {
          "command": "/gamemode creative",
          "description": "切换到创造模式"
        },
        "传送到玩家": {
          "command": "/tp @s {player}",
          "description": "传送到另一个玩家"
        },
        "多步骤指令": {
          "commands": [
            "/gamemode creative",
            "/effect give @s minecraft:night_vision infinite 1"
          ],
          "description": "创造模式 + 夜视"
        }
      }
    },
    {
      "id": "farming",
      "nameKey": "",
      "displayName": "农业",
      "commands": {
        "设为白天": {
          "command": "/time set day",
          "description": ""
        }
      }
    }
  ]
}
```

#### 分类对象

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | string | 唯一分类标识符（不要使用 `default`） |
| `nameKey` | string | 分类名称的翻译键（自定义显示名时可留空） |
| `displayName` | string | 直接显示名称（设置后覆盖 `nameKey`） |
| `commands` | object | 指令名称 → CommandEntry 的映射 |

#### CommandEntry 对象

| 字段 | 类型 | 是否必须 | 说明 |
|------|------|----------|------|
| `command` | string | `command`/`commands` 二选一 | 单条指令字符串 |
| `commands` | 字符串数组 | `command`/`commands` 二选一 | 多条指令（按顺序执行） |
| `description` | string | 否 | 悬停时显示的提示说明 |

---

### `presets/vanilla.json` 和 `presets/carpet.json`

这些文件在首次启动时从模组内置资源复制。可以编辑它们以自定义预设标签页，或添加新的组和指令。

**数据结构：**
```json
{
  "id": "vanilla",
  "nameKey": "screen.command-gui.tab.vanilla",
  "groups": [
    {
      "nameKey": "screen.command-gui.vanilla.gamemode",
      "commands": [
        {
          "nameKey": "screen.command-gui.vanilla.gamemode.creative",
          "command": "/gamemode creative",
          "description": "screen.command-gui.vanilla.gamemode.creative.desc"
        },
        {
          "nameKey": "screen.command-gui.vanilla.tick.rate",
          "command": "/tick rate {number}",
          "description": "screen.command-gui.vanilla.tick.rate.desc",
          "minValue": 1,
          "maxValue": 10000,
          "quickValues": [1, 5, 10, 20, 40, 60, 100, 200]
        }
      ]
    }
  ]
}
```

#### Preset 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | string | 唯一预设标识符（用于查找） |
| `nameKey` | string | 标签页名称的翻译键 |
| `groups` | 数组 | 指令组列表 |

#### CommandGroup 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| `nameKey` | string | 组标题的翻译键 |
| `commands` | 数组 | 预设指令列表 |

#### PresetCommand 对象

| 字段 | 类型 | 是否必须 | 说明 |
|------|------|----------|------|
| `nameKey` | string | 是 | 按钮标签的翻译键 |
| `command` | string | 是 | 指令字符串（可包含占位符） |
| `description` | string | 否 | 提示文字的翻译键 |
| `minValue` | integer | 否 | `{number}` 占位符的最小值 |
| `maxValue` | integer | 否 | `{number}` 占位符的最大值 |
| `quickValues` | int[] | 否 | `{number}` 的快速选择整数 |
| `quickStrValues` | string[] | 否 | `{time}` 的快速选择字符串 |

---

### 添加自定义预设文件

可以在 `presets/` 文件夹中创建额外的预设 JSON 文件。将文件放在：

```
config/command-gui/presets/my_preset.json
```

文件将在下次游戏启动（或资源包重载）时自动加载。JSON 中的 `id` 字段用于标识预设 — 确保它是唯一的。

> **提示：** 预设显示名称需要在资源包中设置翻译键。作为替代方案，可以将显示文本直接作为 `nameKey` 的值 — 但这只适用于用户自定义文件，而非游戏内翻译系统。
