package io.ruin.model.activities.tournament;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public enum TournamentSchedule {

    MONDAY("Main - Dharok", TournamentPreset.DHAROK),
    TUESDAY("Pure - NH Tribrid", TournamentPreset.PURE_NH),
    WEDNESDAY("Main - Whip/GMaul/BGS - No Body/Legs", TournamentPreset.MAIN_NO_ARMOUR),
    THURSDAY("Pure - Range/Granite Maul", TournamentPreset.PURE_RANGE_GRANITE_MAUL),
    FRIDAY("Main - Welfare NH Tribrid", TournamentPreset.MAIN_WELFARE_NH),
    SATURDAY("Main - Mystic/Barrows Hybrid", TournamentPreset.MYSTIC_BARROWS),
    SUNDAY("Main - NH Tribrid", TournamentPreset.MAIN_NH);

    public String presetName;
    public final TournamentPreset preset;
    private static final List<TournamentSchedule> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    TournamentSchedule(String presetName, TournamentPreset preset) {
        this.presetName = presetName;
        this.preset = preset;
    }

    public static TournamentSchedule randomSchedule()  {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }

    public static String timeUntilTournament(long seconds) {
        long hours = TimeUnit.SECONDS.toHours(seconds);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        if(hours == 0 && minutes == 0)
            return "less than 1 minute";
        else
            return (hours >= 1 ? (hours + " hour" + (hours > 1 ? "s" : "") + " and ") : "") +
                Math.max((minutes - TimeUnit.HOURS.toMinutes(hours)), 1) + " minute" +
                ((minutes - TimeUnit.HOURS.toMinutes(hours)) > 1 ? "s" : "");
    }


}
