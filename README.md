![Header](https://capsule-render.vercel.app/api?type=Waving&color=timeGradient&height=300&animation=fadeIn&section=header&text=TrafficLight&fontSize=120)

---
<div align="center">
  <h2>🚀 TrafficLight</h2>
  <p style="font-size: 1.1rem; color: #666; margin: 1rem 0 2rem;">轻量可定制的 Minecraft 红绿灯控制插件</p>
  <p style="font-size: 1.1rem; color: #666; margin: 1rem 0 2rem;">Lightweight & customizable traffic light plugin for Minecraft</p>
</div>

[![Star](https://img.shields.io/github/stars/MineUnion/TrafficLight?style=social)](https://github.com/MineUnion/TrafficLight)
[![License](https://img.shields.io/github/license/MineUnion/TrafficLight?color=blueviolet)](LICENSE)

---

## 功能特性 | Features
### 核心能力
- 支持红绿灯的创建/删除/修改，可自定义红黄绿灯光切换时长
- 按世界/区域分组管理，支持批量配置和状态同步
- 可选**距离激活**机制，有效降低服务器性能消耗
- 提供简易管理指令，支持控制台与游戏内双端操作
- 精细化权限体系，可按需分配管理权限

### 兼容信息
- 适配服务端：Spigot/Paper/Purpur（1.18-1.21+）
- 内置中英双语，支持一键切换
- 低资源占用，单服务器可稳定运行百级红绿灯

### Core Features
- Create/delete/edit traffic lights with custom red/green/yellow switching durations
- Group management by world/region with batch configuration and state synchronization
- Optional **proximity activation** to reduce server performance overhead
- Simple admin commands for both console and in-game operations
- Granular permission system for role-based access control

### Compatibility
- Supported servers: Spigot/Paper/Purpur (1.18-1.21+)
- Built-in Chinese/English bilingual support with one-click switch
- Low resource usage, supports 100+ traffic lights per server stably

---

## 安装说明 | Installation
1. 前往 [Releases](https://github.com/MineUnion/TrafficLight/releases) 下载插件包
2. 将 jar 包放入服务器 `plugins/` 目录
3. 重启服务器，插件自动生成配置文件
4. 修改 `plugins/TrafficLight/config.yml` 后重启生效

1. Download the plugin from [Releases](https://github.com/MineUnion/TrafficLight/releases)
2. Place the jar file into the server's `plugins/` folder
3. Restart the server and the plugin will generate config files automatically
4. Modify `plugins/TrafficLight/config.yml` and restart to apply changes

---

## 指令说明 | Commands
| 指令 | 权限 | 说明 | Command | Permission | Description |
|------|------|------|---------|------------|-------------|
| `/tl create <名称> [时长]` | `mu.trafficlight.create` | 创建红绿灯 | `/tl create <name> [duration]` | `mu.trafficlight.create` | Create traffic light |
| `/tl delete <名称>` | `mu.trafficlight.delete` | 删除红绿灯 | `/tl delete <name>` | `mu.trafficlight.delete` | Delete traffic light |
| `/tl set <名称> <颜色> <时长>` | `mu.trafficlight.set` | 设置灯光时长 | `/tl set <name> <color> <duration>` | `mu.trafficlight.set` | Set light duration |
| `/tl list [世界名]` | `mu.trafficlight.list` | 查看红绿灯列表 | `/tl list [world]` | `mu.trafficlight.list` | List traffic lights |
| `/tl reload` | `mu.trafficlight.reload` | 重载配置 | `/tl reload` | `mu.trafficlight.reload` | Reload config |

> 注：颜色参数支持`红/绿/黄`或`red/green/yellow`，时长单位为秒（默认10s）  
> Note: Color parameters: `red/green/yellow`, duration unit is second (default 10s)

---

## 配置示例 | Config Example
```yaml
# 全局配置
global:
  default_duration: 10  # 红绿灯默认切换时长（秒）
  proximity_activation: true  # 是否开启距离激活
  activation_radius: 50  # 激活距离（方块数）
  language: zh_CN  # 插件语言（zh_CN/en_US）

# 红绿灯分组同步配置
groups:
  city_center:  # 分组名称
    world: "world"  # 所属世界
    lights: ["tl_main_street", "tl_park_entrance"]  # 组内红绿灯
    sync: true  # 开启组内状态同步
```

---

## 权限列表 | Permissions
| 权限节点 | 说明 | Permission Node | Description |
|----------|------|----------------|-------------|
| `mu.trafficlight.*` | 拥有所有管理权限 | `mu.trafficlight.*` | All management permissions |
| `mu.trafficlight.create` | 红绿灯创建权限 | `mu.trafficlight.create` | Traffic light creation permission |
| `mu.trafficlight.delete` | 红绿灯删除权限 | `mu.trafficlight.delete` | Traffic light deletion permission |
| `mu.trafficlight.set` | 红绿灯配置权限 | `mu.trafficlight.set` | Traffic light configuration permission |
| `mu.trafficlight.list` | 红绿灯查看权限 | `mu.trafficlight.list` | Traffic light viewing permission |
| `mu.trafficlight.reload` | 配置重载权限 | `mu.trafficlight.reload` | Config reload permission |

---

## 常见问题 | FAQ
### Q1: 红绿灯无响应如何排查？
A1: 按以下步骤排查：
1. 确认插件版本与服务端版本兼容
2. 检查红绿灯生成位置无方块遮挡
3. 验证账号已授予对应`mu.trafficlight`权限
4. 距离激活模式下需进入50方块内的激活范围

### Q1: Traffic lights not responding?
A1: Troubleshooting steps:
1. Confirm plugin version is compatible with server version
2. Ensure no blocks are blocking the traffic light
3. Verify account has the corresponding `mu.trafficlight` permissions
4. Enter the 50-block activation radius (if proximity activation is enabled)

### Q2: 如何实现多个红绿灯状态同步？
A2: 在配置文件`groups`节点下创建分组，将需同步的红绿灯名称加入`lights`列表，设置`sync: true`后执行`/tl reload`即可生效。

### Q2: How to synchronize multiple traffic light states?
A2: Create a group in the `groups` section of the config file, add target traffic light names to the `lights` list, set `sync: true`, and execute `/tl reload` to take effect.

---

## 🤝 贡献指南 | Contribution
1. Fork 本仓库到个人账号
2. 新建功能分支 `git checkout -b feature/xxx`
3. 提交功能修改 `git commit -m 'Add xxx feature'`
4. 推送分支到远程 `git push origin feature/xxx`
5. 提交 Pull Request 等待审核

1. Fork this repository to your personal account
2. Create a feature branch: `git checkout -b feature/xxx`
3. Commit your changes: `git commit -m 'Add xxx feature'`
4. Push the branch to remote: `git push origin feature/xxx`
5. Submit a Pull Request for review

---

## 许可证 | License
本项目基于**MIT 协议**开源，详细协议内容可查看 [LICENSE](LICENSE) 文件。  
This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## 📬 反馈渠道 | Contact
- 问题提交：[GitHub Issues](https://github.com/MineUnion/TrafficLight/issues)
- Feedback: [GitHub Issues](https://github.com/MineUnion/TrafficLight/issues)