package io.ruin.model.activities.tournamentv2.map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.ruin.model.entity.player.PlayerAction;
import io.ruin.model.map.Bounds;
import io.ruin.model.map.MapListener;
import io.ruin.model.map.Position;
import java.util.List;

public enum TournamentMap {

    /**
     * North ruins
     */
    NORTH_RUINS_1(new Position(3629, 9090, 0), 7, TournamentMapIdentifier.QUARTER_FINALS),
    NORTH_RUINS_2(new Bounds(3640, 9089, 3647, 9095, 0), TournamentMapIdentifier.SEMI_FINALS),
    NORTH_RUINS_3(new Bounds(3648, 9089, 3655, 9095, 0), TournamentMapIdentifier.SEMI_FINALS),
    NORTH_RUINS_4(new Position(3660, 9090, 0), 7, TournamentMapIdentifier.QUARTER_FINALS),
    NORTH_RUINS_5(new Position(3639, 9100, 0), 7, TournamentMapIdentifier.QUARTER_FINALS),
    NORTH_RUINS_6(new Position(3650, 9100, 0), 7, TournamentMapIdentifier.QUARTER_FINALS),

    /**
     * North west castle
     */
    NORTH_WEST_CASTLE_FIRST_FLOOR_1(new Position(3625, 9124, 0), 6, TournamentMapIdentifier.ROUND_THREE),
    NORTH_WEST_CASTLE_FIRST_FLOOR_2(new Position(3635, 9124, 0), 6, TournamentMapIdentifier.ROUND_THREE),
    NORTH_WEST_CASTLE_FIRST_FLOOR_3(new Position(3625, 9134, 0), 6, TournamentMapIdentifier.ROUND_THREE),
    NORTH_WEST_CASTLE_FIRST_FLOOR_4(new Position(3635, 9134, 0), 6, TournamentMapIdentifier.ROUND_THREE),

