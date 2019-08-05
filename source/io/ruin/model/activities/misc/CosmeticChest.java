package io.ruin.model.activities.misc;

import io.ruin.cache.Color;
import io.ruin.model.World;
import io.ruin.model.entity.shared.LockType;
import io.ruin.model.inter.InterfaceType;
import io.ruin.model.inter.dialogue.ActionDialogue;
import io.ruin.model.inter.dialogue.ItemDialogue;
import io.ruin.model.inter.dialogue.MessageDialogue;
import io.ruin.model.inter.dialogue.OptionsDialogue;
import io.ruin.model.inter.utils.Option;
import io.ruin.model.item.Item;
import io.ruin.model.item.loot.LootItem;
import io.ruin.model.item.loot.LootTable;
import io.ruin.model.map.object.GameObject;
import io.ruin.model.map.object.actions.ObjectAction;

public class CosmeticChest {

    private static int OPENED = 40010;
    private static int CLOSED = 40009;

    private static int PRICE = World.isPVP() ? 500 : 50_000;
    private static int CURRENCY_ID = World.isPVP() ? 13307 : 995;
    private static String CURRENCY_NAME = World.isPVP() ? "blood money" : "coins";

    private static final LootTable COSMETIC_ITEMS = new LootTable().addTable(1,
            new LootItem(12245, 1, 1), // Beanie
            new LootItem(12430, 1, 1), // Afro
            new LootItem(12359, 1, 1), // Leprechaun hat
            new LootItem(12540, 1, 1), // Deerstalker
            new LootItem(2645, 1, 1), // Red headband
            new LootItem(2647, 1, 1), // Black headband
            new LootItem(2649, 1, 1), // Brown headband
            new LootItem(12299, 1, 1), // White headband
            new LootItem(12301, 1, 1), // Blue headband
            new LootItem(12303, 1, 1), // Gold headband
            new LootItem(12305, 1, 1), // Pink headband
            new LootItem(12307, 1, 1), // Green headband
            new LootItem(10316, 1, 1), // Bob's red shirt
            new LootItem(10318, 1, 1), // Bob's blue shirt
            new LootItem(10322, 1, 1), // Bob's black shirt4
            new LootItem(10324, 1, 1), // Bob's purple shirt
            new LootItem(12375, 1, 1), // Black cane
            new LootItem(12377, 1, 1), // Adamant cane
            new LootItem(12379, 1, 1), // Rune cane
            new LootItem(12373, 1, 1), // Dragon cane
            new LootItem(12363, 1, 1), // Bronze dragon mask
            new LootItem(12365, 1, 1), // Iron dragon mask
            new LootItem(12367, 1, 1), // Steel dragon mask
            new LootItem(12369, 1, 1), // Mithril dragon mask
            new LootItem(12371, 1, 1), // Lava dragon mask
            new LootItem(12518, 1, 1), // Green dragon mask
            new LootItem(12522, 1, 1), // Red dragon mask
            new LootItem(12524, 1, 1), // Black dragon mask
            new LootItem(7537, 1, 1), // Crab claw
            new LootItem(8929, 1, 1), // Crab claw hook
            new LootItem(2997, 1, 1), // Pirate's hook
            new LootItem(8924, 1, 1), // Bandana eyepatch
            new LootItem(8925, 1, 1), // Bandana eyepatch
            new LootItem(8926, 1, 1), // Bandana eyepatch
            new LootItem(8927, 1, 1), // Bandana eyepatch
            new LootItem(8950, 1, 1), // Pirate's hat
            new LootItem(12412, 1, 1), // Piate's hat & patch
            new LootItem(12355, 1, 1), // Big pirate's hat
            new LootItem(2631, 1, 1), // Highwayman mask
            new LootItem(2639, 1, 1), // Tan cavalier
            new LootItem(2641, 1, 1), // Dark cavalier
            new LootItem(12325, 1, 1), // Navy cavalier
            new LootItem(12323, 1, 1), // Red cavalier
            new LootItem(12321, 1, 1), // White cavalier
            new LootItem(2643, 1, 1), // Black cavalier
            new LootItem(11280, 1, 1), // Cavalier mask
            new LootItem(12319, 1, 1), // Crier hat
            new LootItem(12249, 1, 1), // Imp mask
            new LootItem(12251, 1, 1), // Goblin mask
            new LootItem(12361, 1, 1), // Cat mask
            new LootItem(12428, 1, 1), // Penguin mask
            new LootItem(12434, 1, 1), // Top hat & monocle
            new LootItem(12337, 1, 1), // Sagacious spectacles
            new LootItem(11919, 1, 1), // Cow mask
            new LootItem(12956, 1, 1), // Cow top
            new LootItem(12957, 1, 1), // Cow leggings
            new LootItem(12958, 1, 1), // Cow gloves
            new LootItem(12959, 1, 1), // Cow boots
            new LootItem(12845, 1, 1), // Grim reaper hood
            new LootItem(9920, 1, 1), // Jack lantern mask
            new LootItem(9925, 1, 1), // Skeleton mask
            new LootItem(9924, 1, 1), // Skeleton shirt
            new LootItem(9923, 1, 1), // Skeleton leggings
            new LootItem(9922, 1, 1), // Skeleton gloves
            new LootItem(9921, 1, 1), // Skeleton boots
            new LootItem(1037, 1, 1), // Bunny Ears
            new LootItem(13283, 1, 1), // Gravedigger mask
            new LootItem(13284, 1, 1), // Gravedigger top
            new LootItem(13285, 1, 1), // Gravedigger leggings
            new LootItem(13286, 1, 1), // Gravedigger gloves
            new LootItem(13287, 1, 1), // Gravedigger boots
            new LootItem(5553, 1, 1), // Rogue set
            new LootItem(5554, 1, 1), // Rogue set
            new LootItem(5555, 1, 1), // Rogue set
            new LootItem(5556, 1, 1), // Rogue set
            new LootItem(5557, 1, 1), // Rogue set
            new LootItem(10933, 1, 1), // Lumberjack outfit
            new LootItem(10939, 1, 1), // Lumberjack outfit
            new LootItem(10940, 1, 1), // Lumberjack outfit
            new LootItem(10941, 1, 1), // Lumberjack outfit
            new LootItem(13258, 1, 1), // Angler outfit
            new LootItem(13259, 1, 1), // Angler outfit
            new LootItem(13260, 1, 1), // Angler outfit
            new LootItem(13261, 1, 1), // Angler outfit
            new LootItem(19973, 1, 1), // Light Jackt
            new LootItem(19979, 1, 1), // Light pants
            new LootItem(19976, 1, 1), // Light cuffs
            new LootItem(19982, 1, 1), // Light shoes
            new LootItem(19985, 1, 1), // Light bowtie
            new LootItem(6184, 1, 1), // Prince top/bot
            new LootItem(6185, 1, 1), // Prince top/bot
            new LootItem(6186, 1, 1), // Princess
            new LootItem(6187, 1, 1), // Princess
            new LootItem(12397, 1, 1), // Royal Outfit
            new LootItem(12393, 1, 1), // Royal Outfit
            new LootItem(12395, 1, 1), // Royal Outfit
            new LootItem(12439, 1, 1), // Royal Outfit
            new LootItem(10836, 1, 1), // Jester outfit
            new LootItem(10837, 1, 1), // Jester outfit
            new LootItem(10838, 1, 1), // Jester outfit
            new LootItem(10839, 1, 1), // Jester outfit
            new LootItem(10051, 1, 1), // Graahk outfit
            new LootItem(10049, 1, 1), // Graahk outfit
            new LootItem(10047, 1, 1), // Graahk outfit
            new LootItem(6182, 1, 1), // Leden outfit
            new LootItem(6180, 1, 1), // Leden outfit
            new LootItem(6181, 1, 1), // Leden outfit
            new LootItem(7594, 1, 1), // Zombie outfit
            new LootItem(7592, 1, 1), // Zombie outfit
            new LootItem(7593, 1, 1), // Zombie outfit
            new LootItem(7595, 1, 1), // Zombie outfit
            new LootItem(7596, 1, 1), // Zombie outfit
            new LootItem(6656, 1, 1), // Camo helmet
            new LootItem(6654, 1, 1), // Camo top
            new LootItem(6655, 1, 1), // Camo bottoms
            new LootItem(3057, 1, 1), // Mime mask
            new LootItem(3058, 1, 1), // Mime top
            new LootItem(3059, 1, 1), // Mime bottom
            new LootItem(3060, 1, 1), // Mime gloves
            new LootItem(3061, 1, 1) // Mime boots
    );

