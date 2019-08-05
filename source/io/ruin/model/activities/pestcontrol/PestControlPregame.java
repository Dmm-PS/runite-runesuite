package io.ruin.model.activities.pestcontrol;

import io.ruin.model.World;
import io.ruin.model.entity.npc.NPCAction;
import io.ruin.model.inter.InterfaceType;
import io.ruin.model.inter.dialogue.MessageDialogue;
import io.ruin.model.map.MapListener;
import io.ruin.model.map.Position;
import io.ruin.model.map.object.actions.ObjectAction;

import static io.ruin.model.activities.pestcontrol.PestControlLanderDifficulty.*;

/**
 * A class that manages pregame Pest Control functionality (wait lobby, lander boarding, etc.).
 *
 * @author Andys1814
 */
public final class PestControlPregame {

    public static boolean enabled = true;

    private static final PestControlLander noviceBoat = new PestControlLander(NOVICE);

    /* Currently disabled */
    private static final PestControlLander intermediateBoat = new PestControlLander(INTERMEDIATE);

    /* Currently disabled */
    private static final PestControlLander veteranBoat = new PestControlLander(VETERAN);

    static {
        /* Lobby timer */
        World.startEvent(event -> {
            while (true) {
                event.delay(100);

                noviceBoat.decrementTimer();
                if (noviceBoat.getMinutesUntilDeparture() == 0 || noviceBoat.size() >= 3) {
                    noviceBoat.attemptStart();
                } else {
                    noviceBoat.refreshDepartureString();
                }

                intermediateBoat.decrementTimer();
                if (intermediateBoat.getMinutesUntilDeparture() == 0 || intermediateBoat.size() >= 3) {
                    intermediateBoat.attemptStart();
                } else {
                    intermediateBoat.refreshDepartureString();
                }

                veteranBoat.decrementTimer();
                if (veteranBoat.getMinutesUntilDeparture() == 0 || intermediateBoat.size() >= 3) {
                    veteranBoat.attemptStart();
                } else {
                    veteranBoat.refreshDepartureString();
                }
            }
        });

        /* Exiting the lander */
        ObjectAction.register(14314, "climb", (player, obj) -> {
            player.getMovement().teleport(new Position(2657, 2639, 0));
        });

        /* Boarding the lander */
        for (PestControlLanderDifficulty landerDifficulty : PestControlLanderDifficulty.values()) {
            ObjectAction.register(landerDifficulty.getBoardObjectId(), "cross", (player, obj) -> {
                if (!enabled) {
                    player.dialogue(new MessageDialogue("Pest Control is currently under maintenance"));
                    return;
                }
                landerDifficulty.board(player);
            });
        }

        /* Enter/leave listener */
        MapListener.registerBounds(PestControlLanderDifficulty.NOVICE.getBounds()).onEnter(player -> {
            noviceBoat.addPlayer(player);
            player.openInterface(InterfaceType.PRIMARY_OVERLAY, 407);
            player.getPacketSender().sendString(407, 21, "Novice");
            noviceBoat.refreshPlayerString();
            noviceBoat.refreshDepartureString();
            player.getPacketSender().sendString(407, 6, "Points: " + player.voidKnightCommendationPoints);
        }).onExit((player, logout) -> {
            player.pestControlParticipation = 0;
            if (!player.isVisibleInterface(408)) {
                player.closeInterface(InterfaceType.PRIMARY_OVERLAY);
            }
            noviceBoat.removePlayer(player);
            noviceBoat.refreshPlayerString();
        });

        NPCAction.register(1755, "talk-to", (player, npc) -> player.dialogue(new MessageDialogue("You currently have " + player.voidKnightCommendationPoints + " Void Knight commendation points.")));
    }
}
