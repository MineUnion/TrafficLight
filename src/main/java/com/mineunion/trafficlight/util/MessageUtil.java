package com.mineunion.trafficlight.util;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class MessageUtil {
    private static final TrafficLight plugin = TrafficLight.getInstance();
    private static final ConfigManager configManager = plugin.getConfigManager();
    // 多语言消息缓存
    private static final Map<String, String> messageMap = new HashMap<>();

    // 初始化多语言
    public static void init() {
        String lang = configManager.getConfig().getString("global.language", "zh_CN");
        // 加载默认消息（可扩展为从lang文件加载）
        if ("zh_CN".equals(lang)) {
            loadZhCNMessages();
        } else {
            loadEnUSMessages();
        }
    }

    // 加载中文消息
    private static void loadZhCNMessages() {
        messageMap.put("no_permission", "§c你没有权限执行此指令！");
        messageMap.put("only_player", "§c只有玩家可以执行此指令！");
        messageMap.put("light_exists", "§c红绿灯名称已存在！");
        messageMap.put("light_not_found", "§c红绿灯不存在！");
        messageMap.put("duration_invalid", "§c时长必须是大于0的数字！");
        messageMap.put("color_invalid", "§c无效的颜色！支持：red/green/yellow（红/绿/黄）");
        messageMap.put("world_not_found", "§c世界不存在！");
        messageMap.put("create_success", "§a红绿灯%s创建成功！默认时长：%d秒");
        messageMap.put("delete_success", "§a红绿灯%s删除成功！");
        messageMap.put("set_duration_success", "§a红绿灯%s的%s灯时长已设置为%d秒！");
        messageMap.put("reload_success", "§a配置文件已重载！");
    }

    // 加载英文消息
    private static void loadEnUSMessages() {
        messageMap.put("no_permission", "§cYou do not have permission to execute this command!");
        messageMap.put("only_player", "§cOnly players can execute this command!");
        messageMap.put("light_exists", "§cTraffic light name already exists!");
        messageMap.put("light_not_found", "§cTraffic light not found!");
        messageMap.put("duration_invalid", "§cDuration must be a number greater than 0!");
        messageMap.put("color_invalid", "§cInvalid color! Supported: red/green/yellow");
        messageMap.put("world_not_found", "§cWorld not found!");
        messageMap.put("create_success", "§aTraffic light %s created successfully! Default duration: %d seconds");
        messageMap.put("delete_success", "§aTraffic light %s deleted successfully!");
        messageMap.put("set_duration_success", "§aTraffic light %s %s duration set to %d seconds!");
        messageMap.put("reload_success", "§aConfig reloaded successfully!");
    }

    // 发送消息（支持占位符）
    public static void sendMessage(CommandSender sender, String key, Object... args) {
        String msg = messageMap.getOrDefault(key, key);
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }
}