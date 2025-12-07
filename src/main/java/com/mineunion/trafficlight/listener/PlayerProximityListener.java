package com.mineunion.trafficlight.listener;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import com.mineunion.trafficlight.manager.ConfigManager; // 正确导入
import com.mineunion.trafficlight.manager.TrafficLightManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class PlayerProximityListener implements Listener {
    private final TrafficLight plugin;
    private final ConfigManager configManager;
    private final TrafficLightManager trafficLightManager;
    private final int proximityRadius;

    public PlayerProximityListener(TrafficLight plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.trafficLightManager = plugin.getTrafficLightManager();
        this.proximityRadius = configManager.getProximityRadius();
    }

    // 玩家退出服务器时，禁用所有由该玩家激活的红绿灯
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!configManager.isProximityTrigger()) {
            return;
        }

        Player player = event.getPlayer();
        List<TrafficLightEntity> allLights = trafficLightManager.getAllTrafficLights();

        for (TrafficLightEntity light : allLights) {
            if (light.isActivated() && player.getLocation().distance(light.getLocation()) <= proximityRadius) {
                trafficLightManager.updateLightActivation(light, false);
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[Debug] 玩家 " + player.getName() + " 退出，禁用红绿灯：" + light.getId());
                }
            }
        }
    }

    // 玩家传送时，禁用原位置附近的红绿灯
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!configManager.isProximityTrigger() || event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        List<TrafficLightEntity> allLights = trafficLightManager.getAllTrafficLights();

        for (TrafficLightEntity light : allLights) {
            if (light.isActivated() && player.getLocation().distance(light.getLocation()) <= proximityRadius) {
                trafficLightManager.updateLightActivation(light, false);
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[Debug] 玩家 " + player.getName() + " 传送，禁用红绿灯：" + light.getId());
                }
            }
        }
    }
}
