package io.ruin.model.activities.raids.tob.wave.impl;

import io.ruin.model.activities.raids.tob.wave.TheatreOfBloodWave;
import io.ruin.model.entity.player.Player;
import io.ruin.model.map.dynamic.DynamicMap;

public final class PestilentBloatWave extends TheatreOfBloodWave {

    public PestilentBloatWave() {
        super("Pestilent Bloat");
    }

    @Override
    public DynamicMap buildMap() {
        return new DynamicMap().build(13125, 0);
    }

    @Override
    public void onTheatreStart() {

    }

    @Override
    public void teleportToTest(Player player) {

    }
}
