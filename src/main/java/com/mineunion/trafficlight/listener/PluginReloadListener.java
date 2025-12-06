package com.mineunion.trafficlight.listener;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

public class PluginReloadListener implements Listener {
    private final TrafficLight plugin;
    private final TrafficLightManager lightManager;

    public PluginReloadListener(TrafficLight plugin) {
        this.plugin = plugin;
        this.lightManager = plugin.getTrafficLightManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // 插件禁用时保存数据
    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(plugin)) {
            lightManager.saveAllTrafficLights(); // 调用补充的saveAllTrafficLights方法
        }
    }
}
