package io.ruin.model.activities.pestcontrol.pests;

import io.ruin.model.activities.pestcontrol.PestControlGame;
import io.ruin.model.combat.Killer;
import io.ruin.model.entity.Entity;
import io.ruin.model.entity.npc.NPCCombat;

public final class Spinner extends NPCCombat {

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
    public boolean allowRetaliate(Entity attacker) {
        return false;
    }

    @Override
    public void dropItems(Killer killer) {
        // Overriding this prevents npc from dropping items in Pest Control
    }

    private PestControlGame getPestControlGame() {
        return PestControlGame.getInstance(npc);
    }

}