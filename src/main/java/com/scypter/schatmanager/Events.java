package com.scypter.schatmanager;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

public final class Events {

    public interface Handler {
        void handle(Event event);
    }

    private Events() {
    }

    public static Class<?> find(Plugin plugin, String className) {
        try {
            Class<?> type = Class.forName(className, false, plugin.getClass().getClassLoader());
            return Event.class.isAssignableFrom(type) ? type : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean register(Plugin plugin, Listener listener, Class<?> type,
                                   EventPriority priority, final Handler handler) {
        if (type == null) {
            return false;
        }
        try {
            plugin.getServer().getPluginManager().registerEvent(
                    (Class<? extends Event>) type,
                    listener,
                    priority,
                    new EventExecutor() {
                        public void execute(Listener registered, Event event) {
                            handler.handle(event);
                        }
                    },
                    plugin);
            return true;
        } catch (Throwable error) {
            plugin.getLogger().warning("Не удалось подписаться на " + type.getName() + ": " + error);
            return false;
        }
    }
}
