package io.ruin.model.activities.pestcontrol;

import io.ruin.Server;
import io.ruin.api.utils.Random;
import io.ruin.cache.NPCDef;
import io.ruin.data.impl.npcs.npc_combat;
import io.ruin.model.World;
import io.ruin.model.activities.miscpvm.PassiveCombat;
import io.ruin.model.activities.pestcontrol.pests.PestType;
import io.ruin.model.entity.npc.NPC;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.player.PlayerGroup;
import io.ruin.model.entity.shared.listeners.DeathListener;
import io.ruin.model.entity.shared.listeners.HitListener;
import io.ruin.model.entity.shared.listeners.LogoutListener;
import io.ruin.model.inter.InterfaceType;
import io.ruin.model.inter.dialogue.NPCDialogue;
import io.ruin.model.inter.utils.Config;
import io.ruin.model.map.Position;
import io.ruin.model.map.dynamic.DynamicMap;

import java.util.*;

/**
 * A single Pest Control game instance that manages all in-game functionality and behavior.
 *
 * @author Andys1814
 */
public final class PestControlGame {

    private static final Position START = new Position(2658, 2611, 0);

    private static final Position EXIT = new Position(2657, 2639, 0);

    private static final Position VOID_KNIGHT_POSITION = new Position(2656, 2592, 0);

    private List<Player> players;

    private final PestControlLander lander;

    private DynamicMap map;

    /* 2950, 2951, 2952, 2953 <- idk the difference */
    private NPC voidKnight = new NPC(2950);

    private final HashMap<PestControlPortal, NPC> portals = new LinkedHashMap<>();
    private final HashMap<PestControlPortal, PestControlPortalStatus> statuses = new LinkedHashMap<>();

    private final int SPINNER_EFFECT_INTERVAL = 12;

    private int minutesRemaining = 20;

    PestControlGame(List<Player> players, PestControlLander lander) {
        this.players = players;
        this.lander = lander;
        map = new DynamicMap().build(10536, 1);
        players.forEach(player -> {
            map.assignListener(player).onExit((p, logout) -> {
                player.deathEndListener = null;
                player.pestControlParticipation = 0;
                player.logoutListener = null;
                player.closeInterface(InterfaceType.PRIMARY_OVERLAY);
                players.remove(player);
            });
            player.logoutListener = new LogoutListener().onLogout(action -> {
                player.pestControlParticipation = 0;
                player.getMovement().teleport(EXIT);
                players.remove(player);
            });
            player.deathEndListener = (DeathListener.Simple) () -> player.getMovement().teleport(map.convertPosition(START));
        });
    }

    public void start() {
        initEnvironment();
        players.forEach(player -> {
            player.getPacketSender().setHidden(408, 12, true);
            player.pestControlParticipation = 0;
            player.getMovement().teleport(map.convertPosition(START));
            player.openInterface(InterfaceType.PRIMARY_OVERLAY, 408);
            player.dialogue(new NPCDialogue(1771, "You must defend the Void Knight while the portals are " +
                    "unsommoned. The ritual takes twenty minutes though, " +
                    "so you can help out by destroying them yourselves. " +
                    "Now GO GO GO!"));
        });
        initialInterface();

        map.addEvent(event -> {
            while (true) {
                if (players.isEmpty())
                    endGame(null);
                event.delay(1);
            }
        });

        map.addEvent(event -> {
            while (true) {
                spinnerHeal();
                event.delay(SPINNER_EFFECT_INTERVAL);
            }
        });

        map.addEvent(event -> {
            while (true) {
                event.delay(100);
                minutesRemaining--;
                if (minutesRemaining == 0) {
                    endGame(PestControlEndPolicy.TIME_OUT);
                }
                refreshTimer();
            }
        });
    }

    private void initEnvironment() {
        NPCDef def = voidKnight.getDef();
        def.combatHandlerClass = PassiveCombat.class;
        def.combatInfo = new npc_combat.Info();
        def.combatInfo.hitpoints = 200;
        def.combatInfo.defend_animation = -1;
        def.combatInfo.spawn_animation = -1;
        def.ignoreMultiCheck = true;
        voidKnight.hitListener = new HitListener().postDamage(hit -> {
            if (hit.attacker == null) {
                Server.logError(new Throwable("Why does this happen!?!?!?!?"));
                return;
            }

            if (hit.attacker.player != null) {
                hit.block();
            }

            players.forEach(player -> {
                player.getPacketSender().sendString(408, 7, "" + voidKnight.getHp());
            });
        });

        voidKnight.deathEndListener = (DeathListener.Simple) () -> World.startEvent(event -> {
            event.delay(2);
            endGame(PestControlEndPolicy.KNIGHT_PERISHED);
        });
        voidKnight.spawn(map.convertPosition(VOID_KNIGHT_POSITION));
        map.addNpc(voidKnight);
        voidKnight.set("pest_control", this);
        initPortals();
    }


