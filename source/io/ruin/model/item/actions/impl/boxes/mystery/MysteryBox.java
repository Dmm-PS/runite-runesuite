package io.ruin.model.item.actions.impl.boxes.mystery;

import io.ruin.api.utils.Random;
import io.ruin.cache.Color;
import io.ruin.cache.Icon;
import io.ruin.model.World;
import io.ruin.model.entity.player.Player;
import io.ruin.model.inter.dialogue.YesNoDialogue;
import io.ruin.model.item.Item;
import io.ruin.model.item.actions.ItemAction;
import io.ruin.model.item.loot.LootItem;
import io.ruin.model.item.loot.LootTable;
import io.ruin.utility.Broadcast;

public class MysteryBox {

    private static final LootTable MYSTERY_BOX_TABLE = new LootTable().addTable(1,
            /* Common */
            new LootItem(11212, 500, 50),
            new LootItem(11840, 1, 50),
            new LootItem(8850, 1, 50),
            new LootItem(2572, 1, 50),
            new LootItem(12791, 1, 50),
            new LootItem(6735, 1, 50),
            new LootItem(6731, 1, 50),
            new LootItem(6733, 1, 50),
            new LootItem(12746, 1, 50),
            new LootItem(12748, 1, 50),
            new LootItem(12749, 1, 50),
            new LootItem(4214, 1, 50),
            new LootItem(4224, 1, 50),
            new LootItem(12002, 1, 50),
            new LootItem(6918, 1, 50),
            new LootItem(6916, 1, 50),
            new LootItem(6924, 1, 50),
            new LootItem(6922, 1, 50),
            new LootItem(6920, 1, 50),
            new LootItem(19478, 1, 50),
            new LootItem(6570, 1, 50),
            new LootItem(4716, 1, 50),
            new LootItem(4720, 1, 50),
            new LootItem(4722, 1, 50),
            new LootItem(4718, 1, 50),
            new LootItem(4745, 1, 50),
            new LootItem(4749, 1, 50),
            new LootItem(4751, 1, 50),
            new LootItem(4747, 1, 50),
            new LootItem(4753, 1, 50),
            new LootItem(4757, 1, 50),
            new LootItem(4759, 1, 50),
            new LootItem(4755, 1, 50),
            new LootItem(995, 5000000, 50).broadcast(Broadcast.GLOBAL),
            new LootItem(990, 10, 50).broadcast(Broadcast.GLOBAL),
            new LootItem(4732, 1, 50).broadcast(Broadcast.GLOBAL),
            new LootItem(11230, 2500, 50),

            new LootItem(12763, 1, 25), //White dark bow paint
            new LootItem(12761, 1, 25), //Yellow dark bow paint
            new LootItem(12759, 1, 25), //Green dark bow paint
            new LootItem(12757, 1, 25), //Blue dark bow paint
            new LootItem(12769, 1, 25), //Frozen whip mix
            new LootItem(12771, 1, 25), //Volcanic whip mix

            /* Uncommon */
            new LootItem(10551, 1, 15),
            new LootItem(4151, 1, 15), // Abyssal Whip
            new LootItem(4724, 1, 15),
            new LootItem(4728, 1, 15),
            new LootItem(4730, 1, 15),
            new LootItem(4726, 1, 15),
            new LootItem(12750, 1, 15),
            new LootItem(12751, 1, 15),
            new LootItem(12752, 1, 15),
            new LootItem(12753, 1, 15),
            new LootItem(19481, 1, 15),
            new LootItem(11808, 1, 15).broadcast(Broadcast.GLOBAL),
            new LootItem(11804, 1, 15).broadcast(Broadcast.GLOBAL),
            new LootItem(6739, 1, 15).broadcast(Broadcast.GLOBAL),
            new LootItem(11920, 1, 15).broadcast(Broadcast.GLOBAL),
            new LootItem(11824, 1, 15).broadcast(Broadcast.GLOBAL),
            new LootItem(12006, 1, 15).broadcast(Broadcast.GLOBAL),
            new LootItem(995, 20000000, 15).broadcast(Broadcast.GLOBAL),
            new LootItem(11838, 1, 15).broadcast(Broadcast.GLOBAL),
            new LootItem(11128, 1, 15).broadcast(Broadcast.GLOBAL),


            new LootItem(1050, 1, 1),
            new LootItem(13652, 1, 5).broadcast(Broadcast.GLOBAL),
            new LootItem(11802, 1, 5).broadcast(Broadcast.GLOBAL),
            new LootItem(11806, 1, 1).broadcast(Broadcast.GLOBAL),
            new LootItem(12754, 1, 1),
            new LootItem(12755, 1, 1).broadcast(Broadcast.GLOBAL),
            new LootItem(12756, 1, 1),
            new LootItem(995, 50000000, 1)
    );



