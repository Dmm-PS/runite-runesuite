package io.ruin.model.activities.pestcontrol.pests;

import io.ruin.model.combat.Killer;
import io.ruin.model.entity.npc.NPCCombat;

public class Ravager  extends NPCCombat {
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
    public void dropItems(Killer killer) {
        // Overriding this prevents npc from dropping items in Pest Control
    }
}
