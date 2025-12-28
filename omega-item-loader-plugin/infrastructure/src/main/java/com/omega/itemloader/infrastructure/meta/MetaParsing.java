package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class MetaParsing {

    private MetaParsing() {
    }

    public static String normalizeType(String raw) {
        if (raw == null) {
            return "";
        }
        String lower = raw.trim().toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder(lower.length());
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static Optional<Material> resolveMaterial(String raw) {
        if (raw == null) {
            return Optional.empty();
        }

        String cleaned = raw.trim();
        int colon = cleaned.indexOf(':');
        if (colon >= 0) {
            cleaned = cleaned.substring(colon + 1);
        }
        cleaned = cleaned.replace(' ', '_').toUpperCase(Locale.ROOT);

        Material material = Material.matchMaterial(cleaned);
        return Optional.ofNullable(material);
    }

    public static Optional<NamespacedKey> resolveKey(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String cleaned = raw.trim();
        NamespacedKey key;
        if (cleaned.contains(":")) {
            key = NamespacedKey.fromString(cleaned.toLowerCase(Locale.ROOT));
        } else {
            key = NamespacedKey.minecraft(cleaned.toLowerCase(Locale.ROOT));
        }
        return Optional.ofNullable(key);
    }

    public static Optional<Enchantment> resolveEnchantment(String raw) {
        if (raw == null) {
            return Optional.empty();
        }

        String cleaned = raw.trim();

        NamespacedKey key;
        if (cleaned.contains(":")) {
            key = NamespacedKey.fromString(cleaned.toLowerCase(Locale.ROOT));
        } else {
            key = NamespacedKey.minecraft(cleaned.toLowerCase(Locale.ROOT));
        }

        if (key != null) {
            Enchantment byKey = Enchantment.getByKey(key);
            if (byKey != null) {
                return Optional.of(byKey);
            }
        }

        Enchantment byName = Enchantment.getByName(cleaned.toUpperCase(Locale.ROOT));
        return Optional.ofNullable(byName);
    }

    public static Optional<Color> optionalColor(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return asColor(element);
    }

    public static Optional<Color> asColor(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return Optional.empty();
        }

        if (element.isJsonPrimitive()) {
            Optional<String> sOpt = JsonRead.asString(element);
            if (sOpt.isEmpty()) {
                return Optional.empty();
            }
            String s = sOpt.get().trim();

            // #RRGGBB or RRGGBB
            String hex = s;
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }
            if (hex.length() == 6) {
                try {
                    int rgb = Integer.parseInt(hex, 16);
                    return Optional.of(Color.fromRGB(rgb));
                } catch (Exception ignored) {
                }
            }

            // DyeColor name
            try {
                DyeColor dyeColor = DyeColor.valueOf(s.toUpperCase(Locale.ROOT));
                return Optional.of(dyeColor.getColor());
            } catch (Exception ignored) {
            }

            return Optional.empty();
        }

        if (!element.isJsonObject()) {
            return Optional.empty();
        }

        JsonObject obj = element.getAsJsonObject();
        Optional<Integer> r = JsonRead.optionalInt(obj, "r");
        Optional<Integer> g = JsonRead.optionalInt(obj, "g");
        Optional<Integer> b = JsonRead.optionalInt(obj, "b");
        if (r.isEmpty() || g.isEmpty() || b.isEmpty()) {
            return Optional.empty();
        }

        int rr = clampRgb(r.get());
        int gg = clampRgb(g.get());
        int bb = clampRgb(b.get());
        return Optional.of(Color.fromRGB(rr, gg, bb));
    }

    private static int clampRgb(int v) {
        if (v < 0) {
            return 0;
        }
        if (v > 255) {
            return 255;
        }
        return v;
    }

    public static Optional<DyeColor> optionalDyeColor(JsonObject object, String key) {
        return JsonRead.optionalString(object, key)
                .map(s -> s.trim().toUpperCase(Locale.ROOT))
                .flatMap(s -> {
                    try {
                        return Optional.of(DyeColor.valueOf(s));
                    } catch (Exception ignored) {
                        return Optional.empty();
                    }
                });
    }

    public static Optional<Location> optionalLocation(JsonObject object, String key) {
        Optional<JsonObject> locObjOpt = JsonRead.optionalObject(object, key);
        if (locObjOpt.isEmpty()) {
            return Optional.empty();
        }

        JsonObject locObj = locObjOpt.get();
        String worldName = JsonRead.optionalString(locObj, "world").orElse("");
        if (worldName.isBlank()) {
            return Optional.empty();
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return Optional.empty();
        }

        double x = JsonRead.optionalInt(locObj, "x").orElse(0);
        double y = JsonRead.optionalInt(locObj, "y").orElse(0);
        double z = JsonRead.optionalInt(locObj, "z").orElse(0);

        return Optional.of(new Location(world, x, y, z));
    }

    public static Optional<PotionEffectType> resolvePotionEffectType(String raw) {
        if (raw == null) {
            return Optional.empty();
        }

        String cleaned = raw.trim();

        Optional<NamespacedKey> keyOpt = resolveKey(cleaned);
        if (keyOpt.isPresent()) {
            PotionEffectType byKey = PotionEffectType.getByKey(keyOpt.get());
            if (byKey != null) {
                return Optional.of(byKey);
            }
        }

        PotionEffectType byName = PotionEffectType.getByName(cleaned.toUpperCase(Locale.ROOT));
        return Optional.ofNullable(byName);
    }

    public static Optional<PotionEffect> parsePotionEffect(JsonObject effectObj) {
        String typeRaw = JsonRead.optionalString(effectObj, "type").orElse("");
        if (typeRaw.isBlank()) {
            return Optional.empty();
        }

        Optional<PotionEffectType> typeOpt = resolvePotionEffectType(typeRaw);
        if (typeOpt.isEmpty()) {
            return Optional.empty();
        }

        int duration = JsonRead.optionalInt(effectObj, "duration").orElse(1);
        int amplifier = JsonRead.optionalInt(effectObj, "amplifier").orElse(0);
        boolean ambient = JsonRead.optionalBoolean(effectObj, "ambient").orElse(false);
        boolean particles = JsonRead.optionalBoolean(effectObj, "particles").orElse(true);
        boolean icon = JsonRead.optionalBoolean(effectObj, "icon").orElse(true);

        return Optional.of(new PotionEffect(typeOpt.get(), duration, amplifier, ambient, particles, icon));
    }

    public static List<PotionEffect> parsePotionEffectsArray(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull() || !element.isJsonArray()) {
            return List.of();
        }

        JsonArray array = element.getAsJsonArray();
        List<PotionEffect> effects = new ArrayList<>(array.size());
        for (JsonElement e : array) {
            if (!e.isJsonObject()) {
                continue;
            }
            parsePotionEffect(e.getAsJsonObject()).ifPresent(effects::add);
        }

        return effects;
    }
}
