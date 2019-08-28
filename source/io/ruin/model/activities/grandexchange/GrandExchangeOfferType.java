package io.ruin.model.activities.grandexchange;

/**
 * @author Andys1814
 */
public enum GrandExchangeOfferType {
    BUYING(2, "buying"),
    BUYING_FINISHED(5, "buying"),
    SELLING(10, "selling"),
    SELLING_FINISHED(13, "selling"),
    BUYING_ABORTED(5, "selling"),
    SELLING_ABORTED(13, "selling");

    private final int state;
    private final String formatted;

    public static final GrandExchangeOfferType[] TYPES = values();

    GrandExchangeOfferType(int state, String formatted) {
        this.state = state;
        this.formatted = formatted;
    }

    public int getState() {
        return state;
    }

    public String getFormatted() {
        return formatted;
    }

    public boolean isBuyOffer() {
        return this.equals(BUYING) || this.equals(BUYING_FINISHED);
    }

    public boolean isSellOffer() {
        return this.equals(SELLING) || this.equals(SELLING_FINISHED);
    }

    public boolean isFinished() {
        return this.equals(BUYING_FINISHED) || this.equals(SELLING_FINISHED) || isAborted();
    }

    public boolean isAborted() {
        return this.equals(BUYING_ABORTED) || this.equals(SELLING_ABORTED);
    }

}
