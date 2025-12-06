package com.mineunion.trafficlight.manager;

import com.mineunion.trafficlight.TrafficLight;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class PermissionManager {
    private final TrafficLight plugin;

    // 权限常量
    public static final String PERM_ALL = "mu.trafficlight.*";
    public static final String PERM_CREATE = "mu.trafficlight.create";
    public static final String PERM_DELETE = "mu.trafficlight.delete";
    public static final String PERM_SET = "mu.trafficlight.set";
    public static final String PERM_LIST = "mu.trafficlight.list";
    public static final String PERM_RELOAD = "mu.trafficlight.reload";

    public PermissionManager(TrafficLight plugin) {
        this.plugin = plugin;
        // 注册权限（防止权限未加载）
        registerPermissions();
    }

    // 注册权限节点
    private void registerPermissions() {
        // 父权限
        Permission allPerm = new Permission(PERM_ALL, PermissionDefault.OP);
        // 子权限
        Permission createPerm = new Permission(PERM_CREATE, PermissionDefault.OP);
        Permission deletePerm = new Permission(PERM_DELETE, PermissionDefault.OP);
        Permission setPerm = new Permission(PERM_SET, PermissionDefault.OP);
        Permission listPerm = new Permission(PERM_LIST, PermissionDefault.OP);
        Permission reloadPerm = new Permission(PERM_RELOAD, PermissionDefault.OP);

        // 添加父子关系
        allPerm.getChildren().put(PERM_CREATE, true);
        allPerm.getChildren().put(PERM_DELETE, true);
        allPerm.getChildren().put(PERM_SET, true);
        allPerm.getChildren().put(PERM_LIST, true);
        allPerm.getChildren().put(PERM_RELOAD, true);

        // 注册到服务器
        plugin.getServer().getPluginManager().addPermission(allPerm);
        plugin.getServer().getPluginManager().addPermission(createPerm);
        plugin.getServer().getPluginManager().addPermission(deletePerm);
        plugin.getServer().getPluginManager().addPermission(setPerm);
        plugin.getServer().getPluginManager().addPermission(listPerm);
        plugin.getServer().getPluginManager().addPermission(reloadPerm);
    }

    // 校验权限
    public boolean hasPermission(CommandSender sender, String perm) {
        return sender.hasPermission(perm) || sender.isOp();
    }

    // 快捷校验：创建权限
    public boolean canCreate(CommandSender sender) {
        return hasPermission(sender, PERM_CREATE);
    }

    // 快捷校验：删除权限
    public boolean canDelete(CommandSender sender) {
        return hasPermission(sender, PERM_DELETE);
    }

    // 快捷校验：设置权限
    public boolean canSet(CommandSender sender) {
        return hasPermission(sender, PERM_SET);
    }

    // 快捷校验：列表权限
    public boolean canList(CommandSender sender) {
        return hasPermission(sender, PERM_LIST);
    }

    // 快捷校验：重载权限
    public boolean canReload(CommandSender sender) {
        return hasPermission(sender, PERM_RELOAD);
    }
}