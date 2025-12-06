package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCommand implements TrafficLightCommand.SubCommand {
    private final TrafficLight plugin;
    private final TrafficLightManager lightManager;

    public CreateCommand(TrafficLight plugin) {
        this.plugin = plugin;
        this.lightManager = plugin.getTrafficLightManager();
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("mu.trafficlight.create");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 必须是玩家执行（需要位置）
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以执行此指令！");
            return;
        }
        Player player = (Player) sender;

        // 参数校验
        if (args.length < 1) {
            sender.sendMessage("§c用法：/tl create <名称> [时长]");
            return;
        }
        String name = args[0];
        int duration = plugin.getConfigManager().getDefaultDuration();
        if (args.length >= 2) {
            try {
                duration = Integer.parseInt(args[1]);
                if (duration < 1) {
                    sender.sendMessage("§c时长必须大于0！");
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§c时长必须是数字！");
                return;
            }
        }

        // 创建红绿灯
        boolean success = lightManager.createTrafficLight(name, player.getLocation());
        if (success) {
            sender.sendMessage("§a红绿灯" + name + "创建成功！默认时长：" + duration + "秒");
        } else {
            sender.sendMessage("§c红绿灯名称已存在！");
        }
    }
}