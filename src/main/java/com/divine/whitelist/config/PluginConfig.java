package com.divine.whitelist.config;

import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.configuration.file.FileConfiguration;

public class PluginConfig {
    private final boolean enabled;
    private final boolean caseInsensitiveNames;
    private final int qqMinLength;
    private final int qqMaxLength;
    private final Pattern qqPattern;
    private final int maxNamesPerQq;
    private final List<String> kickMessage;
    private final String serverPlaceholder;
    private final boolean syncVanillaEnabled;
    private final String syncMode;
    private final boolean requireBukkitWhitelistEnabled;
    private final boolean auditFile;
    private final boolean auditConsole;
    private final int noteMaxLength;
    private final String importPlaceholderNote;
    private final int importQqLength;

    public PluginConfig(FileConfiguration config) {
        this.enabled = config.getBoolean("enabled", true);
        this.caseInsensitiveNames = config.getBoolean("case-insensitive-names", true);
        this.qqMinLength = config.getInt("qq.min-length", 5);
        this.qqMaxLength = config.getInt("qq.max-length", 12);
        this.qqPattern = Pattern.compile(config.getString("qq.regex", "^[0-9]+$"));
        this.maxNamesPerQq = config.getInt("limits.max-names-per-qq", 1);
        this.kickMessage = config.getStringList("kick-message");
        this.serverPlaceholder = config.getString("placeholders.server", "Server");
        this.syncVanillaEnabled = config.getBoolean("sync-vanilla-whitelist.enabled", true);
        this.syncMode = config.getString("sync-vanilla-whitelist.mode", "ON_FIRST_JOIN");
        this.requireBukkitWhitelistEnabled = config.getBoolean(
                "sync-vanilla-whitelist.require-bukkit-whitelist-enabled", false);
        this.auditFile = config.getBoolean("logging.audit-file", true);
        this.auditConsole = config.getBoolean("logging.audit-console", true);
        this.noteMaxLength = config.getInt("notes.max-length", 64);
        this.importPlaceholderNote = config.getString("import.placeholder-note", "imported from vanilla whitelist");
        this.importQqLength = config.getInt("import.qq-length", 10);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isCaseInsensitiveNames() {
        return caseInsensitiveNames;
    }

    public int getQqMinLength() {
        return qqMinLength;
    }

    public int getQqMaxLength() {
        return qqMaxLength;
    }

    public Pattern getQqPattern() {
        return qqPattern;
    }

    public int getMaxNamesPerQq() {
        return maxNamesPerQq;
    }

    public List<String> getKickMessage() {
        return kickMessage;
    }

    public String getServerPlaceholder() {
        return serverPlaceholder;
    }

    public boolean isSyncVanillaEnabled() {
        return syncVanillaEnabled;
    }

    public String getSyncMode() {
        return syncMode;
    }

    public boolean isRequireBukkitWhitelistEnabled() {
        return requireBukkitWhitelistEnabled;
    }

    public boolean isAuditFile() {
        return auditFile;
    }

    public boolean isAuditConsole() {
        return auditConsole;
    }

    public int getNoteMaxLength() {
        return noteMaxLength;
    }

    public String getImportPlaceholderNote() {
        return importPlaceholderNote;
    }

    public int getImportQqLength() {
        return importQqLength;
    }
}
