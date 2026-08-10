package com.scypter.schatmanager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SChatManager extends JavaPlugin implements CommandExecutor {

    private final Settings settings = new Settings();
    private final Set<String> ownLabels = new HashSet<String>();
    private boolean loaded;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        load(null);

        collectOwnLabels();

        Hooks.register(this);

        PluginCommand command = getCommand("schatmanager");
        if (command != null) {
            command.setExecutor(this);
        }

        getLogger().info("Включён: чат " + (settings.isChatEnabled() ? "разрешён" : "заблокирован")
                + ", " + settings.describeCommands() + ".");
    }

    private void load(CommandSender sender) {
        File file = new File(getDataFolder(), "config.yml");
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (Throwable error) {
            getLogger().severe("config.yml не прочитан: " + describe(error));
            if (loaded) {
                getLogger().severe("Настройки не изменены, работают предыдущие значения.");
            } else {
                settings.load(defaults());
                getLogger().severe("Применены значения по умолчанию: чат заблокирован, блеклист пуст.");
            }
            if (sender != null) {
                send(sender, settings.getErrorMessage());
            }
            return;
        }

        settings.load(config);
        loaded = true;
        if (config.getConfigurationSection("chat") == null
                || config.getConfigurationSection("commands") == null) {
            getLogger().warning("В config.yml нет секции chat и/или commands, "
                    + "для них взяты значения по умолчанию.");
        }
        if (sender != null) {
            send(sender, settings.getReloadMessage());
        }
    }

    private YamlConfiguration defaults() {
        YamlConfiguration config = new YamlConfiguration();
        InputStream stream = getResource("config.yml");
        if (stream == null) {
            return config;
        }
        try {
            config.load(new InputStreamReader(stream, "UTF-8"));
        } catch (Throwable ignored) {
        }
        return config;
    }

    private static String describe(Throwable error) {
        String message = error.getMessage();
        if (message == null) {
            message = error.toString();
        }
        message = message.replaceAll("\\s+", " ").trim();
        return message.length() > 300 ? message.substring(0, 300) + "..." : message;
    }

    private void collectOwnLabels() {
        ownLabels.add("schatmanager");
        PluginCommand command = getCommand("schatmanager");
        if (command != null) {
            for (String alias : command.getAliases()) {
                ownLabels.add(alias.toLowerCase(Locale.ROOT));
            }
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public void handleChat(Event event, Player player) {
        if (settings.isChatEnabled()) {
            return;
        }
        cancel(event);
        if (player != null) {
            send(player, settings.getChatMessage());
        }
    }

    public void enforceChat(Event event) {
        if (!settings.isChatEnabled()) {
            cancel(event);
        }
    }

    public void handleCommand(Event event, String message, Player player) {
        if (!isBlockedCommand(message)) {
            return;
        }
        cancel(event);
        if (player != null) {
            send(player, settings.getCommandMessage());
        }
    }

    public void enforceCommand(Event event, String message) {
        if (isBlockedCommand(message)) {
            cancel(event);
        }
    }

    private void cancel(Event event) {
        if (event instanceof Cancellable) {
            ((Cancellable) event).setCancelled(true);
        }
    }

    public boolean isBlockedCommand(String message) {
        String normalized = Settings.normalize(message);
        if (normalized.isEmpty()) {
            return false;
        }
        int space = normalized.indexOf(' ');
        String label = space == -1 ? normalized : normalized.substring(0, space);
        if (ownLabels.contains(label)) {
            return false;
        }
        return settings.isCommandBlocked(normalized);
    }

    public void send(CommandSender target, List<String> lines) {
        for (String line : lines) {
            target.sendMessage(line);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && !sender.isOp()) {
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            load(sender);
            return true;
        }
        sender.sendMessage(Text.color("&7/" + label + " reload"));
        return true;
    }
}
