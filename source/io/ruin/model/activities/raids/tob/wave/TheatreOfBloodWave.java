package io.ruin.model.activities.raids.tob.wave;

import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.shared.StepType;
import io.ruin.model.inter.dialogue.OptionsDialogue;
import io.ruin.model.inter.utils.Option;
import io.ruin.model.map.Position;
import io.ruin.model.map.dynamic.DynamicMap;
import io.ruin.model.map.object.GameObject;
import io.ruin.model.map.object.actions.ObjectAction;

import java.time.Duration;
import java.time.Instant;

public abstract class TheatreOfBloodWave {

    private static final int BARRIER = 32755;

    protected final String name;

    protected final DynamicMap map;

    protected Position basePosition;

    protected TheatreOfBloodWaveStatus status;

    protected Instant startTime = Instant.now();

    protected TheatreOfBloodWave(String name) {
        this.name = name;
        this.map = buildMap();
        status = TheatreOfBloodWaveStatus.UNSTARTED;
        ObjectAction.register(BARRIER, "pass", (player, obj1) -> {
            if (status == TheatreOfBloodWaveStatus.UNSTARTED) {
                player.dialogue(new OptionsDialogue("Is your party ready to fight?",
                        new Option("Yes, let's begin.", p -> {
                            status = TheatreOfBloodWaveStatus.IN_PROGRESS;
                            startTime = Instant.now();
                            passBarrier(player, obj1);
                        }),
                        new Option("No, don't start yet")
                ));
            }
        });
    }

    /** So that wave implementations can provide their own map (waves differ in height, region count, etc.) */
    public abstract DynamicMap buildMap();

    public abstract void onTheatreStart();

    public abstract void teleportToTest(Player player);

    private String getTimeSinceStart() {
        Duration d = Duration.between(startTime, Instant.now());
        return String.format("%02d:%02d", d.toMinutes(), d.getSeconds() % 60);
    }

    public void passBarrier(Player player, GameObject object) {
        int x = player.getAbsX();
        int y = player.getAbsY();
        if (x == object.x && y < object.y) {
            y += 2;
        }

        player.stepAbs(x, y, StepType.FORCE_WALK);
    }

    public Position getPosition(int localX, int localY) {
        if (basePosition == null)
            throw new IllegalStateException("Base position not set");
        return basePosition.relative(localX, localY);
    }

    public Position getPosition(int[] localCoords) {
        return getPosition(localCoords[0], localCoords[1]);
    }

}
