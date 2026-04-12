# Preset Commands

[English](#english) | [中文](#中文)

---

## English

Preset command tabs (**Vanilla** and **Carpet**) provide ready-to-use buttons for common commands. Each tab can be shown or hidden in Settings.

### Vanilla Tab

The Vanilla tab contains commands available in vanilla Minecraft. Most require **OP permission** or a **single-player world**.

#### Gamemode

| Button | Command | Description |
|--------|---------|-------------|
| Survival | `/gamemode survival` | Switch to Survival mode |
| Creative | `/gamemode creative` | Switch to Creative mode |
| Adventure | `/gamemode adventure` | Switch to Adventure mode |
| Spectator | `/gamemode spectator` | Switch to Spectator mode |

#### Time

| Button | Command | Description |
|--------|---------|-------------|
| Day | `/time set day` | Set time to day (1000 ticks) |
| Noon | `/time set noon` | Set time to noon (6000 ticks) |
| Night | `/time set night` | Set time to night (13000 ticks) |
| Midnight | `/time set midnight` | Set time to midnight (18000 ticks) |

#### Weather

| Button | Command | Description |
|--------|---------|-------------|
| Clear | `/weather clear` | Set weather to clear |
| Rain | `/weather rain` | Set weather to rain |
| Thunder | `/weather thunder` | Set weather to thunderstorm |

#### Difficulty

| Button | Command | Description |
|--------|---------|-------------|
| Peaceful | `/difficulty peaceful` | Set difficulty to Peaceful |
| Easy | `/difficulty easy` | Set difficulty to Easy |
| Normal | `/difficulty normal` | Set difficulty to Normal |
| Hard | `/difficulty hard` | Set difficulty to Hard |

#### Tick Control

| Button | Command | Description |
|--------|---------|-------------|
| Freeze | `/tick freeze` | Freeze all game ticks |
| Unfreeze | `/tick unfreeze` | Resume all game ticks |
| Step | `/tick step {time}` | Execute specified time while frozen (opens time input) |
| Stop Step | `/tick step stop` | Cancel current step and refreeze |
| Rate | `/tick rate {number}` | Set tick rate (default 20; opens number input) |
| Sprint | `/tick sprint {time}` | Fast-forward specified time (opens time input) |
| Stop Sprint | `/tick sprint stop` | Cancel current sprint |

Quick values for **Rate**: 1, 5, 10, 20, 40, 60, 100, 200  
Quick values for **Step / Sprint**: `1t`, `5t`, `10t`, `20t`, `1s`, `5s`, `10s`, `30s`, `1d`

#### Teleport

| Button | Command | Description |
|--------|---------|-------------|
| To Player | `/tp {player}` | Teleport to selected player (opens player selector) |
| To Coordinates | `/tp @s {coords}` | Teleport to coordinates (opens coord input) |
| To Spawn | `/tp @s 0 ~ 0` | Teleport to world spawn point |

#### Player

| Button | Command | Description |
|--------|---------|-------------|
| Kill Self | `/kill @s` | Kill yourself |
| Full Health | `/effect give @s minecraft:instant_health infinite 255` | Restore health to full |
| Full Hunger | `/effect give @s minecraft:saturation infinite 255` | Restore hunger to full |
| Clear Effects | `/effect clear @s` | Remove all status effects |

#### World

| Button | Command | Description |
|--------|---------|-------------|
| Kill Entities | `/kill @e[type=!player]` | Kill all non-player entities |
| KeepInv ON | `/gamerule keepInventory true` | Keep inventory on death |
| KeepInv OFF | `/gamerule keepInventory false` | Drop inventory on death |
| MobGrief ON | `/gamerule mobGriefing true` | Allow mobs to grief terrain |
| MobGrief OFF | `/gamerule mobGriefing false` | Prevent mobs from griefing |
| Daylight ON | `/gamerule doDaylightCycle true` | Enable day/night cycle |
| Daylight OFF | `/gamerule doDaylightCycle false` | Disable day/night cycle |

---

### Carpet Tab

The Carpet tab contains commands from [Carpet Mod](https://github.com/gnembon/fabric-carpet). Requires Carpet Mod on the server.

#### Profile

| Button | Command | Description |
|--------|---------|-------------|
| Health | `/profile health {number}` | Profile server tick time for N ticks (opens number input; range 20–24000) |
| Entities | `/profile entities {number}` | Profile entity tick times for N ticks |

Quick values: 20, 100, 200, 600, 1200

#### Fake Player

| Button | Command | Description |
|--------|---------|-------------|
| Spawn | `/player {name} spawn` | Spawn fake player at current position (opens name input) |
| Spawn At | `/player {name} spawn at {coords}` | Spawn at coordinates (opens name + coord inputs) |
| Kill | `/player {player_fake} kill` | Remove specified fake player (opens fake player selector) |
| Stop | `/player {player_fake} stop` | Stop fake player's current action |

#### Fake Player Actions

| Button | Command |
|--------|---------|
| Attack | `/player {player_fake} attack continuous` |
| Attack Once | `/player {player_fake} attack once` |
| Use | `/player {player_fake} use continuous` |
| Use Once | `/player {player_fake} use once` |
| Jump | `/player {player_fake} jump continuous` |
| Drop Item | `/player {player_fake} drop` |
| Drop Stack | `/player {player_fake} dropStack` |
| Swap Hands | `/player {player_fake} swapHands` |

#### Fake Player Movement

| Button | Command |
|--------|---------|
| Sneak | `/player {player_fake} sneak` |
| Unsneak | `/player {player_fake} unsneak` |
| Sprint | `/player {player_fake} sprint` |
| Stop Sprint | `/player {player_fake} unsprint` |
| Mount | `/player {player_fake} mount` |
| Dismount | `/player {player_fake} dismount` |
| Look At | `/player {player_fake} look at {coords}` |
| Turn Around | `/player {player_fake} turn` |

#### Log

| Button | Command | Description |
|--------|---------|-------------|
| List | `/log` | Show all available log options |
| Clear All | `/log clear` | Clear all subscribed logs |
| TPS | `/log tps` | Toggle TPS display in HUD |
| Mobcaps | `/log mobcaps` | Toggle mobcap display in HUD |
| Counter | `/log counter` | Toggle hopper counter output |

#### Log Tracking

| Button | Command | Description |
|--------|---------|-------------|
| TNT | `/log tnt` | Track TNT explosions and landing positions |
| Projectiles | `/log projectiles` | Track projectile trajectories |
| Falling Blocks | `/log fallingBlocks` | Track falling block landing positions |

#### Info

| Button | Command | Description |
|--------|---------|-------------|
| Carpet List | `/carpet list defaults` | List all Carpet rules and their values |
| Ping | `/ping` | Show current network latency |
| Block Info | `/info block {coords}` | Show block details at coordinates |
| Block Here | `/info block ~ ~ ~` | Show block details at current position |
| Perimeter Info | `/perimeterinfo {coords}` | Analyze mob spawning around coordinates |
| Perimeter Here | `/perimeterinfo ~ ~ ~` | Analyze mob spawning at current position |

#### Distance

| Button | Command | Description |
|--------|---------|-------------|
| From | `/distance from` | Mark distance measurement start point |
| To | `/distance to` | Mark end point and display distance |

#### Track

| Button | Command | Description |
|--------|---------|-------------|
| Villager Breeding | `/track villager breeding` | Track villager breeding events |
| Iron Golem Spawn | `/track villager iron_golem_spawning` | Track iron golem spawn conditions |

#### Script

| Button | Command | Description |
|--------|---------|-------------|
| List Apps | `/script list` | List all loaded Scarpet apps |

---

## 中文

预设指令标签页（**原版**和 **Carpet**）提供常用指令的即用按钮。每个标签页可以在设置中显示或隐藏。

### 原版标签页

原版标签页包含原版 Minecraft 中可用的指令。大多数需要**管理员权限**或**单人游戏世界**。

#### 游戏模式

| 按钮 | 指令 | 说明 |
|------|------|------|
| 生存 | `/gamemode survival` | 切换到生存模式 |
| 创造 | `/gamemode creative` | 切换到创造模式 |
| 冒险 | `/gamemode adventure` | 切换到冒险模式 |
| 旁观 | `/gamemode spectator` | 切换到旁观模式 |

#### 时间

| 按钮 | 指令 | 说明 |
|------|------|------|
| 白天 | `/time set day` | 设置时间为白天（1000 刻） |
| 正午 | `/time set noon` | 设置时间为正午（6000 刻） |
| 夜晚 | `/time set night` | 设置时间为夜晚（13000 刻） |
| 午夜 | `/time set midnight` | 设置时间为午夜（18000 刻） |

#### 天气

| 按钮 | 指令 | 说明 |
|------|------|------|
| 晴天 | `/weather clear` | 设置天气为晴天 |
| 雨天 | `/weather rain` | 设置天气为雨天 |
| 雷暴 | `/weather thunder` | 设置天气为雷暴 |

#### 难度

| 按钮 | 指令 | 说明 |
|------|------|------|
| 和平 | `/difficulty peaceful` | 设置难度为和平 |
| 简单 | `/difficulty easy` | 设置难度为简单 |
| 普通 | `/difficulty normal` | 设置难度为普通 |
| 困难 | `/difficulty hard` | 设置难度为困难 |

#### 游戏刻控制

| 按钮 | 指令 | 说明 |
|------|------|------|
| 冻结 | `/tick freeze` | 冻结所有游戏刻 |
| 解冻 | `/tick unfreeze` | 恢复所有游戏刻 |
| 步进 | `/tick step {time}` | 在冻结状态下执行指定时间（弹出时间输入） |
| 停止步进 | `/tick step stop` | 取消当前步进并重新冻结 |
| 速率 | `/tick rate {number}` | 设置游戏刻速率（默认 20；弹出数字输入） |
| 快进 | `/tick sprint {time}` | 快进指定时间（弹出时间输入） |
| 停止快进 | `/tick sprint stop` | 取消当前快进 |

**速率**快速值：1、5、10、20、40、60、100、200  
**步进/快进**快速值：`1t`、`5t`、`10t`、`20t`、`1s`、`5s`、`10s`、`30s`、`1d`

#### 传送

| 按钮 | 指令 | 说明 |
|------|------|------|
| 传送到玩家 | `/tp {player}` | 传送到选中的玩家（弹出玩家选择器） |
| 传送到坐标 | `/tp @s {coords}` | 传送到坐标（弹出坐标输入） |
| 回到出生点 | `/tp @s 0 ~ 0` | 传送到世界出生点 |

#### 玩家

| 按钮 | 指令 | 说明 |
|------|------|------|
| 自杀 | `/kill @s` | 杀死自己 |
| 满血 | `/effect give @s minecraft:instant_health infinite 255` | 恢复满生命值 |
| 满饱食度 | `/effect give @s minecraft:saturation infinite 255` | 恢复满饱食度 |
| 清除效果 | `/effect clear @s` | 清除所有状态效果 |

#### 世界

| 按钮 | 指令 | 说明 |
|------|------|------|
| 清除实体 | `/kill @e[type=!player]` | 清除所有非玩家实体 |
| 死亡保留开 | `/gamerule keepInventory true` | 死亡时保留物品栏 |
| 死亡保留关 | `/gamerule keepInventory false` | 死亡时掉落物品栏 |
| 生物破坏开 | `/gamerule mobGriefing true` | 允许生物破坏地形 |
| 生物破坏关 | `/gamerule mobGriefing false` | 禁止生物破坏地形 |
| 日夜循环开 | `/gamerule doDaylightCycle true` | 启用日夜循环 |
| 日夜循环关 | `/gamerule doDaylightCycle false` | 禁用日夜循环 |

---

### Carpet 标签页

Carpet 标签页包含来自 [Carpet Mod](https://github.com/gnembon/fabric-carpet) 的指令。需要服务器上安装 Carpet Mod。

#### 性能分析

| 按钮 | 指令 | 说明 |
|------|------|------|
| 性能监控 | `/profile health {number}` | 分析服务器 N 刻的耗时（弹出数字输入；范围 20–24000） |
| 实体耗时 | `/profile entities {number}` | 分析 N 刻内各实体类型的耗时 |

快速值：20、100、200、600、1200

#### 假人管理

| 按钮 | 指令 | 说明 |
|------|------|------|
| 生成 | `/player {name} spawn` | 在当前位置生成假人（弹出名称输入） |
| 生成在 | `/player {name} spawn at {coords}` | 在坐标处生成（弹出名称+坐标输入） |
| 移除 | `/player {player_fake} kill` | 移除指定假人（弹出假人选择器） |
| 停止 | `/player {player_fake} stop` | 停止假人当前动作 |

#### 假人动作

| 按钮 | 指令 |
|------|------|
| 攻击 | `/player {player_fake} attack continuous` |
| 攻击一次 | `/player {player_fake} attack once` |
| 使用 | `/player {player_fake} use continuous` |
| 使用一次 | `/player {player_fake} use once` |
| 跳跃 | `/player {player_fake} jump continuous` |
| 丢弃物品 | `/player {player_fake} drop` |
| 丢弃整组 | `/player {player_fake} dropStack` |
| 交换双手 | `/player {player_fake} swapHands` |

#### 假人移动

| 按钮 | 指令 |
|------|------|
| 潜行 | `/player {player_fake} sneak` |
| 取消潜行 | `/player {player_fake} unsneak` |
| 疾跑 | `/player {player_fake} sprint` |
| 停止疾跑 | `/player {player_fake} unsprint` |
| 骑乘 | `/player {player_fake} mount` |
| 下马 | `/player {player_fake} dismount` |
| 看向 | `/player {player_fake} look at {coords}` |
| 转身 | `/player {player_fake} turn` |

#### 日志

| 按钮 | 指令 | 说明 |
|------|------|------|
| 列表 | `/log` | 显示所有可用日志选项 |
| 清除全部 | `/log clear` | 清除所有已订阅日志 |
| TPS | `/log tps` | 在 HUD 中切换 TPS 显示 |
| 生物上限 | `/log mobcaps` | 在 HUD 中切换生物上限显示 |
| 计数器 | `/log counter` | 切换漏斗计数器输出 |

#### 追踪日志

| 按钮 | 指令 | 说明 |
|------|------|------|
| TNT | `/log tnt` | 追踪 TNT 爆炸和落点 |
| 抛射物 | `/log projectiles` | 追踪抛射物轨迹 |
| 下落方块 | `/log fallingBlocks` | 追踪下落方块落点 |

#### 信息

| 按钮 | 指令 | 说明 |
|------|------|------|
| 规则列表 | `/carpet list defaults` | 列出所有 Carpet 规则及其值 |
| 延迟 | `/ping` | 显示当前网络延迟 |
| 方块信息 | `/info block {coords}` | 显示指定坐标处方块的详细信息 |
| 当前方块 | `/info block ~ ~ ~` | 显示当前位置方块的详细信息 |
| 范围分析 | `/perimeterinfo {coords}` | 分析指定坐标周围的刷怪条件 |
| 当前范围 | `/perimeterinfo ~ ~ ~` | 分析当前位置周围的刷怪条件 |

#### 距离

| 按钮 | 指令 | 说明 |
|------|------|------|
| 起点 | `/distance from` | 标记距离测量起点 |
| 终点 | `/distance to` | 标记终点并显示距离 |

#### 追踪

| 按钮 | 指令 | 说明 |
|------|------|------|
| 村民繁殖 | `/track villager breeding` | 追踪村民繁殖事件 |
| 铁傀儡生成 | `/track villager iron_golem_spawning` | 追踪铁傀儡生成条件 |

#### 脚本

| 按钮 | 指令 | 说明 |
|------|------|------|
| 列出应用 | `/script list` | 列出所有已加载的 Scarpet 应用 |
