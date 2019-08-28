package io.ruin.model.achievements;

import io.ruin.cache.Color;
import io.ruin.model.World;
import io.ruin.model.achievements.listeners.experienced.*;
import io.ruin.model.achievements.listeners.intro.CommenceSlaughter;
import io.ruin.model.achievements.listeners.intro.Loyalty;
import io.ruin.model.achievements.listeners.intro.TheBestiary;
import io.ruin.model.achievements.listeners.intro.TournamentParticipant;
import io.ruin.model.achievements.listeners.master.ExpertRunecrafter;
import io.ruin.model.achievements.listeners.master.TournamentChampion;
import io.ruin.model.achievements.listeners.novice.ImplingHunter;
import io.ruin.model.achievements.listeners.novice.IntoTheAbyss;
import io.ruin.model.achievements.listeners.novice.Lightness;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.shared.listeners.LoginListener;
import io.ruin.model.inter.journal.Journal;
import io.ruin.model.inter.journal.JournalEntry;

public enum Achievement {

    /* Intro */
    THE_BESTIARY(new TheBestiary(), AchievementCategory.INTRODUCTORY),
    COMMENCE_SLAUGHTER(new CommenceSlaughter(), AchievementCategory.INTRODUCTORY),
    LOYALTY(new Loyalty(), AchievementCategory.INTRODUCTORY),
    TOURNAMENT_PARTICIPANT(new TournamentParticipant(), AchievementCategory.INTRODUCTORY),
    //PRESETS(new PresetsIntro(), AchievementCategory.INTRODUCTORY),

    /* Novice */
    INTO_THE_ABYSS(new IntoTheAbyss(), AchievementCategory.NOVICE),
    //MORYTANIA_FARMING(new MorytaniaFarming(), AchievementCategory.NOVICE),
    LIGHTNESS(new Lightness(), AchievementCategory.NOVICE),
    IMPLING_HUNTER(new ImplingHunter(), AchievementCategory.NOVICE),

    /* Experienced */
    DOWN_IN_THE_DIRT(new DownInTheDirt(), AchievementCategory.EXPERIENCED),
    ESSENCE_EXTRACTOR(new EssenceExtractor(), AchievementCategory.EXPERIENCED),
    GOLDEN_TOUCH(new GoldenTouch(), AchievementCategory.EXPERIENCED),
    NATURES_TOUCH(new NaturesTouch(), AchievementCategory.EXPERIENCED),
    ABYSSAL_DISTURBANCE(new AbyssalDisturbance(), AchievementCategory.EXPERIENCED),
    PRACTICE_MAKES_PERFECT(new PracticeMakesPerfect(), AchievementCategory.EXPERIENCED),
    QUICK_HANDS(new QuickHands(), AchievementCategory.EXPERIENCED),
    MY_ARMS_PATCH(new MyArmsPatch(), AchievementCategory.EXPERIENCED),
    WELCOME_TO_THE_JUNGLE(new WelcomeToTheJungle(), AchievementCategory.EXPERIENCED),
    DEMON_SLAYER(new DemonSlayer(), AchievementCategory.EXPERIENCED),
    DEAD_OR_ALIVE(new DeadOrAlive(), AchievementCategory.EXPERIENCED),

    /* Master */
    EXPERT_RUNECRAFTER(new ExpertRunecrafter(), AchievementCategory.MASTER),
    TOURNAMENT_CHAMPION(new TournamentChampion(), AchievementCategory.MASTER);

    private final AchievementListener listener;
    private final AchievementCategory category;

    private JournalEntry entry;

    Achievement(AchievementListener listener, AchievementCategory category) {
        this.listener = listener;
        this.category = category;
    }

    public void update(Player player) {
        if (entry == null) {
            //never displayed on this world
            return;
        }
        if (World.isPVP()) {
            return;
        }
        AchievementStage oldStage = player.achievementStages[ordinal()];
        entry.send(player);
        AchievementStage newStage = player.achievementStages[ordinal()];
        if(newStage != oldStage) {
            if(newStage == AchievementStage.STARTED) {
                player.sendMessage("<col=000080>You have started the achievement: <col=800000>" + getListener().name());
                getListener().started(player);
            } else if(newStage == AchievementStage.FINISHED) {
                player.sendMessage("<col=000080>You have completed the achievement: <col=800000>" + getListener().name());
                getListener().finished(player);
            }
        }
    }

    public JournalEntry toEntry() {
        return entry = new JournalEntry() {
            @Override
            public void send(Player player) {
                AchievementStage stage = player.achievementStages[ordinal()] = getListener().stage(player);
                if (player.journal != Journal.ACHIEVEMENTS) {
                    return;
                }
                if(stage == AchievementStage.FINISHED) {
                    send(player, getListener().name(), Color.GREEN);
                } else if(stage == AchievementStage.STARTED) {
                    send(player, getListener().name(), Color.YELLOW);
                } else {
                    send(player, getListener().name(), Color.RED);
                }
            }
            @Override
            public void select(Player player) {
                player.sendScroll("<col=800000>" + getListener().name(), getListener().lines(player, isFinished(player)));
            }
        };
    }

    public boolean isStarted(Player player) {
        return player.achievementStages[ordinal()] != AchievementStage.NOT_STARTED;
    }

    public boolean isFinished(Player player) {
        return player.achievementStages[ordinal()] == AchievementStage.FINISHED;
    }

    public static String slashIf(String string, boolean slash) {
        return slash ? ("<str>" + string + "</str>") : string;
    }

    public AchievementListener getListener() {
        return listener;
    }

    public AchievementCategory getCategory() {
        return category;
    }

    public static AchievementStage counterStage(int current, int start, int finish) {
        if (current >= finish) {
            return AchievementStage.FINISHED;
        } else if (current <= start) {
            return AchievementStage.NOT_STARTED;
        }

        return AchievementStage.STARTED;
    }

    static {
        LoginListener.register(player -> {
            for (Achievement achievement : values()) {
                player.achievementStages[achievement.ordinal()] = achievement.getListener().stage(player); // Forces achievements to have the correct state on login
            }
        });
    }
}