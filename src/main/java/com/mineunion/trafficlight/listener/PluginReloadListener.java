package com.mineunion.trafficlight.listener;

import com.mineunion.trafficlight.TrafficLight;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public class PluginReloadListener implements Listener {
    private final TrafficLight plugin;

    public PluginReloadListener(TrafficLight plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // 插件启用时触发
    @EventHandler
    public void onPluginEnable(PluginEnableEvent e) {
        if (e.getPlugin().getName().equals(plugin.getName())) {
            plugin.getLogger().info("插件启用完成，监听事件已注册！");
        }
    }

    // 插件禁用时触发
    @EventHandler
    public void onPluginDisable(PluginDisableEvent e) {
        if (e.getPlugin().getName().equals(plugin.getName())) {
            // 保存数据
            plugin.getTrafficLightManager().saveAllTrafficLights();
            plugin.getLogger().info("插件禁用，数据已保存！");
        }
    }
}