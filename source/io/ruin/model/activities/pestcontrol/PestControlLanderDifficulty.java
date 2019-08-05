package io.ruin.model.activities.pestcontrol;

import io.ruin.model.entity.player.Player;
import io.ruin.model.map.Bounds;
import io.ruin.model.map.Position;

/**
 * An enumerated type whose elements represent possible Pest Control lander difficulties. For now, the Novice lander
 * is the only implemented and functional one.
 *
 * @author Andys1814
 */
public enum PestControlLanderDifficulty {
    NOVICE(14315, 40, 200, 2,
            new Position(2661, 2639, 0),
            new Bounds(2660, 2638, 2663, 2643, 0)),

    /* Disabled, nobody uses these anyways */
    INTERMEDIATE(25631, 70, 250, 3,
            new Position(0, 0, 0),
            new Bounds(0, 0, 0, 0, 0)),

    VETERAN(25632, 100, 250, 4,
            new Position(0, 0, 0),
            new Bounds(0, 0, 0, 0, 0));

    private final int boardObjectId;
    private final int combatLevelRequirement;
    private final int portalHitpoints;
    private final int pointsPerWin;

    private final Position boardPosition;
    private final Bounds bounds;

    PestControlLanderDifficulty(int boardObjectId, int combatLevelRequirement, int portalHitpoints, int pointsPerWin, Position boardPosition, Bounds bounds) {
        this.boardObjectId = boardObjectId;
        this.combatLevelRequirement = combatLevelRequirement;
        this.portalHitpoints = portalHitpoints;
        this.pointsPerWin = pointsPerWin;
        this.boardPosition = boardPosition;
        this.bounds = bounds;
    }

    public void board(Player player) {
        if (this == INTERMEDIATE || this == VETERAN) {
            player.sendMessage("This boat is not yet enabled, use Novice instead!");
            return;
        }
        if (player.getCombat().getLevel() < combatLevelRequirement) {
            player.sendMessage("You need a combat level of at least " + combatLevelRequirement + " to board this boat!");
            return;
        }
        player.getMovement().teleport(boardPosition);
        player.sendMessage("You board the lander.");
    }

    public int getBoardObjectId() {
        return boardObjectId;
    }

    public int getPortalHitpoints() {
        return portalHitpoints;
    }

    public int getPointsPerWin() {
        return pointsPerWin;
    }

    public Bounds getBounds() {
        return bounds;
    }

}
