package com.mineunion.trafficlight;

import com.mineunion.trafficlight.command.subcommands.ListCommand;
import com.mineunion.trafficlight.manager.GroupManager;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import com.mineunion.trafficlight.task.LightUpdateTask;
import com.mineunion.trafficlight.util.SerializationUtil;
import org.bukkit.plugin.java.JavaPlugin;

public final class TrafficLight extends JavaPlugin {
    private TrafficLightManager trafficLightManager;
    private GroupManager groupManager;

    @Override
    public void onEnable() {
        // 初始化管理器
        trafficLightManager = new TrafficLightManager(this);
        groupManager = new GroupManager(this);
        
        // 注册序列化工具
        SerializationUtil.registerSerializables();
        
        // 修复：LightUpdateTask构造器参数匹配（根据业务需求选择传入/不传入红绿灯）
        // 场景1：启动默认红绿灯的更新任务（需先添加红绿灯）
        // trafficLightManager.addLight("test", "测试红绿灯", getServer().getWorld("world"), 100, 64, 200);
        // new LightUpdateTask(this, trafficLightManager.getLight("test")).runTaskLater(this, 20L);
        
        // 场景2：启动空任务（适配原代码只传plugin的调用）
        new LightUpdateTask(this).runTaskLater(this, 20L);

        // 注册命令
        this.getCommand("trafficlight").setExecutor((sender, command, label, args) -> {
            if (args.length == 0) {
                sender.sendMessage("§c用法：/trafficlight list");
                return true;
            }
            
            if (args[0].equalsIgnoreCase("list")) {
                new ListCommand(this).execute(sender, args);
                return true;
            }
            return false;
        });

        this.getLogger().info("TrafficLight插件已启用！");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("TrafficLight插件已禁用！");
    }

    // Getter方法（供其他类调用管理器）
    public TrafficLightManager getTrafficLightManager() {
        return trafficLightManager;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }
}
