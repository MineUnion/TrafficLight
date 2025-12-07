package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import com.mineunion.trafficlight.manager.LanguageManager;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import org.bukkit.command.CommandSender;

public class ToggleCommand implements TrafficLightCommand.SubCommand {
    private final TrafficLight plugin;
    private final LanguageManager languageManager;
    private final TrafficLightManager trafficLightManager;

    public ToggleCommand(TrafficLight plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.trafficLightManager = plugin.getTrafficLightManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // 参数校验（需传入 ID）
        if (args.length < 1) {
            sender.sendMessage(languageManager.getMessage("argument-error") + "：/tl toggle <ID> [activate/deactivate]");
            return true;
        }

        String lightId = args[0];
        TrafficLightEntity light = trafficLightManager.getLight(lightId);

        // 检查红绿灯是否存在
        if (light == null) {
            sender.sendMessage(
                languageManager.getMessage("light-not-found")
                    .replace("%id%", lightId)
            );
            return true;
        }

        // 处理可选参数（activate/deactivate）
        if (args.length >= 2) {
            String action = args[1].toLowerCase();
            switch (action) {
                case "activate" -> {
                    trafficLightManager.updateLightActivation(light, true);
                    sender.sendMessage(languageManager.getMessage("toggle-activate").replace("%id%", lightId));
                }
                case "deactivate" -> {
                    trafficLightManager.updateLightActivation(light, false);
                    sender.sendMessage(languageManager.getMessage("toggle-deactivate").replace("%id%", lightId));
                }
                default -> sender.sendMessage(languageManager.getMessage("argument-error") + "：可选参数仅支持 activate/deactivate");
            }
        } else {
            // 无参数时切换状态
            boolean newState = !light.isActivated();
            trafficLightManager.updateLightActivation(light, newState);
            if (newState) {
                sender.sendMessage(languageManager.getMessage("toggle-activate").replace("%id%", lightId));
            } else {
                sender.sendMessage(languageManager.getMessage("toggle-deactivate").replace("%id%", lightId));
            }
        }

        // 保存状态变更
        trafficLightManager.saveAllTrafficLights();
        return true;
    }
}
