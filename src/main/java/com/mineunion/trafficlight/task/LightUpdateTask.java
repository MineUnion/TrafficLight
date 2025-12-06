package com.mineunion.trafficlight.task;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import org.bukkit.scheduler.BukkitRunnable;

public class LightUpdateTask extends BukkitRunnable {
    private final TrafficLight plugin;
    private final TrafficLightManager lightManager;

    public LightUpdateTask(TrafficLight plugin) {
        this.plugin = plugin;
        this.lightManager = plugin.getTrafficLightManager();
    }

    @Override
    public void run() {
        // 遍历所有红绿灯，更新状态
        for (TrafficLightEntity tle : lightManager.getAllTrafficLights()) {
            // 未激活则跳过
            if (!tle.isActivated()) {
                continue;
            }
            // 检查是否需要切换状态
            if (tle.updateDuration()) {
                tle.switchState();
                // TODO: 这里可以添加灯光特效/粒子效果等可视化逻辑
                plugin.getLogger().debug("红绿灯" + tle.getName() + "切换为" + tle.getCurrentState() + "状态");
            }
        }
    }
}