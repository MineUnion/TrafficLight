package com.mineunion.trafficlight;

import com.mineunion.trafficlight.manager.ConfigManager;
import com.mineunion.trafficlight.manager.GroupManager;
import com.mineunion.trafficlight.manager.LanguageManager; // 新增导入
import com.mineunion.trafficlight.manager.TrafficLightManager;
import com.mineunion.trafficlight.task.LightUpdateTask;
import com.mineunion.trafficlight.task.ProximityCheckTask;
import com.mineunion.trafficlight.util.SerializationUtil;
import org.bukkit.plugin.java.JavaPlugin;

public final class TrafficLight extends JavaPlugin {
    private static TrafficLight instance;
    private TrafficLightManager trafficLightManager;
    private GroupManager groupManager;
    private ConfigManager configManager;
    private LanguageManager languageManager; // 新增：语言管理器

    @Override
    public void onEnable() {
        instance = this;
        // 初始化顺序：Config → Language → 其他管理器
        configManager = new ConfigManager(this);
        languageManager = new LanguageManager(this); // 初始化语言管理器
        trafficLightManager = new TrafficLightManager(this);
        groupManager = new GroupManager(this);
        
        SerializationUtil.registerSerializables();
        new LightUpdateTask(this).runTaskLater(this, 20L);
        new ProximityCheckTask(this).runTaskTimer(this, 0L, 20L);

        // 注册命令（补充所有子命令）
        this.getCommand("trafficlight").setExecutor((sender, command, label, args) -> {
            if (args.length == 0) {
                sendMessage(sender, "command-usage");
                return true;
            }
            
            switch (args[0].toLowerCase()) {
                case "list" -> new com.mineunion.trafficlight.command.subcommands.ListCommand(this).execute(sender, args);
                case "create" -> new com.mineunion.trafficlight.command.subcommands.CreateCommand(this).execute(sender, args);
                case "delete" -> new com.mineunion.trafficlight.command.subcommands.DeleteCommand(this).execute(sender, args);
                case "set" -> new com.mineunion.trafficlight.command.subcommands.SetCommand(this).execute(sender, args);
                case "reload" -> new com.mineunion.trafficlight.command.subcommands.ReloadCommand(this).execute(sender, args);
                default -> sendMessage(sender, "command-usage");
            }
            return true;
        });

        getLogger().info(languageManager.getMessage("prefix") + "插件已启用！");
    }

    @Override
    public void onDisable() {
        trafficLightManager.saveAllTrafficLights();
        getLogger().info(languageManager.getMessage("prefix") + "插件已禁用！");
    }

    // 新增：快捷发送语言消息
    public void sendMessage(org.bukkit.command.CommandSender sender, String key, Object... placeholders) {
        sender.sendMessage(languageManager.getMessage(key, placeholders));
    }

    // 新增：获取语言管理器
    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    // 原有Getter方法保留
    public static TrafficLight getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public TrafficLightManager getTrafficLightManager() {
        return trafficLightManager;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }
}
