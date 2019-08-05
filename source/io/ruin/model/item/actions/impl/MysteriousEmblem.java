package io.ruin.model.item.actions.impl;

import io.ruin.api.utils.NumberUtils;
import io.ruin.cache.Color;
import io.ruin.cache.ItemDef;
import io.ruin.model.inter.dialogue.ItemDialogue;
import io.ruin.model.inter.dialogue.YesNoDialogue;
import io.ruin.model.inter.journal.Journal;
import io.ruin.model.inter.journal.main.WildernessPoints;
import io.ruin.model.item.actions.ItemAction;

public class MysteriousEmblem {

    static {
        int[][] emblems = {
                {12746, 300},    //Tier 1
                {12748, 500},    //Tier 2
                {12749, 750},    //Tier 3
                {12750, 1000},   //Tier 4
                {12751, 1500},   //Tier 5
                {12752, 2000},   //Tier 6
                {12753, 3000},   //Tier 7
                {12754, 5000},   //Tier 8
                {12755, 7500},   //Tier 9
                {12756, 10000},  //Tier 10
        };
        for(int[] emblem : emblems) {
            ItemDef def = ItemDef.get(emblem[0]);
            def.sigmundBuyPrice = emblem[1];
            ItemAction.registerInventory(def.id, "info", (player, item) -> {
                player.sendScroll("<col=800000>About Mysterious Emblems",
                        "Mysterious Emblems are items dropped from killing",
                        "Bounty Hunter targets. If you kill a target with an emblem",
                        "in your inventory, your emblem will be upgraded. If you kill",
                        " a target with an emblem in their inventory, their emblem",
                        "will be downgraded one tier and dropped.",
                        "",
                        "You can right click and redeem emblems in exchange",
                        "for wilderness points. Wilderness points can be spent",
                        "by trading Sigmund. Here are the emblem values:",
                        "",
                        Color.DARK_RED.wrap("Tier 1 ") + "           300 wilderness points",
                        Color.DARK_RED.wrap("Tier 2 ") + "           500 wilderness points",
                        Color.DARK_RED.wrap("Tier 3 ") + "           750 wilderness points",
                        Color.DARK_RED.wrap("   Tier 4 ") + "         1,000 wilderness points",
                        Color.DARK_RED.wrap("   Tier 5 ") + "         1,500 wilderness points",
                        Color.DARK_RED.wrap("   Tier 6 ") + "         2,000 wilderness points",
                        Color.DARK_RED.wrap("   Tier 7 ") + "         3,000 wilderness points",
                        Color.DARK_RED.wrap("   Tier 8 ") + "         5,000 wilderness points",
                        Color.DARK_RED.wrap("   Tier 9 ") + "         7,500 wilderness points",
                        Color.DARK_RED.wrap("   Tier 10 ") + "        10,000 wilderness points"
                );
            });
            ItemAction.registerInventory(def.id, "redeem", (player, item) -> {
                int emblemValue = emblem[1];
                String emblemName = item.getDef().name;
                if(player.wildernessLevel > 0 || player.pvpAttackZone) {
                    player.sendMessage("You can't redeem a mysterious emblem inside the wilderness.");
                    return;
                }
                player.dialogue(new YesNoDialogue("Are you sure you want to do this?", "Redeem your " + emblemName +
                        " for " + NumberUtils.formatNumber(emblemValue) + " wilderness points?", item, () -> {
                    item.remove();
                    player.wildernessPoints += emblemValue;
                    if(player.journal == Journal.MAIN)
                        WildernessPoints.INSTANCE.send(player);
                    player.dialogue(new ItemDialogue().one(item.getId(), "You redeem your " + emblemName + " for " +
                            Color.DARK_RED.wrap(NumberUtils.formatNumber(emblemValue)) + " wilderness points. You now have a total of " +
                            Color.DARK_RED.wrap(NumberUtils.formatNumber(player.wildernessPoints)) + " wilderness points."));
                }));
            });
        }
    }

}