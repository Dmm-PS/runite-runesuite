package io.ruin.model.achievements.listeners.intro;

import io.ruin.api.utils.NumberUtils;
import io.ruin.model.achievements.Achievement;
import io.ruin.model.achievements.AchievementListener;
import io.ruin.model.achievements.AchievementStage;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.player.PlayerCounter;

import static io.ruin.model.achievements.Achievement.counterStage;

/**
 * @author Andys1814
 */
public final class TournamentParticipant implements AchievementListener {

    @Override
    public String name() {
        return "Tournament Participant";
    }

    @Override
    public AchievementStage stage(Player player) {
        return counterStage(PlayerCounter.TOURNAMENT_PARTICIPATION.get(player), 0, 1);
    }

    @Override
    public String[] lines(Player player, boolean finished) {
        return new String[]{
                Achievement.slashIf("Tournaments are continuously hosted every 6 hours, and are a great", finished),
                Achievement.slashIf("way to practice your PvP combat skills, interact with the community, and earn great rewards!", finished),
                "",
                Achievement.slashIf("<col=000080>Assignment</col>: Participate in a tournament.", finished),
                Achievement.slashIf("<col=000080>Reward</col>: Ability to purchase and equip the <col=c1a900>Champion</col> title.", finished),
                "",
                "<col=000080>Tournament participations: <col=800000>" + NumberUtils.formatNumber(player.tournamentWins),
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
