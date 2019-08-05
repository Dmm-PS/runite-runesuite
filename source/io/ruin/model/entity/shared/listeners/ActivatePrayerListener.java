package io.ruin.model.entity.shared.listeners;

import io.ruin.model.entity.player.Player;

public interface ActivatePrayerListener {

    boolean allow(Player player);

}
