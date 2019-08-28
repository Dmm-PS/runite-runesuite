package io.ruin.model.achievements.listeners.experienced;

import io.ruin.api.utils.NumberUtils;
import io.ruin.model.achievements.Achievement;
import io.ruin.model.achievements.AchievementListener;
import io.ruin.model.achievements.AchievementStage;
import io.ruin.model.entity.npc.NPC;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.player.PlayerCounter;

public final class DeadOrAlive implements AchievementListener {

    @Override
    public String name() {
        return "Dead or Alive";
    }

    @Override
    public AchievementStage stage(Player player) {
        int amount = PlayerCounter.UNDEAD_MONSTERS_SLAIN.get(player);
        if (amount == 0) {
            return AchievementStage.NOT_STARTED;
        } else if (amount < 225) {
            return AchievementStage.STARTED;
        }

        return AchievementStage.FINISHED;
    }

    @Override
    public String[] lines(Player player, boolean finished) {
        return new String[]{
                Achievement.slashIf("MASTER the undead by killing enough of them", finished),
                "",
                Achievement.slashIf("<col=000080>Assignment</col>: Slay 225 undead monsters", finished),
                Achievement.slashIf("<col=000080>Reward</col>: Ability to purchase and wield the Salve Amulet", finished),
                "",
                Achievement.slashIf("<col=000080>Undead monsters slain</col>: " + NumberUtils.formatNumber(PlayerCounter.UNDEAD_MONSTERS_SLAIN.get(player)), finished),
        };
    }

    @Override
    public void started(Player player) {

    }

    @Override
    public void finished(Player player) {

    }

    public static void check(Player player, NPC killed) {
        if (killed.getDef().undead) {
            PlayerCounter.UNDEAD_MONSTERS_SLAIN.increment(player, 1);
        }
    }

}