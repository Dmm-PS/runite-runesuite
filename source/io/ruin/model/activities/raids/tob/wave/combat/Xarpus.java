package io.ruin.model.activities.raids.tob.wave.combat;

import io.ruin.api.utils.Random;
import io.ruin.model.entity.npc.NPCCombat;
import io.ruin.model.entity.player.Player;
import io.ruin.model.map.Projectile;

import java.util.List;

public final class Xarpus extends NPCCombat {

    private static final Projectile POISON_PROJECTILE = new Projectile(1555, 75, 11, 40, 75, 4, 16, 64);

    private static final int POISON_SPLATTER = 1556;

    private XarpusPhase phase = XarpusPhase.ONE;

    @Override
    public void init() {
        System.out.println("OH HI!");
    }

    @Override
    public void follow() {
        // Prevents Xarpus from following
    }

    @Override
    public boolean attack() {
        System.out.println("Hi");
        List<Player> localPlayers = npc.localPlayers();
        Player player = Random.get(localPlayers);
        if (phase == XarpusPhase.ONE) {
            int delay = POISON_PROJECTILE.send(npc, player.getPosition().copy());
            player.graphics(POISON_SPLATTER, 10, delay);
            return true;
        } else {

            return true;
        }
    }

    private enum XarpusPhase {
        ONE, TWO
    }
}
