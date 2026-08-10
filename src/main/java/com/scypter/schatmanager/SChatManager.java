package com.scypter.schatmanager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SChatManager extends JavaPlugin implements CommandExecutor {

    private final Settings settings = new Settings();
    private final Set<String> ownLabels = new HashSet<String>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reload();

        collectOwnLabels();

        Hooks.register(this);

        PluginCommand command = getCommand("schatmanager");
        if (command != null) {
            command.setExecutor(this);
        }

        getLogger().info("Включён: чат " + (settings.isChatEnabled() ? "разрешён" : "заблокирован")
                + ", " + settings.describeCommands() + ".");
    }

    private void reload() {
        settings.load(getConfig());
        if (getConfig().getConfigurationSection("chat") == null
                || getConfig().getConfigurationSection("commands") == null) {
            getLogger().warning("В config.yml нет секций chat и/или commands, "
                    + "применены значения по умолчанию. Проверьте синтаксис файла.");
        }
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
            reloadConfig();
            reload();
            send(sender, settings.getReloadMessage());
            return true;
        }
        sender.sendMessage(Text.color("&7/" + label + " reload"));
        return true;
    }
}
