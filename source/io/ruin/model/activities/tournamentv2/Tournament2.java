package io.ruin.model.activities.tournamentv2;

import com.google.common.collect.Lists;
import io.ruin.api.utils.NumberUtils;
import io.ruin.model.World;
import io.ruin.model.activities.tournamentv2.map.TournamentMap;
import io.ruin.model.activities.tournamentv2.map.TournamentMapIdentifier;
import io.ruin.model.combat.Hit;
import io.ruin.model.combat.Killer;
import io.ruin.model.entity.Entity;
import io.ruin.model.entity.npc.NPC;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.player.PlayerAction;
import io.ruin.model.entity.player.ai.AIPlayer;
import io.ruin.model.item.containers.bank.BankActions;
import io.ruin.model.map.Bounds;
import io.ruin.model.map.Direction;
import io.ruin.model.map.Position;
import io.ruin.model.map.Tile;
import io.ruin.model.map.dynamic.DynamicMap;
import io.ruin.model.map.object.GameObject;

import java.util.*;

public class Tournament2 {

    /**
     * The bounds of where the lobby is.
     */
    private static final Bounds LOBBY_BOUNDS = new Bounds(3612, 9073, 3621, 9082, 0);

    /**
     * A reference to the main dynamic map used to host the lobby.
     */
    private final DynamicMap mainMap;

    /**
     * A list of all dynamic maps used to host the tournament.
     */
    private final List<DynamicMap> maps = new ArrayList<>(2);

    /**
     * A list of players waiting for the tournament or next round to start.
     */
    private final List<Player> playersInLobby = new LinkedList<>();

    /**
     * A list of players that are current in an active fight.
     */
    private final List<Player> playersFighting = new LinkedList<>();

    /**
     * A list of players waiting for the next round to start.
     */
    private final Queue<Player> playersWaiting = new LinkedList<>();

    /**
     * The tournaments current state.
     */
    private TournamentState state = TournamentState.WAITING_TO_START;

    /**
     * The current round number.
     */
    private int roundCount = 0;

    public Tournament2() {
        maps.add(mainMap = createMap(true));
    }

    /**
     * Adds the player to the waiting lobby.
     */
    public void join(Player player) {
        if (state == TournamentState.WAITING_TO_START) {
            playersWaiting.add(player);
        } else {
            playersInLobby.add(player);
        }

        player.getMovement().teleport(mainMap.convertX(LOBBY_BOUNDS.randomX()), mainMap.convertY(LOBBY_BOUNDS.randomY()), 0);
        mainMap.assignListener(player).onExit(this::onExit);
    }

    /**
     * Starts the tournament.
     */
    public void start() {
        // allocate the maps required to hold all of the players in the tournament
        if (playersWaiting.size() > 128) {
            for (int i = 0, mapCount = playersWaiting.size() / 128; i < mapCount; i++) {
                maps.add(createMap(false));
            }
        }

        state = TournamentState.IN_PROGRESS;
        // TODO start round based on player count
        startRound(TournamentMapIdentifier.ROUND_ONE, true);

        for (int i = 0; i < maps.size(); i++) {
            DynamicMap map = maps.get(i);
            System.out.println(i + ": " + map.convertX(3584) + ", " + map.convertY(9024));
        }
    }

    /**
     * Destroys the tournament safely by moving all players to a safe place and freeing up the maps in use.
     */
    public void destroy() {
        for (Player player : playersInLobby) {
            if (player instanceof AIPlayer) {
                player.forceLogout();
                continue;
            }

            player.getMovement().teleport(3200, 3200, 0);
        }

        for (Player player : playersFighting) {
            if (player instanceof AIPlayer) {
                player.forceLogout();
                continue;
            }

            player.getMovement().teleport(3200, 3200, 0);
        }

        for (Player player : playersWaiting) {
            if (player instanceof AIPlayer) {
                player.forceLogout();
                continue;
            }

            player.getMovement().teleport(3200, 3200, 0);
        }

        for (DynamicMap map : maps) {
            map.destroy();
        }

        maps.clear();
        playersInLobby.clear();
        playersWaiting.clear();
        playersFighting.clear();
    }

