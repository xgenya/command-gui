# Development Guide

[English](#english) | [中文](#中文)

---

## English

This guide covers building the mod from source, understanding the project structure, and extending it with new features.

### Prerequisites

| Tool | Version |
|------|---------|
| JDK | 21+ |
| Git | any |
| Internet access | Required on first build (to download Gradle dependencies) |

Gradle is included as a wrapper (`gradlew` / `gradlew.bat`) — no separate installation needed.

### Cloning and Building

```bash
# Clone
git clone https://github.com/remrin/command-gui
cd command-gui

# Build (first run downloads ~500 MB of dependencies)
./gradlew build

# Output
ls build/libs/
# command-gui-<version>.jar
# command-gui-<version>-sources.jar
```

The `build/libs/` directory contains the final mod `.jar`.

### Running in Development

```bash
# Launch the Minecraft client with the mod loaded
./gradlew runClient
```

This opens a development Minecraft instance with Command-GUI loaded. All changes are hot-reloaded when you rebuild.

### Project Structure

```
command-gui/
├── build.gradle               # Build configuration
├── gradle.properties          # Mod version, MC version, Fabric API version
├── settings.gradle            # Project settings
│
└── src/
    ├── main/
    │   ├── java/com/remrin/
    │   │   ├── CommandGUI.java              # Server-side mod initializer (logging only)
    │   │   └── mixin/                       # Server-side mixins (if any)
    │   └── resources/
    │       ├── fabric.mod.json              # Mod metadata
    │       └── assets/command-gui/
    │           ├── icon.png                 # Mod icon
    │           ├── lang/
    │           │   ├── en_us.json           # English translations
    │           │   └── zh_cn.json           # Simplified Chinese translations
    │           └── presets/
    │               ├── vanilla.json         # Built-in Vanilla preset
    │               └── carpet.json          # Built-in Carpet preset
    │
    └── client/
        ├── java/com/remrin/client/
        │   ├── CommandGUIClient.java         # Client initializer, keybind, tick loop
        │   ├── config/
        │   │   ├── CommandConfig.java        # Custom command storage & CRUD
        │   │   ├── PresetConfig.java         # Preset loading from JSON files
        │   │   └── SettingsConfig.java       # Settings (show_vanilla, etc.)
        │   └── gui/
        │       ├── CommandGUIScreen.java      # Main screen (tabs, search, footer)
        │       ├── AbstractCommandTab.java    # Base class for command list tabs
        │       ├── CustomCommandTab.java      # Custom tab implementation
        │       ├── FakePlayerTab.java         # Fake Player tab
        │       ├── PresetCommandTab.java      # Vanilla / Carpet tab
        │       ├── SettingsScreen.java        # Settings popup
        │       ├── AddCommandScreen.java      # Add command dialog
        │       ├── EditCommandScreen.java     # Edit command dialog
        │       ├── AddFakePlayerCommandScreen.java  # Visual fake player command builder
        │       ├── AddCategoryScreen.java     # Add category dialog
        │       ├── MoveCategoryScreen.java    # Move command to category
        │       ├── BatchSpawnScreen.java      # Batch spawn setup
        │       ├── TimedSpawnSetupScreen.java # Timed spawn setup
        │       ├── TimedKillSetupScreen.java  # Timed kill setup
        │       ├── TimedTaskManager.java      # In-memory timed task queue
        │       ├── ChainedCommandExecutor.java # Placeholder resolution + execution
        │       ├── VanillaCommands.java       # Adapter: PresetConfig → VanillaCommand
        │       ├── PlayerSelectorScreen.java  # Player picker (all/other/fake)
        │       ├── NumberInputScreen.java     # Number input popup
        │       ├── TextInputScreen.java       # Text input popup
        │       ├── TimeInputScreen.java       # Time input popup
        │       ├── CoordinateInputScreen.java # XYZ coordinate input
        │       ├── BaseCommandEditorScreen.java  # Shared base for Add/Edit screens
        │       ├── BaseParentedScreen.java    # Base screen with parent reference
        │       ├── SettingsButton.java        # Gear icon button widget
        │       └── ItemIconButton.java        # Icon button using MC item renderer
        └── resources/
            └── command-gui.client.mixins.json
```

### Key Classes

#### `CommandGUIClient`
The client entry point. Registers:
- The `C` keybind
- A `ClientTickEvents.END_CLIENT_TICK` handler that checks the keybind, ticks `TimedTaskManager`, and ticks `ChainedCommandExecutor`'s delayed queue

#### `CommandConfig`
Manages `config/command-gui/presets/custom.json`. All CRUD operations call `save()` immediately to persist data. Categories hold an ordered `LinkedHashMap<String, CommandEntry>` of commands.

#### `PresetConfig`
Loads preset JSON files from the config directory. On load, it first copies built-in presets from the mod's resource pack (if they don't already exist), then reads all `.json` files in the directory (excluding `custom.json`).

