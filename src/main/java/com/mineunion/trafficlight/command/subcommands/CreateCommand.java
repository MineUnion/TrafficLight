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

        // 调用管理器创建红绿灯
        boolean createSuccess = trafficLightManager.createTrafficLight(lightId, lightName, player.getLocation());
        if (createSuccess) {
            sender.sendMessage(
                languageManager.getMessage("create-success")
                    .replace("%id%", lightId)
                    .replace("%name%", lightName)
            );
        } else {
            sender.sendMessage(
                languageManager.getMessage("create-failed")
                    .replace("%id%", lightId)
            );
        }

        return true;
    }
}
