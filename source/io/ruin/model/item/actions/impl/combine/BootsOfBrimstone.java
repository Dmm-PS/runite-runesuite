package io.ruin.model.item.actions.impl.combine;

import io.ruin.model.entity.player.Player;
import io.ruin.model.inter.dialogue.YesNoDialogue;
import io.ruin.model.item.Item;
import io.ruin.model.item.actions.ItemItemAction;

import java.util.ArrayList;

/**
 * @author Andys1814
 */
public class BootsOfBrimstone {

    private static final int BOOTS_OF_STONE = 23037;
    private static final int DRAKES_CLAW = 22957;
    private static final int BOOTS_OF_BRIMSTONE = 22951;

    private static void makeBoots(Player player) { // TODO find proper gfx id
        ArrayList<Item> items = player.getInventory().collectOneOfEach(BOOTS_OF_STONE, DRAKES_CLAW);
        if (items == null) {
            player.sendMessage("You need Boots of stone and a Drake's claw to make Boots of brimstone.");
            return;
        }

        player.dialogue(
                new YesNoDialogue("Are you sure you want to do this?", "Combining these items into Boots of brimstone will be irreversible", BOOTS_OF_BRIMSTONE, 1, () -> {
                    items.forEach(Item::remove);
                    player.getInventory().add(BOOTS_OF_BRIMSTONE, 1);
                    player.sendMessage("You combine the items into a Brimstone ring.");
                })
        );
    }

    static {
        ItemItemAction.register(BOOTS_OF_STONE, DRAKES_CLAW, (player, primary, secondary) -> makeBoots(player));
    }
}
