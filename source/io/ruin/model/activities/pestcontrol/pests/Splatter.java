package io.ruin.model.activities.pestcontrol.pests;

import io.ruin.model.combat.Hit;
import io.ruin.model.combat.Killer;
import io.ruin.model.entity.npc.NPCCombat;
import io.ruin.model.entity.player.Player;
import io.ruin.utility.Misc;

public class Splatter extends NPCCombat {


    @Override
    public void init() {

    }

    @Override
    public void follow() {

    }

    @Override
    public boolean attack() {
        return false;
    }
    @Override
    public void startDeath(Hit killHit) {
        super.startDeath(killHit);

        npc.addEvent(event -> {
            event.delay(1);
            for(Player player : npc.localPlayers()) {
                if(Misc.getDistance(npc.getPosition(), player.getPosition()) > 1)
                    continue;
                player.hit(new Hit().randDamage((player.getMaxHp() / 5)));
            }
        });
    }

    @Override
    public void dropItems(Killer killer) {
        // Overriding this prevents npc from dropping items in Pest Control
    }

}
