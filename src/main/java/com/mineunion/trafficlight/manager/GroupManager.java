package com.mineunion.trafficlight.manager;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.config.ConfigManager;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GroupManager {
    private final TrafficLight plugin;
    private final ConfigManager configManager;
    private final TrafficLightManager lightManager;
    // 分组存储：分组名 -> (世界名, 红绿灯列表, 是否同步)
    private final Map<String, GroupInfo> groupMap = new ConcurrentHashMap<>();

    // 分组信息内部类
    public static class GroupInfo {
        private final String world;
        private final List<String> lightNames;
        private final boolean sync;

        public GroupInfo(String world, List<String> lightNames, boolean sync) {
            this.world = world;
            this.lightNames = lightNames;
            this.sync = sync;
        }

        // Getter
        public String getWorld() { return world; }
        public List<String> getLightNames() { return lightNames; }
        public boolean isSync() { return sync; }
    }

    public GroupManager(TrafficLight plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.lightManager = plugin.getTrafficLightManager();
        // 加载分组配置
        loadGroupsFromConfig();
    }

    // 从配置加载分组
    private void loadGroupsFromConfig() {
        ConfigurationSection groupsSection = configManager.getConfig().getConfigurationSection("groups");
        if (groupsSection == null) {
            return;
        }

        for (String groupName : groupsSection.getKeys(false)) {
            String world = groupsSection.getString(groupName + ".world", "world");
            List<String> lightNames = groupsSection.getStringList(groupName + ".lights");
            boolean sync = groupsSection.getBoolean(groupName + ".sync", false);
            groupMap.put(groupName, new GroupInfo(world, lightNames, sync));
        }
        plugin.getLogger().info("加载了" + groupMap.size() + "个红绿灯分组");
    }

    // 获取分组信息
    public GroupInfo getGroup(String groupName) {
        return groupMap.get(groupName);
    }

    // 给红绿灯分配分组
    public boolean assignLightToGroup(String lightName, String groupName) {
        TrafficLightEntity tle = lightManager.getTrafficLight(lightName);
        GroupInfo group = groupMap.get(groupName);
        if (tle == null || group == null) {
            return false;
        }
        // 检查世界匹配
        if (!tle.getLocation().getWorld().getName().equals(group.getWorld())) {
            plugin.getLogger().warning("红绿灯" + lightName + "所在世界与分组" + groupName + "不匹配！");
            return false;
        }
        tle.setGroupName(groupName);
        // 同步分组时，更新所有同组红绿灯状态
        if (group.isSync()) {
            syncGroupLights(groupName, tle.getCurrentState());
        }
        return true;
    }

    // 同步分组内红绿灯状态
    public void syncGroupLights(String groupName, TrafficLightEntity.LightState state) {
        GroupInfo group = groupMap.get(groupName);
        if (group == null || !group.isSync()) {
            return;
        }
        for (String lightName : group.getLightNames()) {
            TrafficLightEntity tle = lightManager.getTrafficLight(lightName);
            if (tle != null) {
                tle.setCurrentState(state);
                plugin.getLogger().debug("同步分组" + groupName + "的红绿灯" + lightName + "为" + state + "状态");
            }
        }
    }

    // 获取所有分组名
    public Set<String> getAllGroupNames() {
        return groupMap.keySet();
    }
}