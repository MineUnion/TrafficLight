package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.World;

import java.util.List;
import java.util.stream.Collectors;

public class ListCommand implements TrafficLightCommand.SubCommand {
    private final TrafficLight plugin;
    private final TrafficLightManager lightManager;

    public ListCommand(TrafficLight plugin) {
        this.plugin = plugin;
        this.lightManager = plugin.getTrafficLightManager();
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("mu.trafficlight.list");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        List<TrafficLightEntity> lights;

        // 按世界筛选
        if (args.length >= 1) {
            String worldName = args[0];
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                sender.sendMessage("§c世界" + worldName + "不存在！");
                return;
            }
            lights = lightManager.getTrafficLightsByWorld(worldName);
            sender.sendMessage("§e===== " + worldName + " 世界的红绿灯列表（共" + lights.size() + "个） =====");
        } else {
            // 所有世界
            lights = lightManager.getAllTrafficLights().stream().collect(Collectors.toList());
            sender.sendMessage("§e===== 所有红绿灯列表（共" + lights.size() + "个） =====");
        }

        // 输出列表
        if (lights.isEmpty()) {
            sender.sendMessage("§7暂无红绿灯");
            return;
        }
        for (TrafficLightEntity tle : lights) {
            sender.sendMessage(String.format(
                    "§6%s §7- 世界：%s | 状态：%s | 分组：%s",
                    tle.getName(),
                    tle.getLocation().getWorld().getName(),
                    tle.getCurrentState().name(),
                    tle.getGroupName()
            ));
        }
    }

    // 补全世界名
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .filter(name -> name.startsWith(args[0]))
                    .collect(Collectors.toList());
        }
        return super.tabComplete(sender, args);
    }
}