package io.ruin.model.entity.npc.actions.edgeville;

import io.ruin.cache.ItemDef;
import io.ruin.model.World;
import io.ruin.model.entity.npc.NPCAction;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.shared.listeners.SpawnListener;
import io.ruin.model.inter.dialogue.MessageDialogue;
import io.ruin.model.inter.dialogue.NPCDialogue;
import io.ruin.model.inter.dialogue.OptionsDialogue;
import io.ruin.model.inter.dialogue.YesNoDialogue;
import io.ruin.model.inter.utils.Config;
import io.ruin.model.inter.utils.Option;
import io.ruin.model.item.actions.ItemNPCAction;
import io.ruin.model.map.Bounds;

public class Rustin {

    static {
        NPCAction.register(315, "set-skull", (player, npc) -> skull(player));
        NPCAction.register(315, "reset-kdr", (player, npc) -> {
            player.dialogue(
                    new MessageDialogue("<col=ff0000>Warning:</col> You are about to reset your kills & deaths. All " +
                            "statistics related to kills will also be reset. Are you sure you want to continue?").lineHeight(25),
                    new OptionsDialogue(
                            new Option("Yes", () -> {
                                Config.PVP_KILLS.set(player, 0);
                                Config.PVP_DEATHS.set(player, 0);
                                player.currentKillSpree = 0;
                                player.highestKillSpree = 0;
                                player.highestShutdown = 0;
                                player.dialogue(new NPCDialogue(npc, "Your kills, deaths, sprees and highest shutdown has been reset."));
                            }),
                            new Option("No")
                    )
            );
        });
        if(World.isPVP()) {
            int[][] ancientArtifacts = {
                    {21807, 2500},   //Emblem
                    {21810, 5000},   //Totem
                    {21813, 10000},   //Statuette
            };
            for (int[] artifact : ancientArtifacts) {
                int id = artifact[0];
                ItemDef.get(id).sigmundBuyPrice = artifact[1];
                ItemNPCAction.register(id, 7941, (player, item, npc) -> {
                    player.dialogue(new YesNoDialogue("Are you sure you want to do this?", "Sell your " + ItemDef.get(id).name +
                            " for " + artifact[1] + " blood money?", item, () -> {
                        item.remove();
                        player.getInventory().add(13307, artifact[1]);
                        player.dialogue(new NPCDialogue(npc, "Excellent find, " + player.getName() + "! If you find anymore artifacts you know where to find me!"));
                    }));
                });
            }
        } else {
            int[][] ancientArtifacts = {
                    {21807, 500000},   //Emblem
                    {21810, 1000000},  //Totem
                    {21813, 2000000},  //Statuette
                    {22299, 4000000},  //Medallion
                    {22302, 8000000},  //Effigy
                    {22305, 10000000}  //Relic
            };
            for (int[] artifact : ancientArtifacts) {
                int id = artifact[0];
                ItemDef.get(id).sigmundBuyPrice = artifact[1];
                ItemNPCAction.register(id, 7941, (player, item, npc) -> {
                    player.dialogue(new YesNoDialogue("Are you sure you want to do this?", "Sell your " + ItemDef.get(id).name +
                            " for " + artifact[1] + " coins?", item, () -> {
                        item.remove();
                        player.getInventory().add(995, artifact[1]);
                        player.dialogue(new NPCDialogue(npc, "Excellent find, " + player.getName() + "! If you find anymore artifacts you know where to find me!"));
                    }));
                });
            }
        }
        NPCAction.register(7941, "talk-to", (player, npc) -> {
            String currencyName = World.isPVP() ? "bloody money" : "coins";
            player.dialogue(new NPCDialogue(npc, "If you find an ancient emblem, totem, or statuette, use it on me and I'll exchange it for " + currencyName + "."));
        });
        SpawnListener.forEach(npc -> {
            if(npc.getId() == 315 && npc.walkBounds != null)
                npc.walkBounds = new Bounds(3099, 3518, 3092, 3516, 0);
        });
    }

    public static void skull(Player player) {
        player.dialogue(new OptionsDialogue(
                new Option("<img=46> Regular <img=46>", () -> player.getCombat().skullNormal()),
                new Option("<img=93> High-Risk <img=93>", () -> player.dialogue(new OptionsDialogue("This skull will prevent you from using the Protect Item prayer.",
                        new Option("Give me the high risk skull!", () -> player.getCombat().skullHighRisk()),
                        new Option("No, I want to use the Protect Item prayer.")))),
                new Option("No Skull", () -> player.getCombat().resetSkull())
        ));
    }

}
