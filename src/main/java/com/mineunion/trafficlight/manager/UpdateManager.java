package com.mineunion.trafficlight.manager;

import com.mineunion.trafficlight.TrafficLight;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UpdateManager implements Listener {
    private final TrafficLight plugin;
    private final String currentVersion;
    private final String githubRepository = "MineUnion/TrafficLight";
    private boolean updateAvailable = false;
    private String latestVersion = "";
    private String downloadUrl = "";
    private boolean checkUpdatesOnJoin = true;

    public UpdateManager(TrafficLight plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
        this.checkUpdatesOnJoin = plugin.getConfigManager().getConfig().getBoolean("check-updates-on-join", true);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // 检查更新
    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL("https://api.github.com/repos/" + githubRepository + "/releases/latest");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                if (connection.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    String jsonResponse = response.toString();
                    latestVersion = getValueFromJson(jsonResponse, "tag_name");
                    downloadUrl = getValueFromJson(jsonResponse, "html_url");

                    if (latestVersion != null && !latestVersion.equals(currentVersion)) {
                        updateAvailable = true;
                        plugin.getLogger().info(ChatColor.GREEN + "[TrafficLight] Update available! Current: " + currentVersion + ", Latest: " + latestVersion);
                        plugin.getLogger().info(ChatColor.GREEN + "[TrafficLight] Download: " + downloadUrl);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "[TrafficLight] Failed to check for updates: " + e.getMessage());
            }
        });
    }

    // 从JSON字符串中提取值
    private String getValueFromJson(String json, String key) {
        try {
            int startIndex = json.indexOf('"' + key + '"') + key.length() + 3;
            int endIndex = json.indexOf('"', startIndex);
            return json.substring(startIndex, endIndex);
        } catch (Exception e) {
            return null;
        }
    }

    // 玩家加入事件，通知OP有更新可用
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (checkUpdatesOnJoin && updateAvailable && (player.isOp() || player.hasPermission("mu.trafficlight.update"))) {
            player.sendMessage(ChatColor.YELLOW + "[TrafficLight] Update available! Current: " + currentVersion + ", Latest: " + latestVersion);
            player.sendMessage(ChatColor.YELLOW + "[TrafficLight] Download: " + downloadUrl);
        }
    }

    // 获取是否有更新可用
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    // 获取最新版本
    public String getLatestVersion() {
        return latestVersion;
    }

    // 获取下载链接
    public String getDownloadUrl() {
        return downloadUrl;
    }

    // 获取当前版本
    public String getCurrentVersion() {
        return currentVersion;
    }

    // 设置是否在玩家加入时检查更新
    public void setCheckUpdatesOnJoin(boolean checkUpdatesOnJoin) {
        this.checkUpdatesOnJoin = checkUpdatesOnJoin;
    }

    // 是否在玩家加入时检查更新
    public boolean isCheckUpdatesOnJoin() {
        return checkUpdatesOnJoin;
    }
}