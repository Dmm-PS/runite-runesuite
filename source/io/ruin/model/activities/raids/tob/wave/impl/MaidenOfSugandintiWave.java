package io.ruin.model.activities.raids.tob.wave.impl;

import io.ruin.model.activities.raids.tob.wave.TheatreOfBloodWave;
import io.ruin.model.entity.npc.NPC;
import io.ruin.model.entity.player.Player;
import io.ruin.model.map.Direction;
import io.ruin.model.map.dynamic.DynamicMap;

public final class MaidenOfSugandintiWave extends TheatreOfBloodWave {

    private static final int MAIDEN_OF_SUGANDINTI = 8360;

    private static final int[] TEST_SPAWN = { 35, 26 };

    public MaidenOfSugandintiWave() {
        super("Maiden of Sugandinti");
    }

    @Override
    public DynamicMap buildMap() {
        return new DynamicMap().buildNe(12613, 0).buildNw(12869, 0);
    }

    @Override
    public void onTheatreStart() {
        NPC npc = new NPC(MAIDEN_OF_SUGANDINTI);
        npc.spawn(map.neRegion.baseX + 26, map.neRegion.baseY + 28, 0, Direction.EAST, 0);
        System.out.println(npc.getPosition());
    }

    @Override
    public void teleportToTest(Player player) {
        player.getMovement().teleport(getPosition(TEST_SPAWN));
    }
}
