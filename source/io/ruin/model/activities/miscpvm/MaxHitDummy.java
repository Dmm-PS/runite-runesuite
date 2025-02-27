package io.ruin.model.activities.miscpvm;

import io.ruin.model.World;
import io.ruin.model.combat.Hit;
import io.ruin.model.combat.HitType;
import io.ruin.model.entity.Entity;
import io.ruin.model.entity.shared.listeners.HitListener;
import io.ruin.model.inter.utils.Config;

public class MaxHitDummy extends PassiveCombat {

    @Override
    public void init() {
        npc.hitListener = new HitListener()
                .postDefend(h -> {
                    if(h.isBlocked())
                        h.type = HitType.DAMAGE;
                    if(h.type == HitType.DAMAGE)
                        h.damage = h.maxDamage;
                });
    }

    @Override
    public void updateLastDefend(Entity attacker) {
        super.updateLastDefend(attacker);
        if(attacker.player != null && !World.isEco()) {
            attacker.player.addEvent(e -> {
                e.delay(2);
                if(attacker.player.nurseSpecialRefillCooldown.isDelayed() && !attacker.player.isAdmin()) {
                    return;
                }
                attacker.player.nurseSpecialRefillCooldown.delaySeconds(60);
                attacker.player.getCombat().restoreSpecial(100);
            });
        }
    }

    @Override
    public void startDeath(Hit killHit) {
        npc.setHp(npc.getMaxHp());
    }

}