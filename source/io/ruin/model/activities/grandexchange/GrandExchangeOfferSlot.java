package io.ruin.model.activities.grandexchange;

/**
 * @author Andys1814
 */
public enum GrandExchangeOfferSlot {
    SLOT_1(7, 24, 63784, 518),
    SLOT_2(8, 40, 63783, 519),
    SLOT_3(9, 56, 63782, 520),
    SLOT_4(10, 72, 63781, 521),
    SLOT_5(11, 88, 63780, 522),
    SLOT_6(12, 104, 63779, 523),
    SLOT_7(13, 120, 63763, 539),
    SLOT_8(14, 136, 63762, 540);

    private final int buttonId;
    private final int slotId;
    private final int itemSlotChild;
    private final int itemSlotType;

    public static final GrandExchangeOfferSlot[] SLOTS = values();

    GrandExchangeOfferSlot(int buttonId, int slotId, int itemSlotChild, int itemSlotType) {
        this.buttonId = buttonId;
        this.slotId = slotId;
        this.itemSlotChild = itemSlotChild;
        this.itemSlotType = itemSlotType;
    }

    public int getButtonId() {
        return buttonId;
    }

    public int getSlotId() {
        return slotId;
    }

    public int getItemSlotChild() {
        return itemSlotChild;
    }

    public int getItemSlotType() {
        return itemSlotType;
    }
}
