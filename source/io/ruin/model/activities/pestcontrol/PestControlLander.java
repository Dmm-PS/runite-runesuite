package io.ruin.model.activities.pestcontrol;

import io.ruin.model.World;
import io.ruin.model.entity.npc.NPCAction;
import io.ruin.model.entity.player.Player;
import io.ruin.model.inter.InterfaceType;
import io.ruin.model.inter.dialogue.MessageDialogue;
import io.ruin.model.map.Bounds;
import io.ruin.model.map.MapListener;
import io.ruin.model.map.Position;
import io.ruin.model.map.object.actions.ObjectAction;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An enumerated type whose elements represent possible Pest Control lander difficulties. For now, the Novice lander
 * is the only implemented and functional one.
 *
 * @author Andys1814
 */
public enum PestControlLander {
    NOVICE("Novice", 14315, 40, 200, 2,
            new Position(2661, 2639, 0),
            new Bounds(2660, 2638, 2663, 2643, 0)),

    INTERMEDIATE("Intermediate", 25631, 70, 250, 3,
            new Position(0, 0, 0),
            new Bounds(0, 0, 0, 0, 0)),

    VETERAN("Veteran", 25632, 100, 250, 4,
            new Position(0, 0, 0),
            new Bounds(0, 0, 0, 0, 0));

    private final String name;
    private final int gangplankId;
    private final int combatLevelRequirement;
    public final int portalHitpoints;
    public final int pointsPerWin;

    private final Position boardPosition;
    private final Bounds bounds;

    private final List<Player> players = new LinkedList<>();
    private int minutesUntilDeparture = 5;

    private static final int MINIMUM_PLAYERS = 3;
    private static final int MAXIMUM_PLAYERS = 25;

    private static final PestControlLander[] LANDERS = values();

    PestControlLander(String name, int gangplankId, int combatLevelRequirement, int portalHitpoints, int pointsPerWin, Position boardPosition, Bounds bounds) {
        this.name = name;
        this.gangplankId = gangplankId;
        this.combatLevelRequirement = combatLevelRequirement;
        this.portalHitpoints = portalHitpoints;
        this.pointsPerWin = pointsPerWin;
        this.boardPosition = boardPosition;
        this.bounds = bounds;
    }

    private void board(Player player) {
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

    private void refreshPlayerString() {
        players.forEach(player -> player.getPacketSender().sendString(407, 5, "Players Ready: " + players.size()));
    }

    private void refreshDepartureString() {
        players.forEach(player -> player.getPacketSender().sendString(407, 4,  "Next Departure: " + minutesUntilDeparture + " min"));
    }

    private void startGame() {
        if (players.size() < MINIMUM_PLAYERS) {
            players.forEach(player -> player.sendMessage("Skipping this departure; not enough players!"));
            minutesUntilDeparture = 5;
            refreshDepartureString();
            return;
        }

        /* Only take the first 25 players, at max */
        List<Player> gamePlayers = players.stream().limit(MAXIMUM_PLAYERS).collect(Collectors.toList());
        PestControlGame game =  new PestControlGame(gamePlayers, this);
        game.start();
        minutesUntilDeparture = 5;
    }

    static {
        for (PestControlLander lander : LANDERS) {
            ObjectAction.register(lander.gangplankId, "cross", (player, obj) -> {
                lander.board(player);
            });

            ObjectAction.register(14314, "climb", (player, obj) -> {
                player.getMovement().teleport(new Position(2657, 2639, 0));
            });

            MapListener.registerBounds(lander.bounds).onEnter(player -> {
                player.openInterface(InterfaceType.PRIMARY_OVERLAY, 407);
                player.getPacketSender().sendString(407, 21, lander.name);
                player.getPacketSender().sendString(407, 6, "Points: " + player.voidKnightCommendationPoints);
                lander.players.add(player);
                lander.refreshPlayerString();
            }).onExit((player, logout) -> {
                player.pestControlParticipation = 0;
                if (!player.isVisibleInterface(408)) {
                    player.closeInterface(InterfaceType.PRIMARY_OVERLAY);
                }
                lander.players.remove(player);
                lander.refreshPlayerString();
            });
        }

        World.startEvent(event -> {
            while (true) {
                event.delay(100);
                for (PestControlLander lander : LANDERS) {
                    lander.minutesUntilDeparture--;
                    if (lander.minutesUntilDeparture == 0 || lander.players.size() >= MINIMUM_PLAYERS) {
                        lander.startGame();
                    } else {
                        lander.refreshDepartureString();
                    }
                }
            }
        });

        NPCAction.register(1755, "talk-to", (player, npc) -> player.dialogue(new MessageDialogue("You currently have " + player.voidKnightCommendationPoints + " Void Knight commendation points.")));
    }

}
