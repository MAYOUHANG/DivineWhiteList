package com.divine.whitelist;

import com.divine.whitelist.command.DwlCommand;
import com.divine.whitelist.config.PluginConfig;
import com.divine.whitelist.data.WhitelistStore;
import com.divine.whitelist.util.AuditLogger;
import com.divine.whitelist.util.TextUtil;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class DivineWhiteListPlugin extends JavaPlugin implements Listener {
    private PluginConfig pluginConfig;
    private WhitelistStore whitelistStore;
    private AuditLogger auditLogger;

    @Override
    public void onEnable() {
        logStartupBanner();
        saveDefaultConfig();
        reloadPluginConfig();
        File dataFile = new File(getDataFolder(), "data.yml");
        whitelistStore = new WhitelistStore(dataFile, pluginConfig);
        try {
            whitelistStore.load();
        } catch (IOException ex) {
            getLogger().warning("Failed to load data.yml: " + ex.getMessage());
        }
        getServer().getPluginManager().registerEvents(this, this);
        DwlCommand command = new DwlCommand(this, whitelistStore);
        if (getCommand("dwl") != null) {
            getCommand("dwl").setExecutor(command);
            getCommand("dwl").setTabCompleter(command);
        }
    }

    @Override
    public void onDisable() {
        try {
            whitelistStore.save();
        } catch (IOException ex) {
            getLogger().warning("Failed to save data.yml: " + ex.getMessage());
        }
    }

    public void reloadPluginConfig() {
        reloadConfig();
        pluginConfig = new PluginConfig(getConfig());
        auditLogger = new AuditLogger(this, pluginConfig.isAuditFile(), pluginConfig.isAuditConsole());
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public WhitelistStore getWhitelistStore() {
        return whitelistStore;
    }

    public AuditLogger getAuditLogger() {
        return auditLogger;
    }

    private void logStartupBanner() {
        getLogger().info("╔════════════════════════════════════════════╗");
        getLogger().info("║     ✦ DivineWhiteList Core Ignition ✦      ║");
        getLogger().info("║   ⚡ Energizing whitelist matrix...         ║");
        getLogger().info("║   ✧ Author: MAAAABG                         ║");
        getLogger().info("║   ☄️  Syncing realm access protocols...      ║");
        getLogger().info("╚════════════════════════════════════════════╝");
    }

    @EventHandler
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!pluginConfig.isEnabled()) {
            return;
        }
        String playerName = event.getName();
        if (!whitelistStore.isWhitelisted(playerName)) {
            List<String> lines = TextUtil.colorize(pluginConfig.getKickMessage());
            String message = String.join("\n", lines)
                    .replace("{player}", playerName)
                    .replace("{server}", pluginConfig.getServerPlaceholder());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, message);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!pluginConfig.isSyncVanillaEnabled()) {
            return;
        }
        if (pluginConfig.isRequireBukkitWhitelistEnabled() && !Bukkit.hasWhitelist()) {
            return;
        }
        if (!"ON_FIRST_JOIN".equalsIgnoreCase(pluginConfig.getSyncMode())) {
            return;
        }
        OfflinePlayer offlinePlayer = event.getPlayer();
        if (!offlinePlayer.isWhitelisted()) {
            offlinePlayer.setWhitelisted(true);
            getLogger().info("Synced vanilla whitelist for " + offlinePlayer.getName());
        }
    }
}
