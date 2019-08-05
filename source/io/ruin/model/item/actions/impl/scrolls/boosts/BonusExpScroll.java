package io.ruin.model.item.actions.impl.scrolls.boosts;

import io.ruin.model.inter.dialogue.ItemDialogue;
import io.ruin.model.inter.dialogue.MessageDialogue;
import io.ruin.model.inter.dialogue.OptionsDialogue;
import io.ruin.model.inter.utils.Option;
import io.ruin.model.item.actions.ItemAction;

public class BonusExpScroll {

    private static final int BONUS_EXP_SCROLL = 6758;

    static {
        ItemAction.registerInventory(BONUS_EXP_SCROLL, "activate", (player, item) -> {
            if(player.expBonus.isDelayed()) {
                player.dialogue(new MessageDialogue("You already have an experience boost activated!"));
                return;
            }
            player.dialogue(new OptionsDialogue("Are you sure you want to active your scroll?",
                    new Option("Yes, active my experience boost scroll!", () -> {
                        item.remove();
                        player.expBonus.delaySeconds(30 * 60);
                        player.dialogue(new ItemDialogue().one(item.getId(), "You have activated your experience boost scroll. Your experience " +
                                "will be boosted by 50% for the next 30 minutes."));
                    }),
                    new Option("No, I'm not ready yet!")));
        });
    }

}
