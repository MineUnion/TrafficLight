package com.mineunion.trafficlight.util;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.manager.LanguageManager;
import org.bukkit.command.CommandSender;

public class MessageUtil {
    private static final LanguageManager langManager = TrafficLight.getInstance().getLanguageManager();

    // 发送普通消息（从语言文件获取）
    public static void sendMessage(CommandSender sender, String key, Object... placeholders) {
        sender.sendMessage(langManager.getMessage(key, placeholders));
    }

    // 发送错误消息（从语言文件获取）
    public static void sendError(CommandSender sender, String key, Object... placeholders) {
        sender.sendMessage(langManager.getMessage("error-" + key, placeholders));
    }
}
