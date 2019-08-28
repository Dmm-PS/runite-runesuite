package io.ruin.model.activities.raids.tob.wave.impl;

import io.ruin.api.utils.Random;
import io.ruin.cache.Color;
import io.ruin.cache.NPCDef;
import io.ruin.data.impl.npcs.npc_combat;
import io.ruin.model.World;
import io.ruin.model.activities.miscpvm.PassiveCombat;
import io.ruin.model.activities.raids.tob.wave.TheatreOfBloodWave;
import io.ruin.model.activities.raids.tob.wave.TheatreOfBloodWaveStatus;
import io.ruin.model.combat.Hit;
import io.ruin.model.combat.HitType;
import io.ruin.model.entity.npc.NPC;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.shared.listeners.DeathListener;
import io.ruin.model.inter.dialogue.ItemDialogue;
import io.ruin.model.map.Position;
import io.ruin.model.map.Projectile;
import io.ruin.model.map.dynamic.DynamicMap;
import io.ruin.model.map.object.GameObject;
import io.ruin.model.map.object.actions.ObjectAction;

public final class XarpusWave extends TheatreOfBloodWave {

    private static final int XARPUS_IDLE = 8338;
    private static final int XARPUS_HEAL = 8339;
    private static final int XARPUS_COMBAT = 8340;

    private static final int[] TEST_WAVE_SPAWN = { 35, 26 };

    private static final int[] XARPUS_SPAWN = { 33, 34 };

    private static final int EXHUME = 32743;

    /** Number of exhumes is always 25, independent of party size. */
    private static final int NUM_EXHUMES = 25;

    private static final int AWAKE_ANIMATION = 8061;

    private static final int SKELETON = 32741;
    private static final int DAWNBRINGER = 22516;

    private static final Projectile EXHUME_HEAL_PROJECTILE = new Projectile(1550, 11, 55, 40, 75, 12, 48, 64);

    private boolean dawnBringerDiscovered = false;

    public XarpusWave() {
        super("Xarpus");
    }

    @Override
    public DynamicMap buildMap() {
        DynamicMap map = new DynamicMap().build(12612, 1);
        basePosition = new Position(map.swRegion.baseX, map.swRegion.baseY, 1);
        return map;
    }

    @Override
    public void onTheatreStart() {
        ObjectAction.register(SKELETON, "search", (player, obj) -> {
            if (dawnBringerDiscovered) {
                player.sendMessage(Color.RED.tag() + "The dawnbringer has already been discovered.");
                return;
            }

            player.dialogue(new ItemDialogue().one(DAWNBRINGER, "You find the Dawnbringer; you feel a pulse of energy burst through it."));
            player.getInventory().addOrDrop(DAWNBRINGER, 1);
            dawnBringerDiscovered = true;
        });

        NPC xarpus = new NPC(XARPUS_IDLE);
        xarpus.spawn(getPosition(XARPUS_SPAWN));
        xarpus.hitsUpdate.hpBarType = 2;
        xarpus.deathEndListener = (DeathListener.Simple) () -> {
            status = TheatreOfBloodWaveStatus.COMPLETED;
        };

        xarpus.addEvent(event -> {
            while (xarpus.localPlayers().isEmpty() && status != TheatreOfBloodWaveStatus.IN_PROGRESS) {
                event.delay(5);
            }

            for (int i = 0; i < 5; i++) {
                addExhume(xarpus);
                event.delay(16);
            }

            event.delay(1);
            xarpus.transform(XARPUS_COMBAT); // Xarpus combat phase begins
            xarpus.animate(AWAKE_ANIMATION);
        });
    }

    @Override
    public void teleportToTest(Player player) {
        player.getMovement().teleport(getPosition(TEST_WAVE_SPAWN));
    }

    private void addExhume(NPC xarpus) {
        int[] exhumeOffset = { Random.get(-7, 7), Random.get(-7, 7) };
        GameObject object = GameObject.spawn(EXHUME, xarpus.getSpawnPosition().relative(exhumeOffset[0], exhumeOffset[1]), 22, 0);
        Position position = new Position(object.x, object.y, object.z);
        World.startEvent(event -> {
            event.delay(1);
            object.animate(8065);
            for (int i = 0; i < 8; i++) { // Exhumes last for 16 ticks; delay each loop for 2 ticks
                if (object.tile.playerCount == 0) {
                    int xarpusX = xarpus.getSize() / 2;
                    int xarpusY = xarpus.getSize() / 2;
                    int clientDelay = EXHUME_HEAL_PROJECTILE.send(position, xarpus.getSpawnPosition().relative(xarpusX, xarpusY));
                    xarpus.hit(new Hit(HitType.HEAL).fixedDamage(6).clientDelay(clientDelay).postDamage(hit -> {
                        xarpus.incrementHp(6);
                        xarpus.getCombat().getInfo().hitpoints += 6;
                        xarpus.localPlayers().forEach(player -> {
                            player.sendMessage("Xarpus hp : " + xarpus.getHp());
                        });
                    }));

                }
                event.delay(2);
            }
            object.remove();
            World.sendGraphics(1549, 0, 0, position);
        });
    }

    static {
        NPCDef def = NPCDef.get(XARPUS_IDLE);
        def.combatHandlerClass = PassiveCombat.class;
        def.combatInfo = new npc_combat.Info();
        def.combatInfo.hitpoints = 5080;
        def.combatInfo.defend_animation = -1;
        def.combatInfo.death_animation = -1;
        def.ignoreOccupiedTiles = true;
    }
}
