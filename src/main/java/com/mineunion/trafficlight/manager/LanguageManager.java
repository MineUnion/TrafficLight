package com.mineunion.trafficlight.manager;

import com.mineunion.trafficlight.TrafficLight;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LanguageManager {
    private final TrafficLight plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;

    public LanguageManager(TrafficLight plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    // 加载语言文件（messages.yml）
    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    // 获取单个消息（支持占位符替换）
    public String getMessage(String key) {
        String message = messagesConfig.getString(key, "§c未找到消息：" + key);
        // 替换前缀占位符
        String prefix = messagesConfig.getString("prefix", "§6[TrafficLight] §r");
        return message.replace("%prefix%", prefix);
    }

    // 补充：获取列表类型消息（修复 HelpCommand 报错）
    public List<String> getMessageList(String key) {
        List<String> list = messagesConfig.getStringList(key);
        if (list.isEmpty()) {
            list.add("§c未找到消息列表：" + key);
        }
        // 替换列表中每个消息的前缀占位符
        String prefix = messagesConfig.getString("prefix", "§6[TrafficLight] §r");
        for (int i = 0; i < list.size(); i++) {
            list.set(i, list.get(i).replace("%prefix%", prefix));
        }
        return list;
    }

    // 重载语言文件
    public void reloadMessages() {
        loadMessages();
    }
}
