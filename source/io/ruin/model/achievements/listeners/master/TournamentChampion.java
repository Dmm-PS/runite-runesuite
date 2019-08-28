package io.ruin.model.achievements.listeners.master;

import io.ruin.api.utils.NumberUtils;
import io.ruin.model.achievements.Achievement;
import io.ruin.model.achievements.AchievementListener;
import io.ruin.model.achievements.AchievementStage;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.player.PlayerCounter;

import java.util.Arrays;
import java.util.List;

import static io.ruin.model.entity.player.PlayerCounter.*;

/**
 * @author Andys1814
 */
public final class TournamentChampion implements AchievementListener {

    @Override
    public String name() {
        return "Tournament Champion";
    }

    @Override
    public AchievementStage stage(Player player) {
        int amount = player.tournamentWins;
        if (amount >= 10) {
            return AchievementStage.FINISHED;
        } else if (amount > 0) {
            return AchievementStage.STARTED;
        }
        return AchievementStage.NOT_STARTED;
    }

    @Override
    public String[] lines(Player player, boolean finished) {
        return new String[]{
                Achievement.slashIf("A task for only the bravest of combatants", finished),
                "",
                Achievement.slashIf("<col=000080>Assignment</col>: Place first in 10 tournaments.", finished),
                Achievement.slashIf("<col=000080>Reward</col>: Ability to purchase and equip the <col=c1a900>Champion</col> title.", finished),
                "",
                "<col=000080>Tournaments won: <col=800000>" + NumberUtils.formatNumber(player.tournamentWins),
        };
    }

    @Override
    public void started(Player player) {
        player.sendMessage("<col=000080>You have started the achievement: <col=800000>" + name());
    }

    @Override
    public void finished(Player player) {
        player.sendMessage("<col=000080>You have completed the achievement: <col=800000>" + name());
    }

}
