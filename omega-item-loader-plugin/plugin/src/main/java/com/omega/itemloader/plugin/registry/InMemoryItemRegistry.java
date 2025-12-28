package com.omega.itemloader.plugin.registry;

import com.omega.itemloader.core.api.ItemRegistry;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class InMemoryItemRegistry implements ItemRegistry {

    private final Map<String, ItemStack> templates;

    public InMemoryItemRegistry(Map<String, ItemStack> templates) {
        this.templates = Collections.unmodifiableMap(new LinkedHashMap<>(templates));
    }

    @Override
    public Set<String> getIds() {
        return templates.keySet();
    }

    @Override
    public Optional<ItemStack> create(String id) {
        ItemStack template = templates.get(id);
        return template == null ? Optional.empty() : Optional.of(template.clone());
    }

    @Override
    public Map<String, ItemStack> createAll() {
        Map<String, ItemStack> result = new LinkedHashMap<>(templates.size());
        for (Map.Entry<String, ItemStack> entry : templates.entrySet()) {
            result.put(entry.getKey(), entry.getValue().clone());
        }
        return result;
    }
}