    /**
     * Splits the players into chunks of {@link TournamentMapIdentifier#playerSize} and assigns opponents to others then
     * teleports them to the area they will fight in.
     */
    private void startRound(TournamentMapIdentifier identifier, boolean byes) {
        // shuffle the players and then split them into chunks to 128
        Collections.shuffle((List<?>) playersWaiting);

        List<Player> roundByes = null;
        if (byes) {
            int byeCount = NumberUtils.roundUpPowerOf2(playersWaiting.size()) - playersWaiting.size();
            roundByes = new ArrayList<>(byeCount);
            for (int i = 0; i < byeCount; i++) {
                roundByes.add(playersWaiting.poll());
            }
        }

        List<List<Player>> playerLists = Lists.partition(new LinkedList<>(playersWaiting), 128);

        // clear the list of players in the lobby
        playersWaiting.clear();

        int dynamicMapIndex = 0;
        for (List<Player> playerList : playerLists) {
            LinkedList<Player> playerQueue = new LinkedList<>(playerList);

            DynamicMap map = maps.get(dynamicMapIndex);
            Queue<TournamentMap> arenas = new LinkedList<>(TournamentMap.getMapsForIdenitifer(identifier));

            Bounds arena;
            Player player, playerOpponent;
            while (playerQueue.size() >= 2 && arenas.size() > 0) {
                arena = arenas.poll().bounds;
                player = playerQueue.poll();
                playerOpponent = playerQueue.poll();

                setupPlayerForRound(player, playerOpponent, map,
                    new Position(map.convertX(arena.swX + 1), map.convertY(arena.swY + 1), arena.z));

                setupPlayerForRound(playerOpponent, player, map,
                    new Position(map.convertX(arena.neX - 1), map.convertY(arena.neY - 1), arena.z));

                final Player p1 = player;
                final Player p2 = playerOpponent;

                World.startEvent(event -> {
                    event.delay(5);
                    p1.getCombat().setTarget(p2);
                });
            }

            dynamicMapIndex++;
        }

        if (roundByes != null) {
            // we have some byes to add to waiting list
            for (Player player : roundByes) {
                player.sendMessage("You were automatically advanced to the next round.");
                playersWaiting.add(player);
            }

            System.out.println("There were " + roundByes.size() + " this round.");
        }

        if (maps.size() != dynamicMapIndex) {
            System.out.println(maps.size() + " is not equal to the amount used " + dynamicMapIndex);
        }

        int mapsNeeded = (playersWaiting.size() + playersFighting.size()) / 128;

        int i = 0;
        Iterator<DynamicMap> iterator = maps.iterator();
        while (iterator.hasNext()) {
            DynamicMap map = iterator.next();

            if (i++ > mapsNeeded) {
                System.out.println("Removed: " + map.convertX(3584) + ", " + map.convertY(9024));
                map.destroy();
                iterator.remove();
            }
        }

        roundCount++;
        System.out.println("Starting round " + roundCount + ".");
        // TODO update player displays
    }

    /**
     * Sets up a player for a round by assigning their opponent and teleporting them Ato the arena to fight.
     */
    private void setupPlayerForRound(Player player, Player opponent, DynamicMap map, Position telePos) {
        player.getMovement().teleport(telePos);
        player.tournamentTarget = opponent;
        player.attackPlayerListener = this::canAttack;
        player.deathEndListener = this::deathEnd;
        player.setAction(1, PlayerAction.ATTACK);
        map.assignListener(player).onExit(this::onExit);
        playersFighting.add(player);
    }

