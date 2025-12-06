package com.mineunion.trafficlight.task;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class LightUpdateTask extends BukkitRunnable {
    private final TrafficLight plugin;
    private final TrafficLightEntity light;

    public LightUpdateTask(TrafficLight plugin, TrafficLightEntity light) {
        this.plugin = plugin;
        this.light = light;
    }

    @Override
    public void run() {
        // 切换红绿灯状态
        TrafficLightEntity.LightState nextState = light.getState() == TrafficLightEntity.LightState.RED ?
                TrafficLightEntity.LightState.GREEN : TrafficLightEntity.LightState.RED;
        light.setState(nextState);
        
        // 修复：替换debug()为info()，添加[DEBUG]标识
        plugin.getLogger().info("[DEBUG] 红绿灯" + light.getId() + "切换为" + nextState + "状态");
    }
}