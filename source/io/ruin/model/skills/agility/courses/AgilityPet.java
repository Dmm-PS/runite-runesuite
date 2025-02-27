package io.ruin.model.skills.agility.courses;

import io.ruin.api.utils.Random;
import io.ruin.model.World;
import io.ruin.model.entity.player.Player;
import io.ruin.model.item.actions.impl.Pet;

public class AgilityPet {

    public static void rollForPet(Player player, int chance) {
        if (Random.rollDie(World.isPVP() ? (chance / 20) : chance))
            Pet.GIANT_SQUIRREL.unlock(player);
    }

}