    private void initPortals() {
        // Todo fix this method.
        for (PestControlPortal portal : PestControlPortal.VALUES) {
            NPC npc = new NPC(portal.getNpcId()).spawn(map.convertPosition(portal.getPosition()));
            portals.put(portal, npc);
            statuses.put(portal, PestControlPortalStatus.SHIELDED);
            map.addNpc(npc);
        }

        /* Deactivate portal shields, the first one after 15 seconds, and the rest staggered by 30 seconds */
        map.addEvent(event -> {
            event.delay(25);
            deactivatePortalShield(PestControlPortal.WEST);
            event.delay(50);
            deactivatePortalShield(PestControlPortal.EAST);
            event.delay(50);
            deactivatePortalShield(PestControlPortal.SOUTHEAST);
            event.delay(50);
            deactivatePortalShield(PestControlPortal.SOUTHWEST);
        });

        portals.keySet().forEach(this::spawnPests);
    }

    private void deactivatePortalShield(PestControlPortal portal) {
        String message = "The " + portal.getName() + ", " + portal.getColor() + " portal shield has dropped!";
        voidKnight.forceText(message);
        players.forEach(player -> player.sendMessage(message));
        Optional<NPC> shieldedPortal = map.getNpcs().stream().filter(npc -> npc.getId() == portal.getNpcId()).findFirst();
        statuses.replace(portal, PestControlPortalStatus.UNSHIELDED);
        shieldedPortal.ifPresent(npc -> { //TODO this should just transform
//            map.removeNpc(npc);
//            npc.remove();
//            NPC unshielded = new NPC(portal.getUnshieldedNpcId());
//            unshielded.spawn(map.convertPosition(portal.getPosition()));
//            map.addNpc(unshielded);
            npc.transform(portal.getNpcId() - 4);
            portals.replace(portal, npc);
            npc.hitListener = new HitListener().postDamage(hit -> {
                if (hit.attacker == null || hit.attacker.player == null) {
                    Server.logError(new Throwable("Why does this happen!?!?!?!? in shield one"));
                    return;
                }

                players.forEach(player -> player.getPacketSender().sendString(408, portal.getHealthChildId(), "" + npc.getHp()));

                Player player = hit.attacker.player;
                if (player != null) {
                    player.pestControlParticipation += hit.damage;
                    String str = (player.pestControlParticipation >= 100 ? "<col=5ebb29>" : "<col=ff3333>") + player.pestControlParticipation + " </col>";
                    player.getPacketSender().sendString(408, 8, str);
                }
            });
            npc.deathEndListener = (DeathListener.Simple) () -> {
                map.removeNpc(npc);
                statuses.replace(portal, PestControlPortalStatus.DEAD);
                if (checkPortalDeaths()) {
                    endGame(PestControlEndPolicy.WIN);
                }
                voidKnight.incrementHp(50); // Each portal death heals the Void Knight
            };
        });

        players.forEach(player -> player.getPacketSender().setHidden(408, portal.getShieldIconChildId(), true));
    }

    private void spawnPests(PestControlPortal portal) {
        map.addEvent(event -> {
            while (true) {
                event.setCancelCondition(() -> statuses.get(portal) == PestControlPortalStatus.DEAD);
                int id = Random.get(PestType.VALUES).random();
                NPC pest = new NPC(id);
                try {
                    pest.set("pest_control", this);
                    pest.spawn(map.convertPosition(portal.getPestSpawnPosition()));
                    map.addNpc(pest);
                    pest.getCombat().setAllowRespawn(false);
                    registerHitListener(pest);
                } catch (Exception e) {
                    System.err.println("Error spawning npc: " + id);
                }
                event.delay(20); // Not sure about the spawn rate; consider changing this
            }
        });
    }

