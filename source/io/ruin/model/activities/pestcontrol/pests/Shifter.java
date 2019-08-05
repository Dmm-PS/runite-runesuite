package io.ruin.model.activities.pestcontrol.pests;

import io.ruin.api.utils.Random;
import io.ruin.model.activities.pestcontrol.PestControlGame;
import io.ruin.model.combat.Killer;
import io.ruin.model.entity.npc.NPCCombat;
import io.ruin.model.entity.player.Player;
import io.ruin.model.map.Bounds;
import io.ruin.model.map.Position;

public final class Shifter extends NPCCombat {

    @Override
    public void init() {
        npc.startEvent(event -> {
            event.delay(3);
            teleport();
        });
    }

    @Override
    public void follow() {
        follow(1);
    }

    @Override
    public boolean attack() {
        //if (!withinDistance(1)) {
        //   return false;
        //}
        npc.face(getPestControlGame().getVoidKnight());
        basicAttack(info.attack_animation, info.attack_style, info.max_damage);
        return true;
    }

    @Override
    public void dropItems(Killer killer) {
        // Overriding this prevents npc from dropping items in Pest Control
    }

    private void teleport() {
        Position destination = getPestControlGame().getVoidKnight().getPosition().copy().translate(Random.get(-1, 1), Random.get(-1, 1), 0);

        npc.graphics(654);
        npc.animate(3904);

        npc.getMovement().teleport(destination);
        npc.attackBounds = new Bounds(destination, 16);
        npc.getPosition().set(destination.getX(), destination.getY());

        npc.startEvent(event -> {
            event.delay(1);
            npc.getCombat().setTarget(getPestControlGame().getVoidKnight());
        });

    }

    private PestControlGame getPestControlGame() {
        return PestControlGame.getInstance(npc);
    }

}