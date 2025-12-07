package com.mineunion.trafficlight.manager;

import com.mineunion.trafficlight.TrafficLight;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateManager {
    private final TrafficLight plugin;
    private final String currentVersion;
    private final String githubRepo = "MineUnion/TrafficLight";
    private final String githubApiUrl = "https://api.github.com/repos/" + githubRepo + "/releases/latest";
    
    public UpdateManager(TrafficLight plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
    }
    
    // 检查更新
    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String latestVersion = getLatestVersion();
                if (latestVersion != null && isNewerVersion(latestVersion)) {
                    plugin.getLogger().info("§a发现新版本：§e" + latestVersion + " §a当前版本：§e" + currentVersion);
                    plugin.getLogger().info("§a更新地址：§ehttps://github.com/" + githubRepo + "/releases/latest");
                    plugin.getLogger().info("§a使用 /tl update 命令可以自动更新插件");
                } else if (latestVersion != null) {
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("§a当前已是最新版本：§e" + currentVersion);
                    }
                }
            } catch (Exception e) {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().warning("§c检查更新失败：" + e.getMessage());
                }
            }
        });
    }
    
    // 获取最新版本号
    private String getLatestVersion() throws Exception {
        URL url = new URL(githubApiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            // 解析JSON响应，提取tag_name
            Pattern pattern = Pattern.compile("\"tag_name\"\s*:\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(response.toString());
            if (matcher.find()) {
                String tag = matcher.group(1);
                // 移除可能的"v"前缀
                return tag.startsWith("v") ? tag.substring(1) : tag;
            }
        } finally {
            connection.disconnect();
        }
        return null;
    }
    
    // 检查是否为新版本
    private boolean isNewerVersion(String latestVersion) {
        try {
            // 简单的版本号比较（假设版本号格式为x.y.z）
            String[] currentParts = currentVersion.split("\\.");
            String[] latestParts = latestVersion.split("\\.");
            
            for (int i = 0; i < Math.max(currentParts.length, latestParts.length); i++) {
                int currentNum = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
                int latestNum = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
                
                if (latestNum > currentNum) {
                    return true;
                } else if (latestNum < currentNum) {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("§c版本号格式错误：" + e.getMessage());
        }
        return false;
    }
    
    // 下载并安装更新
    public boolean downloadAndInstallUpdate() {
        try {
            String latestVersion = getLatestVersion();
            if (latestVersion == null || !isNewerVersion(latestVersion)) {
                return false;
            }
            
            // 获取最新版本下载URL
            String downloadUrl = getDownloadUrl(latestVersion);
            if (downloadUrl == null) {
                return false;
            }
            
            // 下载插件文件
            File pluginFile = downloadPlugin(downloadUrl);
            if (pluginFile == null) {
                return false;
            }
            
            // 替换当前插件文件
            return replacePluginFile(pluginFile);
            
        } catch (Exception e) {
            plugin.getLogger().severe("§c更新插件失败：" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // 获取下载URL
    private String getDownloadUrl(String version) throws Exception {
        URL url = new URL(githubApiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            // 解析JSON响应，提取browser_download_url
            Pattern pattern = Pattern.compile("\"browser_download_url\"\\s*:\\s*\"([^\"]+\\.jar)\"");
            Matcher matcher = pattern.matcher(response.toString());
            if (matcher.find()) {
                return matcher.group(1);
            }
        } finally {
            connection.disconnect();
        }
        return null;
    }
    
    // 下载插件
    private File downloadPlugin(String downloadUrl) throws Exception {
        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        
        // 创建临时文件
        File tempFile = File.createTempFile("TrafficLight-", ".jar");
        
        // 下载文件
        try (ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
             FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } finally {
            connection.disconnect();
        }
        
        return tempFile;
    }
    
    // 替换插件文件
    private boolean replacePluginFile(File newPluginFile) {
        try {
            // 获取当前插件文件
            Plugin currentPlugin = Bukkit.getPluginManager().getPlugin("TrafficLight");
            if (currentPlugin == null) {
                return false;
            }
            
            File currentFile = currentPlugin.getDataFolder().getParentFile();
            File pluginFile = new File(currentFile, "TrafficLight.jar");
            
            // 关闭当前插件
            Bukkit.getPluginManager().disablePlugin(currentPlugin);
            
            // 替换文件
            if (pluginFile.exists()) {
                pluginFile.delete();
            }
            
            // 复制新文件到插件目录
            try (InputStream is = new FileInputStream(newPluginFile);
                 OutputStream os = new FileOutputStream(pluginFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            }
            
            // 删除临时文件
            newPluginFile.delete();
            
            plugin.getLogger().info("§a插件更新成功！请重启服务器以应用更新。");
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("§c替换插件文件失败：" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // 获取当前版本
    public String getCurrentVersion() {
        return currentVersion;
    }
}