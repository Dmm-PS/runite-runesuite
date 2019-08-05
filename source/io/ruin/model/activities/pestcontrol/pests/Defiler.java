package io.ruin.model.activities.pestcontrol.pests;

import io.ruin.model.activities.pestcontrol.PestControlGame;
import io.ruin.model.combat.AttackStyle;
import io.ruin.model.combat.Hit;
import io.ruin.model.combat.Killer;
import io.ruin.model.entity.npc.NPCCombat;
import io.ruin.model.map.Projectile;

public final class Defiler extends NPCCombat {

    private static final Projectile RANGED_PROJECTILE = new Projectile(476, 10, 31, 15, 25, 10, 15, 0);

    @Override
    public void init() {
        setTarget(getPestControlGame().getVoidKnight());
        if (target == getPestControlGame().getVoidKnight()) {
            follow();
        }
    }

    @Override
    public void follow() {
        follow(16);
    }

    @Override
    public boolean attack() {
        if (!withinDistance(15)) {
            return false;
        }
        projectileAttack(RANGED_PROJECTILE, info.attack_animation, AttackStyle.RANGED, info.max_damage);
        return true;
    }

    @Override
    public void dropItems(Killer killer) {
        // Overriding this prevents Defiler from dropping items in Pest Control
    }

    @Override
    public boolean isAggressive() {
        return true;
    }


    @Override
    public int getAttackBoundsRange() {
        return 64;
    }

    private PestControlGame getPestControlGame() {
        return PestControlGame.getInstance(npc);
    }

}