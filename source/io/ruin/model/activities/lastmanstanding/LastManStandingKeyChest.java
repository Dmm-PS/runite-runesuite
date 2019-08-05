package io.ruin.model.activities.lastmanstanding;

import io.ruin.model.entity.player.Player;
import io.ruin.model.item.Item;
import io.ruin.model.item.loot.LootItem;
import io.ruin.model.item.loot.LootTable;
import io.ruin.model.map.object.actions.ObjectAction;

/**
 * @author Andys1814
 */
public final class LastManStandingKeyChest {

    private static final int BLOODY_KEY = 20526;
    private static final int BLOODIER_KEY = 20608;

    private static final LootTable OFFENSIVE_TABLE = new LootTable().addTable(1,
            new LootItem(13652, 1, 1), // Dragon Claws
            new LootItem(11802, 1, 1), // Armadyl Godsword
            new LootItem(11785, 1, 1), // Aramadyl Crossbow
            new LootItem(11791, 1, 1), // Staff of the Dead
            new LootItem(4153, 1, 1), // Granite Maul
            new LootItem(11235, 1, 1), // Dark Bow
            new LootItem(21295, 1, 1), // Infernal Cape
            new LootItem(13576, 1, 1), // Dragon Warhammer
            new LootItem(11770, 1, 1), // Seers Ring (i)
            new LootItem(21003, 1, 1), // Elder Maul
            new LootItem(21006, 1, 1), // Kodai Wand
            new LootItem(21003, 1, 1), // Elder Maul
            new LootItem(22324, 1, 1), // Ghrazi Rapier
            new LootItem(19481, 1, 1) // Heavy Ballista
    );

    private static final LootTable UPGRADED_OFFENSIVE_TABLE = new LootTable().addTable(1,
            new LootItem(22647, 1, 1), // Zuriel's Staff
            new LootItem(22636, 12, 1), // Morrigan's Javelins (not sure on amount) TODO
            new LootItem(22613, 1, 1), // Vesta's Longsword
            new LootItem(22622, 1, 1) // Statius' Warhammer
    );

    private static final LootTable DEFENSIVE_TABLE = new LootTable().addTable(1,
            new LootItem(4712, 1, 1), // Ahrim's Robetop
            new LootItem(4714, 1, 1), // Ahrim's Robeskirt
            new LootItem(4737, 1, 1), // Karil's Leathertop
            new LootItem(4722, 1, 1), // Dharok's Platelegs
            new LootItem(4751, 1, 1), // Torag's platelegs
            new LootItem(4759, 1, 1), // Verac's plateskirt
            new LootItem(4716, 1, 1), // Dharok's Helm
            new LootItem(4724, 1, 1), // Guthan's Helm
            new LootItem(4745, 1, 1), // Torag's Helm
            new LootItem(4753, 1, 1), // Verac's Helm
            new LootItem(6585, 1, 1), // Amulet of Fury
            new LootItem(12831, 1, 1), // Blessed Spirit Shield
            new LootItem(13235, 1, 1), // Eternal Boots
            new LootItem(11834, 1, 1) // Bandos Tassets
    );

    private static void open(Player player) {
        Item key = player.getInventory().findFirst(BLOODY_KEY, BLOODIER_KEY);
        if (key == null) {
            player.sendMessage("You need a key to claim the loot in this chest!");
            return;
        }

        /* Determine the offensive table to roll from based on the player's key */
        //final LootTable offensiveTable = key.getId() == BLOODY_KEY ? OFFENSIVE_TABLE : OFFENSIVE_TABLE.combine(UPGRADED_OFFENSIVE_TABLE);

        /* The chest rolls from both offensive and defensive gear tables */
        Item offensiveItem = OFFENSIVE_TABLE.rollItem();
        Item defensiveItem = DEFENSIVE_TABLE.rollItem();

        player.startEvent(event -> {
            player.sendMessage("You search the chest...");
            event.delay(1);

            player.animate(834);
            player.getInventory().remove(key.getId(), 1);

            player.sendMessage("You find some loot!");
            player.getInventory().addOrDrop(offensiveItem);
            player.getInventory().addOrDrop(defensiveItem);
        });

    }

    static {
        ObjectAction.register(29069, 1, (player, obj) -> open(player));
    }

}
