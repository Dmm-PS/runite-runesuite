package io.ruin.model.activities.tournament;

import io.ruin.Server;
import io.ruin.api.utils.NumberUtils;
import io.ruin.cache.Color;
import io.ruin.cache.Icon;
import io.ruin.model.World;
import io.ruin.model.combat.Hit;
import io.ruin.model.combat.Killer;
import io.ruin.model.entity.Entity;
import io.ruin.model.entity.npc.NPC;
import io.ruin.model.entity.npc.NPCAction;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.player.PlayerAction;
import io.ruin.model.entity.player.PlayerCounter;
import io.ruin.model.entity.shared.LockType;
import io.ruin.model.entity.shared.StepType;
import io.ruin.model.entity.shared.listeners.SpawnListener;
import io.ruin.model.inter.InterfaceType;
import io.ruin.model.inter.dialogue.*;
import io.ruin.model.inter.utils.Config;
import io.ruin.model.inter.utils.Option;
import io.ruin.model.item.Item;
import io.ruin.model.item.ItemContainer;
import io.ruin.model.map.*;
import io.ruin.model.map.object.GameObject;
import io.ruin.model.map.object.actions.ObjectAction;
import io.ruin.model.stat.StatType;
import io.ruin.services.Loggers;
import io.ruin.utility.Broadcast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static io.ruin.model.inter.utils.Config.varpbit;

public class Tournament {

    private static final GameObject TOURNAMENT_INFORMATION = GameObject.spawn(31846, 3111, 3513, 0, 10, 0);

    /**
     * Debug boolean
     */
    private static boolean DISABLED = false;

    /**
     * Tournament bounds
     */
    public static final Bounds JOIN_BOUNDS = World.isPVP() ? new Bounds(3077, 3488, 3081, 3497, 0) : new Bounds(3107, 3511, 3111, 3517, 0);
    private static final Bounds LEAVE_BOUNDS = World.isPVP() ? new Bounds(3077, 3488, 3081, 3497, 0) : new Bounds(3110, 3511, 3110, 3515, 0);

    private static final Bounds START_BOUNDS = new Bounds(3322, 4964, 3324, 4976, 0);
    private static final Bounds WAIT_BOUNDS = new Bounds(3322, 4943, 3324, 4955, 0);
    private static final Bounds FIGHT_BOUNDS = new Bounds(3268, 4932, 3317, 4987, 0);
    private static final Bounds DEATH_BOUNDS = new Bounds(3322, 4964, 3324, 4976, 0);
    private static final Bounds TOURNAMENT_BOUNDS = new Bounds(3264, 4928, 3327, 4996, 0);

    /**
     * Viewing orb positions
     */
    private static final Position[] viewingOrb = {
            new Position(3286, 4958, 0),
            new Position(3279, 4976, 0),
            new Position(3306, 4976, 0),
            new Position(3306, 4951, 0),
            new Position(3281, 4943, 0)
    };

    /**
     * Tournament final integers
     */
    private static final int TOURNAMENT_INTERFACE = 328, TOURNAMENT_MANAGER = 7316, CRUSHED_BARRICADE = 6882, TOURNAMENT_BARRIER = 40008, TICKET_EXCHANGE = 29087;

    /**
     * Time until the event starts
     */
    private static long ticksUntilStart;

    /**
     * List of participants
     */
    private static List<Player> participants = new ArrayList<>();

    /**
     * Identify the Tournament Manager
     */
    public static final NPC MANAGER = SpawnListener.find(TOURNAMENT_MANAGER).get(0);

    /**
     * Countdown timer
     */
    private static final Config COUNTDOWN = varpbit(4286, false);

    /**
     * Tournament in progress
     */
    static boolean tournamentInProgress = false;

    /**
     * Our scheduled tournament
     */
    public static TournamentSchedule schedule;

    /**
     * Blood money reward amounts
     */
    private static final int REWARD_CURRENCY = World.isPVP() ? 13307 : 995;
    private static final int FIRST_PLACE_BM = World.isPVP() ? 5000 : 1_000_000;
    private static final int SECOND_PLACE_BM = World.isPVP() ? 3000 : 500_000;
    private static final int THIRD_PLACE_BM = World.isPVP() ? 2500 : 250_000;
    private static final int DEFAULT_BM = 500;

    /**
     * Ticket rewards amounts
     */
    private static final int TOURNAMENT_TICKET = 5023;
    private static final int FIRST_PLACE_TT = 4;
    private static final int SECOND_PLACE_TT = 3;
    private static final int THIRD_PLACE_TT = 2;
    private static final int DEFAULT_TT = 1;

    /**
     * Separator
     */

