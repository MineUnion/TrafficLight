package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import com.mineunion.trafficlight.manager.LanguageManager;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import org.bukkit.command.CommandSender;

// 类名=文件名：SwitchCommand
public class SwitchCommand implements TrafficLightCommand.SubCommand {
    private final TrafficLight plugin;
    private final LanguageManager languageManager;
    private final TrafficLightManager trafficLightManager;

    public SwitchCommand(TrafficLight plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.trafficLightManager = plugin.getTrafficLightManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // 参数校验（需传入 ID、目标状态）
        if (args.length < 2) {
            sender.sendMessage(languageManager.getMessage("argument-error") + "：/tl switch <ID> <state>（state：red/green/yellow）");
            return true;
        }

        String lightId = args[0];
        String stateStr = args[1].toLowerCase();

        // 解析灯色状态
        TrafficLightEntity.LightState state;
        try {
            state = TrafficLightEntity.LightState.valueOf(stateStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(languageManager.getMessage("argument-error") + "：状态仅支持 red/green/yellow");
            return true;
        }

        // 检查红绿灯是否存在
        if (trafficLightManager.getLight(lightId) == null) {
            sender.sendMessage(
                languageManager.getMessage("light-not-found")
                    .replace("%id%", lightId)
            );
            return true;
        }

        // 调用管理器切换状态
        boolean switchSuccess = trafficLightManager.switchLightState(lightId, state);
        if (switchSuccess) {
            String stateName = switch (state) {
                case RED -> "红灯";
                case GREEN -> "绿灯";
                case YELLOW -> "黄灯";
            };
            sender.sendMessage(
                languageManager.getMessage("switch-state-success")
                    .replace("%id%", lightId)
                    .replace("%state%", stateName)
            );
            // 切换状态后保存数据
            trafficLightManager.saveAllTrafficLights();
        } else {
            sender.sendMessage(languageManager.getMessage("argument-error") + "：切换灯色失败");
        }

        return true;
    }
}
