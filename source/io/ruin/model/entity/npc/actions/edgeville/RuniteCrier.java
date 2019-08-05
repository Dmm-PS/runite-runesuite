package io.ruin.model.entity.npc.actions.edgeville;

import io.ruin.api.utils.Random;
import io.ruin.model.World;
import io.ruin.model.entity.npc.NPCAction;
import io.ruin.model.entity.shared.listeners.SpawnListener;
import io.ruin.model.inter.dialogue.*;
import io.ruin.model.inter.utils.Option;

public class RuniteCrier {

    private static final int RUNITE_CRIER = 6823;

    private static final String[] MESSAGES = new String[]{
            "I hope you are enjoying Runite!",
            "Don't forget to ::vote once every 24 hours!",
            "Watch the discord for cool events, and offers!",
            "Check the forums for updates!",
            "Runite is #1!!"
    };

    static {
        SpawnListener.register(RUNITE_CRIER, npc -> npc.startEvent(event -> {
            while (true) {
                npc.forceText(Random.get(MESSAGES));
                npc.animate(6865);
                event.delay(Random.get(20, 100));
            }
        }));

        NPCAction.register(RUNITE_CRIER, "talk-to", (player, npc) -> {
            player.dialogue(
                    new NPCDialogue(npc, "Would you like a " + World.type.getWorldName() + " Herald?"),
                    new OptionsDialogue(
                            new Option("Yes please.", () -> player.dialogue(new PlayerDialogue("Yes please."),
                                    new ActionDialogue(() -> {
                                        npc.faceTemp(player);
                                        player.dialogue(new ItemDialogue().one(11169, "The Runite Crier gives you a " + World.type.getWorldName() + " Herald."));
                                        player.getInventory().addOrDrop(11169, 1); })
                            )),
                            new Option("Not right now.", () -> player.dialogue(new PlayerDialogue("Not right now.")))
                    )
            );
        });
    }





}
