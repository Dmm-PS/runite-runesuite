package io.ruin.model.activities.raids.tob.wave.impl;

import io.ruin.model.activities.raids.tob.wave.TheatreOfBloodWave;
import io.ruin.model.entity.player.Player;
import io.ruin.model.map.dynamic.DynamicMap;

public final class NylocasWave extends TheatreOfBloodWave {

    public NylocasWave() {
        super("The Nylocas");
    }

    @Override
    public DynamicMap buildMap() {
        return new DynamicMap().build(13122, 0);
    }

    @Override
    public void onTheatreStart() {

    }

    @Override
    public void teleportToTest(Player player) {

    }
}
