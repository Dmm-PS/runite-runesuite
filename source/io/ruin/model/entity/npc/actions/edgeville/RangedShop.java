package io.ruin.model.entity.npc.actions.edgeville;

import io.ruin.model.entity.shared.listeners.SpawnListener;

public class RangedShop {

    private final static int BOW_AND_ARROW_SALESMAN = 6060;
    private final static int ROWE = 536;

    static {
        SpawnListener.register(BOW_AND_ARROW_SALESMAN, npc -> npc.skipReachCheck = p -> p.equals(3096, 3516));
        SpawnListener.register(ROWE, npc -> npc.skipReachCheck = p -> p.equals(3097,3516));
    }
}
