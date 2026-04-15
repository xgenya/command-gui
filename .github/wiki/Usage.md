# Usage

[English](#english) | [中文](#中文)

---

## English

### Opening and Closing the GUI

| Action | Default |
|--------|---------|
| Open GUI | `C` |
| Close GUI | `C` again, or `Esc` |

The GUI can only be opened when no other screen is open (e.g., not in inventory or chat).  
You can change the keybind in **Options → Controls → Key Binds → Command-GUI**.

### The Main Screen

The main screen has three areas:

```
┌──────────────────────────────────────────────┐
│  [Custom] [Fake Player] [Vanilla] [Carpet]   │  ← Tab bar (top)
├──────────────────────────────────────────────┤
│                                              │
│              Tab Content                     │  ← Main content area
│                                              │
├──────────────────────────────────────────────┤
│  [Search...]  [+]  [⚙]  [Keep Open □]  [X]  │  ← Footer (bottom)
└──────────────────────────────────────────────┘
```

**Tab bar** — Switch between Custom, Fake Player, Vanilla, and Carpet tabs.  
**Footer controls:**
- **Search box** — Filter commands in the Custom or preset tabs by name, description, or command text
- **`+` (Add)** — Add a new custom command
- **⚙ (Settings)** — Open the Settings screen
- **Keep Open checkbox** — When checked, the GUI stays open after a command is executed
- **✕ (Close)** — Close the GUI (same as `Esc`)

### Tabs Overview

| Tab | When shown | Description |
|-----|-----------|-------------|
| **Custom** | Always | Your personal commands, organized by category |
| **Fake Player** | When enabled in Settings | Carpet fake player management panel |
| **Vanilla** | When enabled in Settings | Common vanilla game commands |
| **Carpet** | When enabled in Settings | Carpet mod utility commands |

### Executing a Command

1. Click the button for the command you want to run
2. If the command has **placeholders**, a helper screen appears — fill in the required input and confirm
3. The command is sent to the server
4. If **Keep Open** is unchecked, the GUI closes automatically

### Searching Commands

Type in the search box to filter the currently visible tab:
- Matches against command **name**, **description**, and the raw **command text**
- Search is case-insensitive
- The filter applies to all categories simultaneously

### Keeping the GUI Open

Check the **"Keep open after execute"** checkbox at the bottom to prevent the GUI from closing after each command execution. This is useful when you need to run multiple commands in a row.

---

## 中文

### 打开和关闭界面

| 操作 | 默认按键 |
|------|----------|
| 打开界面 | `C` |
| 关闭界面 | 再次按 `C` 或按 `Esc` |

只有在没有其他界面打开时才能打开 GUI（例如不在背包或聊天界面中）。  
可以在**选项 → 控制 → 按键设置 → Command-GUI** 中修改快捷键。

### 主界面

主界面由三个区域组成：

```
┌──────────────────────────────────────────────┐
│  [自定义] [假人控制] [原版] [Carpet]          │  ← 标签栏（顶部）
├──────────────────────────────────────────────┤
│                                              │
│              标签页内容                       │  ← 主内容区
│                                              │
├──────────────────────────────────────────────┤
│  [搜索...]  [+]  [⚙]  [执行后不关闭 □]  [X] │  ← 底栏（底部）
└──────────────────────────────────────────────┘
```

**标签栏** — 在自定义、假人控制、原版和 Carpet 标签页之间切换。  
**底栏控件：**
- **搜索框** — 按名称、描述或指令内容筛选当前标签页的指令
- **`+`（添加）** — 添加新的自定义指令
- **⚙（设置）** — 打开设置界面
- **执行后不关闭复选框** — 勾选后，执行指令后界面保持打开
- **✕（关闭）** — 关闭界面（与 `Esc` 相同）

### 标签页概览

| 标签页 | 显示条件 | 说明 |
|--------|----------|------|
| **自定义** | 始终显示 | 你的个人指令，按分类整理 |
| **假人控制** | 在设置中启用时 | Carpet 假人管理面板 |
| **原版** | 在设置中启用时 | 常用原版游戏指令 |
| **Carpet** | 在设置中启用时 | Carpet 模组实用指令 |

### 执行指令

1. 点击你想运行的指令按钮
2. 如果指令包含**占位符**，会弹出辅助输入界面 — 填写所需内容并确认
3. 指令被发送到服务器
4. 如果**执行后不关闭**未勾选，界面会自动关闭

### 搜索指令

在搜索框中输入文字以筛选当前标签页中的指令：
- 匹配指令的**名称**、**描述**和原始**指令文本**
- 搜索不区分大小写
- 筛选同时应用于所有分类

### 执行后保持界面打开

勾选底部的**"执行后不关闭界面"**复选框，可以防止每次执行指令后界面自动关闭。这在需要连续运行多条指令时非常有用。