#### `ChainedCommandExecutor`
Parses a command string for placeholder tokens left-to-right, builds a queue of `PlaceholderType` values, then iterates through them — each iteration opens the appropriate input screen. When all placeholders are resolved, the final command is sent.

#### `TimedTaskManager`
A static list of `TimedTask` objects (type SPAWN or KILL, with a `remainingTicks` counter). `tick()` is called every client tick; when `remainingTicks` reaches zero the command is sent and the task removed.

---

### Adding a New Preset Tab

1. **Create the preset JSON** in `src/main/resources/assets/command-gui/presets/`:
   ```json
   {
     "id": "my_mod",
     "nameKey": "screen.command-gui.tab.my_mod",
     "groups": [
       {
         "nameKey": "screen.command-gui.my_mod.group1",
         "commands": [
           {
             "nameKey": "screen.command-gui.my_mod.cmd1",
             "command": "/mymod command {number}",
             "description": "screen.command-gui.my_mod.cmd1.desc",
             "minValue": 1,
             "maxValue": 100,
             "quickValues": [1, 10, 50, 100]
           }
         ]
       }
     ]
   }
   ```

2. **Add translation keys** to `src/main/resources/assets/command-gui/lang/en_us.json`:
   ```json
   "screen.command-gui.tab.my_mod": "My Mod",
   "screen.command-gui.my_mod.group1": "Group 1",
   "screen.command-gui.my_mod.cmd1": "My Command",
   "screen.command-gui.my_mod.cmd1.desc": "Runs my command"
   ```

3. **Add Chinese translations** to `zh_cn.json` (optional but recommended).

4. **Rebuild** — the preset is automatically loaded and displayed as a new tab (after the built-in tabs).

> The preset file is **always overwritten** on load if it exists in the mod's resources. To make user-editable presets, place the file directly in `config/command-gui/presets/` on the user's machine.

---

### Adding a New Placeholder Type

1. Add the new `PlaceholderType` enum value in `ChainedCommandExecutor`
2. Add detection logic in `parseTypes()` (scan for the token string)
3. Add a `case` in `showNextInput()` to open the appropriate input screen and call `start()` in the callback
4. Add the token string to `hasPlaceholders()` for quick detection
5. Add `hasPlaceholders()` in `CommandConfig.CommandEntry.hasPlaceholders()` if needed
6. Create a new `*InputScreen` if no existing screen fits

---

### Translations

Translation files are at:
- `src/main/resources/assets/command-gui/lang/en_us.json`
- `src/main/resources/assets/command-gui/lang/zh_cn.json`

To add a new language, create `<lang_code>.json` in the same directory and add all keys from `en_us.json`.

---

### Code Style

- Java 21, no preview features
- 1 tab = 1 tab character (not spaces)
- Class/method names follow standard Java conventions
- Screen classes extend `BaseParentedScreen<P>` or `Screen` directly
- No external dependencies beyond Fabric API and Minecraft itself

---

## 中文

本指南涵盖从源码构建模组、了解项目结构以及扩展新功能的内容。

### 前置条件

| 工具 | 版本 |
|------|------|
| JDK | 21+ |
| Git | 任意 |
| 网络访问 | 首次构建时必须（下载 Gradle 依赖） |

