package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.manager.LanguageManager;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import org.bukkit.command.CommandSender;

public class DeleteCommand implements TrafficLightCommand.SubCommand {
    private final TrafficLight plugin;
    private final LanguageManager languageManager;
    private final TrafficLightManager trafficLightManager;

    public DeleteCommand(TrafficLight plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.trafficLightManager = plugin.getTrafficLightManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // 参数校验（需传入 ID）
        if (args.length < 1) {
            sender.sendMessage(languageManager.getMessage("argument-error") + "：/tl delete <ID>");
            return true;
        }

        String lightId = args[0];
        // 检查红绿灯是否存在
        if (trafficLightManager.getLight(lightId) == null) {
            sender.sendMessage(
                languageManager.getMessage("light-not-found")
                    .replace("%id%", lightId)
            );
            return true;
        }

        // 调用管理器删除红绿灯
        boolean deleteSuccess = trafficLightManager.deleteTrafficLight(lightId);
        if (deleteSuccess) {
            sender.sendMessage(
                languageManager.getMessage("delete-success")
                    .replace("%id%", lightId)
            );
            // 删除后保存数据
            trafficLightManager.saveAllTrafficLights();
        } else {
            sender.sendMessage(
                languageManager.getMessage("delete-failed")
                    .replace("%id%", lightId)
            );
        }

        return true;
    }
}
