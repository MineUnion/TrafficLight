package com.mineunion.trafficlight.manager;

import com.mineunion.trafficlight.TrafficLight;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class LanguageManager {
    private final TrafficLight plugin;
    private FileConfiguration langConfig;
    private File langFile;

    // 默认语言（英文），中文可通过配置文件覆盖
    private static final String DEFAULT_LANG = """
            prefix: "&a[TrafficLight] &7"
            error-prefix: "&c[TrafficLight] &7"
            no-permission: "你没有执行此命令的权限！"
            only-player: "仅玩家可执行此命令！"
            command-usage: "用法：/trafficlight <list/create/delete/set/reload>"
            create-usage: "用法：/trafficlight create <ID> <名称>"
            delete-usage: "用法：/trafficlight delete <ID>"
            set-usage: "用法：/trafficlight set <ID> <状态> <持续时间(秒)>"
            set-state-valid: "支持状态：RED/GREEN/YELLOW"
            set-duration-valid: "持续时间必须是正整数！"
            create-success: "成功创建红绿灯：%name%（ID：%id%）"
            create-fail-exist: "创建失败！ID：%id% 已存在"
            delete-success: "成功删除红绿灯（ID：%id%）"
            delete-fail-not-found: "删除失败！未找到ID：%id% 的红绿灯"
            set-success: "成功设置红绿灯（ID：%id%）%state%状态持续时间为 %duration% 秒"
            set-fail-not-found: "设置失败！未找到ID：%id% 的红绿灯"
            reload-success: "配置和红绿灯数据已重新加载！"
            list-empty: "当前无已配置的红绿灯！"
            list-title: "已配置的红绿灯列表："
            activate: "你已进入红绿灯 %name% 的触发范围，已激活！"
            deactivate: "你已离开红绿灯 %name% 的触发范围，已禁用！"
            """;

    public LanguageManager(TrafficLight plugin) {
        this.plugin = plugin;
        loadLanguageFile();
    }

    // 加载语言文件（优先加载自定义，无则生成默认）
    public void loadLanguageFile() {
        langFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!langFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                // 写入默认语言配置
                java.nio.file.Files.write(langFile.toPath(), DEFAULT_LANG.getBytes());
            } catch (IOException e) {
                plugin.getLogger().severe("生成默认语言文件失败：" + e.getMessage());
            }
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        // 替换颜色代码
        langConfig.getKeys(true).forEach(key -> {
            Object value = langConfig.get(key);
            if (value instanceof String) {
                langConfig.set(key, ((String) value).replace("&", "§"));
            }
        });
    }

    // 获取语言文本（支持占位符替换）
    public String getMessage(String key, Object... placeholders) {
        String message = langConfig.getString(key, "未知文本：" + key);
        // 替换占位符（格式：%key% → 对应值）
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                String placeholder = "%" + placeholders[i] + "%";
                String value = String.valueOf(placeholders[i + 1]);
                message = message.replace(placeholder, value);
            }
        }
        // 添加前缀（如果是普通消息）
        if (!key.startsWith("error-") && !key.equals("prefix") && !key.equals("error-prefix")) {
            message = getPrefix() + message;
        } else if (key.startsWith("error-")) {
            message = getErrorPrefix() + message;
        }
        return message;
    }

    // 获取普通前缀
    public String getPrefix() {
        return langConfig.getString("prefix", "§a[TrafficLight] §7");
    }

    // 获取错误前缀
    public String getErrorPrefix() {
        return langConfig.getString("error-prefix", "§c[TrafficLight] §7");
    }

    // 保存自定义语言配置
    public void saveLanguageFile() {
        try {
            langConfig.save(langFile);
        } catch (IOException e) {
            plugin.getLogger().severe("保存语言文件失败：" + e.getMessage());
        }
    }
}