Gradle 以 Wrapper 形式包含（`gradlew` / `gradlew.bat`）— 无需单独安装。

### 克隆和构建

```bash
# 克隆仓库
git clone https://github.com/remrin/command-gui
cd command-gui

# 构建（首次运行会下载约 500 MB 的依赖）
./gradlew build

# 输出文件
ls build/libs/
# command-gui-<version>.jar
# command-gui-<version>-sources.jar
```

`build/libs/` 目录包含最终的模组 `.jar` 文件。

### 在开发环境中运行

```bash
# 启动加载了该模组的 Minecraft 客户端
./gradlew runClient
```

这会打开一个已加载 Command-GUI 的开发版 Minecraft 实例。每次重新构建后，更改会被热加载。

### 项目结构

```
command-gui/
├── build.gradle               # 构建配置
├── gradle.properties          # 模组版本、MC 版本、Fabric API 版本
├── settings.gradle            # 项目设置
│
└── src/
    ├── main/
    │   ├── java/com/remrin/
    │   │   ├── CommandGUI.java              # 服务端模组初始化器（仅日志）
    │   │   └── mixin/                       # 服务端 Mixin（如有）
    │   └── resources/
    │       ├── fabric.mod.json              # 模组元数据
    │       └── assets/command-gui/
    │           ├── icon.png                 # 模组图标
    │           ├── lang/
    │           │   ├── en_us.json           # 英文翻译
    │           │   └── zh_cn.json           # 简体中文翻译
    │           └── presets/
    │               ├── vanilla.json         # 内置原版预设
    │               └── carpet.json          # 内置 Carpet 预设
    │
    └── client/
        ├── java/com/remrin/client/
        │   ├── CommandGUIClient.java         # 客户端初始化器、快捷键、Tick 循环
        │   ├── config/
        │   │   ├── CommandConfig.java        # 自定义指令存储与增删改查
        │   │   ├── PresetConfig.java         # 从 JSON 文件加载预设
        │   │   └── SettingsConfig.java       # 设置（show_vanilla 等）
        │   └── gui/
        │       ├── CommandGUIScreen.java      # 主界面（标签页、搜索、底栏）
        │       ├── AbstractCommandTab.java    # 指令列表标签页的基类
        │       ├── CustomCommandTab.java      # 自定义标签页实现
        │       ├── FakePlayerTab.java         # 假人控制标签页
        │       ├── PresetCommandTab.java      # 原版/Carpet 标签页
        │       ├── SettingsScreen.java        # 设置弹出窗口
        │       ├── AddCommandScreen.java      # 添加指令对话框
        │       ├── EditCommandScreen.java     # 编辑指令对话框
        │       ├── AddFakePlayerCommandScreen.java  # 可视化假人指令构建器
        │       ├── AddCategoryScreen.java     # 添加分类对话框
        │       ├── MoveCategoryScreen.java    # 将指令移动到分类
        │       ├── BatchSpawnScreen.java      # 批量生成设置
        │       ├── TimedSpawnSetupScreen.java # 定时生成设置
        │       ├── TimedKillSetupScreen.java  # 定时移除设置
        │       ├── TimedTaskManager.java      # 内存中的定时任务队列
        │       ├── ChainedCommandExecutor.java # 占位符解析与执行
        │       ├── VanillaCommands.java       # 适配器：PresetConfig → VanillaCommand
        │       ├── PlayerSelectorScreen.java  # 玩家选择器（全部/其他/假人）
        │       ├── NumberInputScreen.java     # 数字输入弹窗
        │       ├── TextInputScreen.java       # 文本输入弹窗
        │       ├── TimeInputScreen.java       # 时间输入弹窗
        │       ├── CoordinateInputScreen.java # XYZ 坐标输入
        │       ├── BaseCommandEditorScreen.java  # 添加/编辑界面的共享基类
        │       ├── BaseParentedScreen.java    # 带父界面引用的基础界面
        │       ├── SettingsButton.java        # 齿轮图标按钮组件
        │       └── ItemIconButton.java        # 使用 MC 物品渲染器的图标按钮
        └── resources/
            └── command-gui.client.mixins.json
```