    private void registerHitListener(NPC npc) {
        npc.hitListener = new HitListener().postDamage(hit -> {
            if (hit.attacker == null) {
                Server.logError(new Throwable("Why does this happen!?!?!?!?"));
                return;
            }

            Player player = hit.attacker.player;
            if (player != null) {
                player.pestControlParticipation += hit.damage;
                String str = (player.pestControlParticipation >= 100 ? "<col=5ebb29>" : "<col=ff3333>") + player.pestControlParticipation + " </col>";
                player.getPacketSender().sendString(408, 8, str);
            }
        });
    }

    private void spinnerHeal() {
        portals.forEach((pestControlPortal, npc) -> {
            if (statuses.get(pestControlPortal) == PestControlPortalStatus.UNSHIELDED) {
                for (NPC pest : npc.localNpcs()) {
                    if (pest.getDef().name.equals("Spinner")) {
                        if (npc.getHp() == 200)
                            continue;
                        if (npc.getHp() + 25 > 200) {
                            npc.incrementHp(200 - npc.getHp());
                        } else
                            npc.incrementHp(25);
                    }
                }
            }
        });
    }

    private void refreshTimer() {
        players.forEach(player -> player.getPacketSender().sendString(408, 6, minutesRemaining + " min"));
    }

    private void initialInterface() {
        players.forEach(player -> {
            player.getPacketSender().sendString(408, 6, minutesRemaining + " min");
            player.getPacketSender().sendString(408, 7, "" + voidKnight.getHp());
            for (int i = 23; i < 27; i++) {
                player.getPacketSender().sendString(408, i, "200");
            }
        });
    }

    private void endGame(PestControlEndPolicy pestControlEndPolicy) {
        if (pestControlEndPolicy == null) {
            Server.logError(new Throwable("Game ended with policy of null"));
        }
        players.forEach(player -> {
            player.getMovement().teleport(EXIT);
            switch (pestControlEndPolicy) {
                case WIN:
                    if (player.pestControlParticipation >= 100) {
                        int points = lander.pointsPerWin + getAdditionalPoints(player);
                        player.dialogue(new NPCDialogue(1771,
                                "Congratulations! You managed to destroy all the portals!" +
                                        " We've awarded you " + points + " " +
                                        "Void Knight Commendation points. Please also accept these coins as a reward."));
                        player.voidKnightCommendationPoints += points;
                        player.getInventory().addOrDrop(995, player.getCombat().getLevel() * 10);
                    } else {
                        player.dialogue(new NPCDialogue(1771,
                                "You did not do enough damage on the monsters" +
                                        "To get a reward! Try harder next time."));
                    }
                    break;
                case TIME_OUT:
                    player.dialogue(new NPCDialogue(1771,"The time has ran out and the game finished!"));
                    break;
                case KNIGHT_PERISHED:
                    player.dialogue(new NPCDialogue(1771,"The Void Knight has perished, and all hope has been lost."));
                    break;
            }

            player.deathEndListener = null;
            player.logoutListener = null;
            player.pestControlParticipation = 0;
            player.getStats().restore(false);
            player.getPrayer().deactivateAll();
            player.getMovement().restoreEnergy(100);
            Config.SPECIAL_ENERGY.set(player, 1000);
            player.cureVenom(0);

        });
        dispose();
    }

    private void dispose() {
        players = null;
        map.destroy();
        map = null;
    }

    public static PestControlGame getInstance(NPC npc) {
        return npc.get("pest_control");
    }

    private boolean checkPortalDeaths() {
        return statuses.values().stream().allMatch(status -> status == PestControlPortalStatus.DEAD);
    }

    private int getAdditionalPoints(Player player) {
        if (player.isGroup(PlayerGroup.GODLIKE_DONATOR)) {
            return 5;
        } else if (player.isGroup(PlayerGroup.UBER_DONATOR)) {
            return 4;
        } else if (player.isGroup(PlayerGroup.LEGENDARY_DONATOR)) {
            return 3;
        } else if (player.isGroup(PlayerGroup.EXTREME_DONATOR)) {
            return 2;
        } else if (player.isGroup(PlayerGroup.SUPER_DONATOR) || player.isGroup(PlayerGroup.DONATOR)) {
            return 1;
        }
        return 0;
    }

    public DynamicMap getMap() {
        return map;
    }

    public NPC getVoidKnight() {
        return voidKnight;
    }

    public enum PestControlEndPolicy {
        TIME_OUT,
        KNIGHT_PERISHED,
        WIN
    }
}
