package io.ruin.model.achievements;

import io.ruin.cache.Color;
import io.ruin.model.entity.player.Player;

public interface AchievementListener {

    String name();

    AchievementStage stage(Player player);

    String[] lines(Player player, boolean finished);

    void started(Player player);

    void finished(Player player);

    default void rewardCoins(Player player, int amount) {
        if (player.getInventory().hasRoomFor(995)) {
            player.getInventory().add(995, amount);
            player.sendMessage(Color.COOL_BLUE.wrap("You've been rewarded 10,000 gold coins for completing " + name() + "."));
        } else {
            player.getBank().add(995, amount);
            player.sendMessage(Color.COOL_BLUE.wrap("10,000 gold coins have been deposited into your bank for completing " + name() + "."));
        }
    }

}