    public static void joinTournament(Player player, NPC npc) {
        if (DISABLED) {
            player.dialogue(new MessageDialogue("Tournaments are currently disabled while we work out a few issues.<br>Thanks for your patience."));
            return;
        }
        if (tournamentInProgress) {
            player.dialogue(new NPCDialogue(npc, "You can't join a tournament that's already in progress.").lineHeight(18));
            return;
        }
        if (secondsUntilStart(ticksUntilStart) > 1800) {
            player.dialogue(new NPCDialogue(npc, "You can only join the tournament 30 minutes before it's about to begin. The " +
                    "next tournament is in: <br><img=42> " + timeRemaining()).lineHeight(18));
            return;
        }
        if (participants.contains(player)) {
            player.dialogue(new NPCDialogue(npc, "You are already participating in the tournament. It will begin in " + timeRemaining() + ".").lineHeight(18));
            return;
        }
        if (sameIpInTournament(player)) {
            player.sendMessage("<col=FA0000>There is already a participant with the same IP Address as you in the tournament.");
            return;
        }
        if (!bankItems(player, true) || !bankItems(player, false)) {
            player.sendMessage(Color.RED.wrap("You need to bank your items before joining the tournament."));
            return;
        }
        player.dialogue(new NPCDialogue(npc, "Would you like to join the tournament?"),
                new OptionsDialogue("Would you like to join the tournament?",
                        new Option("Yes, sign me up!", () -> player.getMovement().startTeleport(event -> {
                            player.lock();
                            Position location = new Position(JOIN_BOUNDS.randomX(), JOIN_BOUNDS.randomY(), 0);
                            while (location.getTile().clipping != 0)
                                location = new Position(JOIN_BOUNDS.randomX(), JOIN_BOUNDS.randomY(), 0);
                            final Position teleLoc = location;
                            player.animate(3864);
                            player.graphics(1039);
                            player.privateSound(200, 0, 10);
                            event.delay(2);
                            player.getMovement().teleport(teleLoc);
                            player.preTournyAttack = new int[] {player.getStats().get(StatType.Attack).fixedLevel, (int) player.getStats().get(StatType.Attack).experience};
                            player.preTournyStrength = new int[] {player.getStats().get(StatType.Strength).fixedLevel, (int) player.getStats().get(StatType.Strength).experience};
                            player.preTournyDefence = new int[] {player.getStats().get(StatType.Defence).fixedLevel, (int) player.getStats().get(StatType.Defence).experience};
                            player.preTournyRanged = new int[] {player.getStats().get(StatType.Ranged).fixedLevel, (int) player.getStats().get(StatType.Ranged).experience};
                            player.preTournyPrayer = new int[] {player.getStats().get(StatType.Prayer).fixedLevel, (int) player.getStats().get(StatType.Prayer).experience};
                            player.preTournyMagic = new int[] {player.getStats().get(StatType.Magic).fixedLevel, (int) player.getStats().get(StatType.Magic).experience};
                            player.preTournyHitpoints = new int[] {player.getStats().get(StatType.Hitpoints).fixedLevel, (int) player.getStats().get(StatType.Hitpoints).experience};
                            event.delay(1);
                            participants.add(player);
                            TournamentPreset.use(player, schedule.preset);
                            player.tournamentPreset = schedule.preset;
                            player.joinedTournament = true;
                            player.dialogue(new NPCDialogue(npc, "I've signed you up for the tournament. It will begin in " + timeRemaining() + ".").lineHeight(18));
                            player.unlock();
                        })),
                        new Option("No, I don't want to join the tournament.", () -> player.dialogue(
                                new PlayerDialogue("No, I don't want to join the tournament right now."),
                                new NPCDialogue(npc, "If you change your mind, you know where to find me!")
                        ))));
    }

    private static void tournamentInformation(Player player) {
        player.sendScroll("<col=800000>Tournament Information",
                "<col=800000>Time Until Next Tournament: </col>",
                TournamentSchedule.timeUntilTournament(secondsUntilStart(ticksUntilStart)),
                "",
                "<col=800000>Tournament Information:</col>",
                "Tournaments are hosted all day every 3 hours. There is a random",
                "combat style selected every tournament. Tournaments do not cost",
                "anything to join.",
                "",
                "<col=800000>Tournament Rewards: </col>",
                "<col=800000>First Place: </col> " + NumberUtils.formatNumber(FIRST_PLACE_BM) + " coins and " + FIRST_PLACE_TT + " Tournament tickets",
                "<col=800000>Second Place: </col>" + NumberUtils.formatNumber(SECOND_PLACE_BM) + " coins and " + SECOND_PLACE_TT + " Tournament tickets",
                "<col=800000>Third Place: </col> " + NumberUtils.formatNumber(THIRD_PLACE_BM) + " coins and " + THIRD_PLACE_TT + " Tournament tickets",
                "<col=800000>Unplaced: </col> Nothing!"
        );
    }