    /**
     * Progresses the tournament to the next round.
     */
    private void progressRound() {
        if (!checkRoundAdvancement()) {
            return;
        }

        int playersWaitingCount = playersWaiting.size();

        // we just keep repeating "round one" until we are down to one map and 64 players
        if (playersWaitingCount > 64) {
            startRound(TournamentMapIdentifier.ROUND_ONE, false);
            return;
        }

        for (TournamentMapIdentifier identifier : TournamentMapIdentifier.VALUES) {
            if (identifier.playerSize <= playersWaitingCount) {
                startRound(identifier, false);
                break;
            }
        }
    }

    /**
     * Checks if there are other players still fighting.
     */
    private boolean checkRoundAdvancement() {
        return playersFighting.size() == 0;
    }

    /**
     * The method used when a player leaves an arena map.
     */
    private void onExit(Player player, boolean logout) {
        boolean inTournament = false;

        for (DynamicMap map : maps) {
            if (map.isIn(player)) {
                inTournament = true;
                break;
            }
        }

        removePlayer(player, !inTournament, logout);
    }

    private void removePlayer(Player player, boolean leftMap, boolean logout) {
        if (leftMap) {
            if (playersInLobby.remove(player)) {
                System.out.println(player.getName() + " left the lobby(1).");
            } else if (playersFighting.remove(player)) {
                System.out.println(player.getName() + " left a fight(1).");
            } else if (playersWaiting.remove(player)) {
                System.out.println(player.getName() + " left while waiting for next round(1).");
            }

            if (logout) {
                player.getMovement().teleport(3092, 3494, 0);
            }

            resetPlayer(player);
        } else {
//            if (playersInLobby.remove(player)) {
//                System.out.println(player.getName() + " left the lobby(2).");
//            } else if (playersFighting.remove(player)) {
//                System.out.println(player.getName() + " left a fight(2).");
//            } else if (playersWaiting.remove(player)) {
//                System.out.println(player.getName() + " left while waiting for next round(2).");
//            }
        }
    }

    /**
     * Checks if {@code player} can attack {@code pTarget} in the tournament.
     */
    private boolean canAttack(Player player, Player pTarget, boolean message) {
        if (player.tournamentTarget != pTarget) {
            if (message) {
                player.sendMessage("That is not your opponent!");
            }
            return false;
        }

        // TODO check countdown
        return true;
    }

    private void deathEnd(Entity entity, Killer killer, Hit hit) {
        Player p1 = entity.player;
        Player p2 = p1.tournamentTarget;

        if (p2 != killer.player) {
            System.out.println("Target isn't the killer? (player=" + p1.getName() +", opponent=" + p2.getName() + ", killer=" + killer.player.getName() + ")");
        }

        if (p1.getCombat().isDead() && (p2 != null && p2.getCombat().isDead())) {
            // TODO determine winner based on damage dealt, if equal they both are removed from tourney
        }

        resetPlayer(p1);
        playersFighting.remove(p1);
        playersInLobby.add(p1);
        p1.getMovement().teleport(mainMap.convertX(LOBBY_BOUNDS.randomX()), mainMap.convertY(LOBBY_BOUNDS.randomY()), 0);
        p1.getCombat().restore();
        p1.getCombat().resetKillers();

        resetPlayer(p2);
        playersFighting.remove(p2);
        playersWaiting.add(p2);
        p2.getMovement().teleport(mainMap.convertX(LOBBY_BOUNDS.randomX()), mainMap.convertY(LOBBY_BOUNDS.randomY()), 0);
        p2.getCombat().restore();
        p2.getCombat().resetKillers();

        // TODO update client displays
        System.out.println(p2.getName() + " has killed " + p1.getName());

        progressRound();
        System.out.println(playersFighting.size() + " remaining, " + playersWaiting.size() + " waiting, " + playersInLobby.size() + " in lobby");

        if (playersFighting.size() == 0 && playersWaiting.size() == 1) {
            // TODO mark the tournament as ended and start a timer before kicking everyone out
            playersInLobby.add(playersWaiting.poll());
            playersWaiting.clear();
            System.out.println("tournament end");
        }
    }

