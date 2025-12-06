package com.mineunion.trafficlight.command;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.subcommands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class TrafficLightCommand implements CommandExecutor, TabCompleter {
    private final TrafficLight plugin;
    // 子指令映射
    private final Map<String, SubCommand> subCommandMap = new HashMap<>();

    public TrafficLightCommand(TrafficLight plugin) {
        this.plugin = plugin;
        // 注册子指令
        subCommandMap.put("create", new CreateCommand(plugin));
        subCommandMap.put("delete", new DeleteCommand(plugin));
        subCommandMap.put("set", new SetCommand(plugin));
        subCommandMap.put("list", new ListCommand(plugin));
        subCommandMap.put("reload", new ReloadCommand(plugin));

        // 注册指令
        plugin.getCommand("trafficlight").setExecutor(this);
        plugin.getCommand("trafficlight").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 无参数：显示帮助
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        // 匹配子指令
        String subCmd = args[0].toLowerCase();
        SubCommand command = subCommandMap.get(subCmd);
        if (command == null) {
            sender.sendMessage("§c未知指令！输入/tl查看帮助");
            return true;
        }

        // 权限校验
        if (!command.hasPermission(sender)) {
            sender.sendMessage("§c你没有权限执行此指令！");
            return true;
        }

        // 执行子指令
        try {
            command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        } catch (Exception e) {
            sender.sendMessage("§c指令执行失败：" + e.getMessage());
            plugin.getLogger().severe("指令执行异常：" + e.getMessage());
        }
        return true;
    }

    // 发送帮助信息
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§e===== TrafficLight 指令帮助 =====");
        sender.sendMessage("§6/tl create <名称> [时长] §7- 创建红绿灯（时长为默认绿灯时长）");
        sender.sendMessage("§6/tl delete <名称> §7- 删除指定红绿灯");
        sender.sendMessage("§6/tl set <名称> <颜色> <时长> §7- 设置红绿灯状态时长（颜色：red/green/yellow）");
        sender.sendMessage("§6/tl list [世界名] §7- 列出红绿灯列表");
        sender.sendMessage("§6/tl reload §7- 重载配置文件");
        sender.sendMessage("§e==============================");
    }

    // 指令补全
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            // 补全子指令
            List<String> subCmds = new ArrayList<>(subCommandMap.keySet());
            subCmds.removeIf(s -> !s.startsWith(args[0].toLowerCase()));
            return subCmds;
        } else if (args.length >= 2) {
            // 子指令补全
            SubCommand subCommand = subCommandMap.get(args[0].toLowerCase());
            if (subCommand != null) {
                return subCommand.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return Collections.emptyList();
    }

    // 修复：添加 public 修饰符，允许跨包访问
    public interface SubCommand {
        boolean hasPermission(CommandSender sender);
        void execute(CommandSender sender, String[] args);
        
        // 默认实现tabComplete，避免子类调用super出错
        default List<String> tabComplete(CommandSender sender, String[] args) {
            return List.of();
        }
    }
}