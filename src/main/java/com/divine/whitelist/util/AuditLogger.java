package com.divine.whitelist.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.bukkit.plugin.java.JavaPlugin;

public class AuditLogger {
    private final JavaPlugin plugin;
    private final boolean logToFile;
    private final boolean logToConsole;
    private final File auditFile;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public AuditLogger(JavaPlugin plugin, boolean logToFile, boolean logToConsole) {
        this.plugin = plugin;
        this.logToFile = logToFile;
        this.logToConsole = logToConsole;
        this.auditFile = new File(plugin.getDataFolder(), "logs/audit.log");
    }

    public void log(String message) {
        String line = "[" + ZonedDateTime.now().format(formatter) + "] " + message;
        if (logToConsole) {
            plugin.getLogger().info(line);
        }
        if (logToFile) {
            writeFile(line);
        }
    }

    private void writeFile(String line) {
        File folder = auditFile.getParentFile();
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("Failed to create audit log folder: " + folder.getAbsolutePath());
            return;
        }
        try (FileWriter writer = new FileWriter(auditFile, StandardCharsets.UTF_8, true)) {
            writer.write(line + System.lineSeparator());
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to write audit log: " + ex.getMessage());
        }
    }
}
