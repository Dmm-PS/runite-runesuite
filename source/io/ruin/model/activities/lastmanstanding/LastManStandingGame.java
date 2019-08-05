package io.ruin.model.activities.lastmanstanding;

import io.ruin.model.World;
import io.ruin.model.entity.player.Player;
import io.ruin.model.inter.Interface;
import io.ruin.model.inter.InterfaceType;
import io.ruin.model.inter.utils.Config;
import io.ruin.model.item.Item;
import io.ruin.model.map.Tile;
import io.ruin.model.map.dynamic.DynamicMap;
import io.ruin.utility.TickDelay;

import java.util.LinkedList;
import java.util.List;

/**
 * Handles functionality for Last Man Standing minigame
 *
 * @author Andys1814
 */
public final class LastManStandingGame { //inter 333, 328

    private final DynamicMap map;

    private final List<Player> players;

    private final int initialSurvivors;

    private int minutesElapsed;

    private final LastManStandingSafeZone safeZone = LastManStandingSafeZone.random();

    private FogStatus fogStatus = FogStatus.SAFE;

    //survivors: 9, kills: 11, fog status: 13

    public LastManStandingGame(List<Player> players) {
        this.players = players;
        initialSurvivors = players.size();
        map = new DynamicMap()
                .buildSw(13658, 1)
                .buildNw(13659, 1)
                .buildSe(13914, 1)
                .buildNe(13915, 1);
    }

    private TickDelay fogTimer = new TickDelay();

    private TickDelay minutesTimer = new TickDelay();

    private TickDelay chestSpawnDelay = new TickDelay();

    private TickDelay gracePeriod = new TickDelay();

    public void start() {
        players.forEach(player -> {
            Config.LMS_IN_GAME.set(player, 1);
            player.getMovement().teleport(map.swRegion.bounds.randomX(), map.swRegion.bounds.randomY());
            new LastManStandingPreset().selectFinish(player);
            player.openInterface(InterfaceType.SECONDARY_OVERLAY, Interface.WILDERNESS_OVERLAY);
            player.getPacketSender().setHidden(90, 2, false);
           // player.openInterface(InterfaceType.SECONDARY_OVERLAY, 328);
            player.getPacketSender().sendString(328, 13, fogStatus.status);
            player.getPacketSender().sendString(328, 9, initialSurvivors + " / " + initialSurvivors);
        });

        World.startEvent(event -> { // Main game cycle
            chestSpawnDelay.delaySeconds(120);
            minutesTimer.delaySeconds(60);

//            Config.LMS_POISON_PROGRESS.set(player, 70);
//            Config.LMS_POISON_PROGRESS.get(player)

            startCountdown();

            while (players.size() == 1) { //TODO testing set it > 1
                if (!minutesTimer.isDelayed()) {
                    minutesElapsed++;
                    players.forEach(player -> {
                        if (minutesElapsed == 1) {
                            player.getPacketSender().sendMessage("Letal fog will soon fill the island! The safe zone is " + safeZone.getText() + ".", "", 14);
                            player.getPacketSender().sendHintIcon(map.convertX(safeZone.getX()), map.convertY(safeZone.getY()));
                            Config.LMS_KILLS.set(player, 5);
                            Config.FOG_LEVEL.set(player, 15);
                            Config.LMS_POISON_PROGRESS.set(player, 10);
                            Config.LMS_SAFE_X.set(player, map.convertX(safeZone.getX()));
                            Config.LMS_SAFE_Y.set(player, map.convertY(safeZone.getY()));
                            System.out.println("fogx " + Config.LMS_SAFE_X.get(player) + " fogy " + Config.LMS_SAFE_Y.get(player));
                        }
                        player.getPacketSender().sendString(328, 14, minutesElapsed + " min");
                    });
                    minutesTimer.delaySeconds(60);
                }
                if (!chestSpawnDelay.isDelayed()) {
                    chestSpawnDelay.delaySeconds(120);
                    LastManStandingCrate.spawn(map, players);
                }
                event.delay(1);
                players.forEach(player -> {
                    player.getPacketSender().sendClientScript(1345, "");
                });
            }
        });
    }

    private void startCountdown() {
        players.forEach(player -> player.startEvent(e -> {
            player.lock();
            e.delay(1);

            for (int i = 15; i > 0; i--) {
                player.forceText(i + "...");
                e.delay(2);
            }

            player.forceText("GO!");
            player.unlock();

            gracePeriod.delay(10);
            player.sendMessage("The grace period has begun!");

            e.delay(10);
            player.sendMessage("The fight for survival has begun!");
        }));
    }

    private enum FogStatus {
        SAFE("Safe"), APPROACHING("Approaching"), DANGER("Danger");

        private final String status;

        FogStatus(String status) {
            this.status = status;
        }
    }

}
