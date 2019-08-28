package io.ruin.model.activities.grandexchange;

import com.google.common.collect.Lists;
import io.ruin.Server;
import io.ruin.api.utils.NumberUtils;
import io.ruin.cache.Color;
import io.ruin.cache.ItemDef;
import io.ruin.model.World;
import io.ruin.model.entity.npc.NPCAction;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.shared.listeners.LoginListener;
import io.ruin.model.inter.InterfaceHandler;
import io.ruin.model.inter.InterfaceType;
import io.ruin.model.inter.actions.DefaultAction;
import io.ruin.model.inter.actions.SimpleAction;
import io.ruin.model.inter.actions.SlotAction;
import io.ruin.model.item.Item;
import io.ruin.model.map.object.actions.ObjectAction;

import java.util.*;

import static io.ruin.model.activities.grandexchange.GrandExchangeConstants.*;

/**
 * @author Andys1814
 */
public final class GrandExchange {

    public static boolean ENABLED = true;

    private static void open(Player player) {
        if (!ENABLED) {
            player.sendMessage(Color.RED.tag() + "Grand Exchange is disabled for maintenance");
            return;
        }

        if (player.getGameMode().isIronMan()) {
            player.sendMessage(Color.RED.tag() + "You cannot use the Grand Exchange while playing as an iron man!");
            return;
        }

        if (player.getBankPin().requiresVerification(p -> open(player))) {
            return;
        }

        updateOffers(player);
        player.getPacketSender().sendClientScript(828, "i", 1);
        player.getPacketSender().sendClientScript(917, "ii", -1, -1);
        player.openInterface(InterfaceType.INVENTORY, MAIN_INVENTORY_INTERFACE);
        player.openInterface(InterfaceType.MAIN, MAIN_INTERFACE);
        player.getPacketSender().sendVarp(1043, 0);
        player.getPacketSender().sendVarp(563, 0);
        player.getPacketSender().sendVarp(375, 0);
        player.getPacketSender().sendVarp(1151, -1);
        player.getPacketSender().sendString(465, 26, "");
        player.getPacketSender().sendString(465, 25, "Click the icon on the left to search for items.");
        player.getPacketSender().sendClientScript(915, "i", 3);

        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 7, 2, 2, 6);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 7, 3, 4, 2);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 8, 2, 2, 6);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 8, 3, 4, 2);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 9, 2, 2, 6);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 9, 3, 4, 2);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 10, 2, 2, 6);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 10, 3, 4, 2);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 11, 2, 2, 6);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 11, 3, 4, 2);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 12, 2, 2, 6);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 12, 3, 4, 2);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 13, 2, 2, 6);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 13, 3, 4, 2);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 14, 2, 2, 6);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 14, 3, 4, 2);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 22, 0, 0, 2);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 23, 2, 3, 1038);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 6, 0, 0, 6);
        player.getPacketSender().sendAccessMask(MAIN_INVENTORY_INTERFACE, 0, 0, 27, 1026);
    }

    private static void openCollectionBox(Player player) {
        if (!ENABLED) {
            player.sendMessage(Color.RED.tag() + "Grand Exchange is disabled for maintenance");
            return;
        }

        if (player.getGameMode().isIronMan()) {
            player.sendMessage(Color.RED.tag() + "You cannot use the Grand Exchange while playing as an iron man!");
            return;
        }

        if (player.getBankPin().requiresVerification(p -> openCollectionBox(player))) {
            return;
        }

        updateOffers(player);
        player.openInterface(InterfaceType.MAIN, COLLECTION_BOX_INTERFACE);
        player.getPacketSender().sendAccessMask(COLLECTION_BOX_INTERFACE, 5, 3, 4, 1038);
        player.getPacketSender().sendAccessMask(COLLECTION_BOX_INTERFACE, 6, 3, 4, 1038);
        player.getPacketSender().sendAccessMask(COLLECTION_BOX_INTERFACE, 7, 3, 4, 1038);
        player.getPacketSender().sendAccessMask(COLLECTION_BOX_INTERFACE, 8, 3, 4, 1038);
        player.getPacketSender().sendAccessMask(COLLECTION_BOX_INTERFACE, 9, 3, 4, 1038);
        player.getPacketSender().sendAccessMask(COLLECTION_BOX_INTERFACE, 10, 3, 4, 1038);
        player.getPacketSender().sendAccessMask(COLLECTION_BOX_INTERFACE, 11, 3, 4, 1038);
        player.getPacketSender().sendAccessMask(COLLECTION_BOX_INTERFACE, 12, 3, 4, 1038);
    }

    private static void openHistory(Player player) {
        if (player.getGameMode().isIronMan()) {
            player.sendMessage(Color.RED.tag() + "You cannot use the Grand Exchange while playing as an iron man!");
            return;
        }

        player.openInterface(InterfaceType.MAIN, TRADE_HISTORY_INTERFACE);
//        player.getPacketSender().sendClientScript(1644, "");
//        player.getPacketSender().sendClientScript(1645, "iiiii", 1, 4151, 1, 3, 10); // ? obj, type(1= sell, 2 = buy?), AMT, num coins
        player.getPacketSender().sendClientScript(1646, "");
    }

    private static void updateOffers(Player player) {
        for (GrandExchangeOfferSlot slot : GrandExchangeOfferSlot.SLOTS) {
            int hash = -1 << 16 | slot.getItemSlotChild();
            GrandExchangeOffer offer;
            if ((offer = GrandExchangeData.getOffer(player, slot)) != null) {
                player.getPacketSender().sendGrandExchangeUpdate(offer.getType().getState(), offer.getSlot().getButtonId() - 7,
                        offer.getItemId(), offer.getPrice(), offer.getItemAmount(), offer.getFulfilled(), offer.getFulfilled() * offer.getItemAmount());
                player.getPacketSender().updateItems(hash, slot.getItemSlotType(), new Item[] { new Item(-1, 0) }, new boolean[] { true }, 1);

                List<Item> collectables = getCollectableItems(offer);
                if (collectables.size() == 0) {
                    collectables.add(new Item(-1, 0));
                    collectables.add(new Item(-1, 0));
                }

                if(collectables.size() == 1) {
                    collectables.add(new Item(-1, 0));
                }
                player.getPacketSender().updateItems(hash, slot.getItemSlotType(), collectables.toArray(new Item[0]), new boolean[] { true, true }, 2);
            } else {
                player.getPacketSender().sendGrandExchangeUpdate(0, slot.getButtonId() - 7, 0, 0, 0, 0, 0);
                player.getPacketSender().updateItems(hash, slot.getItemSlotType(), new Item[] { new Item(-1, 0), new Item(-1, 0) }, new boolean[] { true, true }, 2);
            }
        }
    }

    private static GrandExchangeOffer createOffer(Player player, GrandExchangeOfferType type, GrandExchangeOfferSlot slot, int itemId, int itemAmount) {
        GrandExchangeOffer offer = new GrandExchangeOffer(player.getUserId(), slot, type, itemId, itemAmount, getGuidePrice(itemId));
        player.set("ge_temporary_offer", offer);
        return offer;
    }

    private static void updateBuyOffer(Player player, GrandExchangeOffer offer) {
        player.getPacketSender().sendVarp(563, -2147483648);
        player.getPacketSender().sendVarp(375, offer.getSlot().getSlotId());
        player.getPacketSender().sendVarp(563, offer.getItemAmount());
        player.getPacketSender().sendVarp(1043, offer.getPrice());
        player.getPacketSender().sendVarp(1151, offer.getItemId());
        if (offer.getItemId() != -1 && ItemDef.get(offer.getItemId()) != null) {
            player.getPacketSender().sendString(MAIN_INTERFACE, 26, ItemDef.get(offer.getItemId()).name);
            player.getPacketSender().sendString(MAIN_INTERFACE, 25, ItemDef.get(offer.getItemId()).examine);
        }

        for (GrandExchangeOfferSlot slot : GrandExchangeOfferSlot.SLOTS) {
            player.getPacketSender().sendAccessMask(MAIN_INTERFACE, slot.getButtonId(), 2, 2,6);
            player.getPacketSender().sendAccessMask(MAIN_INTERFACE, slot.getButtonId(), 3, 4, 2);
        }

        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 22, 0, 0, 2);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 23, 2, 3, 1038);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 6, 0, 0, 6);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 24, 0, 13, 2);
        player.getPacketSender().sendAccessMask(MAIN_INVENTORY_INTERFACE, 0, 0, 27, 1026);
    }

    private static void updateSellOffer(Player player, GrandExchangeOffer offer) {
        player.getPacketSender().sendVarp(563, -2147483648);
        player.getPacketSender().sendVarp(375, offer.getSlot().getSlotId());
        player.getPacketSender().sendVarp(563, Integer.MIN_VALUE + offer.getItemAmount());
        player.getPacketSender().sendVarp(1043, offer.getPrice());
        player.getPacketSender().sendVarp(1151, offer.getItemId());
        if (offer.getItemId() != -1 && ItemDef.get(offer.getItemId()) != null) {
            player.getPacketSender().sendString(MAIN_INTERFACE, 26, ItemDef.get(offer.getItemId()).name);
            player.getPacketSender().sendString(MAIN_INTERFACE, 25, ItemDef.get(offer.getItemId()).examine);
        }

        for (GrandExchangeOfferSlot slot : GrandExchangeOfferSlot.SLOTS) {
            player.getPacketSender().sendAccessMask(MAIN_INTERFACE, slot.getButtonId(), 2, 2,6);
            player.getPacketSender().sendAccessMask(MAIN_INTERFACE, slot.getButtonId(), 3, 4, 2);
        }

        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 22, 0, 0, 2);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 23, 2, 3, 1038);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 6, 0, 0, 6);
        player.getPacketSender().sendAccessMask(MAIN_INTERFACE, 24, 0, 13, 2);
        player.getPacketSender().sendAccessMask(MAIN_INVENTORY_INTERFACE, 0, 0, 27, 1026);
    }

    private static void updateOffer(Player player, GrandExchangeOffer offer) {
        if (offer.getType() == GrandExchangeOfferType.SELLING) {
            updateSellOffer(player, offer);
        } else if (offer.getType() == GrandExchangeOfferType.BUYING) {
            updateBuyOffer(player, offer);
        }
    }

    private static void viewOffer(Player player, GrandExchangeOffer offer) {
        player.getPacketSender().sendVarp(375, offer.getSlot().getSlotId());
//        player.getPacketSender().sendVarp(563, offer.getItemAmount());
//        player.getPacketSender().sendVarp(1043, offer.getPrice());
//        player.getPacketSender().sendVarp(1151, offer.getItemId());
        updateOffers(player);
    }

    private static void exchange() {
        GrandExchangeData.getBuyOffers().stream().filter(offer -> offer.getFulfilled() < offer.getItemAmount()).forEach(buyOffer -> {
            List<GrandExchangeOffer> sellOffers = GrandExchangeData.getSellOffers(buyOffer.getItemId(), buyOffer.getPrice());
            for (GrandExchangeOffer sellOffer : sellOffers) {
                final int required = buyOffer.getItemAmount() - buyOffer.getFulfilled();
                final int remaining = sellOffer.getItemAmount() - sellOffer.getFulfilled();
                if (remaining == 0) {
                    continue;
                }

                int bought = 0;
                if (remaining < required) {
                    bought = remaining;
                } else if (remaining >= required) {
                    bought = required;
                }

                sellOffer.setFulfilled(sellOffer.getFulfilled() + bought);
                buyOffer.setFulfilled(buyOffer.getFulfilled() + bought);

                // Sell offers are 'claimed' instantly when an item sells since the gold is put into remainingGold
                sellOffer.setClaimed(sellOffer.getClaimed() + bought);

                // Refund the remaining gold: bought * (buy price - sell price)
                buyOffer.setRemainingGold(buyOffer.getRemainingGold() + ((buyOffer.getPrice() - sellOffer.getPrice()) * bought));
                sellOffer.setRemainingGold(sellOffer.getRemainingGold() + (sellOffer.getPrice() * bought));

                if (sellOffer.getFulfilled() == sellOffer.getItemAmount()) {
                    sellOffer.setType(GrandExchangeOfferType.SELLING_FINISHED);
                } else if (sellOffer.getFulfilled() > sellOffer.getItemAmount()) {
                    Server.logWarning("[GrandExchange] Sell offer fulfilled exceeded amount! This is REALLY bad!");
                }

                if (buyOffer.getFulfilled() == buyOffer.getItemAmount()) {
                    buyOffer.setType(GrandExchangeOfferType.BUYING_FINISHED);
                } else if (buyOffer.getFulfilled() > buyOffer.getItemAmount()) {
                    Server.logWarning("[GrandExchange] Buy offer fulfilled exceeded amount! This is REALLY bad!");
                }

                GrandExchangeData.dumpDatabase = true;
                notifyPlayer(buyOffer);
                notifyPlayer(sellOffer);

                // If the buy offer has been fulfilled, break.
                if (bought == required) {
                    break;
                }
            }
        });
    }

    private static Optional<GrandExchangeOffer> abort(Player player, GrandExchangeOffer offer) {
        if (offer.getType().isFinished()) {
            return Optional.empty();
        }

        if (offer.getType() == GrandExchangeOfferType.BUYING) {
            offer.setType(GrandExchangeOfferType.BUYING_ABORTED);
        } else if (offer.getType() == GrandExchangeOfferType.SELLING) {
            offer.setType(GrandExchangeOfferType.SELLING_ABORTED);
        }

        return Optional.of(offer);
    }

    private static List<Item> getCollectableItems(GrandExchangeOffer offer) {
        final List<Item> items = new ArrayList<>();
        if (offer.getType().isBuyOffer() || offer.getType().equals(GrandExchangeOfferType.BUYING_ABORTED)) {
            if (offer.getFulfilled() > offer.getClaimed()) {
                items.add(new Item(offer.getItemId(), offer.getFulfilled() - offer.getClaimed()));
            }
        }

        if (offer.getType().equals(GrandExchangeOfferType.SELLING_ABORTED) && offer.getClaimed() < offer.getItemAmount()) {
            items.add(new Item(offer.getItemId(), offer.getItemAmount() - offer.getClaimed()));
        }

        int collectableGp = offer.getRemainingGold();

        if (offer.getType().equals(GrandExchangeOfferType.BUYING_ABORTED) && offer.getFulfilled() < offer.getItemAmount()) {
            collectableGp += offer.getPrice() * (offer.getItemAmount() - offer.getFulfilled());
        }

        if (collectableGp > 0) {
            items.add(new Item(995, collectableGp));
        }

        return items;
    }

    private static void collect(Player player, GrandExchangeOffer offer, int itemId, int index) {
        List<Item> collectable = getCollectableItems(offer);
        for (Item ci : collectable) {
            if (itemId != ci.getId()) {
                continue;
            }

            if (index == 3) {
                if (!collectItemsBank(player, offer, ci).isPresent()) {
                    return;
                }
            }

            if (ci.getAmount() == 1) {
                if (index == 1) {
                    if (!collectItems(player, offer, ci).isPresent()) {
                        return;
                    }
                } else if (index == 2) {
                    if (!collectItemsNoted(player, offer, ci).isPresent()) {
                        return;
                    }
                }
            } else {
                if (index == 1) {
                    if (!collectItemsNoted(player, offer, ci).isPresent()) {
                        return;
                    }
                } else if (index == 2) {
                    if (!collectItems(player, offer, ci).isPresent()) {
                        return;
                    }
                }
            }
        }

        if (isOfferFinishedAndCollected(offer)) {
            GrandExchangeData.getOffers().remove(offer);
            GrandExchangeData.dumpDatabase = true;
            player.remove("ge_temporary_offer");
            if (player.isVisibleInterface(MAIN_INTERFACE)) {
                open(player);
            }
        }
        updateOffers(player);
    }

    private static void collectAllItems(Player player, int option) {
        GrandExchangeData.getOffers(player).stream()
                .filter(offer -> getCollectableItems(offer).size() > 0)
                .forEach(offer -> {
                    getCollectableItems(offer).forEach(item -> {
                        if (option == 1) {
                            collectItemsNoted(player, offer, item);
                        } else if (option == 2) {
                            collectItemsBank(player, offer, item);
                        }
                    });

                    if (isOfferFinishedAndCollected(offer)) {
                        GrandExchangeData.getOffers().remove(offer);
                        GrandExchangeData.dumpDatabase = true;
                    }
                });
        updateOffers(player);
    }

    private static Optional<GrandExchangeOffer> collectItemsNoted(Player player, GrandExchangeOffer offer, Item item) {
        if (item.getDef().stackable) {
            if (player.getInventory().isFull()) {
                player.sendMessage("Your inventory is too full.");
                return Optional.empty();
            }

            player.getInventory().add(item);
        } else if (item.getDef().notedId != -1) {
            final Item noted = new Item(item.getDef().notedId, item.getAmount());
            if (noted.getDef() == null || !noted.getDef().isNote()) {
                return collectItems(player, offer, item);
            }
            if (player.getInventory().isFull()) {
                player.sendMessage("Your inventory is too full.");
                return Optional.empty();
            }
            player.getInventory().add(noted);
        } else {
            return collectItems(player, offer, item);
        }
        return Optional.of(persistCollectedItem(offer, item));
    }

    private static Optional<GrandExchangeOffer> collectItemsBank(Player player, GrandExchangeOffer offer, Item item) {
        if (player.getBank().isFull()) {
            player.sendMessage("Your bank is too full.");
            return Optional.empty();
        }
        player.getBank().add(item.getId(), item.getAmount());
        return Optional.of(persistCollectedItem(offer, item));
    }

    private static Optional<GrandExchangeOffer> collectItems(Player player, GrandExchangeOffer offer, Item item) {
        if (!player.getInventory().hasRoomFor(item.getId())) {
            player.sendMessage("Your inventory is too full.");
            return Optional.empty();
        }
        player.getInventory().add(item);
        return Optional.of(persistCollectedItem(offer, item));
    }

    private static GrandExchangeOffer persistCollectedItem(GrandExchangeOffer offer, Item item) {
        if (item.getId() == 995) {
            offer.setRemainingGold(offer.getRemainingGold() - item.getAmount());
        } else if (item.getId() == offer.getItemId()) {
            offer.setClaimed(offer.getClaimed() + item.getAmount());
        }
        return offer;
    }

    private static boolean isOfferFinishedAndCollected(GrandExchangeOffer offer) {
        return offer.getType().isFinished() && getCollectableItems(offer).size() == 0;
    }

    private static void notifyPlayer(GrandExchangeOffer offer) {
        Player player = World.getPlayer(offer.getOwnerId(), true);
        if (player == null) {
            return;
        }

        updateOffers(player);

        if (offer.getFulfilled() == offer.getItemAmount()) {
            if (!player.isVisibleInterface(MAIN_INTERFACE)) {
                player.sendMessage("<col=006000>Grand Exchange: Finished " + offer.getType().getFormatted() + " " + offer.getItemAmount() + " x " + ItemDef.get(offer.getItemId()).name + ".");
            }
        }

    }

    private static Optional<GrandExchangeOffer> getTemporaryOffer(Player player) {
        return Optional.ofNullable(player.get("ge_temporary_offer"));
    }

    private static Optional<GrandExchangeOfferSlot> getFreeSlot(Player player) {
        List<GrandExchangeOfferSlot> slots = Lists.newArrayList(GrandExchangeOfferSlot.SLOTS);
        GrandExchangeData.getOffers(player).forEach(offer -> slots.remove(offer.getSlot()));
        if (slots.size() < 1) {
            return Optional.empty();
        }
        return Optional.of(slots.get(0));
    }

    private static int getGuidePrice(int itemId) {
        ItemDef itemDef = ItemDef.get(itemId);
        if (itemDef == null) {
            return 1;
        }

        return itemDef.protectValue > 0 ? itemDef.protectValue : itemDef.highAlchValue;
    }

    private static int[] CLERKS = new int[] { 2148, 2149, 2150, 2151 };

    static  {
        InterfaceHandler.register(MAIN_INTERFACE, h -> {
            for (GrandExchangeOfferSlot slot : GrandExchangeOfferSlot.SLOTS) {
                h.actions[slot.getButtonId()] = (DefaultAction) (player, option, index, itemId)  -> {
                    if (index == 2) { // View offer
                        GrandExchangeOffer offer = GrandExchangeData.getOffer(player, slot);
                        player.set("ge_temporary_offer", offer);
                        if (option == 2) {
                            abort(player, offer);
                        }
                        viewOffer(player, offer);
                    } else if (index == 3) { // Buy offer
                        player.closeInterface(InterfaceType.INVENTORY);
                        GrandExchangeOffer temporaryOffer = createOffer(player, GrandExchangeOfferType.BUYING, slot, -1, 0);
                        updateOffer(player, temporaryOffer);
                        player.itemSearch("What would you like to buy?", false, input -> {
                            if (input == 65535) {
                                return;
                            }
                            temporaryOffer.setItemId(input);
                            temporaryOffer.setItemAmount(1);
                            temporaryOffer.setPrice(getGuidePrice(input));
                            updateBuyOffer(player, temporaryOffer);
                        });
                    } else if (index == 4) { // Sell offer
                        GrandExchangeOffer temporaryOffer = createOffer(player, GrandExchangeOfferType.SELLING, slot, -1, 0);
                        updateOffer(player, temporaryOffer);
                    }
                };
            }

            h.actions[23] = (DefaultAction) (player, option, slot, itemId) -> {
                if (slot == 2 || slot == 3) {
                    GrandExchangeOffer offer = getTemporaryOffer(player).orElse(null);
                    if (offer == null) {
                        return;
                    }

                    GrandExchangeOffer persistedOffer = GrandExchangeData.getOffer(player, offer.getSlot());
                    if (persistedOffer == null) {
                        return;
                    }
                    collect(player, offer, itemId, option);
                    //updateOffers(player);
                }
            };

            h.actions[24] = (SlotAction) (player, index) -> {
                GrandExchangeOffer temporaryOffer = getTemporaryOffer(player).orElse(null);
                if (temporaryOffer == null) {
                    return;
                }
                if (index == 0) {
                    player.itemSearch("What would you like to buy?", false, itemId -> {
                        if (itemId == 65535) {
                            return;
                        }
                        temporaryOffer.setItemId(itemId);
                        temporaryOffer.setItemAmount(1);
                        temporaryOffer.setPrice(getGuidePrice(itemId));
                        updateBuyOffer(player, temporaryOffer);
                    });
                } else if (index == 1) { // -1
                    if (temporaryOffer.getItemAmount() <= 1) {
                        return;
                    }
                    temporaryOffer.setItemAmount(temporaryOffer.getItemAmount() - 1);
                    updateOffer(player, temporaryOffer);
                } else if (index == 2 || index == 3) { // + 1
                    if (temporaryOffer.getItemAmount() >= Integer.MAX_VALUE) {
                        return;
                    }
                    if (temporaryOffer.getType() == GrandExchangeOfferType.SELLING && player.getInventory().getAmount(temporaryOffer.getItemId()) > temporaryOffer.getItemAmount()) {
                        temporaryOffer.setItemAmount(temporaryOffer.getItemAmount() + 1);
                    } else if (temporaryOffer.getType() == GrandExchangeOfferType.BUYING) {
                        temporaryOffer.setItemAmount(temporaryOffer.getItemAmount() + 1);
                    }
                    updateOffer(player, temporaryOffer);
                } else if (index == 4) { // +10
                    if (temporaryOffer.getItemAmount() >= Integer.MAX_VALUE) {
                        return;
                    }
                    if (temporaryOffer.getType() == GrandExchangeOfferType.SELLING && player.getInventory().getAmount(temporaryOffer.getItemId()) > temporaryOffer.getItemAmount()) {
                        temporaryOffer.setItemAmount(temporaryOffer.getItemAmount() + 10);
                    } else if (temporaryOffer.getType() == GrandExchangeOfferType.BUYING) {
                        temporaryOffer.setItemAmount(temporaryOffer.getItemAmount() + 10);
                    }
                    updateOffer(player, temporaryOffer);
                } else if (index == 5) { // +100
                    if (temporaryOffer.getItemAmount() >= Integer.MAX_VALUE) {
                        return;
                    }
                    if (temporaryOffer.getType() == GrandExchangeOfferType.SELLING && player.getInventory().getAmount(temporaryOffer.getItemId()) > temporaryOffer.getItemAmount()) {
                        temporaryOffer.setItemAmount(temporaryOffer.getItemAmount() + 100);
                    } else if (temporaryOffer.getType() == GrandExchangeOfferType.BUYING) {
                        temporaryOffer.setItemAmount(temporaryOffer.getItemAmount() + 100);
                    }
                    updateOffer(player, temporaryOffer);
                } else if (index == 6) { // +1k
                    if (temporaryOffer.getItemAmount() >= Integer.MAX_VALUE) {
                        return;
                    }
                    if (temporaryOffer.getType() == GrandExchangeOfferType.SELLING && player.getInventory().getAmount(temporaryOffer.getItemId()) > temporaryOffer.getItemAmount()) {
                        temporaryOffer.setItemAmount(temporaryOffer.getItemAmount() + 1000);
                    } else if (temporaryOffer.getType() == GrandExchangeOfferType.BUYING) {
                        temporaryOffer.setItemAmount(temporaryOffer.getItemAmount() + 1000);
                    }
                    updateOffer(player, temporaryOffer);
                } else if (index == 7) { // Custom amount
                    if (temporaryOffer.getType() == GrandExchangeOfferType.BUYING) {
                        player.integerInput("How many do you wish to buy?", amt -> {
                            temporaryOffer.setItemAmount(Math.max(1, amt));
                            updateOffer(player, temporaryOffer);
                        });
                    } else if (temporaryOffer.getType() == GrandExchangeOfferType.SELLING) {
                        player.integerInput("How many do you wish to sell?", amt -> {
                            temporaryOffer.setItemAmount(Math.min(Math.max(1, amt), player.getInventory().count(temporaryOffer.getItemId(), true)));
                            updateOffer(player, temporaryOffer);
                        });
                    }
                } else if (index == 8) { // -1 coins
                    temporaryOffer.setPrice(Math.max(temporaryOffer.getPrice() - 1, 1));
                    updateOffer(player, temporaryOffer);
                } else if (index == 9) { // +1 coins
                    if (temporaryOffer.getPrice() >= Integer.MAX_VALUE) {
                        return;
                    }
                    temporaryOffer.setPrice(Math.min(Integer.MAX_VALUE, temporaryOffer.getPrice() + 1));
                    updateOffer(player, temporaryOffer);
                } else if (index == 10) { // -5%
                    temporaryOffer.setPrice((int) (temporaryOffer.getPrice() - (((float) temporaryOffer.getPrice() / 100f) * 5)));
                    updateOffer(player, temporaryOffer);
                } else if (index == 11) { // Guide price
                    temporaryOffer.setPrice(getGuidePrice(temporaryOffer.getItemId()));
                    updateOffer(player, temporaryOffer);
                } else if (index == 12) { // Custom amount (coins)
                    player.integerInput("Set a price for each item:", input -> {
                        temporaryOffer.setPrice(Math.max(1, input));
                        updateOffer(player, temporaryOffer);
                    });
                } else if (index == 13) { // +5%
                    temporaryOffer.setPrice((int) (temporaryOffer.getPrice() + (((float) temporaryOffer.getPrice() / 100f) * 5)));
                    updateOffer(player, temporaryOffer);
                }
            };

            h.actions[6] = (DefaultAction) (player, option, slot, itemId) -> collectAllItems(player, option);

            h.actions[27] = (SimpleAction) player -> {
                GrandExchangeOffer offer = getTemporaryOffer(player).orElse(null);
                if (offer == null) {
                    return;
                }

                int cost = Math.multiplyExact(offer.getItemAmount(), offer.getPrice());
                if (offer.getType() == GrandExchangeOfferType.BUYING) {
                    if (player.getInventory().getAmount(995) < cost) {
                        player.sendMessage("That offer costs " + NumberUtils.formatNumber(cost) + " coins. You haven't got enough.");
                        return;
                    }
                    player.getInventory().remove(995, cost);
                }


                if (offer.getType() == GrandExchangeOfferType.SELLING) {
                    if (player.getInventory().count(offer.getItemId(), true)  < offer.getItemAmount()) {
                        return;
                    }
                    player.getInventory().remove(offer.getItemId(), offer.getItemAmount(), true);
                }

                player.remove("ge_temporary_offer");
                GrandExchangeData.getOffers().add(offer);
                GrandExchangeData.dumpDatabase = true;
                open(player);

                World.startEvent(event -> {
                    event.delay(2);
                    exchange();
                });
            };

            h.actions[3] = (SimpleAction) GrandExchange::openHistory;

            h.actions[4] = (SimpleAction) player -> {
                player.getPacketSender().sendVarp(1043, 0);
                player.getPacketSender().sendVarp(563, 0);
                player.getPacketSender().sendVarp(375, 0);
                player.getPacketSender().sendVarp(1151, -1);
                player.getPacketSender().sendString(MAIN_INTERFACE, 26, "");
                player.getPacketSender().sendString(MAIN_INTERFACE, 25, "");
                player.openInterface(InterfaceType.INVENTORY, MAIN_INVENTORY_INTERFACE);
                player.closeChatbox(false);
                player.remove("ge_temporary_offer");
            };

            h.actions[22] = (SimpleAction) player -> {
                getTemporaryOffer(player).ifPresent(offer -> {
                    GrandExchangeOffer persistedOffer = GrandExchangeData.getOffer(player, offer.getSlot());
                    if (persistedOffer == null) {
                        return;
                    }
                    abort(player, offer).ifPresent(abortedOffer -> viewOffer(player, abortedOffer));
                });
            };
        });

        InterfaceHandler.register(MAIN_INVENTORY_INTERFACE, h -> {
            h.actions[0] = (DefaultAction) (player, option, slot, itemId) -> {
                if (itemId == 995) {
                    player.sendMessage("This item cannot be sold.");
                    return;
                }
                if (ItemDef.get(itemId) != null && !ItemDef.get(itemId).tradeable) {
                    player.sendMessage("You can't trade that item on the Grand Exchange.");
                    return;
                }
                int itemAmount = player.getInventory().getAmount(itemId);
                if (ItemDef.get(itemId).isNote()) {
                    //itemId -= 1;
                    itemId = ItemDef.get(itemId).notedId;
                }
                GrandExchangeOfferSlot geSlot;
                if (getTemporaryOffer(player).isPresent()) {
                    geSlot = getTemporaryOffer(player).get().getSlot();
                } else {
                    if (!getFreeSlot(player).isPresent()) {
                        player.sendMessage("Try freeing up one of your offers before doing this!");
                        return;
                    }
                    geSlot = getFreeSlot(player).get();
                }

                GrandExchangeOffer temporaryOffer = createOffer(player, GrandExchangeOfferType.SELLING, geSlot, itemId, itemAmount);
                updateSellOffer(player, temporaryOffer);
            };
        });

        InterfaceHandler.register(COLLECTION_BOX_INTERFACE, h -> {
            h.actions[3] = (SimpleAction) player -> collectAllItems(player, 1);
            h.actions[4] = (SimpleAction) player -> collectAllItems(player, 2);

            for (GrandExchangeOfferSlot slot : GrandExchangeOfferSlot.SLOTS) {
                h.actions[slot.getButtonId() - 2] = (DefaultAction) (player, option, index, itemId) -> {
                    collect(player, GrandExchangeData.getOffer(player, slot), itemId, option);
                };
            }
        });

        InterfaceHandler.register(TRADE_HISTORY_INTERFACE, h -> {
            h.actions[2] = (SimpleAction) GrandExchange::open;
        });

        ObjectAction.register(10061, "exchange", (player, npc1) -> open(player));
        ObjectAction.register(10061, "collect", (player, npc1) -> openCollectionBox(player));

        LoginListener.register(listener -> {
            for (GrandExchangeOffer offer : GrandExchangeData.getOffers(listener.player)) {
                if (offer.getItemId() == 65535) {
                    GrandExchangeData.getOffers().remove(offer);
                    GrandExchangeData.dumpDatabase = true;
                    return;
                }
                if (offer.getType().isFinished()) {
                    listener.player.sendMessage("<col=006000>Grand Exchange: Finished " + offer.getType().getFormatted() + " " + offer.getItemAmount() + " x " + ItemDef.get(offer.getItemId()).name + ".");
                }
            }
        });

        for (int id : CLERKS) {
            NPCAction.register(id, "history", (player, npc1) -> open(player));
            NPCAction.register(id, "talk-to", (player, npc1) -> open(player));
            NPCAction.register(id, "exchange", (player, npc1) -> open(player));
            NPCAction.register(id, "sets", (player, npc1) -> open(player));
        }
    }

}
