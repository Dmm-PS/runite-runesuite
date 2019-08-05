package io.ruin.model.item.actions.impl.boxes.mystery;

import io.ruin.cache.Icon;
import io.ruin.model.World;
import io.ruin.model.item.Item;
import io.ruin.model.item.actions.ItemAction;
import io.ruin.model.item.loot.LootItem;
import io.ruin.model.item.loot.LootTable;
import io.ruin.utility.Broadcast;

public class VoteMysteryBox {

    private static final LootTable PVP_VOTING_BOX_TABLE = new LootTable().addTable(1,
            new LootItem(13307, 250, 1000, 2000),                  // 2000 Blood money
            new LootItem(6914, 1, 40),                             // Masters wand
            new LootItem(4151, 1, 40),                             // Abyssal whip
            new LootItem(20128, 1, 40),                            // Hood of darkness
            new LootItem(20131, 1, 40),                            // Robe top of darkness
            new LootItem(20137, 1, 40),                            // Robe bottom of darkness
            new LootItem(4153, 1, 40),                             // Granite maul
            new LootItem(6528, 1, 40),                             // Obsidian maul
            new LootItem(10887, 1, 40),                            // Barrelchest anchor
            new LootItem(11128, 1, 40),                            // Berserker necklace
            new LootItem(4716, 1, 40),                             // Dharok's helm
            new LootItem(4718, 1, 40),                             // Dharok's greataxe
            new LootItem(4720, 1, 40),                             // Dharok's platebody
            new LootItem(4722, 1, 40),                             // Dharok's platelegs
            new LootItem(4708, 1, 40),                             // Ahrim's hood
            new LootItem(4712, 1, 40),                             // Ahrim's robetop
            new LootItem(4714, 1, 40),                             // Ahrim's robeskirt
            new LootItem(6585, 1, 40),                             // Dragon boots
            new LootItem(12831, 1, 40),                            // Blessed Spirit shield
            new LootItem(6733, 1, 40),                             // Archer ring
            new LootItem(6735, 1, 40),                             // Warrior ring
            new LootItem(6920, 1, 40),                             // Infinity boots
            new LootItem(6585, 1, 40),                              // Amulet of fury
            new LootItem(12397, 1, 40),                            // Royal Crown
            new LootItem(12791, 1, 5).broadcast(Broadcast.GLOBAL), // Rune Pouch
            new LootItem(11941, 1, 5).broadcast(Broadcast.GLOBAL), // Royal Sceptre
            new LootItem(12846, 1, 5).broadcast(Broadcast.GLOBAL), // Bounty hunter teleport
            new LootItem(1505, 1, 5).broadcast(Broadcast.GLOBAL),  // Obelisk Teleport
            new LootItem(1038, 1, 1).broadcast(Broadcast.GLOBAL),  // Red party hat
            new LootItem(1040, 1, 1).broadcast(Broadcast.GLOBAL),  // Yellow party hat
            new LootItem(1042, 1, 1).broadcast(Broadcast.GLOBAL),  // Blue party hat
            new LootItem(1044, 1, 1).broadcast(Broadcast.GLOBAL),  // Green party hat
            new LootItem(1046, 1, 1).broadcast(Broadcast.GLOBAL),  // Purple party hat
            new LootItem(1048, 1, 1).broadcast(Broadcast.GLOBAL),  // White  party hat
            new LootItem(11862, 1, 1).broadcast(Broadcast.GLOBAL), // Black party hat
            new LootItem(11863, 1, 1).broadcast(Broadcast.GLOBAL), // Rainbow party hat
            new LootItem(12399, 1, 1).broadcast(Broadcast.GLOBAL)  // Partyhat & specs
    );

    private static final LootTable ECO_VOTING_BOX_TABLE = new LootTable().addTable(1,
            new LootItem(995, 50_000, 300_000, 70),                  // Gold
            new LootItem(6914, 1, 1).broadcast(Broadcast.GLOBAL),      // Masters wand
            new LootItem(4151, 1, 1).broadcast(Broadcast.GLOBAL),      // Abyssal whip
            new LootItem(20128, 1, 1).broadcast(Broadcast.GLOBAL),     // Hood of darkness
            new LootItem(20131, 1, 1).broadcast(Broadcast.GLOBAL),     // Robe top of darkness
            new LootItem(20137, 1, 1).broadcast(Broadcast.GLOBAL),     // Robe bottom of darkness
            new LootItem(4153, 1, 1).broadcast(Broadcast.GLOBAL),      // Granite maul
            new LootItem(6528, 1, 1).broadcast(Broadcast.GLOBAL),      // Obsidian maul
            new LootItem(10887, 1, 1).broadcast(Broadcast.GLOBAL),     // Barrelchest anchor
            new LootItem(1249, 1, 1).broadcast(Broadcast.GLOBAL),      // Dragon spear
            new LootItem(11128, 1, 1).broadcast(Broadcast.GLOBAL),     // Berserker necklace
            new LootItem(4716, 1, 1).broadcast(Broadcast.GLOBAL),      // Dharok's helm
            new LootItem(4718, 1, 1).broadcast(Broadcast.GLOBAL),      // Dharok's greataxe
            new LootItem(4720, 1, 1).broadcast(Broadcast.GLOBAL),      // Dharok's platebody
            new LootItem(4722, 1, 1).broadcast(Broadcast.GLOBAL),      // Dharok's platelegs
            new LootItem(4708, 1, 1).broadcast(Broadcast.GLOBAL),      // Ahrim's hood
            new LootItem(4712, 1, 1).broadcast(Broadcast.GLOBAL),      // Ahrim's robetop
            new LootItem(4714, 1, 1).broadcast(Broadcast.GLOBAL),      // Ahrim's robeskirt
            new LootItem(6585, 1, 1).broadcast(Broadcast.GLOBAL),      // Dragon boots
            new LootItem(12831, 1, 1).broadcast(Broadcast.GLOBAL),     // Blessed Spirit shield
            new LootItem(6733, 1, 1).broadcast(Broadcast.GLOBAL),      // Archer ring
            new LootItem(6735, 1, 1).broadcast(Broadcast.GLOBAL),      // Warrior ring
            new LootItem(6920, 1, 1).broadcast(Broadcast.GLOBAL),      // Infinity boots
            new LootItem(6585, 1, 1).broadcast(Broadcast.GLOBAL),      // Amulet of fury
            new LootItem(12397, 1, 1)  .broadcast(Broadcast.GLOBAL)    // Royal Crown
    );


    static {
        ItemAction.registerInventory(6829, "open", (player, item) -> {
            player.lock();
            Item reward = World.isPVP() ? PVP_VOTING_BOX_TABLE.rollItem() : ECO_VOTING_BOX_TABLE.rollItem();
            player.closeDialogue();
            item.remove();
            player.getInventory().add(reward);
            if (reward.lootBroadcast != null)
                Broadcast.GLOBAL.sendNews(Icon.MYSTERY_BOX, "Voting Mystery Box", "" + player.getName() + " just received " + reward.getDef().descriptiveName + "!");
            player.unlock();
        });

    }
}
