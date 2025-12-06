package com.mineunion.trafficlight.task;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class LightUpdateTask extends BukkitRunnable {
    private final TrafficLight plugin;
    private final TrafficLightEntity light;

    // 原构造器（保留，需传入2个参数）
    public LightUpdateTask(TrafficLight plugin, TrafficLightEntity light) {
        this.plugin = plugin;
        this.light = light;
    }

    // 修复：新增支持单个TrafficLight参数的构造器（适配调用时只传plugin的场景）
    public LightUpdateTask(TrafficLight plugin) {
        this.plugin = plugin;
        this.light = null; // 或根据业务逻辑初始化默认红绿灯
    }

    @Override
    public void run() {
        if (light == null) return;

        // 切换红绿灯状态（按持续时间切换）
        TrafficLightEntity.LightState currentState = light.getState();
        TrafficLightEntity.LightState nextState;
        switch (currentState) {
            case RED:
                nextState = TrafficLightEntity.LightState.GREEN;
                break;
            case GREEN:
                nextState = TrafficLightEntity.LightState.YELLOW;
                break;
            case YELLOW:
                nextState = TrafficLightEntity.LightState.RED;
                break;
            default:
                nextState = TrafficLightEntity.LightState.RED;
        }

        light.setState(nextState);
        plugin.getLogger().info("[DEBUG] 红绿灯" + light.getName() + "切换为" + nextState + "状态（持续" + light.getDuration(nextState) + "秒）");

        // 重新调度任务（按当前状态的持续时间）
        this.cancel();
        new LightUpdateTask(plugin, light).runTaskLater(plugin, light.getDuration(nextState) * 20L);
    }
}