    private static long secondsUntilStart(long ticks) {
        return (long) ((ticks - Server.currentTick()) * 0.6);
    }

    private static String timeRemaining() {
        return Color.COOL_BLUE.wrap(TournamentSchedule.timeUntilTournament(secondsUntilStart(ticksUntilStart)));
    }

    /**
     * Tournament eventFir
     */

    public static void startEvent() {
        World.startEvent(event -> {
            tournamentInProgress = true;
            MANAGER.lock();
            MANAGER.animate(1818);
            MANAGER.graphics(343);
            MANAGER.forceText("The tournament is starting - good luck everyone!");
            participants.forEach(Tournament::teleportToStartBounds);
            event.delay(2);
            MANAGER.unlock();
            participants.forEach(Tournament::updateInterface);
            participants.forEach(Tournament::removeUnusedStrings);
            participants.forEach(player -> updateStatus(player, "Waiting..."));
            participants.forEach(player -> updateTarget(player, ""));
            event.delay(Server.toTicks(34));
            participants.forEach(p -> setCountdown(p, 0));
            Collections.shuffle(participants);
            assignInitialTargets();
            while (participants.size() != 0) {
                Collections.shuffle(participants);
                participants.forEach(player -> {
                    if (player == null) {
                        return;
                    }
                    if (player.tournamentTarget != null && player.canAttackTournamentTarget) {
                        checkParticipation(player);
                    } else if (player.tournamentTarget == null && !player.tournamentCooldown.isDelayed()) {
                        setCountdown(player, 0);
                        updateStatus(player, "Searching..");
                        participants.stream()
                                .filter(p -> p != player && p.tournamentTarget == null && !p.tournamentCooldown.isDelayed())
                                .findAny()
                                .ifPresent(other -> {
                                    player.tournamentTarget = other;
                                    other.tournamentTarget = player;
                                    teleportToFightBounds(player);
                                    teleportToFightBounds(other);
                                });
                        /* in case top 3 tie */
                        if (participants.size() == 1 && player.tournamentTarget == null && !player.tournamentCooldown.isDelayed())
                            progressRound(player, false);
                    }
                });
                event.delay(1);
            }

            /* tournament is over! */
            tournamentInProgress = false;
            event.delay(10);
            World.players.forEach(player -> {
                if (player.getPosition().inBounds(TOURNAMENT_BOUNDS)) {
                    World.startEvent(e -> {
                        player.lock();
                        TournamentViewingOrb.reset(player);
                        player.animate(1816);
                        player.graphics(342);
                        e.delay(2);
                        player.getMovement().teleport(LEAVE_BOUNDS);
                        player.resetAnimation();
                        event.delay(2);
                        player.unlock();
                    });
                }
            });

            /* finally set a random preset for the next tournament */
            schedule = TournamentSchedule.randomSchedule();
        });
    }

    private static boolean bankItems(Player player, boolean inventory) {
        ItemContainer container;
        if (inventory) {
            container = player.getInventory();
        } else {
            container = player.getEquipment();
        }
        for (Item item : container.getItems()) {
            if (item != null && player.getBank().deposit(item, item.getAmount(), false) == 0) {
                player.sendMessage(Color.COOL_BLUE.wrap(item.getDef().name) + " x " + item.getAmount() + " could not be banked.");
                if (!inventory) // it's okay if we fail to bank an inventory item, but we have to abort if we fail equipment or we could end up with gear equipped that we don't have stat requirements for
                    return false;
            }
        }
        return true;
    }

    private static void checkParticipation(Player player) {
        if (player.getCombat().isDead() || player.wonRound)
            return;
        long sdTime = Server.currentTick() - player.tournamentFightStarted - 500;
        if (sdTime == -50)
            player.sendMessage(Color.RED.wrap("Sudden death will begin in 30 seconds."));
        else if (sdTime == 0)
            player.sendMessage(Color.RED.wrap("Sudden death has begun!"));
        else if (sdTime > 0 && sdTime % 3 == 0) {
            player.hit(new Hit().fixedDamage((int) (3 + sdTime)));
        }
    }

    private static void teleportToStartBounds(Player player) {
        World.startEvent(event -> {
            if (player != null) {
                player.lock();
                event.delay(2);
                player.animate(1816);
                player.graphics(342);
                event.delay(2);
                player.getMovement().teleport(START_BOUNDS);
                event.delay(1);
                player.dialogue(new ItemDialogue().one(964, "In 30 seconds you will be assigned a target and teleported into the PVP area. Good luck, " + player.getName() + "!").lineHeight(24));
                setCountdown(player, 50);
                player.resetAnimation();
                event.delay(2);
                player.unlock();
            }
        });
    }

