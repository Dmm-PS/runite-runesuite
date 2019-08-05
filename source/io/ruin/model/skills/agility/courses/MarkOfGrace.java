package io.ruin.model.skills.agility.courses;

import io.ruin.api.utils.Random;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.player.PlayerGroup;
import io.ruin.model.item.Item;
import io.ruin.model.map.Position;
import io.ruin.model.map.ground.GroundItem;

import java.util.List;

public class MarkOfGrace {

    public static void rollMark(Player player, int levelReq, List<Position> spawns) {
        if (spawns == null)
            return;
        double chance = levelReq / 2 / 100.0;
        if (Random.get() <= chance) {
            Position spawn = Random.get(spawns);
            new GroundItem(new Item(11849, (Random.get(1, 4) + markOfGraceDonatorIncrease(player)))).owner(player).position(spawn).spawn(2);
        }
    }

    private static int markOfGraceDonatorIncrease(Player player) {
        if (player.isGroup(PlayerGroup.GODLIKE_DONATOR)) {
            return 5;
        } else if (player.isGroup(PlayerGroup.UBER_DONATOR)) {
            return 5;
        } else if (player.isGroup(PlayerGroup.LEGENDARY_DONATOR)) {
            return 4;
        } else if (player.isGroup(PlayerGroup.EXTREME_DONATOR)) {
            return 3;
        } else if (player.isGroup(PlayerGroup.SUPER_DONATOR)) {
            return 2;
        } else if (player.isGroup(PlayerGroup.DONATOR)) {
            return 1;
        } else {
            return 0;
        }
    }
}
