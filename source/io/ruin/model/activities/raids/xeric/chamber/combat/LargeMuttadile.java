package io.ruin.model.activities.raids.xeric.chamber.combat;

import io.ruin.api.utils.Random;
import io.ruin.model.combat.AttackStyle;
import io.ruin.model.combat.Hit;
import io.ruin.model.entity.npc.NPCCombat;
import io.ruin.model.map.Projectile;
import io.ruin.model.skills.prayer.Prayer;

public class LargeMuttadile extends NPCCombat {

    private static final Projectile RANGED_PROJECTILE = new Projectile(1291, 20, 31, 20, 15, 12, 15, 10);
    private static final Projectile MAGIC_PROJECTILE = new Projectile(1046, 20, 31, 20, 15, 12, 15, 10);

    @Override
    public void init() {

    }

    @Override
    public void follow() {
        follow(1);
    }

    @Override
    public boolean attack() {
        if (!withinDistance(8))
            return false;
        if (withinDistance(1) && Random.rollDie(4, 1))
            stompAttack();
        else if (withinDistance(1))
            meleeAttack();
        else if (Random.rollDie(2, 1))
            magicAttack();
        else
            rangedAttack();
        return true;
    }

    private void meleeAttack() {
        int maxDamage = 40;
        if (target.player != null && target.player.getPrayer().isActive(Prayer.PROTECT_FROM_MELEE))
            maxDamage *= 0.6;
        npc.animate(info.attack_animation);
        target.hit(new Hit(npc, AttackStyle.CRUSH).randDamage(maxDamage).ignorePrayer());
    }

    private void stompAttack() {
        npc.animate(7424);
        target.hit(new Hit(npc, AttackStyle.CRUSH).randDamage(78).ignorePrayer().ignoreDefence());
    }

    private void rangedAttack() {
        int maxDamage = 35;
        if (target.player != null && target.player.getPrayer().isActive(Prayer.PROTECT_FROM_MISSILES))
            maxDamage *= 0.6;
        int delay = RANGED_PROJECTILE.send(npc, target);
        Hit hit = new Hit(npc, AttackStyle.RANGED).randDamage(maxDamage).clientDelay(delay).ignorePrayer();
        target.hit(hit);
    }

    private void magicAttack() {
        int maxDamage = 45;
        if (target.player != null && target.player.getPrayer().isActive(Prayer.PROTECT_FROM_MAGIC))
            maxDamage *= 0.6;
        int delay = MAGIC_PROJECTILE.send(npc, target);
        Hit hit = new Hit(npc, AttackStyle.MAGIC).randDamage(maxDamage).clientDelay(delay).ignorePrayer();
        target.hit(hit);
    }

    @Override
    public boolean isAggressive() {
        return npc.getId() != 7561 && !npc.isLocked();
    }
}
