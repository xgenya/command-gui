# Installation

[English](#english) | [中文](#中文)

---

## English

### Requirements

| Dependency | Version | Required? |
|-----------|---------|-----------|
| Minecraft | 1.21.1 | ✅ Yes |
| Java | ≥ 21 | ✅ Yes |
| Fabric Loader | ≥ 0.18.6 | ✅ Yes |
| Fabric API | any | ✅ Yes |
| Carpet Mod | any | ⚠ Optional (for fake player features) |

### Step-by-Step

1. **Install Fabric Loader**
   - Download the installer from [fabricmc.net](https://fabricmc.net/use/installer/)
   - Run the installer and select Minecraft **1.21.1**
   - Click **Install**

2. **Install Fabric API**
   - Download Fabric API from [Modrinth](https://modrinth.com/mod/fabric-api) — choose the version for Minecraft 1.21.1
   - Place the downloaded `.jar` in your `.minecraft/mods/` folder

3. **(Optional) Install Carpet Mod**
   - Download from [Modrinth](https://modrinth.com/mod/carpet) or [GitHub](https://github.com/gnembon/fabric-carpet/releases) — choose the version for Minecraft 1.21.1
   - Place the `.jar` in your `.minecraft/mods/` folder
   - Without Carpet, the **Fake Player** tab and Carpet preset commands are unavailable

4. **Install Command-GUI**
   - Download the latest `command-gui-*.jar` from the [Releases](../../releases) page
   - Place it in your `.minecraft/mods/` folder

5. **Launch Minecraft**
   - Open the Minecraft Launcher
   - Select the **Fabric** profile for 1.21.1
   - Click **Play**

### Verifying the Installation

- After loading into a world, press **`C`** — the Command-GUI window should open
- If it doesn't open, check the game log (`logs/latest.log`) for errors

### Changing the Keybind

1. Go to **Options → Controls → Key Binds**
2. Find the **Command-GUI** section
3. Click the **Open GUI** binding and press your desired key

---

## 中文

### 运行要求

| 依赖 | 版本 | 是否必须 |
|------|------|----------|
| Minecraft | 1.21.1 | ✅ 必须 |
| Java | ≥ 21 | ✅ 必须 |
| Fabric Loader | ≥ 0.18.6 | ✅ 必须 |
| Fabric API | 任意 | ✅ 必须 |
| Carpet Mod | 任意 | ⚠ 可选（用于假人功能） |

### 安装步骤

1. **安装 Fabric Loader**
   - 在 [fabricmc.net](https://fabricmc.net/use/installer/) 下载安装程序
   - 运行安装程序，选择 Minecraft **1.21.1**
   - 点击**安装**

2. **安装 Fabric API**
   - 在 [Modrinth](https://modrinth.com/mod/fabric-api) 下载 Fabric API，选择适用于 Minecraft 1.21.1 的版本
   - 将下载的 `.jar` 放入 `.minecraft/mods/` 文件夹

3. **（可选）安装 Carpet Mod**
   - 在 [Modrinth](https://modrinth.com/mod/carpet) 或 [GitHub](https://github.com/gnembon/fabric-carpet/releases) 下载，选择适用于 Minecraft 1.21.1 的版本
   - 将 `.jar` 放入 `.minecraft/mods/` 文件夹
   - 不安装 Carpet 时，**假人控制**标签页和 Carpet 预设指令不可用

4. **安装 Command-GUI**
   - 在 [Releases](../../releases) 页面下载最新的 `command-gui-*.jar`
   - 将其放入 `.minecraft/mods/` 文件夹

5. **启动 Minecraft**
   - 打开 Minecraft 启动器
   - 选择 1.21.1 的 **Fabric** 配置文件
   - 点击**开始游戏**

### 验证安装

- 进入游戏世界后，按 **`C`** 键 —— Command-GUI 窗口应该打开
- 如果没有打开，请检查游戏日志（`logs/latest.log`）中的错误信息

### 修改快捷键

1. 前往 **选项 → 控制 → 按键设置**
2. 找到 **Command-GUI** 分类
3. 点击**打开界面**绑定，然后按下你想要的按键
