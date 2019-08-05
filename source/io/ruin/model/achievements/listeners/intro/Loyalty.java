package io.ruin.model.achievements.listeners.intro;

import io.ruin.cache.Color;
import io.ruin.model.World;
import io.ruin.model.achievements.Achievement;
import io.ruin.model.achievements.AchievementListener;
import io.ruin.model.achievements.AchievementStage;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.player.PlayerCounter;

import static io.ruin.model.achievements.Achievement.counterStage;

public class Loyalty implements AchievementListener {
    @Override
    public String name() {
        return "Loyalty";
    }

    @Override
    public AchievementStage stage(Player player) {
        return counterStage(PlayerCounter.LOYALTY_CHEST_OPENED.get(player), 0, 1);
    }

    @Override
    public String[] lines(Player player, boolean finished) {
        return new String[]{
                Achievement.slashIf("The loyalty chest can be looted once per day for increasingly", finished),
                Achievement.slashIf("rare rewards. It is located in the building north of the Edgeville", finished),
                Achievement.slashIf("bank, in the south west corner.", finished),
                "",
                Achievement.slashIf("<col=000080>Assignment</col>: Loot the loyalty chest.", finished),
                Achievement.slashIf("<col=000080>Reward</col>: 10,000 coins, claimed from the " + World.type.getWorldName() + " Expert.", finished),
        };
    }

    @Override
    public void started(Player player) {

    }

    @Override
    public void finished(Player player) {
        rewardCoins(player, 10000);
    }

}