    private static void replaceChest(GameObject chest) {
        World.startEvent(event -> {
            chest.setId(OPENED);
            event.delay(2);
            chest.setId(chest.originalId);
        });
    }

    static {
        ObjectAction.register(CLOSED, "open", (player, obj) -> {
            player.dialogue(
                    new ItemDialogue().one(COSMETIC_ITEMS.rollItem().getId(), "Opening this chest will cost you " + Color.COOL_BLUE.wrap("500 blood money ") + "and in return, will give you a random cosmetic!"),
                    new ActionDialogue(() -> {
                        if (player.getInventory().isFull()) {
                            player.dialogue(new MessageDialogue("You must have at least 1 inventory space to open the cosmetic chest."));
                            return;
                        }
                        Item bloodMoney = player.getInventory().findItem(CURRENCY_ID);
                        if (bloodMoney == null || bloodMoney.getAmount() < PRICE) {
                            player.dialogue(new MessageDialogue("You need at least " + Color.COOL_BLUE.wrap(PRICE + " " + CURRENCY_NAME) + " to open the cosmetic chest."));
                            return;
                        }
                        player.dialogue(new OptionsDialogue("Open the Cosmetic Chest for " + PRICE + " " + CURRENCY_NAME + "?",
                                new Option("Yes, open the chest!", () -> {
                                    player.startEvent(event -> {
                                        player.lock(LockType.FULL_DELAY_DAMAGE);
                                        player.sendMessage("You open the cosmetic chest..");
                                        bloodMoney.remove(500);
                                        player.animate(535);
                                        World.sendGraphics(1388, 50, 0, 3083, 3512, 0);
                                        replaceChest(obj);
                                        event.delay(1);
                                        Item reward = COSMETIC_ITEMS.rollItem();
                                        player.sendMessage("..and find " + reward.getDef().descriptiveName + "!");
                                        player.getInventory().add(COSMETIC_ITEMS.rollItem());
                                        //PlayerCounter.ROGUES_CASTLE_CHESTS.increment(player, 1);
                                        player.unlock();
                                    });
                                }),
                                new Option("No, don't open the chest.", player::closeDialogue))
                        );
                    }
            ));
        });
        ObjectAction.register(CLOSED, "view-rewards", (player, obj) -> {
            player.openInterface(InterfaceType.MAIN, 714);
            player.getPacketSender().sendClientScript(149, "IviiiIsssss", 714 << 16 | 3, 582, 10, 20, 0, -1, "null", "null", "null", "null", "null");
            player.getPacketSender().sendItems(-1, -1, 582, COSMETIC_ITEMS.allItems().toArray(new Item[COSMETIC_ITEMS.allItems().size()]));
        });
    }

}