    private static void teleportToFightBounds(Player player) {
        World.startEvent(event -> {
            if (player.tournamentTarget == null)
                return;
            player.lock();
            TournamentViewingOrb.reset(player);
            player.animate(1816);
            player.graphics(342);
            event.delay(2);
            Position location = new Position(FIGHT_BOUNDS.randomX(), FIGHT_BOUNDS.randomY(), 0);
            while (isOrbPosition(location))
                location = new Position(FIGHT_BOUNDS.randomX(), FIGHT_BOUNDS.randomY(), 0);
            player.getMovement().teleport(location);
            event.delay(1);
            setCountdown(player, 0);
            player.getCombat().restore();
            player.getCombat().restoreSpecial(100);
            player.vengeanceActive = false;
            player.sotdDelay.reset();
            player.vestasSpearSpecial.reset();
            if (player.pet != null)
                player.callPet = true;
            event.delay(1);
            beginFight(player);
            player.getPacketSender().sendHintIcon(player.tournamentTarget);
            player.resetAnimation();
            event.delay(2);
            player.unlock();
        });
    }

    private static boolean isOrbPosition(Position pos) {
        for (Position orb : viewingOrb)
            if (orb.equals(pos))
                return true;
        return false;
    }

    private static void beginFight(Player player) {
        player.tournamentFightStarted = Server.currentTick();
        player.wonRound = false;
        World.startEvent(event -> {
            updateStatus(player, "Fighting..");
            updateTarget(player, player.tournamentTarget.getName());
            player.forceText("3!");
            event.delay(3);
            player.forceText("2!");
            event.delay(3);
            player.forceText("1!");
            event.delay(3);
            player.forceText("Fight!");
            player.canAttackTournamentTarget = true;
        });
    }

    private static void assignInitialTargets() {
        LinkedList<Player> queue = new LinkedList<>(participants);
        while (queue.size() > 1) {
            Player p1 = queue.pop();
            Player p2 = queue.pop();
            p1.tournamentTarget = p2;
            p2.tournamentTarget = p1;
            teleportToFightBounds(p1);
            teleportToFightBounds(p2);
        }
        if (queue.size() == 1) {
            Player free = queue.pop();
            progressRound(free, true);
        }
    }


    private static void climbBarricade(Player player, int xDiff, Direction direction) {
        player.startEvent(event -> {
            player.lock(LockType.FULL_DELAY_DAMAGE);
            player.animate(839);
            player.getMovement().force(xDiff, 0, 0, 0, 0, 60, direction);
            event.delay(2);
            if (player.pet != null)
                player.callPet = true;
            player.unlock();
        });
    }

    private static void passThroughBarrier(Player player) {
        player.startEvent(event -> {
            player.lock(LockType.FULL_DELAY_DAMAGE);
            player.step(1, 0, StepType.FORCE_WALK);
            event.delay(1);
            if (player.pet != null)
                player.callPet = true;
            player.unlock();
        });
    }

    /**
     * Progress and lose rounds
     */

    private static void progressRound(Player player, boolean freeSkip) {
        World.startEvent(event -> {
            if (participants.size() == 1) {
                player.lock();
                participants.remove(player);
                player.animate(1816);
                player.graphics(342);
                event.delay(2);
                player.tournamentKills++;
                player.getMovement().teleport(DEATH_BOUNDS);
                if (player.pet != null)
                    player.callPet = true;
                resetListeners(player);
                resetPlayer(player);
                event.delay(2);
                //Loggers.logTournamentResults(player.getUserId(), player.getName(), player.getIp(), 1);
                rewardPlayer(player, "first", "Winner!", FIRST_PLACE_BM, FIRST_PLACE_TT, true);
                Broadcast.WORLD.sendNews(Icon.ANNOUNCEMENT, "Tournament", player.getName() + " has just placed first in the tournament!");
                player.tournamentWins++;
                player.tournamentParticipation++;
                player.resetAnimation();
                event.delay(2);
                player.unlock();
            } else {
                player.lock();
                player.animate(1816);
                player.graphics(342);
                event.delay(2);
                player.getMovement().teleport(WAIT_BOUNDS);
                if (player.pet != null)
                    player.callPet = true;
                player.tournamentKills++;
                player.tournamentRound++;
                player.tournamentTarget = null;
                player.getCombat().resetKillers();
                player.getPacketSender().resetHintIcon(false);
                player.getCombat().restore();
                player.getCombat().restoreSpecial(100);
                TournamentPreset.use(player, player.tournamentPreset);
                player.canAttackTournamentTarget = false;
                player.tournamentCooldown.delaySeconds(30);
                updateStatus(player, "Cooldown..");
                updateTarget(player, "");
                setCountdown(player, 50);
                player.resetAnimation();
                if (freeSkip)
                    player.dialogue(new ItemDialogue().one(964, "You have been randomly selected to progress onto the next round.").lineHeight(24));
                else
                    player.dialogue(new ItemDialogue().one(964, "Congratulations on defeating your opponent. In <col=6f0000>30 seconds</col> you will be assigned another target."));
                event.delay(2);
                player.wonRound = false;
                player.unlock();
            }
        });
    }

