package com.omega.invholder.infrastructure;

import com.omega.invholder.infrastructure.model.InventoryTemplate;

import java.nio.file.Path;
import java.util.Map;

public interface InventoryFileLoader {
    Map<String, InventoryTemplate> load(Path path);
}