    NORTH_WEST_CASTLE_SECOND_FLOOR_1(new Position(3630, 9115, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    NORTH_WEST_CASTLE_SECOND_FLOOR_2(new Position(3623, 9123, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    NORTH_WEST_CASTLE_SECOND_FLOOR_3(new Position(3630, 9123, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    NORTH_WEST_CASTLE_SECOND_FLOOR_4(new Position(3637, 9123, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    NORTH_WEST_CASTLE_SECOND_FLOOR_5(new Position(3623, 9135, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    NORTH_WEST_CASTLE_SECOND_FLOOR_6(new Position(3630, 9135, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    NORTH_WEST_CASTLE_SECOND_FLOOR_7(new Position(3637, 9135, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    NORTH_WEST_CASTLE_SECOND_FLOOR_8(new Position(3630, 9143, 1), 6, TournamentMapIdentifier.ROUND_TWO),

    NORTH_WEST_CASTLE_THIRD_FLOOR_1(new Position(3622, 9121, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_WEST_CASTLE_THIRD_FLOOR_2(new Position(3627, 9121, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_WEST_CASTLE_THIRD_FLOOR_3(new Position(3634, 9121, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_WEST_CASTLE_THIRD_FLOOR_4(new Position(3639, 9121, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_WEST_CASTLE_THIRD_FLOOR_5(new Position(3622, 9126, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_WEST_CASTLE_THIRD_FLOOR_6(new Position(3627, 9126, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_WEST_CASTLE_THIRD_FLOOR_7(new Position(3634, 9126, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_WEST_CASTLE_THIRD_FLOOR_8(new Position(3639, 9126, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_WEST_CASTLE_THIRD_FLOOR_9(new Position(3622, 9133, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_WEST_CASTLE_THIRD_FLOOR_10(new Position(3627, 9133, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_WEST_CASTLE_THIRD_FLOOR_11(new Position(3634, 9133, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_WEST_CASTLE_THIRD_FLOOR_12(new Position(3639, 9133, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_WEST_CASTLE_THIRD_FLOOR_13(new Position(3622, 9138, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_WEST_CASTLE_THIRD_FLOOR_14(new Position(3627, 9138, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_WEST_CASTLE_THIRD_FLOOR_15(new Position(3634, 9138, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_WEST_CASTLE_THIRD_FLOOR_16(new Position(3639, 9138, 2), 5, TournamentMapIdentifier.ROUND_ONE),

    /**
     * North east castle
     */
    NORTH_EAST_CASTLE_FIRST_FLOOR_1(new Position(3655, 9124, 0), 6, TournamentMapIdentifier.ROUND_THREE),
    NORTH_EAST_CASTLE_FIRST_FLOOR_2(new Position(3665, 9124, 0), 6, TournamentMapIdentifier.ROUND_THREE),
    NORTH_EAST_CASTLE_FIRST_FLOOR_3(new Position(3655, 9134, 0), 6, TournamentMapIdentifier.ROUND_THREE),
    NORTH_EAST_CASTLE_FIRST_FLOOR_4(new Position(3665, 9134, 0), 6, TournamentMapIdentifier.ROUND_THREE),

    NORTH_EAST_CASTLE_SECOND_FLOOR_1(new Position(3660, 9115, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    NORTH_EAST_CASTLE_SECOND_FLOOR_2(new Position(3653, 9123, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    NORTH_EAST_CASTLE_SECOND_FLOOR_3(new Position(3660, 9123, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    NORTH_EAST_CASTLE_SECOND_FLOOR_4(new Position(3667, 9123, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    NORTH_EAST_CASTLE_SECOND_FLOOR_5(new Position(3653, 9135, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    NORTH_EAST_CASTLE_SECOND_FLOOR_6(new Position(3660, 9135, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    NORTH_EAST_CASTLE_SECOND_FLOOR_7(new Position(3667, 9135, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    NORTH_EAST_CASTLE_SECOND_FLOOR_8(new Position(3660, 9143, 1), 6, TournamentMapIdentifier.ROUND_TWO),

    NORTH_EAST_CASTLE_THIRD_FLOOR_1(new Position(3652, 9121, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_EAST_CASTLE_THIRD_FLOOR_2(new Position(3657, 9121, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_EAST_CASTLE_THIRD_FLOOR_3(new Position(3664, 9121, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_EAST_CASTLE_THIRD_FLOOR_4(new Position(3669, 9121, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_EAST_CASTLE_THIRD_FLOOR_5(new Position(3652, 9126, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_EAST_CASTLE_THIRD_FLOOR_6(new Position(3657, 9126, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_EAST_CASTLE_THIRD_FLOOR_7(new Position(3664, 9126, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_EAST_CASTLE_THIRD_FLOOR_8(new Position(3669, 9126, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_EAST_CASTLE_THIRD_FLOOR_9(new Position(3652, 9133, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_EAST_CASTLE_THIRD_FLOOR_10(new Position(3657, 9133, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_EAST_CASTLE_THIRD_FLOOR_11(new Position(3664, 9133, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_EAST_CASTLE_THIRD_FLOOR_12(new Position(3669, 9133, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_EAST_CASTLE_THIRD_FLOOR_13(new Position(3652, 9138, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_EAST_CASTLE_THIRD_FLOOR_14(new Position(3657, 9138, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_EAST_CASTLE_THIRD_FLOOR_15(new Position(3664, 9138, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    NORTH_EAST_CASTLE_THIRD_FLOOR_16(new Position(3669, 9138, 2), 5, TournamentMapIdentifier.ROUND_ONE),

    /**
     * North west ruins
     */
    NORTH_WEST_RUINS_1(new Position(3602, 9117, 0), 7, TournamentMapIdentifier.ROUND_FOUR),
    NORTH_WEST_RUINS_2(new Position(3592, 9127, 0), 7, TournamentMapIdentifier.ROUND_FOUR),
    NORTH_WEST_RUINS_3(new Position(3612, 9127, 0), 7, TournamentMapIdentifier.ROUND_FOUR),
    NORTH_WEST_RUINS_4(new Position(3602, 9137, 0), 7, TournamentMapIdentifier.ROUND_FOUR),

    /**
     * South west castle
     */
    SOUTH_WEST_CASTLE_FIRST_FLOOR_1(new Position(3596, 9095, 0), 6, TournamentMapIdentifier.ROUND_THREE),
    SOUTH_WEST_CASTLE_FIRST_FLOOR_2(new Position(3606, 9095, 0), 6, TournamentMapIdentifier.ROUND_THREE),
    SOUTH_WEST_CASTLE_FIRST_FLOOR_3(new Position(3596, 9105, 0), 6, TournamentMapIdentifier.ROUND_THREE),
    SOUTH_WEST_CASTLE_FIRST_FLOOR_4(new Position(3606, 9105, 0), 6, TournamentMapIdentifier.ROUND_THREE),

    SOUTH_WEST_CASTLE_SECOND_FLOOR_1(new Position(3595, 9093, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    SOUTH_WEST_CASTLE_SECOND_FLOOR_2(new Position(3607, 9093, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    SOUTH_WEST_CASTLE_SECOND_FLOOR_3(new Position(3587, 9100, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    SOUTH_WEST_CASTLE_SECOND_FLOOR_4(new Position(3595, 9100, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    SOUTH_WEST_CASTLE_SECOND_FLOOR_5(new Position(3607, 9100, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    SOUTH_WEST_CASTLE_SECOND_FLOOR_6(new Position(3615, 9100, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    SOUTH_WEST_CASTLE_SECOND_FLOOR_7(new Position(3595, 9107, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    SOUTH_WEST_CASTLE_SECOND_FLOOR_8(new Position(3607, 9107, 1), 6, TournamentMapIdentifier.ROUND_TWO),

    SOUTH_WEST_CASTLE_THIRD_FLOOR_1(new Position(3593, 9092, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_WEST_CASTLE_THIRD_FLOOR_2(new Position(3598, 9092, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_WEST_CASTLE_THIRD_FLOOR_3(new Position(3605, 9092, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_WEST_CASTLE_THIRD_FLOOR_4(new Position(3610, 9092, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_WEST_CASTLE_THIRD_FLOOR_5(new Position(3593, 9097, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_WEST_CASTLE_THIRD_FLOOR_6(new Position(3598, 9097, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_WEST_CASTLE_THIRD_FLOOR_7(new Position(3605, 9097, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_WEST_CASTLE_THIRD_FLOOR_8(new Position(3610, 9097, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_WEST_CASTLE_THIRD_FLOOR_9(new Position(3593, 9104, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_WEST_CASTLE_THIRD_FLOOR_10(new Position(3598, 9104, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_WEST_CASTLE_THIRD_FLOOR_11(new Position(3605, 9104, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_WEST_CASTLE_THIRD_FLOOR_12(new Position(3610, 9104, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_WEST_CASTLE_THIRD_FLOOR_13(new Position(3593, 9109, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_WEST_CASTLE_THIRD_FLOOR_14(new Position(3598, 9109, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_WEST_CASTLE_THIRD_FLOOR_15(new Position(3605, 9109, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_WEST_CASTLE_THIRD_FLOOR_16(new Position(3610, 9109, 2), 5, TournamentMapIdentifier.ROUND_ONE),

    /**
     * South east ruins
     */
    SOUTH_EAST_RUINS_1(new Position(3687, 9117, 0), 7, TournamentMapIdentifier.ROUND_FOUR),
    SOUTH_EAST_RUINS_2(new Position(3677, 9127, 0), 7, TournamentMapIdentifier.ROUND_FOUR),
    SOUTH_EAST_RUINS_3(new Position(3697, 9127, 0), 7, TournamentMapIdentifier.ROUND_FOUR),
    SOUTH_EAST_RUINS_4(new Position(3687, 9137, 0), 7, TournamentMapIdentifier.ROUND_FOUR),

    /**
     * South east castle
     */
    SOUTH_EAST_CASTLE_FIRST_FLOOR_1(new Position(3684, 9095, 0), 6, TournamentMapIdentifier.ROUND_THREE),
    SOUTH_EAST_CASTLE_FIRST_FLOOR_2(new Position(3694, 9095, 0), 6, TournamentMapIdentifier.ROUND_THREE),
    SOUTH_EAST_CASTLE_FIRST_FLOOR_3(new Position(3684, 9105, 0), 6, TournamentMapIdentifier.ROUND_THREE),
    SOUTH_EAST_CASTLE_FIRST_FLOOR_4(new Position(3694, 9105, 0), 6, TournamentMapIdentifier.ROUND_THREE),

    SOUTH_EAST_CASTLE_SECOND_FLOOR_1(new Position(3683, 9093, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    SOUTH_EAST_CASTLE_SECOND_FLOOR_2(new Position(3695, 9093, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    SOUTH_EAST_CASTLE_SECOND_FLOOR_3(new Position(3675, 9100, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    SOUTH_EAST_CASTLE_SECOND_FLOOR_4(new Position(3683, 9100, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    SOUTH_EAST_CASTLE_SECOND_FLOOR_5(new Position(3695, 9100, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    SOUTH_EAST_CASTLE_SECOND_FLOOR_6(new Position(3703, 9100, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    SOUTH_EAST_CASTLE_SECOND_FLOOR_7(new Position(3683, 9107, 1), 6, TournamentMapIdentifier.ROUND_TWO),
    SOUTH_EAST_CASTLE_SECOND_FLOOR_8(new Position(3695, 9107, 1), 6, TournamentMapIdentifier.ROUND_TWO),

    SOUTH_EAST_CASTLE_THIRD_FLOOR_1(new Position(3681, 9092, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_EAST_CASTLE_THIRD_FLOOR_2(new Position(3686, 9092, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_EAST_CASTLE_THIRD_FLOOR_3(new Position(3693, 9092, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_EAST_CASTLE_THIRD_FLOOR_4(new Position(3698, 9092, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_EAST_CASTLE_THIRD_FLOOR_5(new Position(3681, 9097, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_EAST_CASTLE_THIRD_FLOOR_6(new Position(3686, 9097, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_EAST_CASTLE_THIRD_FLOOR_7(new Position(3693, 9097, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_EAST_CASTLE_THIRD_FLOOR_8(new Position(3698, 9097, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_EAST_CASTLE_THIRD_FLOOR_9(new Position(3681, 9104, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_EAST_CASTLE_THIRD_FLOOR_10(new Position(3686, 9104, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_EAST_CASTLE_THIRD_FLOOR_11(new Position(3693, 9104, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_EAST_CASTLE_THIRD_FLOOR_12(new Position(3698, 9104, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_EAST_CASTLE_THIRD_FLOOR_13(new Position(3681, 9109, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_EAST_CASTLE_THIRD_FLOOR_14(new Position(3686, 9109, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_EAST_CASTLE_THIRD_FLOOR_15(new Position(3693, 9109, 2), 5, TournamentMapIdentifier.ROUND_ONE),
    SOUTH_EAST_CASTLE_THIRD_FLOOR_16(new Position(3698, 9109, 2), 5, TournamentMapIdentifier.ROUND_ONE),

    /**
     * Final area
     */
    FINAL_MAP(new Position(3644, 9074, 0), 8, TournamentMapIdentifier.FINALS);

    /**
     * A map of all maps by their identifier.
     */
    private static final Multimap<TournamentMapIdentifier, TournamentMap> MAPS_BY_IDENTIFIER = ArrayListMultimap.create();

    static {
        for (TournamentMap map : values()) {
            MAPS_BY_IDENTIFIER.put(map.mapIdentifier, map);
        }
    }

    /**
     * Get a list of {@code TournamentMap}s by the identifier.
     */
    public static List<TournamentMap> getMapsForIdenitifer(TournamentMapIdentifier identifier) {
        return (List<TournamentMap>) MAPS_BY_IDENTIFIER.get(identifier);
    }

    public final TournamentMapIdentifier mapIdentifier;
    public final Bounds bounds;

    TournamentMap(Position position, int size, TournamentMapIdentifier mapIdentifier) {
        this.mapIdentifier = mapIdentifier;
        this.bounds = new Bounds(position.getX(), position.getY(), position.getX() + (size - 1), position.getY() + size - 1, position.getZ());
    }

    TournamentMap(Bounds bounds, TournamentMapIdentifier mapIdentifier) {
        this.mapIdentifier = mapIdentifier;
        this.bounds = bounds;
    }

}