    private static void loseRound(Player player) {
        World.startEvent(event -> {
            player.lock();
            participants.remove(player);
            player.getMovement().teleport(DEATH_BOUNDS);
            if (player.pet != null)
                player.callPet = true;
            resetListeners(player);
            resetPlayer(player);
            event.delay(1);
            if (participants.size() <= 1) {
                rewardPlayer(player, "second", "Second!", SECOND_PLACE_BM, SECOND_PLACE_TT, true);
                // Loggers.logTournamentResults(player.getUserId(), player.getName(), player.getIp(), 2);
            } else if (participants.size() == 2) {
                rewardPlayer(player, "third", "Third!", THIRD_PLACE_BM, THIRD_PLACE_TT, true);
                // Loggers.logTournamentResults(player.getUserId(), player.getName(), player.getIp(), 3);
            } else {
                player.dialogue(new ItemDialogue().one(964, "You have lost the fight and have been kicked out of the tournament.").lineHeight(24));
                PlayerCounter.TOURNAMENT_PARTICIPATION.increment(player, 1);
                if(World.isPVP())
                    rewardPlayer(player, "", "", DEFAULT_BM, DEFAULT_TT, false);
                updateStatus(player, "Lost!");
                updateTarget(player, "");
            }
            event.delay(2);
            player.wonRound = false;
            player.unlock();
        });
    }

    /**
     * Resetting and interfaces
     */

    private static void updateInterface(Player player) {
        player.getPacketSender().sendString(TOURNAMENT_INTERFACE, 12, "Opponents: " + participants.size());
        player.getPacketSender().sendString(TOURNAMENT_INTERFACE, 8, "Kills:");
        player.getPacketSender().sendString(TOURNAMENT_INTERFACE, 9, "" + player.tournamentKills);
    }

    private static void updateStatus(Player player, String status) {
        player.getPacketSender().sendString(TOURNAMENT_INTERFACE, 14, status);
    }

    private static void updateTarget(Player player, String target) {
        player.getPacketSender().sendString(TOURNAMENT_INTERFACE, 10, "Target: " + target);
    }

    private static void removeUnusedStrings(Player player) {
        player.getPacketSender().sendString(TOURNAMENT_INTERFACE, 11, "");
        player.getPacketSender().sendString(TOURNAMENT_INTERFACE, 13, "");
    }

    private static void setCountdown(Player player, int seconds) {
        COUNTDOWN.set(player, seconds);
    }

    private static void rewardPlayer(Player player, String place, String status, int bloodMoneyAmount, int ticketAmount, boolean topThree) {
        player.getInventory().add(REWARD_CURRENCY, bloodMoneyAmount);
        if(World.isPVP())
            player.getInventory().add(TOURNAMENT_TICKET, ticketAmount);
        player.sendFilteredMessage("<col=6f0000>You receive " + bloodMoneyAmount + " blood money and " + ticketAmount + " tournament tickets.");

        if (topThree) {
            player.dialogue(new ItemDialogue().one(REWARD_CURRENCY, "Congratulations, you have placed " + place + " in the tournament.").lineHeight(24));
            updateStatus(player, status);
        }
    }

