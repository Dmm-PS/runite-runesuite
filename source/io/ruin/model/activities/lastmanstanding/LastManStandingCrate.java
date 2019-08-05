package io.ruin.model.activities.lastmanstanding;

import io.ruin.api.utils.Random;
import io.ruin.model.entity.player.Player;
import io.ruin.model.map.Position;
import io.ruin.model.map.dynamic.DynamicMap;
import io.ruin.model.map.object.GameObject;
import io.ruin.model.map.object.actions.ObjectAction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An enumerated type whose elements represent possible Last Man Standing crates.
 *
 * @author Andys1814
 */
public enum LastManStandingCrate { //spawns 45 seconds after game start and again after 60 seconds

    DEBTOR_HIDEOUT_NORTH(new Position(3409, 5819, 0), "near the Debtor hideout"),
    DEBTOR_HIDEOUT_EAST(new Position(3421, 5801, 0), "near the Debtor hideout"),
    MOUNTAIN(new Position(3450, 5852, 0), "near the mountain"),
    MOSER_SETTLEMENT_NORTH(new Position(3479, 5809, 0), "near the Moser settlement"),
    MOSER_SETTLEMENT_EAST(new Position(3494, 5791, 0), "near the Moser settlement"),
    MOSER_SETTLEMENT_WEST(new Position(3459, 5789, 0), "near the Moser settlement"),
    ROCKY_OUTPOST(new Position(3459, 5772, 0), "near the Rocky outcrops"),
    TRINITY_OUTPOST(new Position(3480, 5876, 0), "near the Trinity outpost");

    private final Position spawnPosition;
    private final String spawnHint;

    private static final List<LastManStandingCrate> VALUES = Collections.unmodifiableList(Arrays.asList(values()));

    LastManStandingCrate(Position spawnPosition, String spawnHint) {
        this.spawnPosition = spawnPosition;
        this.spawnHint = spawnHint;
    }

    public static LastManStandingCrate randomCrate()  {
        return VALUES.get(Random.get(VALUES.size() - 1));
    }

    private static void open(Player player, GameObject obj) {
        player.startEvent(event -> {
            player.lock();
            player.animate(832);
            obj.graphics(86, 40, 0);
            player.sendMessage("You find some loot!");
            obj.remove();
            player.unlock();
        });
    }

    public static void spawn(DynamicMap LMSMap, List<Player> players) {
        LastManStandingCrate lmsCrate = randomCrate();
        GameObject.spawn(29081, LMSMap.convertPosition(lmsCrate.spawnPosition), 10, 0);
        players.forEach(player -> player.getPacketSender().sendMessage("A magical loot crate has appeared " + lmsCrate.spawnHint, "", 14));
    }

    static {
        ObjectAction.register(29081, "search", LastManStandingCrate::open);
    }

}