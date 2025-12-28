package com.omega.racing.plugin.runtime.blocks;

import com.omega.racing.core.OmegaRacingConstants;
import org.bukkit.NamespacedKey;

import java.util.Objects;

public final class RaceBlocksKeys {
    private RaceBlocksKeys() {
    }

    private static NamespacedKey key(String path) {
        return Objects.requireNonNull(
            NamespacedKey.fromString(OmegaRacingConstants.NAMESPACE + ":" + path)
        );
    }

    public static final NamespacedKey UI_ACTION = key("race_blocks.action");

    // Position tool (stick) given from the race blocks UI.
    public static final NamespacedKey POSITION_TOOL = key("race_blocks.position_tool");
    public static final NamespacedKey POSITION_TOOL_RACE = key("race_blocks.position_tool_race");
    public static final NamespacedKey PICK_RACER_UUID = key("race_blocks.pick_racer_uuid");
    public static final NamespacedKey PICK_POSITION_INDEX = key("race_blocks.pick_position_index");

    // Compound stage identifier stored on the ItemStack PDC (stable encoding, versioned).
    public static final NamespacedKey STAGE_ID = key("race_blocks.stage_id");

    // Stored on the Chunk PDC to persist identification of placed stage blocks.
    // Keyed by local chunk coords (0..15, 0..15) plus world y.
    public static NamespacedKey placedStageKey(int localX, int y, int localZ) {
        return key("race_blocks.placed." + localX + "_" + y + "_" + localZ);
    }

    public static final String ACTION_PREV = "prev";
    public static final String ACTION_NEXT = "next";
    public static final String ACTION_CLOSE = "close";
    public static final String ACTION_POSITION_TOOL = "position_tool";

    public static final String KIND_SECTION = "section";
}
