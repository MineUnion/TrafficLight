package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import com.mineunion.trafficlight.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

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
        // 权限校验
        if (!hasPermission(sender)) {
            MessageUtil.sendError(sender, "no-permission");
            return;
        }

        // 仅玩家可执行（创建位置基于玩家当前坐标）
        if (!(sender instanceof Player player)) {
            MessageUtil.sendError(sender, "only-player");
            return;
        }

        // 参数校验（必须传入 ID 和名称）
        if (args.length < 3) {
            MessageUtil.sendError(sender, "create-usage");
            return;
        }

        String id = args[1];
        String name = args[2];

        // 校验 ID 格式（仅允许字母、数字、下划线）
        if (!id.matches("^[a-zA-Z0-9_]+$")) {
            MessageUtil.sendError(sender, "create-fail-invalid-id");
            return;
        }

        // 调用管理器创建红绿灯（位置为玩家当前位置）
        boolean success = lightManager.createTrafficLight(id, name, player.getLocation());

        if (success) {
            MessageUtil.sendMessage(sender, "create-success", "name", name, "id", id);
        } else {
            MessageUtil.sendError(sender, "create-fail-exist", "id", id);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // 仅当有创建权限时显示补全
        if (!hasPermission(sender)) return List.of();
        
        // 补全逻辑：args[1] 为 ID（无补全），args[2] 为名称（无补全）
        return List.of();
    }
}
