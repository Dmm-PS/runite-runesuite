package io.ruin.model.inter.handlers;

import io.ruin.api.utils.NumberUtils;
import io.ruin.cache.EnumMap;
import io.ruin.cache.ItemDef;
import io.ruin.model.World;
import io.ruin.model.combat.Killer;
import io.ruin.model.entity.player.Player;
import io.ruin.model.inter.Interface;
import io.ruin.model.inter.InterfaceType;
import io.ruin.model.item.Item;
import io.ruin.model.item.actions.impl.chargable.Blowpipe;
import io.ruin.model.item.actions.impl.ItemBreaking;
import io.ruin.model.item.actions.impl.ItemUpgrading;
import io.ruin.model.item.actions.impl.Pet;
import io.ruin.model.item.actions.impl.combine.ItemCombining;
import io.ruin.model.skills.prayer.Prayer;
import io.ruin.services.Loggers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class IKOD {

    public static void open(Player player) {
        if(player.isVisibleInterface(Interface.ITEMS_KEPT_ON_DEATH))
            player.closeInterface(InterfaceType.MAIN);
        player.openInterface(InterfaceType.MAIN, Interface.ITEMS_KEPT_ON_DEATH);
        player.getPacketSender().sendAccessMask(Interface.ITEMS_KEPT_ON_DEATH, 18, 0, 4, 2);
        player.getPacketSender().sendAccessMask(Interface.ITEMS_KEPT_ON_DEATH, 21, 0, 42, 2);

        boolean skulled = player.getCombat().isSkulled();
        boolean ultimateIronMan = player.getGameMode().isUltimateIronman(); //todo
        int keepCount = getKeepCount(skulled, ultimateIronMan, player.getPrayer().isActive(Prayer.PROTECT_ITEM));

        ArrayList<Item> items = getItems(player);
        ArrayList<Item> keepItems = new ArrayList<>(keepCount);
        int keepCountRemaining = keepCount;
        long value = 0;
        for(Iterator<Item> it = items.iterator(); it.hasNext(); ) {
            Item item = it.next();
            ItemDef def = item.getDef();
            if(!def.neverProtect && keepCountRemaining > 0) {
                int keepAmount = Math.min(item.getAmount(), keepCountRemaining);
                keepItems.add(new Item(item.getId(), keepAmount, item.getUniqueValue()));
                keepCountRemaining -= keepAmount;
                item.incrementAmount(-keepAmount);
                if(item.getAmount() == 0) {
                    it.remove();
                    continue;
                }
            }
            value += getValue(item);
        }
        player.getPacketSender().sendItems(-1, 63834, 468, items.toArray(new Item[items.size()]));
        player.getPacketSender().sendItems(-1, 63718, 584, keepItems.toArray(new Item[keepItems.size()]));
        player.getPacketSender().sendClientScript(118, "isii1s", 0, "", keepCount, skulled ? 1 : 0, ultimateIronMan ? 1 : 0, NumberUtils.formatNumber(value) + " " + (World.isPVP() ? "bm" : "gp"));
    }

    private static final List<Integer> CHARGED_UNTRADEABLES = Arrays.asList(
            12809, 12006, 20655, 20657, 19710,
            12926, 22550, 11283, 21633, 22002,
            12931, 13197, 13199, 22555, 20714,
            12904, 11907, 12899, 22545
    );

    private static boolean allowProtect(Player player, Item item) {  // should this item be allowed to be 'saved'?
        if (item.getDef().neverProtect)
            return false;
        if (!item.getDef().tradeable && item.getDef().breakTo != null)
            return false;
        if (CHARGED_UNTRADEABLES.contains(item.getId()))
            return true;
        if (item.getDef().breakId != 0)
            return true;
        if(item.getDef().combinedFrom != null)
            return true;
        if (item.getDef().upgradedFrom != null) {
            ItemDef broken = ItemDef.get(item.getDef().upgradedFrom.regularId);
            return broken.tradeable;
        }
        if (!item.getDef().tradeable && player.wildernessLevel <= 20)
            return false;
        return true;
    }

    //todo - still want to clean this up
    public static void forLostItem(Player player, Killer killer, Consumer<Item> dropConsumer) {
        ArrayList<Item> items = getItems(player);
        if(items.isEmpty())
            return;
        player.getInventory().clear();
        player.getEquipment().clear();
        Item currency = new Item(World.isPVP() ? 13307 : 995, 0);
        ArrayList<Item> loseItems = new ArrayList<>(items.size());
        ArrayList<Item> keepItems = new ArrayList<>();
        int keepCountRemaining = getKeepCount(player.getCombat().isSkulled(), false, player.getPrayer().isActive(Prayer.PROTECT_ITEM));
        for(Item item : items) {
            /* attempt to protect */
            if(keepCountRemaining > 0 && allowProtect(player, item)) {
                int keepAmount = Math.min(item.getAmount(), keepCountRemaining);
                keepItems.add(new Item(item.getId(), keepAmount, item.getUniqueValue()));
                keepCountRemaining -= keepAmount;
                item.incrementAmount(-keepAmount);
                if(item.getAmount() == 0)
                    continue;
            }
            if (player.wildernessLevel == 0 && (killer == null || killer.player == null) && !item.getDef().tradeable) {
                //on a non-pvp, non-wilderness death, players will keep all untradeables without them breaking/splitting/etc, similar to osrs
                keepItems.add(item);
                continue;
            }
            /* looting bag */
            if(item.getId() == 11941 || item.getId() == 22586) {
                for(Item loot : player.getLootingBag().getItems()) {
                    if(loot != null)
                        loseItems.add(loot);
                }
                player.getLootingBag().clear();
                continue;
            }
            /* rune pouch */
            if(item.getId() == 12791) {
                if(World.isPVP()) {
                    keepItems.add(item);
                    continue;
                }
                for(Item rune : player.getRunePouch().getItems()) {
                    if(rune != null)
                        loseItems.add(rune);
                }
                player.getRunePouch().clear();
                continue;
            }
            /* saradomin's blessed sword */
            if(item.getId() == 12809) {
                item.setId(12804);
                item.setUniqueValue(0);
                loseItems.add(item);
                continue;
            }
            /* tentacle whip */
            if(item.getId() == 12006) {
                item.setId(12004);
                item.setUniqueValue(0);
                loseItems.add(item);
                continue;
            }
            /* ring of suffering */
            if(item.getId() == 20655 || item.getId() == 20657 || item.getId() == 19710) {
                item.setId(19550);
                item.setUniqueValue(0);
                loseItems.add(item);
                continue;
            }
            /* serpentine helm */
            if(item.getDef().breakId == 12929) {
                int scalesAmount = item.getUniqueValue();
                if(scalesAmount > 0)
                    loseItems.add(new Item(12934, scalesAmount));
                item.setId(12929);
                item.setUniqueValue(0);
                loseItems.add(item);
                continue;
            }
            /* viggora's chainmace */
            if(item.getId() == 22545) {
                int etherAmount = item.getUniqueValue();
                if(etherAmount > 0)
                    loseItems.add(new Item(21820, etherAmount));
                item.setId(22542);
                item.setUniqueValue(0);
                loseItems.add(item);
                continue;
            }
            /* craw's bow */
            if(item.getId() == 22550) {
                int etherAmount = item.getUniqueValue();
                if(etherAmount > 0)
                    loseItems.add(new Item(21820, etherAmount));
                item.setId(22547);
                item.setUniqueValue(0);
                loseItems.add(item);
                continue;
            }
            /* thammarons's sceptre */
            if(item.getId() == 22555) {
                int etherAmount = item.getUniqueValue();
                if(etherAmount > 0)
                    loseItems.add(new Item(21820, etherAmount));
                item.setId(22552);
                item.setUniqueValue(0);
                loseItems.add(item);
                continue;
            }
            /* dragonfire shield */
            if(item.getDef().id == 11283) {
                item.setId(11284);
                item.setUniqueValue(0);
                loseItems.add(item);
                continue;
            }
            /*ancient wyvern shield */
            if(item.getDef().id == 21633) {
                item.setId(21634);
                item.setUniqueValue(0);
                loseItems.add(item);
                continue;
            }
            /*dragonfire ward */
            if(item.getDef().id == 22002) {
                item.setId(22003);
                item.setUniqueValue(0);
                loseItems.add(item);
                continue;
            }
            /* toxic staff of the dead */
            if(item.getDef().breakId == 12902) {
                int scalesAmount = item.getUniqueValue();
                if(scalesAmount > 0)
                    loseItems.add(new Item(12934, scalesAmount));
                item.setId(12902);
                item.setUniqueValue(0);
                loseItems.add(item);
                continue;
            }
            /* toxic blowpipe */
            if(item.getId() == 12926) {
                Blowpipe.Dart dart = Blowpipe.getDart(item);
                if(dart != Blowpipe.Dart.NONE)
                    loseItems.add(new Item(dart.id, Blowpipe.getDartAmount(item)));
                int scales = Blowpipe.getScalesAmount(item);
                if(scales > 0)
                    loseItems.add(new Item(12934, scales));
                item.setId(12924);
                item.setUniqueValue(0);
                loseItems.add(item);
                continue;
            }
            /* blood money pouches */
            if(item.getId() >= 22521 && item.getId() <= 22524) {
                loseItems.add(item);
                continue;
            }
            /* bracelet of ethereum */
            if(item.getDef().breakId == 21817) {
                int etherAmount = item.getUniqueValue();
                if(etherAmount > 0)
                    loseItems.add(new Item(21820, etherAmount));
                item.setId(21817);
                item.setUniqueValue(0);
                loseItems.add(item);
                continue;
            }
            /* tome of fire */
            if(item.getId() == 20714) {
                int charges = item.getUniqueValue();
                if(charges > 0)
                    loseItems.add(new Item(20718, Math.max(1, charges / 20)));
                item.setId(20716);
                item.setUniqueValue(0);
                loseItems.add(item);
                continue;
            }
            /* pet */
            Pet pet = item.getDef().pet;
            if(pet != null) {
                keepItems.add(item);
                continue;
            }
            /* breakable items */
            ItemBreaking breakable = item.getDef().breakTo;
            if(breakable != null && !breakable.freeFromShops) {
                ItemDef brokenDef = ItemDef.get(breakable.brokenId);
                if(!brokenDef.tradeable) {
                    if(World.isPVP() || player.wildernessLevel < 20) {
                        item.setId(brokenDef.id);
                        keepItems.add(item);
                        if(item.getDef().bmShopPrice != 0)
                            currency.incrementAmount(World.isPVP() ? breakable.bmRepairCost : breakable.coinRepairCost);
                    }
                    continue;
                }
                if(item.getDef().bmShopPrice != 0)
                    currency.incrementAmount(World.isPVP() ? breakable.bmRepairCost : breakable.coinRepairCost);
                item.setId(brokenDef.id);
            }
            /* upgraded items */
            ItemUpgrading upgrade = item.getDef().upgradedFrom;
            if(upgrade != null) {
                ItemDef regularDef = ItemDef.get(upgrade.regularId);
                if(!regularDef.tradeable) {
                    if(World.isPVP() || player.wildernessLevel < 20) {
                        keepItems.add(item);
                    }
                    continue;
                }
                if(item.getDef().bmShopPrice != 0)
                    currency.incrementAmount(World.isPVP() ? upgrade.bmUpgradeCost : upgrade.coinUpgradeCost);
                item.setId(regularDef.id);
            }
            /* combined items */
            ItemCombining combined = item.getDef().combinedFrom;
            if(combined != null) {
                loseItems.add(new Item(combined.primaryId, item.getAmount()));
                loseItems.add(new Item(combined.secondaryId, item.getAmount()));
                continue;
            }
            /* keep untradeables */
            if (!item.getDef().tradeable) {
                keepItems.add(item);
                continue;
            }
            loseItems.add(item);
        }
        if(currency.getAmount() > 0)
            loseItems.add(currency);
        int size = Math.min(keepItems.size(), player.getInventory().getItems().length);
        for(int i = 0; i < size; i++)
            player.getInventory().set(i, keepItems.get(i));
        for(Item dropItem : loseItems)
            dropConsumer.accept(dropItem);
        if(killer == null)
            Loggers.logDangerousDeath(player.getUserId(), player.getName(), player.getIp(), -1, "", "", keepItems, loseItems);
        else
            Loggers.logDangerousDeath(player.getUserId(), player.getName(), player.getIp(), killer.player.getUserId(), killer.player.getName(), killer.player.getIp(), keepItems, loseItems);
    }

    public static int getKeepCount(boolean skulled, boolean ultimateIronman, boolean protectingItem) {
        if(ultimateIronman)
            return 0;
        int keepCount = skulled ? 0 : 3;
        if(protectingItem)
            keepCount++;
        return keepCount;
    }

    public static ArrayList<Item> getItems(Player player) {
        int count = player.getInventory().getCount() + player.getEquipment().getCount();
        ArrayList<Item> list = new ArrayList<>(count);
        if(count > 0) {
            for(Item item : player.getInventory().getItems()) {
                if(item != null)
                    list.add(item.copy());
            }
            for(Item item : player.getEquipment().getItems()) {
                if(item != null)
                    list.add(item.copy());
            }
            list.sort((i1, i2) -> Integer.compare(i2.getDef().protectValue, i1.getDef().protectValue));
        }
        return list;
    }

    private static long getValue(Item item) {
        if(World.isPVP()) {
            if(item.getId() == 13307)
                return item.getAmount();
            long price = item.getDef().bmShopPrice;
            if(price <= 0)
                return 0;
            return item.getAmount() * price;
        } else {
            if(item.getId() == 995)
                return item.getAmount();
            long price = item.getDef().protectValue; //hmm???
            if(price <= 0 && ((price = item.getDef().highAlchValue)) <= 0)
                return 0;
            return item.getAmount() * price;
        }
    }

    static {
        EnumMap map = EnumMap.get(879);
        for(int id : map.keys)
            ItemDef.get(id).neverProtect = id != 13190 && id != 13192; //true when not bonds

        /**
         * Custom protect items
         */
        if(World.isPVP()) {
            ItemDef.get(12931).protectValue = (int) Math.min(Integer.MAX_VALUE, 20000 * 1000L); // Serpentine helm (charged)
            ItemDef.get(13197).protectValue = (int) Math.min(Integer.MAX_VALUE, 30000 * 1000L); // Tanzanite helm (charged)
            ItemDef.get(13199).protectValue = (int) Math.min(Integer.MAX_VALUE, 30000 * 1000L); // Magma helm (charged)
            ItemDef.get(12926).protectValue = (int) Math.min(Integer.MAX_VALUE, 20000 * 1000L); //Charged blowpipe (charged)
            ItemDef.get(22550).protectValue = (int) Math.min(Integer.MAX_VALUE, 30000 * 1000L); //Craws bow (charged)
            ItemDef.get(22547).protectValue = (int) Math.min(Integer.MAX_VALUE, 30000 * 1000L); //Craws bow (uncharged)
            ItemDef.get(22545).protectValue = (int) Math.min(Integer.MAX_VALUE, 30000 * 1000L); //Viggora's chainmace (charged)
            ItemDef.get(22542).protectValue = (int) Math.min(Integer.MAX_VALUE, 30000 * 1000L); //Viggora's chainmace (uncharged)
            ItemDef.get(22555).protectValue = (int) Math.min(Integer.MAX_VALUE, 30000 * 1000L); //Thammaron's sceptre (charged)
            ItemDef.get(22552).protectValue = (int) Math.min(Integer.MAX_VALUE, 30000 * 1000L); //Thammaron's sceptre (uncharged)
            ItemDef.get(11283).protectValue = (int) Math.min(Integer.MAX_VALUE, 20000 * 1000L); //Dragonfire shield (charged)
            ItemDef.get(21633).protectValue = (int) Math.min(Integer.MAX_VALUE, 25000 * 1000L); //Ancient wyvern (charged)
            ItemDef.get(22002).protectValue = (int) Math.min(Integer.MAX_VALUE, 25000 * 1000L); //Dragonfire ward (charged)
            ItemDef.get(12899).protectValue = (int) Math.min(Integer.MAX_VALUE, 25000 * 1000L); //Trident of the swamp (charged)
            ItemDef.get(11907).protectValue = (int) Math.min(Integer.MAX_VALUE, 5000 * 1000L); //Trident of the seas (charged)
            ItemDef.get(11905).protectValue = (int) Math.min(Integer.MAX_VALUE, 5000 * 1000L); //Trident of the seas (fully charged)
            ItemDef.get(12904).protectValue = (int) Math.min(Integer.MAX_VALUE, 10000 * 1000L); //Toxic staff of the dead
            ItemDef.get(20714).protectValue = (int) Math.min(Integer.MAX_VALUE, 6000 * 1000L); //Tome of fire
            ItemDef.get(19550).protectValue = (int) Math.min(Integer.MAX_VALUE, 15000 * 1000L); //Ring of suffering
            ItemDef.get(22613).protectValue = (int) Math.min(Integer.MAX_VALUE, 120000 * 1000L); //Vesta long
        }
    }

}
