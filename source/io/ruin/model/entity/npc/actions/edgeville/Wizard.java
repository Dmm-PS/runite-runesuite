package io.ruin.model.entity.npc.actions.edgeville;

import io.ruin.data.impl.teleports;
import io.ruin.model.entity.npc.NPCAction;

public class Wizard {

    static {
        for(int wizard : new int[]{4398, 4159}) {
            NPCAction.register(wizard, "teleport", (player, npc) -> teleports.open(player));
            NPCAction.register(wizard, "teleport-previous", (player, npc) -> teleports.previous(player));
        }
    }

}
