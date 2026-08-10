package com.scypter.schatmanager;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;

public final class Hooks implements Listener {

    private static final String[] CHAT_EVENTS = {
            "io.papermc.paper.event.player.AsyncChatEvent",
            "org.bukkit.event.player.AsyncPlayerChatEvent"
    };
    private static final String COMMAND_EVENT = "org.bukkit.event.player.PlayerCommandPreprocessEvent";

    private Hooks() {
    }

    public static void register(final SChatManager plugin) {
        Listener listener = new Hooks();
        registerChat(plugin, listener);
        registerCommands(plugin, listener);
    }

    private static void registerChat(final SChatManager plugin, Listener listener) {
        for (String name : CHAT_EVENTS) {
            Class<?> type = Events.find(plugin, name);
            if (type == null) {
                continue;
            }
            boolean blocked = Events.register(plugin, listener, type, EventPriority.LOWEST, new Events.Handler() {
                public void handle(Event event) {
                    plugin.handleChat(event, playerOf(event));
                }
            });
            if (!blocked) {
                continue;
            }
            Events.register(plugin, listener, type, EventPriority.HIGHEST, new Events.Handler() {
                public void handle(Event event) {
                    plugin.enforceChat(event);
                }
            });
            return;
        }
        plugin.getLogger().severe("Не найдено ни одного события чата, блокировка чата не работает.");
    }

    private static void registerCommands(final SChatManager plugin, Listener listener) {
        Class<?> type = Events.find(plugin, COMMAND_EVENT);
        boolean blocked = Events.register(plugin, listener, type, EventPriority.LOWEST, new Events.Handler() {
            public void handle(Event event) {
                PlayerCommandPreprocessEvent command = (PlayerCommandPreprocessEvent) event;
                plugin.handleCommand(command, command.getMessage(), command.getPlayer());
            }
        });
        if (!blocked) {
            plugin.getLogger().severe("Не найдено событие команд, блокировка команд не работает.");
            return;
        }
        Events.register(plugin, listener, type, EventPriority.HIGHEST, new Events.Handler() {
            public void handle(Event event) {
                PlayerCommandPreprocessEvent command = (PlayerCommandPreprocessEvent) event;
                plugin.enforceCommand(command, command.getMessage());
            }
        });
    }

    private static Player playerOf(Event event) {
        return event instanceof PlayerEvent ? ((PlayerEvent) event).getPlayer() : null;
    }
}
