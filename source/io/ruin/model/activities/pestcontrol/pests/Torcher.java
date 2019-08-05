package io.ruin.model.activities.pestcontrol.pests;

import io.ruin.model.activities.pestcontrol.PestControlGame;
import io.ruin.model.combat.AttackStyle;
import io.ruin.model.combat.Killer;
import io.ruin.model.entity.npc.NPCCombat;
import io.ruin.model.map.Projectile;

public class Torcher extends NPCCombat {
    private static final Projectile MAGIC_PROJECTILE = new Projectile(647, 50, 30, 50, 25, 10, 15, 0);

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
        projectileAttack(MAGIC_PROJECTILE, info.attack_animation, AttackStyle.MAGIC, info.max_damage);
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

