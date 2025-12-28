package com.omega.invholder.plugin.registry;

import com.omega.invholder.core.api.InventoryRegistry;
import com.omega.invholder.infrastructure.model.InventoryTemplate;
import com.omega.invholder.infrastructure.resolve.ItemReferenceResolver;
import org.bukkit.inventory.Inventory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class InMemoryInventoryRegistry implements InventoryRegistry {

    private final ItemReferenceResolver resolver;

    private Map<String, InventoryTemplate> templates = Map.of();

    public InMemoryInventoryRegistry(ItemReferenceResolver resolver) {
        this.resolver = resolver;
    }

    public void setTemplates(Map<String, InventoryTemplate> templates) {
        this.templates = Collections.unmodifiableMap(new LinkedHashMap<>(templates));
    }

    @Override
    public Set<String> getIds() {
        return templates.keySet();
    }

    @Override
    public Optional<Inventory> create(String id) {
        InventoryTemplate template = templates.get(id);
        return template == null ? Optional.empty() : Optional.of(template.create(resolver));
    }

    @Override
    public Map<String, Inventory> createAll() {
        Map<String, Inventory> result = new LinkedHashMap<>(templates.size());
        for (Map.Entry<String, InventoryTemplate> entry : templates.entrySet()) {
            result.put(entry.getKey(), entry.getValue().create(resolver));
        }
        return result;
    }

    public Optional<InventoryTemplate> template(String id) {
        return Optional.ofNullable(templates.get(id));
    }

    public Map<String, InventoryTemplate> templates() {
        return templates;
    }
}