    private static void resetPlayer(Player player) {
        World.startEvent(event -> {
            player.lock();
            for (Item item : player.getInventory().getItems()) {
                if (item != null) {
                    if (item.getId() == REWARD_CURRENCY || item.getId() == TOURNAMENT_TICKET)
                        continue;
                    item.remove();
                }
            }
            player.getEquipment().clear();
            player.getCombat().resetKillers();
            player.joinedTournament = false;
            player.tournamentPrayerDisabled = false;
            player.tournamentTarget = null;
            player.tournamentKills = 0;
            player.getCombat().resetKillers();
            player.tournamentRound = 0;
            player.tournamentPreset = null;
            player.canAttackTournamentTarget = false;
            player.getPacketSender().resetHintIcon(false);
            player.getCombat().updateLevel();
            player.callPet = true;
            TournamentViewingOrb.reset(player);
            /**
             * Set combat levels
             */
            if (player.preTournyAttack != null)
                player.getStats().set(StatType.Attack, player.preTournyAttack[0], player.preTournyAttack[1]);
            if (player.preTournyStrength != null)
                player.getStats().set(StatType.Strength, player.preTournyStrength[0], player.preTournyStrength[1]);
            if (player.preTournyDefence != null)
                player.getStats().set(StatType.Defence, player.preTournyDefence[0], player.preTournyDefence[1]);
            if (player.preTournyRanged != null)
                player.getStats().set(StatType.Ranged, player.preTournyRanged[0], player.preTournyRanged[1]);
            if (player.preTournyPrayer != null)
                player.getStats().set(StatType.Prayer, player.preTournyPrayer[0], player.preTournyPrayer[1]);
            if (player.preTournyMagic != null)
                player.getStats().set(StatType.Magic, player.preTournyMagic[0], player.preTournyMagic[1]);
            if (player.preTournyHitpoints != null)
                player.getStats().set(StatType.Hitpoints, player.preTournyHitpoints[0], player.preTournyHitpoints[1]);
            event.delay(2);
            player.unlock();
        });
    }

    private static void resetListeners(Player player) {
        player.attackPlayerListener = null;
        player.teleportListener = null;
        player.deathStartListener = null;
        player.deathEndListener = null;
        player.activatePrayerListener = null;
    }

    private static void resetInterface(Player player) {
        player.closeInterface(InterfaceType.SECONDARY_OVERLAY);
        player.closeInterface(InterfaceType.PRIMARY_OVERLAY);
    }

    public static void forceStart(Player player, TournamentSchedule sched, int ticks) {
        if (ticksUntilStart <= 1800) {
            player.sendMessage(Color.RED.tag() + "A tournament is already about to begin soon; no point in starting one right now!");
            return;
        }
        ticksUntilStart = ticks;
        schedule = sched;
    }

