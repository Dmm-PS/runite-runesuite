package io.ruin.model.entity.npc.actions.edgeville;

import io.ruin.model.entity.npc.NPCAction;
import io.ruin.model.inter.dialogue.MessageDialogue;
import io.ruin.model.map.object.actions.ObjectAction;

public class GE {

    static int[] npc = new int[]{2148, 2149, 2150, 2151};

    static  {
        ObjectAction.register(10061, "exchange", (player, object)-> player.dialogue(new MessageDialogue("The Grand Exchange is temporarily disabled. And is expected to be fully functional within a week.")));
        ObjectAction.register(10061, "collect", (player, object)-> player.dialogue(new MessageDialogue("The Grand Exchange is temporarily disabled. And is expected to be fully functional within a week.")));

        for(int id : npc) {
            NPCAction.register(id, "history", (player, npc1)-> player.dialogue(new MessageDialogue("The Grand Exchange is temporarily disabled. And is expected to be fully functional within a week.")));
            NPCAction.register(id, "talk-to", (player, npc1)-> player.dialogue(new MessageDialogue("The Grand Exchange is temporarily disabled. And is expected to be fully functional within a week.")));
            NPCAction.register(id, "exchange", (player, npc1)-> player.dialogue(new MessageDialogue("The Grand Exchange is temporarily disabled. And is expected to be fully functional within a week.")));
            NPCAction.register(id, "sets", (player, npc1)-> player.dialogue(new MessageDialogue("The Grand Exchange is temporarily disabled. And is expected to be fully functional within a week.")));
        }
    }

}
