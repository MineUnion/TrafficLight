package com.mineunion.trafficlight.task;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import com.mineunion.trafficlight.manager.ConfigManager; // 正确导入
import com.mineunion.trafficlight.manager.TrafficLightManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class ProximityCheckTask extends BukkitRunnable {
    private final TrafficLight plugin;
    private final ConfigManager configManager;
    private final TrafficLightManager trafficLightManager;
    private final int proximityRadius;

    public ProximityCheckTask(TrafficLight plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.trafficLightManager = plugin.getTrafficLightManager();
        this.proximityRadius = configManager.getProximityRadius();
    }

    @Override
    public void run() {
        // 未开启距离触发则跳过
        if (!configManager.isProximityTrigger()) {
            return;
        }

        List<TrafficLightEntity> allLights = trafficLightManager.getAllTrafficLights();
        if (allLights.isEmpty()) {
            return;
        }

        // 遍历所有玩家
        boolean activationChanged = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            // 遍历所有红绿灯，检查玩家是否在触发范围内
            for (TrafficLightEntity light : allLights) {
                if (light.isActivated()) {
                    continue; // 已激活则跳过
                }

                double distance = player.getLocation().distance(light.getLocation());
                if (distance <= proximityRadius) {
                    // 玩家进入范围，激活红绿灯
                    trafficLightManager.updateLightActivation(light, true);
                    activationChanged = true;
                    if (configManager.isDebugMode()) {
                        plugin.getLogger().info("[Debug] 玩家 " + player.getName() + " 进入范围，激活红绿灯：" + light.getId());
                    }
                }
            }
        }
        // 如果有激活状态变更，保存数据
        if (activationChanged) {
            trafficLightManager.saveAllTrafficLights();
        }
    }
}
