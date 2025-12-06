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
        this.configManager = plugin.getConfigManager(); // 修复：通过plugin获取ConfigManager
        this.proximityRadius = configManager.getProximityRadius();
    }

    @Override
    public void run() {
        // 获取所有红绿灯
        List<TrafficLightEntity> allLights = lightManager.getAllTrafficLights();
        if (allLights.isEmpty()) return;

        // 遍历所有玩家，检测距离
        for (Player player : Bukkit.getOnlinePlayers()) {
            String worldName = player.getWorld().getName();
            List<TrafficLightEntity> lightsInWorld = lightManager.getTrafficLightsByWorld(worldName);

            for (TrafficLightEntity tle : lightsInWorld) {
                double distance = player.getLocation().distance(tle.getLocation());
                boolean shouldActivate = distance <= proximityRadius;

                // 更新激活状态（调用补充的updateLightActivation方法）
                if (tle.isActivated() != shouldActivate) {
                    lightManager.updateLightActivation(tle, shouldActivate);
                }
            }
        }
    }
}
