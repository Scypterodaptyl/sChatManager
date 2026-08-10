package com.scypter.schatmanager;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Text {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9A-Fa-f]{6})");
    private static final Method HEX_METHOD = findHexMethod();

    private Text() {
    }

    private static Method findHexMethod() {
        try {
            return Class.forName("net.md_5.bungee.api.ChatColor").getMethod("of", String.class);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static String color(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String result = input;
        Matcher matcher = HEX_PATTERN.matcher(result);
        if (matcher.find()) {
            matcher.reset();
            StringBuffer buffer = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(hex(matcher.group(1))));
            }
            matcher.appendTail(buffer);
            result = buffer.toString();
        }
        return ChatColor.translateAlternateColorCodes('&', result);
    }

    private static String hex(String value) {
        if (HEX_METHOD == null) {
            return "";
        }
        try {
            return String.valueOf(HEX_METHOD.invoke(null, "#" + value));
        } catch (Throwable ignored) {
            return "";
        }
    }

    public static List<String> lines(ConfigurationSection section, String path, String fallback) {
        List<String> result = new ArrayList<String>();
        Object raw = section == null ? null : section.get(path);
        if (raw == null) {
            raw = fallback;
        }
        if (raw instanceof List) {
            for (Object element : (List<?>) raw) {
                if (element != null) {
                    result.add(color(String.valueOf(element)));
                }
            }
        } else {
            for (String line : String.valueOf(raw).split("\n")) {
                result.add(color(line));
            }
        }
        while (!result.isEmpty() && result.get(result.size() - 1).isEmpty()) {
            result.remove(result.size() - 1);
        }
        return Collections.unmodifiableList(result);
    }
}
