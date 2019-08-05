package io.ruin.model.entity.npc.actions.edgeville;

import io.ruin.model.entity.npc.NPCAction;
import io.ruin.model.inter.dialogue.NPCDialogue;
import io.ruin.model.inter.dialogue.OptionsDialogue;
import io.ruin.model.inter.dialogue.PlayerDialogue;
import io.ruin.model.inter.utils.Option;

public class LoyaltyManager {

    private static final int LOYALTY_MANAGER = 8276;

    static {
        NPCAction.register(LOYALTY_MANAGER, "talk-to", ((player, npc) -> player.dialogue(
                new PlayerDialogue("Hello, who are you?"),
                new NPCDialogue(LOYALTY_MANAGER, "I reward players for being loyal."),
                new PlayerDialogue("How can I get rewarded?"),
                new NPCDialogue(LOYALTY_MANAGER, "Every 10 minutes you are logged in, you receive between 5 and 15 loyalty points. It's as simple as that. Would you like to see the rewards?"),
                new OptionsDialogue(
                        "View loyalty shop?",
                        new Option("Yes, please.", ()-> npc.getDef().shop.open(player)),
                        new Option("No, thank you.", player::closeDialogue)
                ))));
    }
}
