package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import com.mineunion.trafficlight.manager.LanguageManager;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ListCommand implements TrafficLightCommand.SubCommand {
    private final TrafficLight plugin;
    private final LanguageManager languageManager;
    private final TrafficLightManager trafficLightManager;

    public ListCommand(TrafficLight plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.trafficLightManager = plugin.getTrafficLightManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        List<TrafficLightEntity> allLights = trafficLightManager.getAllTrafficLights();
        int lightCount = allLights.size();

        // 发送标题
        sender.sendMessage(
            languageManager.getMessage("list-title")
                .replace("%count%", String.valueOf(lightCount))
        );

        // 发送每个红绿灯信息
        if (lightCount == 0) {
            sender.sendMessage("§7- 暂无已创建的红绿灯");
            return true;
        }

        for (TrafficLightEntity light : allLights) {
            String state = switch (light.getState()) {
                case RED -> "§c红灯";
                case GREEN -> "§a绿灯";
                case YELLOW -> "§e黄灯";
            };
            sender.sendMessage(
                languageManager.getMessage("list-item")
                    .replace("%id%", light.getId())
                    .replace("%name%", light.getName())
                    .replace("%world%", light.getLocation().getWorld().getName())
                    .replace("%state%", state)
            );
        }

        return true;
    }
}
