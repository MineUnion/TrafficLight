package com.mineunion.trafficlight.task;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.config.ConfigManager;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ProximityCheckTask extends BukkitRunnable {
    private final TrafficLight plugin;
    private final TrafficLightManager lightManager;
    private final ConfigManager configManager;

    public ProximityCheckTask(TrafficLight plugin) {
        this.plugin = plugin;
        this.lightManager = plugin.getTrafficLightManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public void run() {
        // 如果关闭距离激活，全部激活
        if (!configManager.isProximityActivation()) {
            for (TrafficLightEntity tle : lightManager.getAllTrafficLights()) {
                tle.setActivated(true);
            }
            return;
        }

        // 遍历所有红绿灯，检测玩家距离
        int activationRadius = configManager.getActivationRadius();
        for (TrafficLightEntity tle : lightManager.getAllTrafficLights()) {
            boolean hasPlayerNear = false;
            // 检测同世界内的玩家
            for (Player player : tle.getLocation().getWorld().getPlayers()) {
                double distance = player.getLocation().distance(tle.getLocation());
                if (distance <= activationRadius) {
                    hasPlayerNear = true;
                    break;
                }
            }
            // 更新激活状态
            lightManager.updateLightActivation(tle, hasPlayerNear);
        }
    }
}