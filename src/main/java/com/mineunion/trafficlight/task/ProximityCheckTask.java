package com.mineunion.trafficlight.task;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import com.mineunion.trafficlight.manager.ConfigManager;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class ProximityCheckTask extends BukkitRunnable {
    private final TrafficLight plugin;
    private final TrafficLightManager lightManager;
    private final ConfigManager configManager;
    private int proximityRadius;

    public ProximityCheckTask(TrafficLight plugin) {
        this.plugin = plugin;
        this.lightManager = plugin.getTrafficLightManager();
        this.configManager = plugin.getConfigManager();
        this.proximityRadius = configManager.getProximityRadius();
    }

    @Override
    public void run() {
        // 关键修复：调用正确的 getAllTrafficLights() 方法
        List<TrafficLightEntity> allLights = lightManager.getAllTrafficLights();
        if (allLights.isEmpty()) {
            return;
        }

        // 异步检测（如果配置启用）
        if (configManager.isAsyncProximityCheck()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                executeProximityCheck(allLights);
            });
        } else {
            executeProximityCheck(allLights);
        }
    }

    // 核心检测逻辑
    private void executeProximityCheck(List<TrafficLightEntity> allLights) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String worldName = player.getWorld().getName();
            List<TrafficLightEntity> lightsInWorld = lightManager.getTrafficLightsByWorld(worldName);

            for (TrafficLightEntity tle : lightsInWorld) {
                double distance = player.getLocation().distance(tle.getLocation());
                boolean shouldActivate = distance <= proximityRadius;

                if (tle.isActivated() != shouldActivate) {
                    // 切换到主线程更新状态（避免异步操作实体）
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        lightManager.updateLightActivation(tle, shouldActivate);
                    });
                }
            }
        }
    }
}
