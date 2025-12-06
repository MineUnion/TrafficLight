package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ListCommand implements TrafficLightCommand.SubCommand {
    private final TrafficLight plugin;

    public ListCommand(TrafficLight plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("mu.trafficlight.list");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c仅玩家可执行此命令！");
            return;
        }

        Player player = (Player) sender;
        List<String> lightNames = plugin.getTrafficLightManager().getAllLightNames();
        
        if (lightNames.isEmpty()) {
            sender.sendMessage("§7当前无已配置的红绿灯！");
            return;
        }

        sender.sendMessage("§a已配置的红绿灯列表：");
        sender.sendMessage("§7- " + String.join("\n§7- ", lightNames));
    }

    // 修复：移除super.tabComplete()，直接返回空列表
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}