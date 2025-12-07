package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.manager.LanguageManager;
import org.bukkit.command.CommandSender;

import java.util.List;

public class HelpCommand implements TrafficLightCommand.SubCommand {
    private final TrafficLight plugin;
    private final LanguageManager languageManager;

    public HelpCommand(TrafficLight plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // 发送帮助标题
        sender.sendMessage(languageManager.getMessage("help-title"));

        // 发送所有命令帮助（从 messages.yml 读取）
        List<String> helpItems = languageManager.getMessageList("help-item");
        for (String item : helpItems) {
            sender.sendMessage(item);
        }

        // 补充说明
        sender.sendMessage("§7权限说明：普通玩家需OP分配权限后使用，部分命令仅OP可用");
        return true;
    }
}
