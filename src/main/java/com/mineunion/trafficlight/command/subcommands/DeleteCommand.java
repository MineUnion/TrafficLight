package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import org.bukkit.command.CommandSender;

public class DeleteCommand implements TrafficLightCommand.SubCommand {
    private final TrafficLight plugin;
    private final TrafficLightManager lightManager;

    public DeleteCommand(TrafficLight plugin) {
        this.plugin = plugin;
        this.lightManager = plugin.getTrafficLightManager();
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("mu.trafficlight.delete");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 参数校验
        if (args.length < 1) {
            sender.sendMessage("§c用法：/tl delete <名称>");
            return;
        }
        String name = args[0];

        // 删除红绿灯
        boolean success = lightManager.deleteTrafficLight(name);
        if (success) {
            sender.sendMessage("§a红绿灯" + name + "删除成功！");
        } else {
            sender.sendMessage("§c红绿灯" + name + "不存在！");
        }
    }
}