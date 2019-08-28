package io.ruin.model.activities.grandexchange;

/**
 * @author Andys1814
 */
public final class GrandExchangeOffer {

    private int ownerId;

    private GrandExchangeOfferSlot slot;

    private GrandExchangeOfferType type;

    private int itemId;

    private int itemAmount;

    private int price; // Per-unit price

    private int fulfilled;

    private int claimed;

    private int remainingGold;

    public GrandExchangeOffer(int ownerId, GrandExchangeOfferSlot slot, GrandExchangeOfferType type, int itemId, int itemAmount, int price) {
        this.ownerId = ownerId;
        this.slot = slot;
        this.type = type;
        this.itemId = itemId;
        this.itemAmount = itemAmount;
        this.price = price;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public GrandExchangeOfferSlot getSlot() {
        return slot;
    }

    public GrandExchangeOfferType getType() {
        return type;
    }

    public void setType(GrandExchangeOfferType type) {
        this.type = type;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getItemAmount() {
        return itemAmount;
    }

    public void setItemAmount(int itemAmount) {
        this.itemAmount = itemAmount;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getFulfilled() {
        return fulfilled;
    }

    public void setFulfilled(int fulfilled) {
        this.fulfilled = fulfilled;
    }

    public int getClaimed() {
        return claimed;
    }

    public void setClaimed(int claimed) {
        this.claimed = claimed;
    }

    public int getRemainingGold() {
        return remainingGold;
    }

    public void setRemainingGold(int remainingGold) {
        this.remainingGold = remainingGold;
    }

    @Override
    public String toString() {
        return "GrandExchangeOffer[ownerId=" + ownerId + ", slot=" + slot + ", type=" + type + ", itemId= " + itemId + ", itemAmount=" + itemAmount + ", price=" + price + "]";
    }
}
