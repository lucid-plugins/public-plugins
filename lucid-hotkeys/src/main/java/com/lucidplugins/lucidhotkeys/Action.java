package com.lucidplugins.lucidhotkeys;

public enum Action
{
    NONE(0, 1),
    PRINT_VARIABLE(1, 2),
    SET_DELAY(2, 2),
    RESET_DELAY(3, 1),
    CLOSE_BANK(4, 1),
    WALK_RELATIVE_TO_SELF(5, 3),
    WALK_ABSOLUTE_LOCATION(6, 3),
    INTERACT_NEAREST_NAMED_NPC(7, 3),
    INTERACT_NEAREST_NAMED_OBJECT(8, 3),
    INTERACT_NAMED_INVENTORY_ITEM(9, 3),
    INTERACT_NEAREST_ID_NPC(10, 3),
    INTERACT_NEAREST_ID_OBJECT(11, 3),
    INTERACT_ID_INVENTORY_ITEM(12, 3),
    RELOAD_VARS(13, 1),
    SET_VAR_VALUE(14, 3),
    INTERACT_NEAREST_NAMED_TILE_ITEM(15, 3),
    INTERACT_NEAREST_ID_TILE_ITEM(16, 3),
    SET_TICK_METRONOME_MAX_VALUE(17, 2),
    SET_TICK_METRONOME_VALUE(18, 2),
    SET_TICK_METRONOME_TO_MAX(19, 1),

    ADD_VALUE_TO_VARIABLE(20, 3),

    INTERACT_NAMED_PLAYER(21, 3),

    INTERACT_LAST_PLAYER_YOU_TARGETED(22, 2),
    INTERACT_LAST_PLAYER_TARGETED_YOU(23, 2),
    INTERACT_LAST_NPC_YOU_TARGETED(24, 2),
    INTERACT_LAST_NPC_TARGETED_YOU(25, 2),

    ACTIVATE_PRAYER(26, 2),
    TOGGLE_PRAYER(27, 2),
    TOGGLE_SPEC(28, 1),
    WALK_SCENE_LOCATION(29, 3),

    ;

    int id;
    int paramsNeeded;

    Action(int id, int paramsNeeded)
    {
        this.id = id;
        this.paramsNeeded = paramsNeeded;
    }

    public static Action forId(int id)
    {
        for (Action action : values())
        {
            if (action.getId() == id)
            {
                return action;
            }
        }
        return NONE;
    }


    public int getId()
    {
        return id;
    }

    public int getParamsNeeded()
    {
        return paramsNeeded;
    }

}
