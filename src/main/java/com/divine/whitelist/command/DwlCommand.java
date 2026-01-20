package com.divine.whitelist.command;

import com.divine.whitelist.DivineWhiteListPlugin;
import com.divine.whitelist.config.PluginConfig;
import com.divine.whitelist.data.WhitelistEntry;
import com.divine.whitelist.data.WhitelistStore;
import com.divine.whitelist.util.AuditLogger;
import com.divine.whitelist.util.TextUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class DwlCommand implements CommandExecutor, TabCompleter {
    private final DivineWhiteListPlugin plugin;
    private final WhitelistStore store;

    public DwlCommand(DivineWhiteListPlugin plugin, WhitelistStore store) {
        this.plugin = plugin;
        this.store = store;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "add":
                handleAdd(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
            case "remove":
                handleRemove(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
            case "setqq":
                handleSetQq(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
            case "info":
                handleInfo(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
            case "qq":
                handleQq(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
            case "list":
                handleList(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
            case "reload":
                handleReload(sender);
                return true;
            case "export":
                handleExport(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
            case "import":
                handleImport(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
            default:
                sendUsage(sender);
                return true;
        }
    }

    private void handleAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("divinewhitelist.add")) {
            sender.sendMessage(TextUtil.colorize("&c你没有权限执行该命令。"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(TextUtil.colorize("&c用法: /dwl add <玩家名> <QQ号> [备注...] [--force]"));
            return;
        }
        boolean force = false;
        List<String> argList = new ArrayList<>(Arrays.asList(args));
        if (argList.get(argList.size() - 1).equalsIgnoreCase("--force")
                || argList.get(argList.size() - 1).equalsIgnoreCase("-f")) {
            force = true;
            argList.remove(argList.size() - 1);
        }
        String name = argList.get(0);
        String qq = argList.get(1);
        String note = "";
        if (argList.size() > 2) {
            note = String.join(" ", argList.subList(2, argList.size()));
        }
        PluginConfig config = plugin.getPluginConfig();
        if (!isValidName(name)) {
            sender.sendMessage(TextUtil.colorize("&c玩家名不合法，必须为 3-16 位字母数字或下划线。"));
            return;
        }
        if (!isValidQq(qq, config.getQqPattern(), config.getQqMinLength(), config.getQqMaxLength())) {
            sender.sendMessage(TextUtil.colorize("&cQQ 号格式不正确。"));
            return;
        }
        if (note.length() > config.getNoteMaxLength()) {
            sender.sendMessage(TextUtil.colorize("&c备注长度不能超过 " + config.getNoteMaxLength() + " 字符。"));
            return;
        }
        if (store.isWhitelisted(name) && !force) {
            sender.sendMessage(TextUtil.colorize("&e该玩家已存在白名单，如需覆盖请使用 --force。"));
            return;
        }
        if (!canBindQq(qq, config.getMaxNamesPerQq(), name)) {
            sender.sendMessage(TextUtil.colorize("&c该 QQ 已达到绑定名额上限。"));
            return;
        }
        if (store.isWhitelisted(name)) {
            store.removeEntry(name);
        }
        store.addEntry(name, qq, note, sender.getName());
        persist(sender);
        audit("ADD", sender.getName(), name, qq, note);
        sender.sendMessage(TextUtil.colorize("&a已添加白名单: " + name + " QQ=" + qq));
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("divinewhitelist.remove")) {
            sender.sendMessage(TextUtil.colorize("&c你没有权限执行该命令。"));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(TextUtil.colorize("&c用法: /dwl remove <玩家名>"));
            return;
        }
        String name = args[0];
        if (!store.removeEntry(name)) {
            sender.sendMessage(TextUtil.colorize("&c未找到该玩家的白名单记录。"));
            return;
        }
        persist(sender);
        audit("REMOVE", sender.getName(), name, "", "");
        sender.sendMessage(TextUtil.colorize("&a已移除白名单: " + name));
    }

    private void handleSetQq(CommandSender sender, String[] args) {
        if (!sender.hasPermission("divinewhitelist.setqq")) {
            sender.sendMessage(TextUtil.colorize("&c你没有权限执行该命令。"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(TextUtil.colorize("&c用法: /dwl setqq <玩家名> <新QQ号>"));
            return;
        }
        String name = args[0];
        String qq = args[1];
        PluginConfig config = plugin.getPluginConfig();
        if (!isValidQq(qq, config.getQqPattern(), config.getQqMinLength(), config.getQqMaxLength())) {
            sender.sendMessage(TextUtil.colorize("&cQQ 号格式不正确。"));
            return;
        }
        if (!store.isWhitelisted(name)) {
            sender.sendMessage(TextUtil.colorize("&c未找到该玩家的白名单记录。"));
            return;
        }
        if (!canBindQq(qq, config.getMaxNamesPerQq(), name)) {
            sender.sendMessage(TextUtil.colorize("&c该 QQ 已达到绑定名额上限。"));
            return;
        }
        store.updateQq(name, qq, sender.getName());
        persist(sender);
        audit("SETQQ", sender.getName(), name, qq, "");
        sender.sendMessage(TextUtil.colorize("&a已更新 QQ: " + name + " -> " + qq));
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("divinewhitelist.info")) {
            sender.sendMessage(TextUtil.colorize("&c你没有权限执行该命令。"));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(TextUtil.colorize("&c用法: /dwl info <玩家名>"));
            return;
        }
        String name = args[0];
        store.getEntry(name).ifPresentOrElse(entry -> {
            sender.sendMessage(TextUtil.colorize("&e玩家名: &f" + entry.getNameOriginal()));
            sender.sendMessage(TextUtil.colorize("&eQQ号: &f" + entry.getQq()));
            sender.sendMessage(TextUtil.colorize("&e备注: &f" + entry.getNote()));
            sender.sendMessage(TextUtil.colorize("&e创建: &f" + entry.getCreatedAt() + " &7by " + entry.getCreatedBy()));
            sender.sendMessage(TextUtil.colorize("&e更新: &f" + entry.getUpdatedAt() + " &7by " + entry.getUpdatedBy()));
        }, () -> sender.sendMessage(TextUtil.colorize("&c未找到该玩家的白名单记录。")));
    }

    private void handleQq(CommandSender sender, String[] args) {
        if (!sender.hasPermission("divinewhitelist.qq")) {
            sender.sendMessage(TextUtil.colorize("&c你没有权限执行该命令。"));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(TextUtil.colorize("&c用法: /dwl qq <QQ号>"));
            return;
        }
        String qq = args[0];
        Set<String> names = store.listNamesByQq(qq);
        if (names.isEmpty()) {
            sender.sendMessage(TextUtil.colorize("&e该 QQ 没有关联的白名单记录。"));
            return;
        }
        sender.sendMessage(TextUtil.colorize("&aQQ " + qq + " 绑定玩家: " + String.join(", ", names)));
    }

    private void handleList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("divinewhitelist.list")) {
            sender.sendMessage(TextUtil.colorize("&c你没有权限执行该命令。"));
            return;
        }
        int page = 1;
        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                sender.sendMessage(TextUtil.colorize("&c页码必须为数字。"));
                return;
            }
        }
        int pageSize = 10;
        List<WhitelistEntry> entries = store.listEntries();
        entries.sort(Comparator.comparing(WhitelistEntry::getNameOriginal, String.CASE_INSENSITIVE_ORDER));
        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) pageSize));
        if (page < 1 || page > totalPages) {
            sender.sendMessage(TextUtil.colorize("&c页码超出范围，最大页数: " + totalPages));
            return;
        }
        sender.sendMessage(TextUtil.colorize("&e白名单列表 (" + page + "/" + totalPages + ")"));
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, entries.size());
        for (int i = start; i < end; i++) {
            WhitelistEntry entry = entries.get(i);
            sender.sendMessage(TextUtil.colorize("&7- &f" + entry.getNameOriginal() + " &7QQ: &f" + entry.getQq()));
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("divinewhitelist.reload")) {
            sender.sendMessage(TextUtil.colorize("&c你没有权限执行该命令。"));
            return;
        }
        plugin.reloadPluginConfig();
        try {
            store.load();
        } catch (IOException ex) {
            sender.sendMessage(TextUtil.colorize("&c配置或数据文件加载失败: " + ex.getMessage()));
            return;
        }
        sender.sendMessage(TextUtil.colorize("&a配置已重载。"));
    }

    private void handleExport(CommandSender sender, String[] args) {
        if (!sender.hasPermission("divinewhitelist.export")) {
            sender.sendMessage(TextUtil.colorize("&c你没有权限执行该命令。"));
            return;
        }
        if (args.length < 1 || !"vanilla".equalsIgnoreCase(args[0])) {
            sender.sendMessage(TextUtil.colorize("&c用法: /dwl export vanilla"));
            return;
        }
        int count = 0;
        for (WhitelistEntry entry : store.listEntries()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.getNameOriginal());
            if (!offlinePlayer.isWhitelisted()) {
                offlinePlayer.setWhitelisted(true);
                count++;
            }
        }
        sender.sendMessage(TextUtil.colorize("&a已同步至原版白名单: " + count + " 条。"));
    }

    private void handleImport(CommandSender sender, String[] args) {
        if (!sender.hasPermission("divinewhitelist.import")) {
            sender.sendMessage(TextUtil.colorize("&c你没有权限执行该命令。"));
            return;
        }
        if (args.length < 1 || !"vanilla".equalsIgnoreCase(args[0])) {
            sender.sendMessage(TextUtil.colorize("&c用法: /dwl import vanilla"));
            return;
        }
        PluginConfig config = plugin.getPluginConfig();
        int imported = 0;
        for (OfflinePlayer offlinePlayer : Bukkit.getWhitelistedPlayers()) {
            String name = offlinePlayer.getName();
            if (name == null || store.isWhitelisted(name)) {
                continue;
            }
            String qq = store.generateImportQq(name, config.getImportQqLength());
            if (!canBindQq(qq, config.getMaxNamesPerQq(), name)) {
                continue;
            }
            store.addEntry(name, qq, config.getImportPlaceholderNote(), sender.getName());
            imported++;
        }
        persist(sender);
        sender.sendMessage(TextUtil.colorize("&a已从原版白名单导入: " + imported + " 条。"));
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(TextUtil.colorize("&e/dwl add <玩家名> <QQ号> [备注...] [--force]"));
        sender.sendMessage(TextUtil.colorize("&e/dwl remove <玩家名>"));
        sender.sendMessage(TextUtil.colorize("&e/dwl setqq <玩家名> <新QQ号>"));
        sender.sendMessage(TextUtil.colorize("&e/dwl info <玩家名>"));
        sender.sendMessage(TextUtil.colorize("&e/dwl qq <QQ号>"));
        sender.sendMessage(TextUtil.colorize("&e/dwl list [页码]"));
        sender.sendMessage(TextUtil.colorize("&e/dwl reload"));
        sender.sendMessage(TextUtil.colorize("&e/dwl export vanilla"));
        sender.sendMessage(TextUtil.colorize("&e/dwl import vanilla"));
    }

    private boolean isValidName(String name) {
        return name != null && name.matches("^[A-Za-z0-9_]{3,16}$");
    }

    private boolean isValidQq(String qq, Pattern pattern, int minLength, int maxLength) {
        return qq != null
                && qq.length() >= minLength
                && qq.length() <= maxLength
                && pattern.matcher(qq).matches();
    }

    private boolean canBindQq(String qq, int maxNames, String currentName) {
        if (maxNames <= 0) {
            return true;
        }
        int count = store.countNamesByQq(qq);
        if (store.getEntry(currentName).map(entry -> entry.getQq().equals(qq)).orElse(false)) {
            return true;
        }
        return count < maxNames;
    }

    private void persist(CommandSender sender) {
        try {
            store.save();
        } catch (IOException ex) {
            sender.sendMessage(TextUtil.colorize("&c保存数据失败: " + ex.getMessage()));
        }
    }

    private void audit(String action, String operator, String name, String qq, String note) {
        AuditLogger logger = plugin.getAuditLogger();
        if (logger == null) {
            return;
        }
        StringJoiner joiner = new StringJoiner(" | ");
        joiner.add(action);
        joiner.add("operator=" + operator);
        joiner.add("name=" + name);
        if (!qq.isEmpty()) {
            joiner.add("qq=" + qq);
        }
        if (!note.isEmpty()) {
            joiner.add("note=" + note);
        }
        logger.log(joiner.toString());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("add", "remove", "setqq", "info", "qq", "list", "reload", "export", "import");
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("export") || args[0].equalsIgnoreCase("import")) {
                return List.of("vanilla");
            }
            if (Arrays.asList("remove", "setqq", "info").contains(args[0].toLowerCase(Locale.ROOT))) {
                return store.listEntryKeys();
            }
        }
        return List.of();
    }
}
