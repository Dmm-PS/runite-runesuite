package io.ruin.model.activities.tournamentv2.map;

public enum TournamentMapIdentifier {

    ROUND_ONE(128),
    ROUND_TWO(64),
    ROUND_THREE(32),
    ROUND_FOUR(16),
    QUARTER_FINALS(8),
    SEMI_FINALS(4),
    FINALS(2);

    public final int playerSize;

    public static final TournamentMapIdentifier[] VALUES = values();

    TournamentMapIdentifier(int playerSize) {
        this.playerSize = playerSize;
    }
}
