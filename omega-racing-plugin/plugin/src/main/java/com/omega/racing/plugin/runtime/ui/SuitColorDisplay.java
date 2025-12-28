package com.omega.racing.plugin.runtime.ui;

import org.bukkit.DyeColor;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class SuitColorDisplay {

    private static final Map<String, DyeColor> DYE_COLORS_BY_HEX;

    static {
        Map<String, DyeColor> byHex = new HashMap<>();

        // Exact-match mapping from Bukkit/Spigot DyeColor palette.
        // If a stored color is not in this palette, we display it as "Custom".
        for (DyeColor dyeColor : DyeColor.values()) {
            if (dyeColor == null) {
                continue;
            }
            org.bukkit.Color bukkitColor = dyeColor.getColor();
            if (bukkitColor == null) {
                continue;
            }
            String hex = toHex6(bukkitColor.asRGB());
            byHex.put(hex, dyeColor);
        }

        DYE_COLORS_BY_HEX = java.util.Collections.unmodifiableMap(byHex);
    }

    private SuitColorDisplay() {
    }

    public static String display(String suitColorHex) {
        String normalized = normalizeHexColor(suitColorHex);
        if (normalized == null) {
            return "Unknown";
        }

        DyeColor dyeColor = DYE_COLORS_BY_HEX.get(normalized);
        return dyeColor != null ? humanizeEnumName(dyeColor.name()) : "Custom";
    }

    private static String humanizeEnumName(String enumName) {
        if (enumName == null || enumName.isBlank()) {
            return "Unknown";
        }
        String lower = enumName.toLowerCase(Locale.ROOT);
        String[] parts = lower.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                sb.append(part.substring(1));
            }
        }
        return sb.length() == 0 ? "Unknown" : sb.toString();
    }

    private static String normalizeHexColor(String suitColorHex) {
        if (suitColorHex == null) {
            return null;
        }
        String s = suitColorHex.trim();
        if (s.isEmpty()) {
            return null;
        }
        if (s.startsWith("#")) {
            s = s.substring(1);
        }
        if (s.length() != 6) {
            return null;
        }
        for (int i = 0; i < 6; i++) {
            char c = s.charAt(i);
            boolean isHex = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
            if (!isHex) {
                return null;
            }
        }
        return "#" + s.toUpperCase(Locale.ROOT);
    }

    private static String toHex6(int rgb) {
        int masked = rgb & 0xFFFFFF;
        return String.format(Locale.ROOT, "#%06X", masked);
    }
}
