package io.ruin.model.activities.raids.xeric.chamber.impl;

import io.ruin.model.activities.raids.xeric.chamber.Chamber;
import io.ruin.model.activities.raids.xeric.chamber.ChamberDefinition;
import io.ruin.model.activities.raids.xeric.chamber.ChamberType;
import io.ruin.model.map.Position;
import io.ruin.model.map.object.actions.ObjectAction;

public class CheckpointChamber extends Chamber {

    private static final int[][] START_POSITION = { // starting chamber, entrance position
            {14, 3},
            {2, 4},
            {2, 4},
    };

    private static final int[] UPPER_FINISH_ENTRANCE_POSITION = {
            14, 16,
    };

    private static final int[] LOWER_START_ENTRANCE_POSITION = {
            15, 15
    };

    private static final int[] LOWER_FINISH_ENTRANCE_POSITION = {
            12, 21,
    };

    private static final int[][] START_RESPAWN = { // starting chamber, respawn
            {14, 12},
            {11, 13},
            {13, 13},
    };

    private static final int[][] LOWER_RESPAWN = {
            {15, 16},
            {5, 11},
            {15, 16}
    };

    @Override
    public void onRaidStart() {
        //System.out.println("CHECK POINT CHAMBER LAYOUT = " + getLayout());
        if (getDefinition() == ChamberDefinition.LOWER_LEVEL_START) {
            System.out.println("lower level start chamber layout " + getLayout());
        }

        ObjectAction.register(30066, "touch", (player, obj) -> {
            player.getStats().restore(false);
            player.getMovement().restoreEnergy(100);
            player.cureVenom(0);
        });
    }

    public Position getRespawnPosition() {
        switch (getDefinition()) {
            case START:
                return getPosition(START_RESPAWN[getLayout()]);
            case LOWER_LEVEL_START:
                return getPosition(LOWER_RESPAWN[getLayout()]);
        }
        return null;
    }

    public Position getEntrancePosition() {
        switch (getDefinition()) {
            case START:
                return getPosition(START_POSITION[getLayout()]);
            case UPPER_FLOOR_FINISH:
                return getPosition(UPPER_FINISH_ENTRANCE_POSITION);
            case LOWER_LEVEL_START:
                return getPosition(LOWER_START_ENTRANCE_POSITION);
            case LOWER_FLOOR_FINISH:
                return getPosition(LOWER_FINISH_ENTRANCE_POSITION);
        }
        return null;
    }
}
