package com.mineunion.trafficlight.listener;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import com.mineunion.trafficlight.manager.ConfigManager;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import com.mineunion.trafficlight.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

public class PlayerProximityListener implements Listener {
    private final TrafficLight plugin;
    private final TrafficLightManager lightManager;
    private final ConfigManager configManager;
    private int proximityRadius;

    public PlayerProximityListener(TrafficLight plugin) {
        this.plugin = plugin;
        this.lightManager = plugin.getTrafficLightManager();
        this.configManager = plugin.getConfigManager(); // 修复：获取ConfigManager
        this.proximityRadius = configManager.getProximityRadius();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        // 调用补充的getTrafficLightsByWorld方法
        List<TrafficLightEntity> lightsInWorld = lightManager.getTrafficLightsByWorld(worldName);

        for (TrafficLightEntity tle : lightsInWorld) {
            double distance = player.getLocation().distance(tle.getLocation());
            boolean isInRange = distance <= proximityRadius;

            // 距离触发激活/禁用（示例逻辑，可根据需求调整）
            if (isInRange && !tle.isActivated()) {
                lightManager.updateLightActivation(tle, true);
                MessageUtil.sendMessage(player, "你已进入红绿灯 " + tle.getName() + " 的触发范围，已激活！");
            } else if (!isInRange && tle.isActivated()) {
                lightManager.updateLightActivation(tle, false);
                MessageUtil.sendMessage(player, "你已离开红绿灯 " + tle.getName() + " 的触发范围，已禁用！");
            }
        }
    }
}
