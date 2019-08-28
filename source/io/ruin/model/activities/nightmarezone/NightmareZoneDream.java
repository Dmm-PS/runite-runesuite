package io.ruin.model.activities.nightmarezone;

import io.ruin.api.utils.Random;
import io.ruin.cache.Color;
import io.ruin.model.World;
import io.ruin.model.entity.npc.NPC;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.shared.listeners.DeathListener;
import io.ruin.model.entity.shared.listeners.HitListener;
import io.ruin.model.entity.shared.listeners.LogoutListener;
import io.ruin.model.entity.shared.listeners.TeleportListener;
import io.ruin.model.inter.InterfaceType;
import io.ruin.model.inter.utils.Config;
import io.ruin.model.map.Bounds;
import io.ruin.model.map.MapListener;
import io.ruin.model.map.Position;
import io.ruin.model.map.Tile;
import io.ruin.model.map.dynamic.DynamicMap;
import io.ruin.model.map.object.GameObject;
import io.ruin.model.map.object.actions.ObjectAction;

public final class NightmareZoneDream {

    // Dream vial:
    // Proceed.
    // Not just now.

    private static final Position START = new Position(2275, 4680, 0);

    private static final Position EXIT = new Position(2608, 3115, 0);

    /* As far as I know the NMZ monsters can spawn pretty much anywhere in the arena, although the arena is not a perfect square. */
    private static final Bounds SPAWN_BOUNDS = new Bounds(2256, 4680, 2287, 4711, 0);

    private static final int[] NORMAL_MONSTERS = {
            8528, // Trapped Soul
            6393, // Count Draynor
            7949, // Corsair Traitor
            7895, // Sand Snake
            7997, // Corrupt Lizardman
            6389, // King Roald
            6386, // Moss Giant
            6387, // Skeleton Hellhound
            6391, // Dad
            6390, // Khazard Warlord
            6356, // Ice troll King
            6355, // Bouncer
            6357, // Black Demon
            6382, // Jungle Demon
    };

    private static final int[] HARD_MONSTERS = {
            8529, // Trapped Soul
            6332, // Count Draynor
            7948, // Corsair Traitor
            7894, // Sand Snake
            7996, // Corrupt Lizardman
            6328, // King Roald
            6325, // Moss Giant
            6326, // Skeleton Hellhound
            6330, // Dad
            6329, // Khazard Warlord
            6294, // Ice troll King
            6293, // Bouncer
            6295, // Black Demon
            6321, // Jungle Demon
    };

    private Player player;

    private final NightmareZoneDreamDifficulty difficulty;

    private DynamicMap map;

    public int absorptionPoints = 0;

    private int npcsRemaining;

    private int rewardPointsGained;

    public NightmareZoneDream(Player player, NightmareZoneDreamDifficulty difficulty) {
        this.player = player;
        this.difficulty = difficulty;
        map = createMap();

        player.hitListener = new HitListener().preDamage(hit -> {
            if (absorptionPoints > 0) {
                if (hit.damage > 0) {
                    absorptionPoints = Math.max(0, absorptionPoints - hit.damage);
                    hit.block();
                    Config.NMZ_ABSORPTION.set(player, absorptionPoints);
                    player.sendMessage(Color.DARK_GREEN.wrap("You now have " + absorptionPoints + " hitpoints of damage absorption left."));
                }
            }
        });

        player.deathEndListener = (DeathListener.Simple) () -> {
            player.getMovement().teleport(EXIT);
            player.getPacketSender().fadeIn();
            player.sendMessage("You wake up feeling refreshed.");
            player.nmzRewardPoints += rewardPointsGained;
            player.sendMessage(Color.DARK_GREEN.wrap("You have earned " + rewardPointsGained + " reward points. New total: " + player.nmzRewardPoints));
            player.teleportListener = null;
            player.deathEndListener = null;
        };

        player.teleportListener = p -> {
            p.sendMessage("Drink from the vial at the south of the arena to wake up.");
            return false;
        };

    }

    public void enter() {
        player.set("nmz", this);
        prepareMap();

        map.assignListener(player).onExit((p, logout) -> p.sendMessage("Hi"));

        World.startEvent(event -> {
            player.lock();
            player.getPacketSender().fadeOut();
            event.delay(2);
            player.getMovement().teleport(map.convertPosition(START));
            event.delay(1);
            player.getPacketSender().fadeIn();
            prepareInterface();
            player.sendMessage("Welcome to The Nightmare Zone.");
            player.unlock();

            event.delay(10);
            spawnMonsters();
        });

    }

    private static DynamicMap createMap() {
        DynamicMap arena = new DynamicMap();
        arena.build(9033, 0);
        return arena;
    }

    private void prepareMap() {
        /* Remove KBD stalagmite, add dream potion */
        GameObject potion = new GameObject(26276, map.convertX(2276), map.convertY(4679), 0, 10, 0);
        Tile.getObject(12576, map.convertX(2276), map.convertY(4679), 0).setId(26267);
        Tile.get(map.convertX(2276), map.convertY(4679), 0).addObject(potion.spawn());

        /* Remove KBD lever */
        Tile.getObject(1817, map.convertX(2271), map.convertY(4680), 0).remove();
    }

    private void prepareInterface() {
        player.openInterface(InterfaceType.SECONDARY_OVERLAY, 202);

        // This is a hash of the arena's southwestern-most tile. This is presumably used by the client to differentiate between KBD lair
        int hash = map.convertY(4680) + (map.convertX(2256) << 14);

        // [clientscript,nzone_game_overlay].cs2 -> tile hash does not seem to matter, empty string at end is some sort of unused in-game notification string
        player.getPacketSender().sendClientScript(255, "cs", hash, "");
    }

    private void spawnMonsters() {
        for (int i = 0; i < 4; i++) {
            NPC npc = new NPC(randomMonster());

            Position spawn = map.convertPosition(SPAWN_BOUNDS.randomPosition());
            npc.spawn(spawn);
            map.addNpc(npc);
            npc.face(player);

            npc.deathEndListener = (DeathListener.Simple) () -> {
                rewardPointsGained += Random.get(20, 45) * (difficulty == NightmareZoneDreamDifficulty.NORMAL ? 1.0 : 1.85);
                Config.NMZ_POINTS.set(player, rewardPointsGained);
                npcsRemaining--;
                map.removeNpc(npc);
                if (npcsRemaining == 0) {
                    spawnMonsters();
                }
            };

            npc.getCombat().setAllowRespawn(false);
            npc.targetPlayer(player, false);
            npc.attackTargetPlayer();

            npcsRemaining++;
        }
    }

    private int randomMonster() {
        return Random.get(difficulty == NightmareZoneDreamDifficulty.NORMAL ? NORMAL_MONSTERS : HARD_MONSTERS);
    }

    private void end(boolean logout) {
        player.nmzRewardPoints += rewardPointsGained;

        if (!logout) {
            player.getPacketSender().fadeIn();
            player.sendMessage("You wake up feeling refreshed.");
            player.sendMessage(Color.DARK_GREEN.wrap("You have earned " + rewardPointsGained + " reward points. New total: " + player.nmzRewardPoints));
        }

        player.teleportListener = null;
        player.deathEndListener = null;
        dispose();
    }

    private void dispose() {
        map.destroy();
        map = null;
        player = null;
    }

    static {
        ObjectAction.register(26276, 1, (player, obj) -> {
            player.getMovement().teleport(EXIT);
            NightmareZoneDream dream = player.get("nmz");
            dream.end(false);
        });
    }

}