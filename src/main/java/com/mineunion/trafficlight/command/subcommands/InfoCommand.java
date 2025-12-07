package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import com.mineunion.trafficlight.manager.LanguageManager;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import org.bukkit.command.CommandSender;
import org.bukkit.Location;

public class InfoCommand implements TrafficLightCommand.SubCommand {
    private final TrafficLight plugin;
    private final LanguageManager languageManager;
    private final TrafficLightManager trafficLightManager;

    public InfoCommand(TrafficLight plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.trafficLightManager = plugin.getTrafficLightManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // 参数校验（需传入 ID）
        if (args.length < 1) {
            sender.sendMessage(languageManager.getMessage("argument-error") + "：/tl info <ID>");
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

        // 拼接详情信息
        Location loc = light.getLocation();
        String activated = light.isActivated() ? "§a已激活" : "§c未激活";
        String redDur = String.valueOf(light.getDuration(TrafficLightEntity.LightState.RED));
        String greenDur = String.valueOf(light.getDuration(TrafficLightEntity.LightState.GREEN));
        String yellowDur = String.valueOf(light.getDuration(TrafficLightEntity.LightState.YELLOW));

        sender.sendMessage(
            languageManager.getMessage("info-title")
                .replace("%id%", lightId)
        );
        sender.sendMessage("§7- ID：§e" + light.getId());
        sender.sendMessage("§7- 名称：§a" + light.getName());
        sender.sendMessage("§7- 世界：§b" + loc.getWorld().getName());
        sender.sendMessage("§7- 坐标：§6(" + String.format("%.1f", loc.getX()) + ", " + String.format("%.1f", loc.getY()) + ", " + String.format("%.1f", loc.getZ()) + ")");
        sender.sendMessage("§7- 当前状态：" + switch (light.getState()) {
            case RED -> "§c红灯";
            case GREEN -> "§a绿灯";
            case YELLOW -> "§e黄灯";
        });
        sender.sendMessage("§7- 激活状态：" + activated);
        sender.sendMessage("§7- 时长配置：§c红灯" + redDur + "秒 §a绿灯" + greenDur + "秒 §e黄灯" + yellowDur + "秒");

        return true;
    }
}
