package io.ruin.model.activities.lastmanstanding;

import io.ruin.model.World;
import io.ruin.model.entity.npc.NPCAction;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.shared.listeners.SpawnListener;
import io.ruin.model.inter.InterfaceType;
import io.ruin.model.inter.dialogue.NPCDialogue;
import io.ruin.model.inter.dialogue.OptionsDialogue;
import io.ruin.model.inter.utils.Option;
import io.ruin.model.map.Bounds;
import io.ruin.model.map.MapListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Andys1814
 */
public final class LastManStandingPregame {

    private static final int LISA = 7317;

    private static final Bounds LOBBY_ENCLOSURE = new Bounds(3411, 3181, 3416, 3186, 0);

    private static final Set<Player> lobby = new HashSet<>();

    private static final int MINIMUM_PLAYERS = 10;

    private static final int MAXIMUM_PLAYERS = 24;

    private static void join(Player player) {
        if (!player.getInventory().isEmpty() || !player.getEquipment().isEmpty()) {
            player.sendMessage("Make sure to empty your inventory and equipment before trying to join a game!");
            return;
        }

        player.getMovement().teleport(LOBBY_ENCLOSURE.randomPosition());
    }

    static {
        /* Allows players to reach her from other side of the wall. */
        SpawnListener.register(LISA, npc -> npc.skipReachCheck = p -> p.equals(3407, 3180));

        NPCAction.register(LISA, "talk-to", (player, npc) -> {
            player.dialogue(
                    new NPCDialogue(LISA, "Welcome, are you looking to take part in a game of Last Man Standing?"),
                    new OptionsDialogue(
                            new Option("Join Last Man Standing.", () -> {
                                join(player);
                            }),
                            new Option("Learn more.", () -> {
                                // TODO link to forums
                            }),
                            new Option("Not right now.")
                    ));
        });

        NPCAction.register(LISA, "join", (player, npc) -> join(player));

        MapListener.registerBounds(LOBBY_ENCLOSURE).onEnter(player -> {
            if (!lobby.contains(player)) { // Should never happen.
                lobby.add(player);
            }
            player.openInterface(InterfaceType.PRIMARY_OVERLAY, 333);
            player.getPacketSender().sendString(333, 7, "N/A");
            player.getPacketSender().sendString(333, 9, "N/A");
            player.getPacketSender().sendString(333, 11, "Standard");
        }).onExit((player, logout) -> {
            lobby.remove(player);
            player.closeInterface(InterfaceType.PRIMARY_OVERLAY);
        });

        World.startEvent(event -> {
            while (true) {
                event.delay(100);
                if (lobby.size() >= 1) {
                    List<Player> gamePlayers = lobby.stream().limit(MAXIMUM_PLAYERS).collect(Collectors.toList());
                    LastManStandingGame game =  new LastManStandingGame(gamePlayers);
                    game.start();
                }
            }
        });
    }
}