    static {
        if (!DISABLED) {
            /**
             * Register the tournament event
             */
            World.startEvent(e -> {
                /**
                 * Delay until next start
                 */
                schedule = TournamentSchedule.randomSchedule();
                ticksUntilStart = Server.getEnd(Server.toTicks(216 * 100));
                e.delay(Server.toTicks(216 * 100));
                /**
                 * Start event every 3 hours
                 */
                while (true) {
                    startEvent();
                    ticksUntilStart = Server.getEnd(Server.toTicks(216 * 100));
                    e.delay(Server.toTicks(216 * 100));
                }
            });

            /**
             * Tournament Manager/Announcement event
             */
            World.startEvent(event -> {
                while (true) {
                    if ((ticksUntilStart - Server.currentTick()) * 0.6 == 1800) {
                        Broadcast.WORLD.sendNews(Icon.ANNOUNCEMENT, "Tournament", "In 30 minutes a <col=006600>"
                                + schedule.presetName + "</col> <col=1e44b3>tournament is starting! Type ::tournament to sign up now!");
                        MANAGER.forceText("30 minutes until the tournament event starts!");
                        MANAGER.animate(862);
                    } else if ((ticksUntilStart - Server.currentTick()) * 0.6 == 900) {
                        Broadcast.WORLD.sendNews(Icon.ANNOUNCEMENT, "Tournament", "In 15 minutes a <col=006600>"
                                + schedule.presetName + "</col> <col=1e44b3>tournament is starting! Type ::tournament to sign up now!");
                        MANAGER.forceText("15 minutes until the tournament event starts!");
                        MANAGER.animate(862);
                        //TournamentEmbedMessage.sendDiscordMessage(schedule.presetName, "15 minutes");
                    } else if ((ticksUntilStart - Server.currentTick()) * 0.6 == 600) {
                        Broadcast.WORLD.sendNews(Icon.ANNOUNCEMENT, "Tournament", "In 10 minutes a <col=006600>"
                                + schedule.presetName + "</col> <col=1e44b3>tournament is starting! Type ::tournament to sign up now!");
                        MANAGER.forceText("10 minutes until the tournament event starts!");
                        MANAGER.animate(862);
                    } else if ((ticksUntilStart - Server.currentTick()) * 0.6 == 300) {
                        Broadcast.WORLD.sendNews(Icon.ANNOUNCEMENT, "Tournament", "In 5 minutes a <col=006600>"
                                + schedule.presetName + "</col> <col=1e44b3>tournament is starting! Type ::tournament to sign up now!");
                        MANAGER.forceText("5 minutes until the tournament event starts!");
                        MANAGER.animate(862);
                    } else if ((ticksUntilStart - Server.currentTick()) * 0.6 == 60) {
                        Broadcast.WORLD.sendNews(Icon.ANNOUNCEMENT, "Tournament", "In 1 minute a <col=006600>"
                                + schedule.presetName + "</col> <col=1e44b3>tournament is starting! Type ::tournament sign up now!");
                        MANAGER.forceText("1 minutes until the tournament event starts!");
                        //TournamentEmbedMessage.sendDiscordMessage(schedule.presetName, "1 minutes");
                        MANAGER.animate(862);
                    } else if ((ticksUntilStart - Server.currentTick()) * 0.6 == 30) {
                        Broadcast.WORLD.sendNews(Icon.ANNOUNCEMENT, "Tournament", "In 30 seconds a <col=006600>"
                                + schedule.presetName + "</col> <col=1e44b3>tournament is starting! Quickly type ::tournament to sign up now!");
                        MANAGER.forceText("30 seconds until the tournament event starts!");
                        MANAGER.animate(862);
                    } else if ((ticksUntilStart - Server.currentTick()) * 0.6 == 10.2) {
                        Broadcast.WORLD.sendNews(Icon.ANNOUNCEMENT, "Tournament", "In 10 seconds a <col=006600>"
                                + schedule.presetName + "</col> <col=1e44b3>tournament is starting! Quickly type ::tournament to sign up now!");
                        MANAGER.forceText("10 seconds until the tournament event starts!");
                        MANAGER.animate(862);
                    }
                    event.delay(1);
                }
            });

            /**
             * Map listeners
             */
            MapListener.registerRegion(13133)
                    .onEnter(player -> {
                        if (player.usingTournamentOrbFromHome)
                            return;
                        player.openInterface(InterfaceType.SECONDARY_OVERLAY, TOURNAMENT_INTERFACE);
                        player.openInterface(InterfaceType.PRIMARY_OVERLAY, 88);
                    })
                    .onExit((player, logout) -> {
                        if (player.usingTournamentOrbFromHome)
                            return;
                        player.closeInterface(InterfaceType.SECONDARY_OVERLAY);
                        player.closeInterface(InterfaceType.PRIMARY_OVERLAY);
                    });
            MapListener.registerBounds(JOIN_BOUNDS)
                    .onExit((player, logout) -> {
                        if (logout || player.joinedTournament && !player.getPosition().inBounds(TOURNAMENT_BOUNDS)) {
                            resetPlayer(player);
                            resetListeners(player);
                            participants.remove(player);
                            if (!logout)
                                player.sendFilteredMessage("<col=6f0000>You have left the waiting area and have been removed from the tournament.");
                        }
                    });
            MapListener.register(Tournament::checkActive)
                    .onEnter(Tournament::entered)
                    .onExit(Tournament::exited);
            MapListener.registerBounds(FIGHT_BOUNDS)
                    .onEnter(player -> {
                        if (player.usingTournamentOrbFromHome)
                            return;
                        player.setAction(1, PlayerAction.ATTACK);
                    })
                    .onExit((player, logout) -> {
                        if (player.usingTournamentOrbFromHome)
                            return;
                        player.setAction(1, null);
                    });

            /**
             * Tournament manager
             */
            NPCAction.register(TOURNAMENT_MANAGER, "sign-up", Tournament::joinTournament);

            /**
             * Ticket exchange
             */
            ObjectAction.register(TICKET_EXCHANGE, 1, (player, obj) -> MANAGER.getDef().shop.open(player));
        }
        /**
         * Crushed barricade
         */
        if(World.isPVP()) {
            ObjectAction.register(CRUSHED_BARRICADE, "climb-over", (player, obj) -> {
                if (player.getAbsX() >= 3082) {
                    player.dialogue(new MessageDialogue("Speak with the " + Color.COOL_BLUE.wrap("Tournament Manager") + " to join this tournament!"));
                } else {
                    player.startEvent(event -> {
                        if (player.joinedTournament) {
                            player.dialogue(new OptionsDialogue("Are you sure you want to leave the tournament?",
                                    new Option("Yes, leave the tournament.", () -> climbBarricade(player, 2, Direction.EAST)),
                                    new Option("No, keep me in the tournament!", player::closeDialogue)));
                        } else {
                            climbBarricade(player, 2, Direction.EAST);
                        }
                    });
                }
            });
        }

        /**
         * Barrier (economy world)
         */
        if(World.isEco()) {
            ObjectAction.register(TOURNAMENT_BARRIER, "use", (player, obj) -> {
                if (player.getAbsX() >= 3112) {
                    player.dialogue(new MessageDialogue("Speak with the " + Color.COOL_BLUE.wrap("Tournament Manager") + " to join this tournament!"));
                } else {
                    player.startEvent(event -> {
                        if (player.joinedTournament) {
                            player.dialogue(new OptionsDialogue("Are you sure you want to leave the tournament?",
                                    new Option("Yes, leave the tournament.", () -> passThroughBarrier(player)),
                                    new Option("No, keep me in the tournament!", player::closeDialogue)));
                        } else {
                            passThroughBarrier(player);
                        }
                    });
                }
            });
        }

        /**
         * Scoreboard
         */
        ObjectAction.register(31846, "read", (player, obj) -> tournamentInformation(player));

        /**
         * Exit portals
         */
        ObjectAction.register(26738, "exit", (player, obj) -> {
            player.dialogue(new OptionsDialogue("Are you sure you want to leave?",
                    new Option("Yes, leave the tournament.", () -> player.getMovement().teleport(LEAVE_BOUNDS)),
                    new Option("No, stay inside the tournament!", player::closeDialogue)
            ));
        });
        ObjectAction.register(26728, "forfeit", (player, obj) -> player.sendFilteredMessage("You can't forfeit you chicken."));
        ObjectAction.register(26727, "forfeit", (player, obj) -> player.sendFilteredMessage("You can't forfeit you chicken."));

        /**
         * Unclip tiles in front of the barricades
         */
        if (World.isPVP()) {
            Tile.get(3083, 3492, 0, true).unflagUnmovable();
            Tile.get(3083, 3493, 0, true).unflagUnmovable();
            Tile.get(3082, 3499, 0, true).unflagUnmovable();
            Tile.get(3082, 3500, 0, true).unflagUnmovable();
            Tile.get(3081, 3501, 0, true).unflagUnmovable();
            Tile.get(3078, 3501, 0, true).unflagUnmovable();
            Tile.get(3077, 3501, 0, true).unflagUnmovable();
            Tile.get(3076, 3499, 0, true).unflagUnmovable();
            Tile.get(3076, 3500, 0, true).unflagUnmovable();
        }
    }

