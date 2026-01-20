package com.divine.whitelist.data;

import com.divine.whitelist.config.PluginConfig;
import com.divine.whitelist.util.TimeUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class WhitelistStore {
    private final File dataFile;
    private final PluginConfig config;
    private final Map<String, WhitelistEntry> entries = new LinkedHashMap<>();

    public WhitelistStore(File dataFile, PluginConfig config) {
        this.dataFile = dataFile;
        this.config = config;
    }

    public void load() throws IOException {
        entries.clear();
        if (!dataFile.exists()) {
            return;
        }
        FileConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection section = yaml.getConfigurationSection("entries");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection entrySection = section.getConfigurationSection(key);
            if (entrySection == null) {
                continue;
            }
            String nameOriginal = entrySection.getString("nameOriginal", key);
            String qq = entrySection.getString("qq", "");
            String note = entrySection.getString("note", "");
            String createdAt = entrySection.getString("createdAt", "");
            String createdBy = entrySection.getString("createdBy", "");
            String updatedAt = entrySection.getString("updatedAt", createdAt);
            String updatedBy = entrySection.getString("updatedBy", createdBy);
            WhitelistEntry entry = new WhitelistEntry(nameOriginal, qq, note, createdAt, createdBy);
            entry.setUpdatedAt(updatedAt);
            entry.setUpdatedBy(updatedBy);
            entries.put(normalizeKey(key), entry);
        }
    }

    public void save() throws IOException {
        FileConfiguration yaml = new YamlConfiguration();
        yaml.set("version", 1);
        for (Map.Entry<String, WhitelistEntry> entry : entries.entrySet()) {
            String key = entry.getKey();
            WhitelistEntry value = entry.getValue();
            String path = "entries." + key + ".";
            yaml.set(path + "nameOriginal", value.getNameOriginal());
            yaml.set(path + "qq", value.getQq());
            yaml.set(path + "note", value.getNote());
            yaml.set(path + "createdAt", value.getCreatedAt());
            yaml.set(path + "createdBy", value.getCreatedBy());
            yaml.set(path + "updatedAt", value.getUpdatedAt());
            yaml.set(path + "updatedBy", value.getUpdatedBy());
        }
        File tmpFile = new File(dataFile.getParentFile(), dataFile.getName() + ".tmp");
        yaml.save(tmpFile);
        Files.move(tmpFile.toPath(), dataFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    public boolean isWhitelisted(String playerName) {
        return entries.containsKey(normalizeKey(playerName));
    }

    public Optional<WhitelistEntry> getEntry(String playerName) {
        return Optional.ofNullable(entries.get(normalizeKey(playerName)));
    }

    public List<WhitelistEntry> listEntries() {
        return new ArrayList<>(entries.values());
    }

    public Set<String> listNamesByQq(String qq) {
        Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (WhitelistEntry entry : entries.values()) {
            if (entry.getQq().equals(qq)) {
                names.add(entry.getNameOriginal());
            }
        }
        return names;
    }

    public int countNamesByQq(String qq) {
        int count = 0;
        for (WhitelistEntry entry : entries.values()) {
            if (entry.getQq().equals(qq)) {
                count++;
            }
        }
        return count;
    }

    public void addEntry(String playerName, String qq, String note, String operator) {
        String now = TimeUtil.now();
        WhitelistEntry entry = new WhitelistEntry(playerName, qq, note, now, operator);
        entries.put(normalizeKey(playerName), entry);
    }

    public boolean removeEntry(String playerName) {
        return entries.remove(normalizeKey(playerName)) != null;
    }

    public boolean updateQq(String playerName, String newQq, String operator) {
        WhitelistEntry entry = entries.get(normalizeKey(playerName));
        if (entry == null) {
            return false;
        }
        entry.setQq(newQq);
        entry.setUpdatedAt(TimeUtil.now());
        entry.setUpdatedBy(operator);
        return true;
    }

    public boolean updateNote(String playerName, String note, String operator) {
        WhitelistEntry entry = entries.get(normalizeKey(playerName));
        if (entry == null) {
            return false;
        }
        entry.setNote(note);
        entry.setUpdatedAt(TimeUtil.now());
        entry.setUpdatedBy(operator);
        return true;
    }

    public String normalizeKey(String name) {
        if (config.isCaseInsensitiveNames()) {
            return name.toLowerCase(Locale.ROOT);
        }
        return name;
    }

    public Map<String, WhitelistEntry> snapshotEntries() {
        return Collections.unmodifiableMap(entries);
    }

    public String generateImportQq(String name, int length) {
        int hash = Math.abs(name.toLowerCase(Locale.ROOT).hashCode());
        String base = String.valueOf(hash);
        if (base.length() < length) {
            base = String.format("%0" + length + "d", hash);
        } else if (base.length() > length) {
            base = base.substring(0, length);
        }
        String candidate = base;
        int counter = 1;
        while (countNamesByQq(candidate) > 0) {
            String suffix = String.valueOf(counter);
            if (suffix.length() >= length) {
                candidate = suffix.substring(0, length);
            } else {
                candidate = candidate.substring(0, length - suffix.length()) + suffix;
            }
            counter++;
        }
        return candidate;
    }

    public List<String> listEntryKeys() {
        return entries.keySet().stream().sorted().collect(Collectors.toList());
    }
}
