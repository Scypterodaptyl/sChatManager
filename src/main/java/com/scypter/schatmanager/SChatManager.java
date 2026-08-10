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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SChatManager extends JavaPlugin implements CommandExecutor {

    private static final long NOTIFY_COOLDOWN = 250L;

    private final Settings settings = new Settings();
    private final Map<UUID, Long> lastNotify = new ConcurrentHashMap<UUID, Long>();
    private final Set<String> ownLabels = new HashSet<String>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        settings.load(getConfig());

        collectOwnLabels();

        Hooks.register(this);

        PluginCommand command = getCommand("schatmanager");
        if (command != null) {
            command.setExecutor(this);
        }
    }

    @Override
    public void onDisable() {
        lastNotify.clear();
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
        if (player != null && canNotify(player)) {
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

    public void forget(Player player) {
        lastNotify.remove(player.getUniqueId());
    }

    private boolean canNotify(Player player) {
        long now = System.currentTimeMillis();
        Long previous = lastNotify.put(player.getUniqueId(), now);
        return previous == null || now - previous > NOTIFY_COOLDOWN;
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
            settings.load(getConfig());
            send(sender, settings.getReloadMessage());
            return true;
        }
        sender.sendMessage(Text.color("&7/" + label + " reload"));
        return true;
    }
}