    /**
     * Handlers
     */
    private static boolean checkActive(Player player) {
        Position position = player.getPosition();
        if (position.inBounds(TOURNAMENT_BOUNDS)) {
            if (player.usingTournamentOrbFromHome)
                return false;
            updateInterface(player);
            return true;
        }
        return false;
    }

    private static boolean allowAttack(Player player, Player pTarget, boolean message) {
        return player.getPosition().inBounds(FIGHT_BOUNDS) && (pTarget != null && player.tournamentTarget != null &&
                pTarget == player.tournamentTarget) && player.canAttackTournamentTarget;
    }

    private static boolean allowTeleport(Player player) {
        if (player.joinedTournament) {
            player.dialogue(new MessageDialogue("Coward! You can't teleport from a tournament."));
            return false;
        }
        return true;
    }

    private static void deathStart(Entity entity, Killer killer, Hit hit) {
        Player target = entity.player.tournamentTarget;
        if (target != null) {
            target.clearHits();
            target.cureVenom(0);
            if (!entity.player.wonRound)
                target.wonRound = true;
        }
    }

    private static void handleDeath(Entity entity, Killer killer, Hit hit) {
        World.startEvent(event -> {
            if (entity.player.wonRound) { // this is a double death
                return;
            }
            Player target = entity.player.tournamentTarget;
            loseRound(entity.player);
            progressRound(target, false);
        });
    }

    private static void entered(Player player) {
        if (player.usingTournamentOrbFromHome)
            return;
        player.getPacketSender().sendDiscordPresence("Tournament");
        player.attackPlayerListener = Tournament::allowAttack;
        player.teleportListener = Tournament::allowTeleport;
        player.deathStartListener = Tournament::deathStart;
        player.deathEndListener = Tournament::handleDeath;
        player.openInterface(InterfaceType.PRIMARY_OVERLAY, 88);
        player.openInterface(InterfaceType.SECONDARY_OVERLAY, TOURNAMENT_INTERFACE);
    }

    private static void exited(Player player, boolean logout) {
        player.lock();
        if (player.usingTournamentOrbFromHome)
            return;
        if (player.tournamentTarget != null)
            progressRound(player.tournamentTarget, false);
        player.getPacketSender().sendDiscordPresence("Idle");
        resetPlayer(player);
        resetListeners(player);
        resetInterface(player);
        participants.remove(player);
        player.unlock();
    }

    /**
     * Checks if the {@code player} and another player in the tournament have the same ip address.
     * @param player The player to check.
     * @return {@code true} if someone else has the same ip.
     */
    private static boolean sameIpInTournament(Player player) {
        for (Player participant : participants) {
            if (player.getIpInt() == participant.getIpInt()) {
                return true;
            }
        }
        return false;
    }

}