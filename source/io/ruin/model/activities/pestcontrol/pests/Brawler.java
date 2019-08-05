package io.ruin.model.activities.pestcontrol.pests;

import io.ruin.model.combat.Killer;
import io.ruin.model.entity.npc.NPCCombat;

public final class Brawler extends NPCCombat {

    @Override
    public void init() {
    }

    @Override
    public void follow() {
        follow(1);
    }

    @Override
    public boolean attack() {
        if (!withinDistance(1)) {
            return false;
        }

        basicAttack(info.attack_animation, info.attack_style, info.max_damage);
        return true;
    }

    @Override
    public void dropItems(Killer killer) {
        // Overriding this prevents Brawler from dropping items in Pest Control
    }

    @Override
    public boolean isAggressive() {
        return true;
    }

}