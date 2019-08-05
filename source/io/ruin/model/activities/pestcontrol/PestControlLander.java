package io.ruin.model.activities.pestcontrol;

import io.ruin.model.entity.player.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a pest control lander. Three objects of this class will be created, one for each lander difficulty.
 *
 * @author Andys1814
 */
final class PestControlLander {

    private static final int MINIMUM_PLAYERS = 1;

    private static final int MAXIMUM_PLAYERS = 25;

    private final LinkedList<Player> players;

    private PestControlLanderDifficulty landerDifficulty;

    private int minutesUntilDeparture;

    PestControlLander(PestControlLanderDifficulty landerDifficulty) {
        this.landerDifficulty = landerDifficulty;
        players = new LinkedList<>();
        minutesUntilDeparture = 1;
    }

    void addPlayer(Player player) {
        if (players.contains(player)) { // Should probably never happen
            return;
        }
        players.add(player);
    }

    void removePlayer(Player player) {
        if (!players.contains(player)) { // Should probably never happen
            return;
        }
        players.remove(player);
    }

    void refreshPlayerString() {
        players.forEach(player -> player.getPacketSender().sendString(407, 5, "Players Ready: " + players.size()));
    }

    void refreshDepartureString() {
        players.forEach(player -> player.getPacketSender().sendString(407, 4,  "Next Departure: " + minutesUntilDeparture + " min"));
    }

    void attemptStart() {
        if (!PestControlPregame.enabled) {
            players.forEach(player -> player.sendMessage("Pest Control is currently disabled, try coming back later!"));
            return;
        }
        if (players.size() < MINIMUM_PLAYERS) {
            players.forEach(player -> player.sendMessage("Skipping this departure; not enough players!"));
            minutesUntilDeparture = 5;
            refreshDepartureString();
            return;
        }

        /* Only take the first 25 players, at max */
        List<Player> gamePlayers = players.stream().limit(MAXIMUM_PLAYERS).collect(Collectors.toList());
        PestControlGame game =  new PestControlGame(gamePlayers, landerDifficulty);

        game.start();
        minutesUntilDeparture = 5;
    }

    public void decrementTimer() {
        minutesUntilDeparture--;
    }

    public int getMinutesUntilDeparture() {
        return minutesUntilDeparture;
    }

    public int size() {
        return players.size();
    }
}
