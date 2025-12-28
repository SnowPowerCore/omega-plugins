package com.omega.itemloader.infrastructure.meta;

import com.google.gson.JsonObject;
import com.omega.itemloader.infrastructure.json.JsonRead;
import org.bukkit.MusicInstrument;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MusicInstrumentMeta;

import jakarta.inject.Inject;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

public final class MusicInstrumentMetaHandler implements ItemMetaHandler {

    private final Logger logger;

    @Inject
    public MusicInstrumentMetaHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> types() {
        return Set.of(
                MetaParsing.normalizeType("musicinstrument"),
                MetaParsing.normalizeType("MusicInstrumentMeta")
        );
    }

    @Override
    public boolean supports(ItemMeta meta) {
        return meta instanceof MusicInstrumentMeta;
    }

    @Override
    public void apply(String itemId, JsonObject metaObject, ItemMeta meta) {
        if (!(meta instanceof MusicInstrumentMeta instrumentMeta)) {
            logger.warning("meta.type 'musicinstrument' provided but ItemMeta is not MusicInstrumentMeta (item id=" + itemId + ")");
            return;
        }

        JsonRead.optionalString(metaObject, "instrument")
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .ifPresent(raw -> {
                    MusicInstrument instrument = null;

                    if (raw.contains(":")) {
                        NamespacedKey key = NamespacedKey.fromString(raw.toLowerCase(Locale.ROOT));
                        if (key != null) {
                            instrument = MusicInstrument.getByKey(key);
                        }
                    } else {
                        instrument = MusicInstrument.getByKey(NamespacedKey.minecraft(raw.toLowerCase(Locale.ROOT)));
                    }

                    if (instrument == null) {
                        try {
                            Field field = MusicInstrument.class.getField(raw.trim().toUpperCase(Locale.ROOT));
                            instrument = (MusicInstrument) field.get(null);
                        } catch (Exception ignored) {
                        }
                    }

                    if (instrument == null) {
                        logger.warning("Unknown MusicInstrument '" + raw + "' (item id=" + itemId + ")");
                        return;
                    }

                    instrumentMeta.setInstrument(instrument);
                });
    }
}
