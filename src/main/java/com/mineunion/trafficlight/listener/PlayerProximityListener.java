package com.mineunion.trafficlight.listener;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.config.ConfigManager;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import com.mineunion.trafficlight.util.LocationUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerProximityListener implements Listener {
    private final TrafficLight plugin;
    private final TrafficLightManager lightManager;
    private final ConfigManager configManager;

    public PlayerProximityListener(TrafficLight plugin) {
        this.plugin = plugin;
        this.lightManager = plugin.getTrafficLightManager();
        this.configManager = plugin.getConfigManager();
        // 注册监听器
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // 玩家移动时触发（辅助距离检测，可选优化）
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        // 关闭距离激活则跳过
        if (!configManager.isProximityActivation()) {
            return;
        }
        Player player = e.getPlayer();
        int radius = configManager.getActivationRadius();

        // 检测附近红绿灯（可选：仅当玩家移动超过1格时检测，减少性能消耗）
        if (e.getFrom().distance(e.getTo()) < 1) {
            return;
        }

        // 遍历玩家所在世界的红绿灯
        for (TrafficLightEntity tle : lightManager.getTrafficLightsByWorld(player.getWorld().getName())) {
            double distance = LocationUtil.getFlatDistance(player.getLocation(), tle.getLocation());
            // 进入范围激活，离开范围休眠
            if (distance <= radius && !tle.isActivated()) {
                tle.setActivated(true);
                plugin.getLogger().debug("玩家" + player.getName() + "激活红绿灯" + tle.getName());
            } else if (distance > radius && tle.isActivated()) {
                tle.setActivated(false);
                plugin.getLogger().debug("玩家" + player.getName() + "休眠红绿灯" + tle.getName());
            }
        }
    }
}