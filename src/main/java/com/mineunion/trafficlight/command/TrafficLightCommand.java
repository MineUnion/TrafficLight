package com.mineunion.trafficlight.command;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.subcommands.*;
import com.mineunion.trafficlight.manager.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TrafficLightCommand implements CommandExecutor {
    private final TrafficLight plugin;
    private final LanguageManager languageManager;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    // 内部子命令接口（统一所有子命令格式）
    public interface SubCommand {
        boolean execute(CommandSender sender, String[] args);
    }

    public TrafficLightCommand(TrafficLight plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        // 注册所有子命令（统一使用 SubCommand 接口）
        registerSubCommands();
    }

    // 注册子命令
    private void registerSubCommands() {
        subCommands.put("create", new CreateCommand(plugin));
        subCommands.put("delete", new DeleteCommand(plugin));
        subCommands.put("list", new ListCommand(plugin));
        subCommands.put("info", new InfoCommand(plugin));
        subCommands.put("set", new SetCommand(plugin));
        subCommands.put("switch", new SwitchCommand(plugin));
        subCommands.put("toggle", new ToggleCommand(plugin));
        subCommands.put("reload", new ReloadCommand(plugin));
        subCommands.put("help", new HelpCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 无参数时显示帮助
        if (args.length == 0) {
            subCommands.get("help").execute(sender, args);
            return true;
        }

        // 获取子命令名称
        String subCommandName = args[0].toLowerCase();
        // 检查子命令是否存在
        if (!subCommands.containsKey(subCommandName)) {
            sender.sendMessage(languageManager.getMessage("command-not-found"));
            return true;
        }

        // 执行子命令（截取参数，去掉子命令名称）
        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, subArgs.length);
        return subCommands.get(subCommandName).execute(sender, subArgs);
    }

    // 获取子命令集合（供帮助命令使用）
    public Map<String, SubCommand> getSubCommands() {
        return subCommands;
    }
}