    private void resetPlayer(Player player) {
        player.setAction(1, null);
        player.attackPlayerListener = null;
        player.deathEndListener = null;
        player.tournamentTarget = null;
    }

    private static DynamicMap createMap(boolean mainMap) {
        DynamicMap tournamentMap = new DynamicMap();
        tournamentMap.buildSw(14477, 3)
            .buildSe(14733, 3)
            .buildNw(14478, 3)
            .buildNe(14734, 3);

        if (mainMap) {
            removeObj(tournamentMap, 14593, 3612, 9078, 0);
            removeObj(tournamentMap, 14593, 3613, 9070, 0);
            removeObj(tournamentMap, 14593, 3613, 9074, 0);
            removeObj(tournamentMap, 14593, 3614, 9083, 0);
            removeObj(tournamentMap, 14593, 3615, 9068, 0);
            removeObj(tournamentMap, 14593, 3617, 9071, 0);
            removeObj(tournamentMap, 14593, 3619, 9077, 0);
            removeObj(tournamentMap, 14593, 3619, 9082, 0);
            removeObj(tournamentMap, 14593, 3621, 9073, 0);
            removeObj(tournamentMap, 14595, 3611, 9069, 0);
            removeObj(tournamentMap, 14595, 3611, 9074, 0);
            removeObj(tournamentMap, 14595, 3616, 9077, 0);
            removeObj(tournamentMap, 14595, 3616, 9079, 0);
            removeObj(tournamentMap, 14595, 3617, 9082, 0);
            removeObj(tournamentMap, 14595, 3618, 9075, 0);
            removeObj(tournamentMap, 14595, 3618, 9076, 0);
            removeObj(tournamentMap, 14595, 3619, 9073, 0);
            removeObj(tournamentMap, 14595, 3622, 9068, 0);
            removeObj(tournamentMap, 14595, 3622, 9077, 0);

            spawnObj(tournamentMap, 14622, 3611, 9072, 0, 2, 3);
            spawnObj(tournamentMap, 3457, 3612, 9073, 0, 2, 3);
            spawnObj(tournamentMap, 3457, 3612, 9074, 0, 0, 0);
            spawnObj(tournamentMap, 3457, 3612, 9075, 0, 0, 0);
            spawnObj(tournamentMap, 3457, 3612, 9076, 0, 0, 0);
            spawnObj(tournamentMap, 3457, 3612, 9077, 0, 0, 0);
            spawnObj(tournamentMap, 3457, 3612, 9078, 0, 0, 0);
            spawnObj(tournamentMap, 3457, 3612, 9082, 0, 2, 0);
            spawnObj(tournamentMap, 3457, 3613, 9073, 0, 0, 3);
            spawnObj(tournamentMap, 3457, 3616, 9082, 0, 0, 1);
            spawnObj(tournamentMap, 3457, 3614, 9073, 0, 0, 3);
            spawnObj(tournamentMap, 3457, 3617, 9082, 0, 0, 1);
            spawnObj(tournamentMap, 3457, 3615, 9073, 0, 0, 3);
            spawnObj(tournamentMap, 14777, 3612, 9079, 0, 22, 1);
            spawnObj(tournamentMap, 3457, 3612, 9079, 0, 0, 0);
            spawnObj(tournamentMap, 14777, 3612, 9080, 0, 22, 1);
            spawnObj(tournamentMap, 3457, 3612, 9080, 0, 0, 0);
            spawnObj(tournamentMap, 14777, 3612, 9081, 0, 22, 1);
            spawnObj(tournamentMap, 3457, 3612, 9081, 0, 0, 0);
            spawnObj(tournamentMap, 3457, 3618, 9082, 0, 0, 1);
            spawnObj(tournamentMap, 3457, 3616, 9073, 0, 0, 3);
            spawnObj(tournamentMap, 3457, 3619, 9082, 0, 0, 1);
            spawnObj(tournamentMap, 3457, 3617, 9073, 0, 0, 3);
            spawnObj(tournamentMap, 3457, 3620, 9082, 0, 0, 1);
            spawnObj(tournamentMap, 3457, 3621, 9073, 0, 2, 2);
            spawnObj(tournamentMap, 3457, 3621, 9074, 0, 0, 2);
            spawnObj(tournamentMap, 3457, 3618, 9073, 0, 0, 3);
            spawnObj(tournamentMap, 14777, 3618, 9073, 0, 22, 0);
            spawnObj(tournamentMap, 3457, 3619, 9073, 0, 0, 3);
            spawnObj(tournamentMap, 14777, 3619, 9073, 0, 22, 0);
            spawnObj(tournamentMap, 3457, 3620, 9073, 0, 0, 3);
            spawnObj(tournamentMap, 14777, 3620, 9073, 0, 22, 0);
            spawnObj(tournamentMap, 3457, 3621, 9075, 0, 0, 2);
            spawnObj(tournamentMap, 3457, 3621, 9076, 0, 0, 2);
            spawnObj(tournamentMap, 3457, 3621, 9077, 0, 0, 2);
            spawnObj(tournamentMap, 3457, 3621, 9078, 0, 0, 2);
            spawnObj(tournamentMap, 3457, 3621, 9079, 0, 0, 2);
            spawnObj(tournamentMap, 3457, 3621, 9080, 0, 0, 2);
            spawnObj(tournamentMap, 3457, 3621, 9081, 0, 0, 2);
            spawnObj(tournamentMap, 3457, 3621, 9082, 0, 2, 1);
            spawnObj(tournamentMap, 14622, 3611, 9076, 0, 2, 3);
            spawnObj(tournamentMap, 14622, 3615, 9072, 0, 2, 2);
            spawnObj(tournamentMap, 14622, 3618, 9083, 0, 2, 0);
            spawnObj(tournamentMap, 14622, 3622, 9072, 0, 2, 2);
            spawnObj(tournamentMap, 14622, 3622, 9076, 0, 2, 1);
            spawnObj(tournamentMap, 14622, 3622, 9083, 0, 2, 1);
            spawnObj(tournamentMap, 14623, 3611, 9075, 0, 0, 0);
            spawnObj(tournamentMap, 14623, 3611, 9079, 0, 0, 0);
            spawnObj(tournamentMap, 14623, 3611, 9083, 0, 2, 0);
            spawnObj(tournamentMap, 14623, 3612, 9072, 0, 0, 3);
            spawnObj(tournamentMap, 14623, 3617, 9083, 0, 0, 1);
            spawnObj(tournamentMap, 14623, 3616, 9072, 0, 0, 3);
            spawnObj(tournamentMap, 14623, 3621, 9083, 0, 0, 1);
            spawnObj(tournamentMap, 14623, 3622, 9073, 0, 0, 2);
            spawnObj(tournamentMap, 14778, 3613, 9083, 0, 22, 0);
            spawnObj(tournamentMap, 14623, 3613, 9083, 0, 0, 1);
            spawnObj(tournamentMap, 14623, 3622, 9077, 0, 0, 2);
            spawnObj(tournamentMap, 14624, 3611, 9073, 0, 0, 0);
            spawnObj(tournamentMap, 14624, 3611, 9074, 0, 0, 0);
            spawnObj(tournamentMap, 14624, 3611, 9078, 0, 0, 0);
            spawnObj(tournamentMap, 14624, 3612, 9083, 0, 0, 1);
            spawnObj(tournamentMap, 14778, 3612, 9083, 0, 22, 0);
            spawnObj(tournamentMap, 14624, 3613, 9072, 0, 0, 3);
            spawnObj(tournamentMap, 14624, 3616, 9083, 0, 0, 1);
            spawnObj(tournamentMap, 14624, 3617, 9072, 0, 0, 3);
            spawnObj(tournamentMap, 14624, 3620, 9083, 0, 0, 1);
            spawnObj(tournamentMap, 14624, 3618, 9072, 0, 0, 3);
            spawnObj(tournamentMap, 14624, 3622, 9074, 0, 0, 2);
            spawnObj(tournamentMap, 14624, 3622, 9078, 0, 0, 2);
            spawnObj(tournamentMap, 14625, 3611, 9077, 0, 0, 0);
            spawnObj(tournamentMap, 14778, 3611, 9080, 0, 22, 0);
            spawnObj(tournamentMap, 14625, 3611, 9080, 0, 0, 0);
            spawnObj(tournamentMap, 14778, 3611, 9081, 0, 22, 3);
            spawnObj(tournamentMap, 14624, 3611, 9081, 0, 0, 0);
            spawnObj(tournamentMap, 14778, 3611, 9082, 0, 22, 3);
            spawnObj(tournamentMap, 14624, 3611, 9082, 0, 0, 0);
            spawnObj(tournamentMap, 14625, 3614, 9072, 0, 0, 3);
            spawnObj(tournamentMap, 14625, 3619, 9083, 0, 0, 1);
            spawnObj(tournamentMap, 14625, 3622, 9075, 0, 0, 2);
            spawnObj(tournamentMap, 14776, 3612, 9073, 0, 22, 1);
            spawnObj(tournamentMap, 14778, 3615, 9083, 0, 22, 3);
            spawnObj(tournamentMap, 14624, 3615, 9083, 0, 0, 1);
            spawnObj(tournamentMap, 14778, 3614, 9083, 0, 22, 3);
            spawnObj(tournamentMap, 14625, 3614, 9083, 0, 0, 1);
            spawnObj(tournamentMap, 14776, 3612, 9082, 0, 22, 2);
            spawnObj(tournamentMap, 14776, 3621, 9073, 0, 22, 0);
            spawnObj(tournamentMap, 14776, 3621, 9082, 0, 22, 3);
            spawnObj(tournamentMap, 14777, 3612, 9074, 0, 22, 1);
            spawnObj(tournamentMap, 14777, 3612, 9075, 0, 22, 1);
            spawnObj(tournamentMap, 14777, 3612, 9076, 0, 22, 1);
            spawnObj(tournamentMap, 14777, 3612, 9077, 0, 22, 1);
            spawnObj(tournamentMap, 14777, 3612, 9078, 0, 22, 1);
            spawnObj(tournamentMap, 14777, 3613, 9073, 0, 22, 0);
            spawnObj(tournamentMap, 14777, 3616, 9082, 0, 22, 2);
            spawnObj(tournamentMap, 14777, 3614, 9073, 0, 22, 0);
            spawnObj(tournamentMap, 14777, 3617, 9082, 0, 22, 2);
            spawnObj(tournamentMap, 14777, 3615, 9073, 0, 22, 0);
            spawnObj(tournamentMap, 14777, 3618, 9082, 0, 22, 2);
            spawnObj(tournamentMap, 14777, 3616, 9073, 0, 22, 0);
            spawnObj(tournamentMap, 14777, 3619, 9082, 0, 22, 2);
            spawnObj(tournamentMap, 3457, 3616, 9082, 0, 0, 1);
            spawnObj(tournamentMap, 14777, 3616, 9082, 0, 22, 2);
            spawnObj(tournamentMap, 3457, 3615, 9082, 0, 0, 1);
            spawnObj(tournamentMap, 14777, 3615, 9082, 0, 22, 2);
            spawnObj(tournamentMap, 3457, 3614, 9082, 0, 0, 1);
            spawnObj(tournamentMap, 14777, 3614, 9082, 0, 22, 2);
            spawnObj(tournamentMap, 3457, 3613, 9082, 0, 0, 1);
            spawnObj(tournamentMap, 14777, 3613, 9082, 0, 22, 2);
            spawnObj(tournamentMap, 14777, 3617, 9073, 0, 22, 0);
            spawnObj(tournamentMap, 14777, 3620, 9082, 0, 22, 2);
            spawnObj(tournamentMap, 14777, 3621, 9074, 0, 22, 3);
            spawnObj(tournamentMap, 14777, 3621, 9075, 0, 22, 3);
            spawnObj(tournamentMap, 14777, 3621, 9076, 0, 22, 3);
            spawnObj(tournamentMap, 14777, 3621, 9077, 0, 22, 3);
            spawnObj(tournamentMap, 14777, 3621, 9078, 0, 22, 3);
            spawnObj(tournamentMap, 14777, 3621, 9079, 0, 22, 3);
            spawnObj(tournamentMap, 14777, 3621, 9080, 0, 22, 3);
            spawnObj(tournamentMap, 14777, 3621, 9081, 0, 22, 3);
            spawnObj(tournamentMap, 14778, 3611, 9072, 0, 22, 3);
            spawnObj(tournamentMap, 14778, 3611, 9073, 0, 22, 1);
            spawnObj(tournamentMap, 14778, 3611, 9074, 0, 22, 1);
            spawnObj(tournamentMap, 14778, 3611, 9075, 0, 22, 3);
            spawnObj(tournamentMap, 14778, 3611, 9076, 0, 22, 2);
            spawnObj(tournamentMap, 14778, 3611, 9077, 0, 22, 0);
            spawnObj(tournamentMap, 14778, 3611, 9078, 0, 22, 3);
            spawnObj(tournamentMap, 14778, 3611, 9079, 0, 22, 3);
            spawnObj(tournamentMap, 14778, 3611, 9083, 0, 22, 3);
            spawnObj(tournamentMap, 14778, 3612, 9072, 0, 22, 1);
            spawnObj(tournamentMap, 14778, 3612, 9030, 0, 22, 3);
            spawnObj(tournamentMap, 14778, 3613, 9072, 0, 22, 1);
            spawnObj(tournamentMap, 14778, 3616, 9083, 0, 22, 3);
            spawnObj(tournamentMap, 14778, 3614, 9072, 0, 22, 1);
            spawnObj(tournamentMap, 14778, 3617, 9083, 0, 22, 0);
            spawnObj(tournamentMap, 14778, 3615, 9072, 0, 22, 1);
            spawnObj(tournamentMap, 14778, 3618, 9083, 0, 22, 3);
            spawnObj(tournamentMap, 14778, 3616, 9072, 0, 22, 1);
            spawnObj(tournamentMap, 14778, 3619, 9083, 0, 22, 3);
            spawnObj(tournamentMap, 14778, 3617, 9072, 0, 22, 0);
            spawnObj(tournamentMap, 14778, 3620, 9083, 0, 22, 3);
            spawnObj(tournamentMap, 14778, 3618, 9072, 0, 22, 3);
            spawnObj(tournamentMap, 14778, 3621, 9083, 0, 22, 3);
            spawnObj(tournamentMap, 14778, 3622, 9072, 0, 22, 1);
            spawnObj(tournamentMap, 14778, 3622, 9073, 0, 22, 1);
            spawnObj(tournamentMap, 14778, 3622, 9074, 0, 22, 1);
            spawnObj(tournamentMap, 14778, 3622, 9079, 0, 22, 1);
            spawnObj(tournamentMap, 14625, 3622, 9079, 0, 0, 2);
            spawnObj(tournamentMap, 14778, 3622, 9075, 0, 22, 1);
            spawnObj(tournamentMap, 14778, 3622, 9076, 0, 22, 1);
            spawnObj(tournamentMap, 14778, 3622, 9077, 0, 22, 1);
            spawnObj(tournamentMap, 14778, 3622, 9078, 0, 22, 2);
            spawnObj(tournamentMap, 14778, 3622, 9083, 0, 22, 3);
            spawnObj(tournamentMap, 16271, 3613, 9076, 0, 22, 3);
            spawnObj(tournamentMap, 14625, 3621, 9072, 0, 0, 3);
            spawnObj(tournamentMap, 14778, 3621, 9072, 0, 22, 1);
            spawnObj(tournamentMap, 14624, 3620, 9072, 0, 0, 3);
            spawnObj(tournamentMap, 14778, 3620, 9072, 0, 22, 3);
            spawnObj(tournamentMap, 14624, 3619, 9072, 0, 0, 3);
            spawnObj(tournamentMap, 14778, 3619, 9072, 0, 22, 3);
            spawnObj(tournamentMap, 14625, 3622, 9082, 0, 0, 2);
            spawnObj(tournamentMap, 14778, 3622, 9082, 0, 22, 1);
            spawnObj(tournamentMap, 16271, 3615, 9074, 0, 22, 1);
            spawnObj(tournamentMap, 16271, 3615, 9078, 0, 22, 0);
            spawnObj(tournamentMap, 14778, 3622, 9080, 0, 22, 1);
            spawnObj(tournamentMap, 14623, 3622, 9080, 0, 0, 2);
            spawnObj(tournamentMap, 14624, 3622, 9081, 0, 0, 2);
            spawnObj(tournamentMap, 14778, 3622, 9081, 0, 22, 2);
            spawnObj(tournamentMap, 16273, 3617, 9077, 0, 22, 3);
            spawnObj(tournamentMap, 31494, 3611, 9072, 0, 11, 2);
            spawnObj(tournamentMap, 31494, 3611, 9083, 0, 11, 3);
            spawnObj(tournamentMap, 31494, 3622, 9072, 0, 11, 1);
            spawnObj(tournamentMap, 31494, 3622, 9083, 0, 11, 0);
            spawnObj(tournamentMap, 16271, 3620, 9075, 0, 22, 1);
            spawnObj(tournamentMap, 16271, 3619, 9081, 0, 22, 1);
            spawnObj(tournamentMap, 16271, 3614, 9080, 0, 22, 1);
            spawnObj(tournamentMap, 33441, 3621, 9082, 0, 10, 0);
            spawnObj(tournamentMap, 33441, 3612, 9082, 0, 10, 0);
            spawnObj(tournamentMap, 33441, 3612, 9073, 0, 10, 0);
            spawnObj(tournamentMap, 33441, 3621, 9073, 0, 10, 0);
            spawnObj(tournamentMap, 40001, 3616, 9081, 0, 10, 0);
            spawnObj(tournamentMap, 40002, 3617, 9073, 0, 10, 2);
            spawnObj(tournamentMap, 40003, 3616, 9073, 0, 10, 2);
            spawnObj(tournamentMap, 20780, 3615, 9073, 0, 10, 2);

            spawnObj(tournamentMap, 20665, 3612, 9081, 0, 10, 0);
            spawnObj(tournamentMap, 20662, 3621, 9079, 0, 10, 1);
        }

        return tournamentMap;
    }

    private static void spawnNpc(DynamicMap map, int id, int x, int y, int z, Direction direction, int walkRange) {
        x = map.convertX(x);
        y = map.convertY(y);
        new NPC(id).spawn(x, y, z, direction, walkRange);
        if(walkRange == 0)
            Tile.get(x, y, z, true).flagUnmovable();
    }

    private static void spawnObj(DynamicMap map, int id, int x, int y, int z, int type, int direction) {
        GameObject obj = GameObject.spawn(id, map.convertX(x), map.convertY(y), z, type, direction);
        BankActions.markTiles(obj);
    }

    private static void removeObj(DynamicMap map, int id, int x, int y, int z) {
        GameObject.forObj(id, map.convertX(x), map.convertY(y), z, obj -> obj.setId(-1));
    }
}
