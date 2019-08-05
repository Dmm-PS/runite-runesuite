package io.ruin.model.activities.misc;

import io.ruin.model.World;
import io.ruin.model.inter.dialogue.OptionsDialogue;
import io.ruin.model.inter.utils.Option;
import io.ruin.model.map.object.actions.ObjectAction;

public class DonationTable {

    private static int DONATION_TABLE = 31379;

    static {
        ObjectAction.register(DONATION_TABLE, "buy-credits", (player, obj) -> player.dialogue(
                new OptionsDialogue("Would you like to view the credit purchase page?",
                        new Option("Yes", () -> player.openUrl(World.type.getWorldName() + " Credit Store", World.type.getWebsiteUrl() + "/credits")),
                        new Option("No", player::closeDialogue)
                )
        ));
    }
}