    private static void gift(Player player, Item box) {
        int boxId = box.getId();
        player.stringInput("Enter player's display name:", name -> {
            if(!player.getInventory().hasId(boxId))
                return;
            name = name.replaceAll("[^a-zA-Z0-9\\s]", "");
            name = name.substring(0, Math.min(name.length(), 12));
            if (name.isEmpty()) {
                player.retryStringInput("Invalid username, try again:");
                return;
            }
            if (name.equalsIgnoreCase(player.getName())) {
                player.retryStringInput("Cannot gift yourself, try again:");
                return;
            }
            Player target = World.getPlayer(name);
            if (target == null) {
                player.retryStringInput("Player cannot be found, try again:");
                return;
            }
            if(target.getGameMode().isIronMan()) {
                player.retryStringInput("That player is an ironman and can't receive gives!");
                return;
            }
            player.stringInput("Enter a message for " + target.getName() + ":", message -> {
                player.dialogue(new YesNoDialogue("Are you sure you want to do this?", "Gift your " + box.getDef().name + " to " + target.getName() + "?", box, () -> {
                    if(!player.getInventory().hasId(boxId))
                        return;
                    player.getInventory().remove(boxId, 1);
                    if (!target.getInventory().isFull())
                        target.getInventory().add(boxId, 1);
                    else
                        target.getBank().add(boxId, 1);
                    target.sendMessage("<img=91> " + Color.DARK_RED.wrap(player.getName() + " has just gifted you " + box.getDef().descriptiveName + "!"));
                    player.sendMessage("<img=91> " + Color.DARK_RED.wrap("You have successfully gifted your " + box.getDef().name + " to " + target.getName() + "."));
                    if (!message.isEmpty())
                        target.sendMessage("<img=91> " + Color.DARK_RED.wrap("[NOTE] " + message));
                }));
            });
        });
    }

    static {
        for (Item item : MYSTERY_BOX_TABLE.allItems()) {
            System.out.println(item.getId() + " " + item.getDef().descriptiveName);
        }
        ItemAction.registerInventory(6199, "open", (player, item) -> {
            player.lock();
            player.closeDialogue();
            Item reward;
            reward = MYSTERY_BOX_TABLE.rollItem();
            player.guaranteedMysteryBoxLoot++;
            item.remove();
            if(World.isEco() && reward.getId() == 13307) {
                int amt = reward.getAmount();
                reward = new Item(995, amt * 100);
            }
            player.getInventory().add(reward);
            if (reward.lootBroadcast != null)
                Broadcast.GLOBAL.sendNews(Icon.MYSTERY_BOX, "Mystery Box", "" + player.getName() + " just received " + reward.getDef().descriptiveName + "!");
            player.unlock();
        });

        /**
         * Mystery box gifting
         */
        ItemAction.registerInventory(6199, "gift", MysteryBox::gift);
        ItemAction.registerInventory(6828, "gift", MysteryBox::gift);
        ItemAction.registerInventory(6829, "gift", MysteryBox::gift);
        ItemAction.registerInventory(290, "gift", MysteryBox::gift);
        ItemAction.registerInventory(6831, "gift", MysteryBox::gift);
        ItemAction.registerInventory(22330, "gift", MysteryBox::gift);
    }
}
