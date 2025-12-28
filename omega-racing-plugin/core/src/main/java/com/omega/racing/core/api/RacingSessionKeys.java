package com.omega.racing.core.api;

/**
 * String keys for {@link RacingSession#data()}.
 *
 * Keep these stable to avoid breaking consumers.
 */
public final class RacingSessionKeys {

    private RacingSessionKeys() {
    }

    public static final String EDITING_RACE_NAME = "editingRaceName";

    public static final String SELECTED_TEAM_INDEX = "selectedTeamIndex";
    public static final String TEAMS_PAGE = "teamsPage";
    public static final String RACERS_PAGE = "racersPage";
    public static final String PLAYER_PICKER_PAGE = "playerPickerPage";

    public static final String PENDING_PROMPT = "pendingPrompt";
    public static final String PROMPT_EXPIRES_AT_MILLIS = "prompt.expiresAtMillis";
    public static final String PROMPT_TOKEN = "prompt.token";
    public static final String PROMPT_RETURN_INVENTORY_ID = "prompt.returnInventoryId";
    public static final String PROMPT_ACTION_BAR_TASK_ID = "prompt.actionBarTaskId";

    /**
     * Map&lt;String inventoryId, String titleOverride&gt; of the last title override used for an inventory.
     *
     * This allows reopening an inventory (e.g., after chat prompt cancel/timeout) with the same dynamic title.
     */
    public static final String INVENTORY_TITLE_OVERRIDES = "inventory.titleOverrides";

    public static final String CONFIRM_KIND = "confirm.kind";
    public static final String CONFIRM_RACE_NAME = "confirm.raceName";
    public static final String CONFIRM_TEAM_INDEX = "confirm.teamIndex";
    public static final String CONFIRM_RACER_UUID = "confirm.racerUuid";
    public static final String CONFIRM_RETURN_INVENTORY_ID = "confirm.returnInventoryId";
    public static final String CONFIRM_TITLE = "confirm.title";
    public static final String CONFIRM_LORE = "confirm.lore";

    public static final String CURRENT_INVENTORY_ID = "currentInventoryId";

    public static final String PLAYER_SELECTION_SUCCESS_DELEGATE = "playerSelection.successDelegate";
    public static final String PLAYER_SELECTION_CANCEL_DELEGATE = "playerSelection.cancelDelegate";

    public static final String NUMBER_ADJUST_SUCCESS_DELEGATE = "numberAdjust.successDelegate";
    public static final String NUMBER_ADJUST_CANCEL_DELEGATE = "numberAdjust.cancelDelegate";
}
