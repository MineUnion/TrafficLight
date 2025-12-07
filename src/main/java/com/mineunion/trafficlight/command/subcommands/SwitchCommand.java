package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import com.mineunion.trafficlight.manager.LanguageManager;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import org.bukkit.command.CommandSender;

public class SetCommand implements TrafficLightCommand.SubCommand {
    private final TrafficLight plugin;
    private final LanguageManager languageManager;
    private final TrafficLightManager trafficLightManager;

    public SetCommand(TrafficLight plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.trafficLightManager = plugin.getTrafficLightManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // 参数校验（需传入 ID、状态、时长）
        if (args.length < 3) {
            sender.sendMessage(languageManager.getMessage("argument-error") + "：/tl set <ID> <state> <秒数>（state：red/green/yellow）");
            return true;
        }

        String lightId = args[0];
        String stateStr = args[1].toLowerCase();
        int seconds;

        // 解析时长（必须为正整数）
        try {
            seconds = Integer.parseInt(args[2]);
            if (seconds <= 0) {
                sender.sendMessage(languageManager.getMessage("set-duration-failed"));
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(languageManager.getMessage("argument-error") + "：时长必须是整数");
            return true;
        }

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

        // 调用管理器设置时长
        boolean setSuccess = trafficLightManager.setLightDuration(lightId, state, seconds);
        if (setSuccess) {
            String stateName = switch (state) {
                case RED -> "红灯";
                case GREEN -> "绿灯";
                case YELLOW -> "黄灯";
            };
            sender.sendMessage(
                languageManager.getMessage("set-duration-success")
                    .replace("%id%", lightId)
                    .replace("%state%", stateName)
                    .replace("%seconds%", String.valueOf(seconds))
            );
            // 设置后保存数据
            trafficLightManager.saveAllTrafficLights();
        } else {
            sender.sendMessage(languageManager.getMessage("set-duration-failed"));
        }

        return true;
    }
}
