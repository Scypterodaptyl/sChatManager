package com.scypter.schatmanager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class Settings {

    private boolean chatEnabled;
    private List<String> chatMessage = Collections.emptyList();
    private boolean blockAllCommands;
    private List<String> blockedCommands = Collections.emptyList();
    private List<String> commandMessage = Collections.emptyList();
    private List<String> reloadMessage = Collections.emptyList();
    private List<String> errorMessage = Collections.emptyList();

    public void load(FileConfiguration config) {
        ConfigurationSection chat = config.getConfigurationSection("chat");
        ConfigurationSection commands = config.getConfigurationSection("commands");

        chatEnabled = chat != null && chat.getBoolean("enabled", false);
        chatMessage = Text.lines(chat, "message", "&cЧат на сервере отключён.");

        blockAllCommands = commands != null && commands.getBoolean("block-all", false);
        commandMessage = Text.lines(commands, "message", "&cЭта команда заблокирована.");
        reloadMessage = Text.lines(config, "reload-message", "&aКонфигурация sChatManager перезагружена.");
        errorMessage = Text.lines(config, "error-message",
                "&cОшибка в config.yml, настройки не изменены. Подробности в консоли.");

        List<String> blacklist = new ArrayList<String>();
        if (commands != null) {
            for (String entry : commands.getStringList("blacklist")) {
                String normalized = normalize(entry);
                if (!normalized.isEmpty()) {
                    blacklist.add(normalized);
                }
            }
        }
        blockedCommands = Collections.unmodifiableList(blacklist);
    }

    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        String value = input.trim().toLowerCase(Locale.ROOT);
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        value = value.replaceAll("\\s+", " ").trim();
        int space = value.indexOf(' ');
        String head = space == -1 ? value : value.substring(0, space);
        int colon = head.indexOf(':');
        if (colon != -1 && colon + 1 < head.length()) {
            head = head.substring(colon + 1);
            value = space == -1 ? head : head + value.substring(space);
        }
        return value;
    }

    public boolean isChatEnabled() {
        return chatEnabled;
    }

    public List<String> getChatMessage() {
        return chatMessage;
    }

    public List<String> getCommandMessage() {
        return commandMessage;
    }

    public List<String> getReloadMessage() {
        return reloadMessage;
    }

    public List<String> getErrorMessage() {
        return errorMessage;
    }

    public String describeCommands() {
        if (blockAllCommands) {
            return "заблокированы все";
        }
        if (blockedCommands.isEmpty()) {
            return "блеклист пуст";
        }
        return "в блеклисте " + blockedCommands.size();
    }

    public boolean isCommandBlocked(String command) {
        if (blockAllCommands) {
            return true;
        }
        String value = normalize(command);
        if (value.isEmpty()) {
            return false;
        }
        for (String entry : blockedCommands) {
            if (value.equals(entry) || value.startsWith(entry + " ")) {
                return true;
            }
        }
        return false;
    }
}