### 关键类说明

#### `CommandGUIClient`
客户端入口点。注册：
- `C` 快捷键
- `ClientTickEvents.END_CLIENT_TICK` 处理器，用于检查快捷键、调用 `TimedTaskManager.tick()` 和 `ChainedCommandExecutor` 的延迟队列

#### `CommandConfig`
管理 `config/command-gui/presets/custom.json`。所有增删改查操作都会立即调用 `save()` 以持久化数据。分类以 `LinkedHashMap<String, CommandEntry>` 有序存储指令。

#### `PresetConfig`
从配置目录加载预设 JSON 文件。加载时，首先从模组资源包复制内置预设（如果尚不存在），然后读取目录中的所有 `.json` 文件（不包括 `custom.json`）。

#### `ChainedCommandExecutor`
从左到右解析指令字符串中的占位符标记，构建 `PlaceholderType` 值队列，然后逐一遍历 — 每次迭代打开对应的输入界面。所有占位符解析完成后，发送最终指令。

#### `TimedTaskManager`
静态的 `TimedTask` 对象列表（类型为 SPAWN 或 KILL，带 `remainingTicks` 计数器）。每个客户端 Tick 调用 `tick()`；当 `remainingTicks` 归零时，发送指令并删除任务。

---

### 添加新的预设标签页

1. **创建预设 JSON** 到 `src/main/resources/assets/command-gui/presets/`：
   ```json
   {
     "id": "my_mod",
     "nameKey": "screen.command-gui.tab.my_mod",
     "groups": [
       {
         "nameKey": "screen.command-gui.my_mod.group1",
         "commands": [
           {
             "nameKey": "screen.command-gui.my_mod.cmd1",
             "command": "/mymod command {number}",
             "description": "screen.command-gui.my_mod.cmd1.desc",
             "minValue": 1,
             "maxValue": 100,
             "quickValues": [1, 10, 50, 100]
           }
         ]
       }
     ]
   }
   ```

2. **添加翻译键** 到 `src/main/resources/assets/command-gui/lang/en_us.json`：
   ```json
   "screen.command-gui.tab.my_mod": "My Mod",
   "screen.command-gui.my_mod.group1": "Group 1",
   "screen.command-gui.my_mod.cmd1": "My Command",
   "screen.command-gui.my_mod.cmd1.desc": "Runs my command"
   ```

3. **添加中文翻译** 到 `zh_cn.json`（可选但推荐）。

4. **重新构建** — 预设将自动加载并显示为新标签页（在内置标签页之后）。

> 如果模组资源中存在预设文件，加载时会**始终覆盖**用户目录中的同名文件。若要制作用户可编辑的预设，请将文件直接放到用户机器上的 `config/command-gui/presets/`。

---

### 添加新的占位符类型

1. 在 `ChainedCommandExecutor` 中添加新的 `PlaceholderType` 枚举值
2. 在 `parseTypes()` 中添加检测逻辑（扫描标记字符串）
3. 在 `showNextInput()` 中添加 `case`，打开对应的输入界面，并在回调中调用 `start()`
4. 将标记字符串添加到 `hasPlaceholders()` 以实现快速检测
5. 如需在 `CommandConfig.CommandEntry.hasPlaceholders()` 中也支持，同样更新
6. 如无现有界面适合，创建新的 `*InputScreen`

---

### 翻译

翻译文件位于：
- `src/main/resources/assets/command-gui/lang/en_us.json`
- `src/main/resources/assets/command-gui/lang/zh_cn.json`

添加新语言时，在同一目录下创建 `<语言代码>.json` 并添加 `en_us.json` 中的所有键。

---

### 代码风格

- Java 21，不使用预览功能
- 1 个缩进 = 1 个 Tab 字符（非空格）
- 类/方法名遵循标准 Java 命名规范
- 界面类继承 `BaseParentedScreen<P>` 或直接继承 `Screen`
- 除 Fabric API 和 Minecraft 本身外，无外部依赖
