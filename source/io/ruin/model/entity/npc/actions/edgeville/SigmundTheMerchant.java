package io.ruin.model.entity.npc.actions.edgeville;

import io.ruin.cache.ItemDef;
import io.ruin.cache.NPCDef;
import io.ruin.model.World;
import io.ruin.model.entity.npc.NPCAction;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.shared.listeners.SpawnListener;
import io.ruin.model.item.Item;
import io.ruin.model.item.actions.impl.ItemSet;
import io.ruin.model.item.containers.shop.Shop;
import io.ruin.model.item.containers.shop.ShopCurrency;
import io.ruin.model.item.containers.shop.ShopItem;
import io.ruin.model.skills.herblore.Potion;
import io.ruin.model.stat.StatType;

import java.util.ArrayList;

public class SigmundTheMerchant {

    private static final int SIGMUND = 3894;

    public static int getPrice(Player player, Item item) {
        ItemDef def = item.getDef();
        if(def.isNote())
            def = def.fromNote();
        if(World.isPVP())
            return Math.min(25000, def.sigmundBuyPrice);
        if(def.sigmundBuyPrice != 0)
            return def.sigmundBuyPrice;
        return player.getGameMode().isIronMan() ? def.lowAlchValue : def.highAlchValue;
    }

    static {
        if(World.isPVP()) {
            SpawnListener.register(SIGMUND, npc -> npc.skipReachCheck = p -> p.equals(3078, 3510));
            /**
             * Custom prices
             */
            ItemDef.get(6922).sigmundBuyPrice = 750; //infinity gloves
            ItemDef.get(12004).sigmundBuyPrice = 1350; //kraken tentacle ((3000 - 750) * 0.6) (MAKE SURE IF ABYSSAL WHIP OR ABYSSAL TENTACLE PRICE CHANGES, SO DOES THIS!)
            ItemDef.get(11920).sigmundBuyPrice = 3000; //dragon pickaxe
            ItemDef.get(13233).sigmundBuyPrice = 2500; //smouldering stone
            ItemDef.get(21532).sigmundBuyPrice = 2; //fragments
            ItemDef.get(22324).sigmundBuyPrice = 100000; //ghrazi rapier
            ItemDef.get(21018).sigmundBuyPrice = 50000; //ancestral hat
            ItemDef.get(21021).sigmundBuyPrice = 75000; //ancestral robe top
            ItemDef.get(21024).sigmundBuyPrice = 75000; //ancestral robe bottom
            ItemDef.get(22326).sigmundBuyPrice = 50000; //justiciar faceguard
            ItemDef.get(22327).sigmundBuyPrice = 75000; //justiciar chestguard
            ItemDef.get(22328).sigmundBuyPrice = 75000; //justiciar legguards
            ItemDef.get(11998).sigmundBuyPrice = 500; //smoke battlestaff
            ItemDef.get(22296).sigmundBuyPrice = 10000; //staff of light
            ItemDef.get(13652).sigmundBuyPrice = 60000; //dragon claws
            ItemDef.get(22622).sigmundBuyPrice = 75000; // Statius's warhammer
            ItemDef.get(22625).sigmundBuyPrice = 50000; // Statius's full helm
            ItemDef.get(22628).sigmundBuyPrice = 50000; // Statius's platebody
            ItemDef.get(22631).sigmundBuyPrice = 50000; // Statius's platelegs
            ItemDef.get(22610).sigmundBuyPrice = 75000; // Vesta's spear
            ItemDef.get(22613).sigmundBuyPrice = 75000; // Vesta's longsword
            ItemDef.get(22616).sigmundBuyPrice = 50000; // Vesta's chainbody
            ItemDef.get(22619).sigmundBuyPrice = 50000; // Vesta's plateskirt
            ItemDef.get(22647).sigmundBuyPrice = 75000; // Zuriel's staff
            ItemDef.get(22650).sigmundBuyPrice = 50000; // Zuriel's hood
            ItemDef.get(22653).sigmundBuyPrice = 50000; // Zuriel's robe top
            ItemDef.get(22656).sigmundBuyPrice = 50000; // Zuriel's robe bottom
            ItemDef.get(22638).sigmundBuyPrice = 50000; // Morrigan's coif
            ItemDef.get(22641).sigmundBuyPrice = 50000; // Morrigan's leather body
            ItemDef.get(22644).sigmundBuyPrice = 50000; // Morrigan's leather chaps
            ItemDef.get(22634).sigmundBuyPrice = 1000; // Morrian's throwing axe
            ItemDef.get(22636).sigmundBuyPrice = 1000; // Morrian's javelin
            ItemDef.get(1751).sigmundBuyPrice = 75; // Blue Dragonhide
            ItemDef.get(1752).sigmundBuyPrice = 75; // Blue Dragonhide (noted)
            ItemDef.get(536).sigmundBuyPrice = 50; // Dragon Bones
            ItemDef.get(537).sigmundBuyPrice = 50; // Dragon Bones (noted)
            ItemDef.get(11943).sigmundBuyPrice = 100; // Lava Dragon Bones
            ItemDef.get(11944).sigmundBuyPrice = 100; // Lava Dragon Bones (noted)
            ItemDef.get(22124).sigmundBuyPrice = 75; // Superior Dragon Bones
            ItemDef.get(22125).sigmundBuyPrice = 75; // Superior Dragon Bones (noted)
            ItemDef.get(1753).sigmundBuyPrice = 50; // Green Dragonhide
            ItemDef.get(1754).sigmundBuyPrice = 50; // Green Dragonhide (noted)
            ItemDef.get(1747).sigmundBuyPrice = 75; // Black Dragonhide
            ItemDef.get(1748).sigmundBuyPrice = 75; // Black Dragonhide (noted)
            ItemDef.get(11992).sigmundBuyPrice = 50; // Lava Scale
            ItemDef.get(21820).sigmundBuyPrice = 1; // Revenant Ether
            ItemDef.get(21817).sigmundBuyPrice = 500; // Bracelet of Ethereum (uncharged)
            ItemDef.get(12938).sigmundBuyPrice = 50; // Zul-andra teleport
            ItemDef.get(12936).sigmundBuyPrice = 10000; // Jar of Swamp
            ItemDef.get(12007).sigmundBuyPrice = 5000; // Jar of Dirt
            ItemDef.get(13245).sigmundBuyPrice = 5000; // jar of Souls
            ItemDef.get(22550).sigmundBuyPrice = 15000; //Craws bow (charged)
            ItemDef.get(22547).sigmundBuyPrice = 15000; //Craws bow (uncharged)
            ItemDef.get(22545).sigmundBuyPrice = 15000; //Viggora's chainmace (charged)
            ItemDef.get(22542).sigmundBuyPrice = 15000; //Viggora's chainmace (uncharged)
            ItemDef.get(22555).sigmundBuyPrice = 15000; //Thammaron's sceptre (charged)
            ItemDef.get(22552).sigmundBuyPrice = 15000; //Thammaron's sceptre (uncharged)
            ItemDef.get(13200).sigmundBuyPrice = 5000; //Tanzanite mutagen
            ItemDef.get(13201).sigmundBuyPrice = 5000; //Magma mutagen
            ItemDef.get(13265).sigmundBuyPrice = 4500; //Abyssal dagger
            ItemDef.get(1149).sigmundBuyPrice = 250; //Dragon medium helm

            /**
             * Custom checks
             */
            NPCDef.forEach(npcDef -> {
                if(npcDef.shop != null && npcDef.shop.currency == ShopCurrency.BLOOD_MONEY) {
                    for(ShopItem item : npcDef.shop.items) {
                        ItemDef def = item.getDef();
                        def.bmShopPrice = item.price;
                        if(def.id == 19564) //Royal seed pod
                            continue;
                        if(def.id == 4170) //Slayer staff (slayer master reward dupe)
                            continue;
                        def.sigmundBuyPrice = (int) (item.price * 0.6);
                        if(item.price == 0) {
                            def.protectValue = 0;
                        } else {
                            if(def.protectValue < item.price) {
                                // don't think this warning is needed anymore
//                                System.err.println(def.name + " (" + item.getId() + ") has a protect value: " + def.protectValue + " which is lower than bm shop price: " + item.price);
                            }
                            def.protectValue = (int) Math.min(Integer.MAX_VALUE, item.price * 1000L);
                            def.sigmundBuyPrice = Math.max(1, (int) (item.price * 0.6));
                        }
                    }
                }
            });
        } else {
            Potion.values(); //forces Potion class to load! :p
            for(ShopItem item : NPCDef.get(SIGMUND).shop.items) {
                ItemDef def = item.getDef();
                int price = (int) Math.max(1, item.price * 0.8);
                if(def.potion != null) {
                    int pricePerDose = Math.max(1, price / def.potionDoses);
                    for(int id : def.potion.vialIds) {
                        ItemDef potDef = ItemDef.get(id);
                        potDef.sigmundBuyPrice = pricePerDose * potDef.potionDoses;
                    }
                    continue;
                }
                def.sigmundBuyPrice = price;
            }
            /**
             * Safe checks
             */
            NPCDef.forEach(npcDef -> {
                if(npcDef.shop != null && npcDef.shop.currency == ShopCurrency.COINS) {
                    for(ShopItem item : npcDef.shop.items) {
                        ItemDef def = item.getDef();
                        if(item.price < def.value)
                            item.price = def.value;
                        if(item.price < def.sigmundBuyPrice)
                            item.price = def.sigmundBuyPrice;
                    }
                }
            });
        }
        /**
         * PVP world
         */

        final int[] BLOOD_MONEY_SHOP_OWNERS =  {4225, 5081, 5051, 2153, 4579, 6650};
        if(World.isPVP()) {
            NPCAction.register(SIGMUND, "buy-items", (player, npc) -> {
                if(player.getGameMode().isIronMan()) {
                    player.sendMessage("You can't access this shop as an ironman!");
                    return;
                }
                StatType[] types = StatType.values();
                ArrayList<ShopItem> stock = new ArrayList<>(types.length);

                for(int shopIds : BLOOD_MONEY_SHOP_OWNERS) {
                    Shop shopOne = NPCDef.get(shopIds).shop;
                    for (ShopItem item : shopOne.items) {
                       // if (item.price > 0)
                            stock.add(item);
                    }
                }

                Shop bloodMoneyShop = new Shop("Sigmund's Blood Money Exchange", ShopCurrency.BLOOD_MONEY, false, stock);
                bloodMoneyShop.open(player);
            });
            NPCAction.register(SIGMUND, "sell-items", (player, npc) -> player.getTrade().tradeSigmund());
            NPCAction.register(SIGMUND, "sets", (player, npc) -> ItemSet.open(player));

            // Set the defs for these items as free
            for(int shopIds : BLOOD_MONEY_SHOP_OWNERS) {
                Shop shopOne = NPCDef.get(shopIds).shop;
                for (ShopItem item : shopOne.items) {
                    if (item.price == 0) {
                        item.getDef().free = true;
                    }
                }
            }
        }

        if(World.isEco()) {
            NPCAction.register(SIGMUND, "buy-items", (player, npc) -> {
                if(player.getGameMode().isIronMan()) {
                    StatType[] types = StatType.values();
                    ArrayList<ShopItem> stock = new ArrayList<>(types.length);
                    stock.add(new ShopItem(8007, 1000));       // Varrock teleport
                    stock.add(new ShopItem(8008, 1000));       // Lumbridge teleport
                    stock.add(new ShopItem(8009, 1000));       // Falador teleport
                    stock.add(new ShopItem(8010, 1000));       // Camelot teleport
                    stock.add(new ShopItem(8011, 1000));       // Ardougne teleport
                    stock.add(new ShopItem(8012, 1000));       // Watchtower teleport
                    stock.add(new ShopItem(8013, 1000));       // House teleport
                    stock.add(new ShopItem(1478, 2500));       // Amulet of accuracy
                    stock.add(new ShopItem(1731, 2500));       // Amulet of power
                    stock.add(new ShopItem(1725, 5000));       // Amulet of strength
                    stock.add(new ShopItem(554, 3));          // Fire rune
                    stock.add(new ShopItem(555, 3));          // Water rune
                    stock.add(new ShopItem(556, 3));          // Air rune
                    stock.add(new ShopItem(557, 3));          // Earth rune
                    stock.add(new ShopItem(558, 3));          // Mind rune
                    stock.add(new ShopItem(559, 3));          // Body rune
                    stock.add(new ShopItem(562, 100));         // Chaos rune
                    stock.add(new ShopItem(563, 300));         // Law rune
                    stock.add(new ShopItem(561, 325));         // Nature rune
                    stock.add(new ShopItem(564, 350));         // Cosmic rune
                    stock.add(new ShopItem(333, 50));          // Trout
                    stock.add(new ShopItem(329, 50));          // Salmon
                    stock.add(new ShopItem(1323, 500));        // Iron scimitar
                    stock.add(new ShopItem(1329, 1500));       // Mithril scimitar
                    stock.add(new ShopItem(1331, 3000));       // Adamant Scimitar
                    stock.add(new ShopItem(1333, 30000));      // Rune scimitar
                    stock.add(new ShopItem(1381, 5000));       // Air staff
                    stock.add(new ShopItem(1383, 5000));       // Water staff
                    stock.add(new ShopItem(1385, 5000));       // Earth staff
                    stock.add(new ShopItem(1387, 5000));       // Fire staff
                    stock.add(new ShopItem(1153, 160));        // Iron full helm
                    stock.add(new ShopItem(1115, 550));        // Iron platebody
                    stock.add(new ShopItem(1067, 300));        // Iron platelegs
                    stock.add(new ShopItem(1081, 300));        // Iron plateskirt
                    stock.add(new ShopItem(1191, 250));        // Iron kiteshield
                    stock.add(new ShopItem(9672, 8000));        // Proselyte sallet
                    stock.add(new ShopItem(9674, 12000));       // Proselyte hauberk
                    stock.add(new ShopItem(9676, 10000));       // Proselyte cuisse
                    stock.add(new ShopItem(9678, 10000));       // Proselyte tasset
                    stock.add(new ShopItem(1540, 2500));        // Anti-dragon shield
                    stock.add(new ShopItem(7458, 5000));       // Mithril gloves
                    stock.add(new ShopItem(7459, 10000));      // Adamant gloves
                    stock.add(new ShopItem(7460, 15000));      // Rune gloves
                    stock.add(new ShopItem(7462, 100000));     // Barrows gloves
                    stock.add(new ShopItem(3105, 1000));       // Rock climbing boots
                    stock.add(new ShopItem(841, 50));          // Shortbow
                    stock.add(new ShopItem(843, 100));         // Oak shortbow
                    stock.add(new ShopItem(849, 250));         // Willow shortbow
                    stock.add(new ShopItem(9179, 1500));       // Steel crossbow
                    stock.add(new ShopItem(9181, 2500));       // Mith crossbow
                    stock.add(new ShopItem(882, 10));          // Bronze arrow
                    stock.add(new ShopItem(884, 20));          // Iron arrow
                    stock.add(new ShopItem(886, 50));          // Steel arrow
                    stock.add(new ShopItem(888, 200));          // Mithril arrow
                    stock.add(new ShopItem(890, 350));          // Adamant arrow
                    stock.add(new ShopItem(877, 30));          // Bronze bolts
                    stock.add(new ShopItem(9140, 50));         // Iron bolts
                    stock.add(new ShopItem(9141, 100));        // Steel bolts
                    stock.add(new ShopItem(864, 100));         // Bronze knife
                    stock.add(new ShopItem(863, 150));         // Iron knife
                    stock.add(new ShopItem(1169, 500));        // Coif
                    stock.add(new ShopItem(1129, 500));        // Leather body
                    stock.add(new ShopItem(1133, 850));        // Studded body
                    stock.add(new ShopItem(10499, 50000));     // Ava's accumulator
                    stock.add(new ShopItem(2415, 80000));      // Saradomin staff
                    stock.add(new ShopItem(2416, 80000));      // Guthix staff
                    stock.add(new ShopItem(2417, 80000));      // Zamorak staff
                    stock.add(new ShopItem(2412, 50000));       // Saradomin cape
                    stock.add(new ShopItem(2413, 50000));       // Guthix cape
                    stock.add(new ShopItem(2414, 50000));       // Zamorak cape
                    stock.add(new ShopItem(1035, 1500));       // Zamorak robe
                    stock.add(new ShopItem(1033, 1500));       // Zamorak robe
                    stock.add(new ShopItem(544, 500));         // Monk's robe top
                    stock.add(new ShopItem(542, 500));         // Monk's robe
                    stock.add(new ShopItem(579, 500));         // Blue wizard hat
                    stock.add(new ShopItem(577, 500));         // Blue wizard robe
                    stock.add(new ShopItem(1011, 500));        // Blue skirt
                    stock.add(new ShopItem(3840,10000));       // Holy book
                    stock.add(new ShopItem(3842,10000));       // Unholy book
                    stock.add(new ShopItem(3844,10000));       // Book of balance
                    stock.add(new ShopItem(5935, 1250));        // Coconut milk
                    stock.add(new ShopItem(245, 1300));         // Wine of zamorak
                    stock.add(new ShopItem(221, 10));            // Eye of newt
                    stock.add(new ShopItem(235, 1000));          // Unicorn horn dust
                    stock.add(new ShopItem(225, 2500));         // Limpwurt root
                    stock.add(new ShopItem(223, 60));           // Red spiders' eggs
                    stock.add(new ShopItem(1975, 90));          // Chocolate dust
                    stock.add(new ShopItem(239, 850));          // White berries
                    stock.add(new ShopItem(2152, 75));          // Toad's legs
                    stock.add(new ShopItem(9736, 250));         // Goat horn dust
                    stock.add(new ShopItem(231, 50));           // Snape grass
                    stock.add(new ShopItem(2970, 950));         // Mort myre fungus
                    stock.add(new ShopItem(241, 150));          // Dragon scale dust
                    stock.add(new ShopItem(6049, 550));         // Yew roots
                    stock.add(new ShopItem(6051, 2750));        // Magic roots
                    stock.add(new ShopItem(6016, 5700));        // Cactus spine
                    stock.add(new ShopItem(3138, 250));         // Potato cactus
                    stock.add(new ShopItem(12640, 6000));        // Amylase crystal
                    stock.add(new ShopItem(247, 450));          // Jangerberries
                    stock.add(new ShopItem(6018, 1250));        // Poison ivy berries
                    stock.add(new ShopItem(1349, 500));         // Iron axe
                    stock.add(new ShopItem(1353, 1000));        // Steel axe
                    stock.add(new ShopItem(1355, 2500));        // Mithril axe
                    stock.add(new ShopItem(1357, 5000));        // Adamant axe
                    stock.add(new ShopItem(1359, 15500));       // Rune axe
                    stock.add(new ShopItem(1267, 500));         // Iron pickaxe
                    stock.add(new ShopItem(1269, 1000));        // Steel pickaxe
                    stock.add(new ShopItem(1273, 2500));        // Mithril pickaxe
                    stock.add(new ShopItem(1271, 5000));        // Adamant pickaxe
                    stock.add(new ShopItem(1275, 15500));       // Rune pickaxe
                    stock.add(new ShopItem(303, 500));         // Small fishing net
                    stock.add(new ShopItem(305, 500));         // Big fishing net
                    stock.add(new ShopItem(307, 500));         // Fishing rod
                    stock.add(new ShopItem(309, 500));         // Fly fishing rod
                    stock.add(new ShopItem(1585, 500));        // Oily fishing rod
                    stock.add(new ShopItem(11323, 500));       // Barbarian rod
                    stock.add(new ShopItem(301, 500));         // Lobster pot
                    stock.add(new ShopItem(311, 500));         // Harpoon
                    stock.add(new ShopItem(313, 1));           // Fishing bait
                    stock.add(new ShopItem(11940, 50));          // Dark fishing bait
                    stock.add(new ShopItem(314, 10));           // Feather
                    stock.add(new ShopItem(590, 250));          // Tinderbox
                    stock.add(new ShopItem(946,250));          // Knife
                    stock.add(new ShopItem(2347, 150));         // Hammer
                    stock.add(new ShopItem(1755, 55));         // Chisel
                    stock.add(new ShopItem(1733, 94));         // Needle
                    stock.add(new ShopItem(1734, 9));          // Thread
                    stock.add(new ShopItem(1741, 400));        // Leather
                    stock.add(new ShopItem(952, 500));         // Spade
                    stock.add(new ShopItem(233, 5000));        // Pestle and mortar
                    stock.add(new ShopItem(227, 10));          // Vial of water
                    stock.add(new ShopItem(1592, 250));       // Ring mould
                    stock.add(new ShopItem(1595, 250));       // Amulet mould
                    stock.add(new ShopItem(1597, 250));       // Necklace mould
                    stock.add(new ShopItem(11065, 250));      // Bracelet mould
                    stock.add(new ShopItem(9434, 250));        //Bolt mould
                    stock.add(new ShopItem(5523, 250));       // Tiara mould
                    stock.add(new ShopItem(5341, 250));       // Rake
                    stock.add(new ShopItem(5343, 250));       // Seed dibber
                    stock.add(new ShopItem(5329, 250));       // Secateurs
                    stock.add(new ShopItem(5340, 100));       // Watering can(8)
                    stock.add(new ShopItem(5325, 250));       // Gardening trowel
                    stock.add(new ShopItem(954, 250));        // Rope
                    stock.add(new ShopItem(10006, 500));      // Bird snare
                    stock.add(new ShopItem(10008, 250));       // Box trap
                    stock.add(new ShopItem(1523, 250));       // Lockpick
                    stock.add(new ShopItem(1735, 250));       // Shears
                    stock.add(new ShopItem(1925, 10));         // Bucket
                    stock.add(new ShopItem(1757, 150));       // Brown Apron
                    stock.add(new ShopItem(1949, 150));        // Chef's Hat
                    stock.add(new ShopItem(1759, 250));        // Ball of wool
                    stock.add(new ShopItem(1786, 125));        // Glassblowing pipe
                    stock.add(new ShopItem(10010, 250));        // Butterfly net
                    stock.add(new ShopItem(11260, 100));        // Impling jar
                    Shop ironManShop = new Shop("Sigmund's Ironman Supply Exchange", ShopCurrency.COINS, true, stock);
                    ironManShop.open(player);
                } else {
                    npc.getDef().shop.open(player);
                }
            });
            NPCAction.register(SIGMUND, "sell-items", (player, npc) -> {
                if(World.isPVP() && player.getGameMode().isIronMan()) {
                    player.sendMessage("You can't sell items to Sigmund.. you're an ironman!");
                    return;
                }
                player.getTrade().tradeSigmund();
            });
            NPCAction.register(SIGMUND, "sets", (player, npc) -> ItemSet.open(player));
        }
    }

}
