package io.ruin.model.activities.pestcontrol.pests;

import io.ruin.api.utils.Random;

public enum PestType {

    BRAWLER(1734, 1737),

    DEFILER(1724, 1733),

    RAVAGER(1704, 1708),

    SHIFTER(1694, 1703),

    SPINNER(1709, 1713),

    SPLATTER(1689, 1693),

    TORCHER(1714, 1723);

    private final int min;
    private final int max;

    PestType(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    public static final PestType[] VALUES = values();

    public int random() {
        return Random.get(min, max);
    }

}
