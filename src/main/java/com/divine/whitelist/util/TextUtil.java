package com.divine.whitelist.util;

import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;

public final class TextUtil {
    private TextUtil() {
    }

    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> colorize(List<String> lines) {
        return lines.stream().map(TextUtil::colorize).collect(Collectors.toList());
    }
}
