package io.ruin.model.activities.miscpvm.slayer;

import io.ruin.api.utils.Random;
import io.ruin.model.combat.Hit;
import io.ruin.model.entity.Entity;
import io.ruin.model.entity.npc.NPCCombat;
import io.ruin.model.map.Position;
import io.ruin.model.map.route.routes.ProjectileRoute;

import java.util.List;

public class AbyssalDemon extends NPCCombat {

    @Override
    public void init() {

    }

    @Override
    public void follow() {
        follow(1);
    }

    @Override
    public boolean attack() {
        if (!withinDistance(1))
            return false;
        if (Random.rollDie(4, 1) && teleportAttack())
            return true;
        else
            basicAttack();
        return true;
    }

    private boolean teleportAttack() {
        Entity entity = Random.rollDie(2, 1) ? npc : target;
        List<Position> positions = target.getPosition().area(1, pos -> pos.getTile().clipping == 0 && !pos.equals(entity.getPosition()) && ProjectileRoute.allow(npc, pos));
        Position destination = Random.get(positions);
        entity.getMovement().teleport(destination);
        entity.graphics(409);
        if (entity == target)
            target.getCombat().reset();
        Hit hit = new Hit(npc, info.attack_style);
        hit.nullify();
        target.hit(hit);
        return true;
    }
}
