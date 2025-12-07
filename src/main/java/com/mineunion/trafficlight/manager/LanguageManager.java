package com.mineunion.trafficlight.manager;

import com.mineunion.trafficlight.TrafficLight;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageManager {
    private final TrafficLight plugin;
    private final Map<String, FileConfiguration> languageFiles = new HashMap<>();
    private String currentLanguage;

    public LanguageManager(TrafficLight plugin) {
        this.plugin = plugin;
        this.currentLanguage = plugin.getConfigManager().loadConfig().getString("language", "zh_CN");
        loadAllLanguageFiles();
    }

    // 加载所有语言文件
    private void loadAllLanguageFiles() {
        // 加载默认语言文件（messages.yml）
        loadDefaultMessages();
        
        // 加载lang目录下的语言包
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.saveResource("lang/zh_CN.yml", false);
        }
        
        // 保存默认语言包（如果不存在）
        saveDefaultLanguageFiles();
        
        // 加载所有语言文件
        File[] files = langDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String langCode = file.getName().replace(".yml", "");
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                languageFiles.put(langCode, config);
            }
        }
    }
    
    // 保存默认语言文件
    private void saveDefaultLanguageFiles() {
        // 保存中文语言包
        saveDefaultLanguageFile("zh_CN");
        // 保存英文语言包
        saveDefaultLanguageFile("en_US");
    }
    
    // 保存单个默认语言文件
    private void saveDefaultLanguageFile(String langCode) {
        File langFile = new File(plugin.getDataFolder(), "lang/" + langCode + ".yml");
        if (!langFile.exists()) {
            plugin.saveResource("lang/" + langCode + ".yml", false);
        }
    }
    
    // 加载默认消息文件（兼容旧版本）
    private void loadDefaultMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (messagesFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(messagesFile);
            languageFiles.put("default", config);
        }
    }

    // 获取当前语言的配置
    private FileConfiguration getCurrentLanguageConfig() {
        return languageFiles.getOrDefault(currentLanguage, 
                languageFiles.getOrDefault("zh_CN", 
                languageFiles.getOrDefault("default", null)));
    }

    // 获取单个消息（支持占位符替换）
    public String getMessage(String key) {
        FileConfiguration config = getCurrentLanguageConfig();
        if (config == null) {
            return "§c语言配置错误：" + key;
        }
        
        String message = config.getString(key, "§c未找到消息：" + key);
        // 替换前缀占位符
        String prefix = config.getString("prefix", "§6[TrafficLight] §r");
        return message.replace("%prefix%", prefix);
    }

    // 获取列表类型消息
    public List<String> getMessageList(String key) {
        FileConfiguration config = getCurrentLanguageConfig();
        if (config == null) {
            List<String> errorList = new java.util.ArrayList<>();
            errorList.add("§c语言配置错误：" + key);
            return errorList;
        }
        
        List<String> list = config.getStringList(key);
        if (list.isEmpty()) {
            list.add("§c未找到消息列表：" + key);
        }
        // 替换列表中每个消息的前缀占位符
        String prefix = config.getString("prefix", "§6[TrafficLight] §r");
        for (int i = 0; i < list.size(); i++) {
            list.set(i, list.get(i).replace("%prefix%", prefix));
        }
        return list;
    }

    // 切换语言
    public void setLanguage(String languageCode) {
        this.currentLanguage = languageCode;
        // 保存语言设置到配置文件
        plugin.getConfigManager().getConfig().set("language", languageCode);
        try {
            plugin.getConfigManager().getConfig().save(plugin.getConfigManager().getConfigFile());
        } catch (IOException e) {
            plugin.getLogger().severe("保存语言设置失败：" + e.getMessage());
        }
        
        // 重新加载语言文件
        loadAllLanguageFiles();
    }
    
    // 获取当前语言
    public String getCurrentLanguage() {
        return currentLanguage;
    }

    // 重载语言文件
    public void reloadMessages() {
        loadAllLanguageFiles();
    }
}
