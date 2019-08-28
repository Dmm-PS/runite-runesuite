package io.ruin.model.activities.grandexchange;

import io.ruin.Server;
import io.ruin.api.utils.FileUtils;
import io.ruin.model.World;
import io.ruin.model.entity.player.Player;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Andys1814
 */
public final class GrandExchangeData {

    private static final List<GrandExchangeOffer> OFFERS = new ArrayList<>();

    public static boolean dumpDatabase = true;

    public static void init() {
        File file = FileUtils.get(Server.dataFolder.getAbsolutePath() + "/ge_offers.runite");
        if (!file.exists()) {
            Server.logWarning("[Grand Exchange] Could not find ge_offers data file!");
            return;
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel c = raf.getChannel()) {
            ByteBuffer b = c.map(FileChannel.MapMode.READ_WRITE, 0, c.size());
            int ownerId;
            while ((ownerId = b.getInt()) != Integer.MIN_VALUE) {
                GrandExchangeOfferSlot slot = GrandExchangeOfferSlot.SLOTS[b.getInt()];
                GrandExchangeOfferType type = GrandExchangeOfferType.TYPES[b.getInt()];
                int itemId = b.getInt();
                int itemAmount = b.getInt();
                int price = b.getInt();
                int fulfilled = b.getInt();
                int claimed = b.getInt();
                int remainingGold = b.getInt();
                GrandExchangeOffer offer = new GrandExchangeOffer(ownerId, slot, type, itemId, itemAmount, price);
                offer.setFulfilled(fulfilled);
                offer.setClaimed(claimed);
                offer.setRemainingGold(remainingGold);
                System.out.println(offer.toString());
                OFFERS.add(offer);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void dump() {
        File file = FileUtils.get(Server.dataFolder.getAbsolutePath() + "/ge_offers.runite");
        ByteBuffer b = ByteBuffer.allocate(50_000_000);
        for (GrandExchangeOffer offer : OFFERS) {
            if (offer == null) {
                continue;
            }

            b.putInt(offer.getOwnerId());
            b.putInt(offer.getSlot().ordinal());
            b.putInt(offer.getType().ordinal());
            b.putInt(offer.getItemId());
            b.putInt(offer.getItemAmount());
            b.putInt(offer.getPrice());
            b.putInt(offer.getFulfilled());
            b.putInt(offer.getClaimed());
            b.putInt(offer.getRemainingGold());
        }
        b.putInt(Integer.MIN_VALUE);
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw"); FileChannel c = raf.getChannel()) {
            b.flip();
            c.write(b);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static List<GrandExchangeOffer> getOffers() {
        return OFFERS;
    }

    public static List<GrandExchangeOffer> getOffers(Player player) {
        return OFFERS.stream().filter(offer -> offer.getOwnerId() == player.getUserId()).collect(Collectors.toList());
    }

    public static GrandExchangeOffer getOffer(Player player, GrandExchangeOfferSlot slot) {
        return OFFERS.stream().filter(offer -> offer.getOwnerId() == player.getUserId() && offer.getSlot() == slot).findFirst().orElse(null);
    }

    public static List<GrandExchangeOffer> getBuyOffers() {
        return OFFERS.stream().filter(offer -> offer.getType() == GrandExchangeOfferType.BUYING).collect(Collectors.toList());
    }

    public static List<GrandExchangeOffer> getSellOffers() {
        return OFFERS.stream().filter(offer -> offer.getType() == GrandExchangeOfferType.SELLING).collect(Collectors.toList());
    }

    public static List<GrandExchangeOffer> getSellOffers(int itemId, int price) {
        return OFFERS.stream().filter(offer -> offer.getType() == GrandExchangeOfferType.SELLING && offer.getItemId() == itemId && offer.getPrice() <= price).collect(Collectors.toList());
    }

    static {
        init();
        World.startEvent(event -> {
            while (true) {
                if (dumpDatabase) {
                    Server.worker.execute(() -> {
                        dump();
                        dumpDatabase = false;
                    });
                }
                event.delay(1);
            }
        });
    }

}
