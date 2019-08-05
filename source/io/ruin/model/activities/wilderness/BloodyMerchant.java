package io.ruin.model.activities.wilderness;

import io.ruin.Server;
import io.ruin.api.utils.Random;
import io.ruin.cache.Color;
import io.ruin.cache.Icon;
import io.ruin.cache.ItemDef;
import io.ruin.data.impl.help;
import io.ruin.model.World;
import io.ruin.model.entity.npc.NPC;
import io.ruin.model.entity.npc.NPCAction;
import io.ruin.model.entity.player.Player;
import io.ruin.model.inter.InterfaceType;
import io.ruin.model.inter.dialogue.MessageDialogue;
import io.ruin.model.inter.dialogue.OptionsDialogue;
import io.ruin.model.inter.journal.JournalEntry;
import io.ruin.model.inter.utils.Option;
import io.ruin.model.item.containers.shop.Shop;
import io.ruin.model.item.containers.shop.ShopCurrency;
import io.ruin.model.item.containers.shop.ShopItem;
import io.ruin.model.map.Position;
import io.ruin.utility.Broadcast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BloodyMerchant {

    private static final int BLOODY_MERCHANT = 8507;
    private static NPC bloodyMerchant = null;
    private static BloodyMerchant ACTIVE;
    private static ArrayList<ShopItem> bloodyMerchantItems = new ArrayList<>();
    private static Shop bloodyMerchantShop = null;

    private static long spawnTicks = 0;

    private static final BloodyMerchant[] SPAWN_LOCATIONS = {
            new BloodyMerchant(new Position(3029, 3631, 0), "at the center of the Dark Warriors' Fortress"),
            new BloodyMerchant(new Position(3079, 3871, 0), "inside the Lava Maze"),
            new BloodyMerchant(new Position(3357, 3872, 0), "as the Fountain of Rune"),
            new BloodyMerchant(new Position(2994, 3956, 0), "inside the Wilderness Agility Course"),
            new BloodyMerchant(new Position(3294, 3933, 0), "at the Rogues' Castle"),
    };

    /**
     * Separator
     */

    private final Position spawnPosition;

    private final String positionHint;

    public BloodyMerchant(Position spawnPosition, String positionHint) {
        this.spawnPosition = spawnPosition;
        this.positionHint = positionHint;
    }

    /**
     * Event
     */
    static {
        if(World.isPVP()) {
            World.startEvent(e -> {
                while (true) {
                    spawnTicks = Server.getEnd(4 * 60 * 100); // 4 hours
                    e.delay(4 * 60 * 100); // 4 hours
                    int randomLocation = Random.get(0, SPAWN_LOCATIONS.length - 1);
                    BloodyMerchant next = SPAWN_LOCATIONS[randomLocation];
                    while (next == ACTIVE) {
                        randomLocation = Random.get(0, SPAWN_LOCATIONS.length - 1);
                        next = SPAWN_LOCATIONS[randomLocation];
                    }
                    ACTIVE = next;
                    String spawnMessage = "The Bloody Merchant has made an appearance " + ACTIVE.positionHint + "!";
                    Broadcast.WORLD.sendNews(Icon.WILDERNESS, "Wilderness Event", spawnMessage);
                    //broadcastEvent(spawnMessage);
                    spawnMerchant();
                    e.delay(15 * 100); // 15 mins
                    despawnMerchant();
                }
            });
            NPCAction.register(BLOODY_MERCHANT, "Trade", (player, npc) -> {
                if (bloodyMerchant == null) {
                    player.sendMessage("The bloody merchant isn't interested in trading.");
                    return;
                }
                if (player.getGameMode().isIronMan()) {
                    player.sendMessage("The Bloody Merchant has no interest in selling to ironmen.");
                    return;
                }
                if (player.bloodyMerchantTradeWarning) {
                    player.dialogue(
                            new MessageDialogue("<col=ff0000>Warning:</col> Trading the bloody merchant will HIGH RISK skull you."),
                            new OptionsDialogue("Are you sure you wish to trade him?",
                                    new Option("Yes, I'm brave.", () -> tradeMerchant(player)),
                                    new Option("Eep! No thank you.", () -> player.sendFilteredMessage("You decide to not to trade the merchant.")),
                                    new Option("Yes please, don't show this message again.", () -> {
                                        player.bloodyMerchantTradeWarning = false;
                                        tradeMerchant(player);
                                    }))
                    );
                } else {
                    tradeMerchant(player);
                }
            });
        }
    }

    private static void broadcastEvent(String eventMessage) {
        for(Player p : World.players) {
            if(p.broadcastBloodyMechant)
                p.getPacketSender().sendMessage(eventMessage, "", 14);
            else
                Broadcast.WORLD.sendNews(Icon.WILDERNESS, "Wilderness Event", eventMessage);
        }
    }

    private static void tradeMerchant(Player player) {
        player.getCombat().skullHighRisk();
        bloodyMerchantShop.open(player);
    }

    private static void spawnMerchant() {
        World.startEvent(event -> {
            bloodyMerchant = new NPC(BLOODY_MERCHANT).spawn(ACTIVE.spawnPosition);
            for(int i = 0; i < 10; i ++) {
                BloodMerchantItems bloodMerchantItems = BloodMerchantItems.randomItem();
                bloodyMerchantItems.add(new BloodyMerchantItem(bloodMerchantItems.itemId, bloodMerchantItems.itemPrice, Random.get(1, bloodMerchantItems.maxAmt)));
            }
            bloodyMerchantShop = new Shop("Bloody Merchant's Findings", ShopCurrency.BLOOD_MONEY, true, bloodyMerchantItems);
        });
    }

    private static void despawnMerchant() {
        if (bloodyMerchant != null) {
            bloodyMerchant.remove();
            bloodyMerchantItems.clear();
            bloodyMerchantShop = null;
            bloodyMerchant = null;
            String despawnMessage = "The Bloody Merchant has left to collect more items.";
            Broadcast.WORLD.sendNews(Icon.WILDERNESS, "Wilderness Event", despawnMessage);
            //broadcastEvent(despawnMessage);
        }
    }

    /**
     * Entry
     */
    public static final class Entry extends JournalEntry {

        public static final Entry INSTANCE = new Entry();

        @Override
        public void send(Player player) {
            int minsLeft = (int) ((spawnTicks - Server.currentTick()) / 100);
            if(minsLeft < 0) {
                send(player, "Bloody Merchant", "Active ", Color.YELLOW);
                return;
            }
            if (minsLeft == 0)
                send(player, "Bloody Merchant", "Right away!", Color.ORANGE_RED);
            else if (minsLeft == 1)
                send(player, "Bloody Merchant", "1 minute", Color.GREEN);
            else if (minsLeft == 60)
                send(player, "Bloody Merchant", "1 hour", Color.GREEN);
            else if (minsLeft > 60) {
                int hours = minsLeft / 60;
                send(player, "Bloody Merchant", hours + " hour" + (hours > 1 ? "s" : ""), Color.GREEN);
            } else
                send(player, "Bloody Merchant", minsLeft + " minutes", Color.GREEN);
        }

        @Override
        public void select(Player player) {
            help.open(player, "bloody_merchant");
        }
    }

    /**
     * Blood merchant shop items
     */
    private static final class BloodyMerchantItem extends ShopItem {

        private BloodyMerchantItem(int itemId, int itemPrice, int remaining) {
            super(itemId, itemPrice, remaining);
        }

        @Override
        public int getDisplayId(Player player) {
            if(remaining == 0) {
                ItemDef def = getDef();
                if(def.hasPlaceholder())
                    return def.placeholderMainId;
            }
            return getId();
        }

        @Override
        public void buy(Player player, ShopCurrency currency, int amount) {
            if (amount <= 0)
                return;
            if(amount > remaining) {
                amount = remaining;
            }
            if(remaining == 0) {
                player.sendMessage("This item has been bought out!");
                return;
            }
            int currencyAmount = currency.getAmount(player);
            if (currencyAmount < (price * amount)) {
                player.sendMessage("You don't have enough " + currency.name + " to buy this item.");
                return;
            }
            if(player.getInventory().isFull()) {
                player.sendMessage("You need at least one inventory space before purchasing this.");
                return;
            }
            remaining -= amount;
            currency.remove(player, price * amount);
            player.getInventory().add(getId(), amount);
            player.closeInterface(InterfaceType.MAIN);
            if(bloodyMerchantShop != null)
                bloodyMerchantShop.open(player);
        }
    }

    /**
     * All the possible items our beautiful merchant offers!
     */
    private enum BloodMerchantItems {
        SERPENTINE_HELM(12929, 12000, 1),
        ARMADYL_GODSWORD(11802, 30000, 1),
        MAGMA_HELM(13198, 18000, 1),
        TANZANITE_HELM(13196, 18000, 1),
        ABYSSAL_WHIP(4151, 450, 3),
        ABYSSAL_TENTACLE(12006, 1600, 2),
        AMULET_OF_TORTURE(19553, 12000, 1),
        NECKLACE_OF_ANGUISH(19547, 9000, 1),
        ABYSSAL_DAGGER(13271, 4500, 2),
        TORMENTED_BRACELET(19544, 9000, 1),
        SEERS_RING(6731, 3000, 3),
        BERSERKER_RING(6737, 3000, 3),
        AMULET_OF_FURY(6585, 3000, 3),
        HEAVY_BALLISTA(19481, 9000, 1),
        DRAGONFIRE_WARD(22003, 15000, 1),
        DRAGON_KNIFE(22804, 150, 500),
        DRAGON_THROWNAXE(20849, 60, 500),
        TOXIC_STAFF(12902, 18000, 1),
        UNCHARGED_TOXIC_TRIDENT(12900, 15000, 1),
        IMBUED_HEART(20724, 9000, 2),
        MAGES_BOOK(6889, 4800, 3),
        ETERNAL_BOOTS(13235, 6000, 3),
        AHRIMS_HOOD(4708, 1200, 5),
        AHRIMS_ROBETOP(4712, 1800, 5),
        AHRIMS_ROBESKIRT(4714, 1800, 5),
        KARILS_LEATHERTOP(4736, 1800, 5),
        KAIRLS_LEATHERSKIRT(4738, 1800, 5),
        DHAROK_HELM(4716, 1200, 5),
        DHAROK_PLATEBODY(4720, 1800, 5),
        DHAROK_PLATELEGS(4722, 1800, 5),
        DHAROK_GREATAXE(4718, 1200, 5),
        PRIMORDIAL_BOOTS(13239, 6000, 2),
        PEGESIAN_BOOTS(13237, 6000, 2),
        DRAGONFIRE_SHIELD(11283, 12000, 1),
        BANDOS_GODSWORD(11804, 15000, 1),
        ABYSSAL_BLUDGEON(13263, 6000, 1),
        ANGLERFISH(13441, 30, 500),
        DARK_CRAB(11937, 15, 500),
        SUPER_COMBAT_POTION(12696, 18, 500),
        STAMINA_POTION(12626, 12, 500);

        private final int itemId, itemPrice, maxAmt;

        BloodMerchantItems(int itemId, int itemPrice, int maxAmt) {
            this.itemId = itemId;
            this.itemPrice = itemPrice;
            this.maxAmt = maxAmt;
        }

        private static final List<BloodMerchantItems> VALUES = Collections.unmodifiableList(Arrays.asList(values()));

        public static BloodMerchantItems randomItem()  {
            return VALUES.get(Random.get(VALUES.size() - 1));
        }

    }

}
