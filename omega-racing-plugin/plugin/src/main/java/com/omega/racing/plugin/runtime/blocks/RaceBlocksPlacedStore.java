package com.omega.racing.plugin.runtime.blocks;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import jakarta.inject.Singleton;

import java.util.Optional;
import java.util.Base64;

@Singleton
public final class RaceBlocksPlacedStore {

    public record StageId(String raceName, String kind, int index) {
    }

    public Optional<StageId> stageIdFromItem(ItemStack stack) {
        if (stack == null) {
            return Optional.empty();
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return Optional.empty();
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String encoded = pdc.get(RaceBlocksKeys.STAGE_ID, PersistentDataType.STRING);
        return encoded == null ? Optional.empty() : decodeStageId(encoded);
    }

    public void setStageId(Block block, StageId id) {
        if (block == null || id == null) {
            return;
        }

        Chunk chunk = block.getChunk();
        int localX = block.getX() & 15;
        int localZ = block.getZ() & 15;
        int y = block.getY();

        String value = encodeStageId(id.raceName(), id.kind(), id.index());
        chunk.getPersistentDataContainer().set(
            RaceBlocksKeys.placedStageKey(localX, y, localZ),
            PersistentDataType.STRING,
            value
        );
    }

    public Optional<StageId> getStageId(Block block) {
        if (block == null) {
            return Optional.empty();
        }

        Chunk chunk = block.getChunk();
        int localX = block.getX() & 15;
        int localZ = block.getZ() & 15;
        int y = block.getY();

        String value = chunk.getPersistentDataContainer().get(
            RaceBlocksKeys.placedStageKey(localX, y, localZ),
            PersistentDataType.STRING
        );
        if (value == null) {
            return Optional.empty();
        }
        return decodeStageId(value);
    }

    public void clearStageId(Block block) {
        if (block == null) {
            return;
        }

        Chunk chunk = block.getChunk();
        int localX = block.getX() & 15;
        int localZ = block.getZ() & 15;
        int y = block.getY();

        chunk.getPersistentDataContainer().remove(RaceBlocksKeys.placedStageKey(localX, y, localZ));
    }

    public static String encodeStageId(String raceName, String kind, int index) {
        // Stable scheme (no versioning).
        // Format: <base64url(raceName)>:<base64url(kind)>:<index>
        // Base64URL avoids delimiter collisions and newline issues.
        return b64url(raceName) + ":" + b64url(kind) + ":" + index;
    }

    public static Optional<StageId> decodeStageId(String value) {
        if (value == null) {
            return Optional.empty();
        }

        String[] parts = value.split(":", 3);
        if (parts.length != 3) {
            return Optional.empty();
        }
        try {
            String raceName = unb64url(parts[0]);
            String kind = unb64url(parts[1]);
            int index = Integer.parseInt(parts[2]);
            return Optional.of(new StageId(raceName, kind, index));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static String b64url(String value) {
        String safe = value == null ? "" : value;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(safe.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static String unb64url(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        byte[] decoded = Base64.getUrlDecoder().decode(value);
        return new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
    }
}
