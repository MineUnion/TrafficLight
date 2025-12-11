package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.manager.LanguageManager;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCommand implements TrafficLightCommand.SubCommand {
    private final TrafficLight plugin;
    private final LanguageManager languageManager;
    private final TrafficLightManager trafficLightManager;

    public CreateCommand(TrafficLight plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.trafficLightManager = plugin.getTrafficLightManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // 仅玩家可执行
        if (!(sender instanceof Player player)) {
            sender.sendMessage(languageManager.getMessage("argument-error") + "：仅玩家可执行该命令");
            return true;
        }

        // 参数校验（需传入 ID 和名称）
        if (args.length < 2) {
            sender.sendMessage(languageManager.getMessage("argument-error") + "：/tl create <ID> <名称>");
            return true;
        }

        String lightId = args[0];
        // 拼接名称（支持空格和中文）
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            nameBuilder.append(args[i]).append(" ");
        }
        String lightName = nameBuilder.toString().trim();

        // 获取玩家准星指向的方块位置
        org.bukkit.util.RayTraceResult rayTraceResult = player.rayTraceBlocks(50); // 最大追踪距离50格
        org.bukkit.Location targetLocation;
        if (rayTraceResult != null && rayTraceResult.getHitBlock() != null) {
            // 如果命中方块，使用方块位置
            targetLocation = rayTraceResult.getHitBlock().getLocation();
        } else {
            // 如果没有命中方块，使用玩家当前位置作为备选
            targetLocation = player.getLocation();
        }
        
        // 调用管理器创建红绿灯
        boolean createSuccess = trafficLightManager.createTrafficLight(lightId, lightName, targetLocation);
        if (createSuccess) {
            sender.sendMessage(
                languageManager.getMessage("create-success")
                    .replace("%id%", lightId)
                    .replace("%name%", lightName)
            );
            // 创建后保存数据
            trafficLightManager.saveAllTrafficLights();
        } else {
            sender.sendMessage(
                languageManager.getMessage("create-failed")
                    .replace("%id%", lightId)
            );
        }

        return true;
    }
}
