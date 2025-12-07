package com.mineunion.trafficlight.util;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.manager.LanguageManager;
import java.util.List; // 补充 List 导入

public class MessageUtil {
    private static TrafficLight plugin;
    private static LanguageManager languageManager;

    // 初始化工具类（在主类 onEnable 中调用）
    public static void init(TrafficLight trafficLight) {
        plugin = trafficLight;
        languageManager = plugin.getLanguageManager();
    }

    // 获取单个消息
    public static String getMessage(String key) {
        if (languageManager == null) {
            return "§c语言管理器未初始化";
        }
        return languageManager.getMessage(key);
    }

    // 获取列表消息
    public static List<String> getMessageList(String key) {
        if (languageManager == null) {
            return List.of("§c语言管理器未初始化");
        }
        return languageManager.getMessageList(key);
    }
}
