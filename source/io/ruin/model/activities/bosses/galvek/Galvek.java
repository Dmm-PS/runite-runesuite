package io.ruin.model.activities.bosses.galvek;

import io.ruin.api.utils.Random;
import io.ruin.cache.Color;
import io.ruin.model.World;
import io.ruin.model.combat.AttackStyle;
import io.ruin.model.combat.Hit;
import io.ruin.model.entity.Entity;
import io.ruin.model.entity.npc.NPC;
import io.ruin.model.entity.npc.NPCCombat;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.shared.LockType;
import io.ruin.model.entity.shared.StepType;
import io.ruin.model.entity.shared.listeners.HitListener;
import io.ruin.model.inter.dialogue.OptionsDialogue;
import io.ruin.model.inter.utils.Option;
import io.ruin.model.map.Direction;
import io.ruin.model.map.Position;
import io.ruin.model.map.Projectile;
import io.ruin.model.map.dynamic.DynamicMap;
import io.ruin.model.map.object.GameObject;
import io.ruin.model.map.object.actions.ObjectAction;
import io.ruin.utility.Misc;
import io.ruin.utility.TickDelay;

/**
 * Handles combat functionality for the Galvek boss from OSRS.
 *
 * @author Andys1814
 */
public final class Galvek extends NPCCombat {

    private static final Position START = new Position(1631, 5723, 2);

    private static final int TSUNAMI = 8099;
    private static final int FIRE_TRAP = 32378;

    private static final Projectile FIRE_MAGIC = new Projectile(1490, 60, 31, 40, 25, 8, 16, 128);
    private static final Projectile DRAIN = new Projectile(1496, 60, 31, 40, 25, 8, 16, 128);
    private static final Projectile AIR_RANGE_RED = new Projectile(1489, 60, 31, 40, 25, 8, 16, 128);
    private static final Projectile RED_DRAGONFIRE = new Projectile(393, 60, 31, 40, 25, 8, 16, 128);
    private static final Projectile PURPLE_DRAGONFIRE = new Projectile(1471, 60, 31, 40, 25, 8, 16, 128);
    private static final Projectile FIRE_ARC_ATTACK = new Projectile(1481, 125, 31, 40, 75, 8, 40, 64);
    private static final Projectile BOULDER_PROJECTILE = new Projectile(1493, 60, 15, 40, 25, 12, 16, 64);

    private GalvekForm form = GalvekForm.FIRE;
    private final TickDelay transitionDelay = new TickDelay();
    private boolean tsunamiActive = false;
    private boolean fireTrapsActive = false;
    private boolean avoidFireArcAttack = false;

    private static final int[][] FIRE_TRAP_OFFSETS = {
            {0, -3}, // Nearest to Galvek
            {6, -3},

            {1, -7},
            {5, -7},

            {0, -11},
            {6, -11},

            {1, -15}, // Furthest from Galvek
            {5, -15},
    };

    @Override
    public void init() {
        npc.hitsUpdate.hpBarType = 2;
        setAllowRespawn(true);
        npc.hitListener = new HitListener().postDamage(this::postDamage);
        npc.deathEndListener = (entity, killer, killHit) -> onDeath(killer.player);
        form = GalvekForm.FIRE;
    }

    @Override
    public boolean isAggressive() {
        return true;
    }

    private void postDamage(Hit hit) {
        if (form == GalvekForm.FIRE && npc.getHp() <= 900) {
            transition(GalvekForm.WIND);
            npc.setHp(900); // Each form begins with its upper-limit hp (this is how OSRS does it)
        } else if (form == GalvekForm.WIND && npc.getHp() <= 600) {
            transition(GalvekForm.WATER);
            npc.setHp(600);
        } else if (form == GalvekForm.WATER && npc.getHp() <= 300) {
            transition(GalvekForm.EARTH);
            npc.setHp(300);
        }
    }

    private void transition(GalvekForm form) {
        if (this.form == form) { // Should never happen but whatever
            return;
        }

        this.form = form;
        transitionDelay.delay(form == GalvekForm.EARTH ? 7 : 6); // Transition animation for earth is one tick longer

        npc.addEvent(event -> {
            npc.lock(LockType.FULL);
            npc.faceNone(false);
            npc.animate(7906);
            event.delay(2);
            Position destination = npc.getPosition().copy().translate(form.xOffset, form.yOffset, 0);
            npc.transform(form.npcId);
            npc.getMovement().teleport(destination);

            npc.animate(form == GalvekForm.EARTH ? 7908 : 7907);

            if (target == null) {
                target = npc.localPlayers().get(0); // In case the player walks out of range while Galvek is transitioning
            }
            faceTarget();
            target.player.sendMessage("The dragon embodies the element of " + form.element + ".");
            npc.unlock();
        });
    }

    @Override
    public void follow() {
        // Prevent Galvek from moving
    }

    @Override
    public boolean attack() {
        if (transitionDelay.isDelayed()) {
            return false;
        }

        if ((form == GalvekForm.FIRE || form == GalvekForm.EARTH) && Misc.getEffectiveDistance(npc, target) <= 1) {
            basicAttack(7900, AttackStyle.SLASH, 28).ignoreDefence();
            avoidFireArcAttack = false;
            return true;
        }

        if (form == GalvekForm.FIRE && Random.rollPercent(15) && !fireTrapsActive) {
            fireTrapAttack();
            fireTrapsActive = true;
            avoidFireArcAttack = false;
            return true;
        }

        if (form == GalvekForm.WIND && Random.rollPercent(35)) {
            drainAttack();
            avoidFireArcAttack = false;
            return true;
        }

        if (form == GalvekForm.WATER && Random.rollPercent(20) && !tsunamiActive) {
            tsunamiAttack();
            tsunamiActive = true;
            avoidFireArcAttack = false;
            return true;
        }

        // Lower chance of consecutive boulder attacks
        if (form == GalvekForm.EARTH && Random.rollPercent(target.player.isStunned() ? 10 : 25)) {
            boulderAttack();
            avoidFireArcAttack = false;
            return true;
        }

        if (Random.rollPercent(20)) {
            prayerDeactivateAttack();
            avoidFireArcAttack = false;
            return true;
        }

        // Higher chance of fire arc attack when the target is stunned due to the earth form boulder attack
        if (Random.rollPercent(target.player.isStunned() ? 75 : 20) && !avoidFireArcAttack) {
            fireArcAttack();
            delayAttack(-2);
            avoidFireArcAttack = true; // Prevents double fire arc attacks (because it resets attack ticks)
            return true;
        }

        if (Random.rollPercent(20)) {
            projectileAttack(AIR_RANGE_RED, form.basicAttackAnimation, AttackStyle.RANGED, info.max_damage).ignoreDefence();
            avoidFireArcAttack = false;
            return true;
        }

        if (Random.rollPercent(50)) {
            projectileAttack(FIRE_MAGIC, form.basicAttackAnimation, AttackStyle.MAGIC, info.max_damage).ignoreDefence();
        } else {
            projectileAttack(RED_DRAGONFIRE, form.basicAttackAnimation, AttackStyle.DRAGONFIRE, info.max_damage).penetrateDragonfireResistance(0.2).ignoreDefence();
        }

        avoidFireArcAttack = false;
        return true;
    }

    private void prayerDeactivateAttack() {
        npc.animate(form.basicAttackAnimation);
        int delay = PURPLE_DRAGONFIRE.send(npc, target);
        target.hit(new Hit(npc, AttackStyle.DRAGONFIRE).penetrateDragonfireResistance(0.2).randDamage(info.max_damage).clientDelay(delay).postDamage(entity -> {
            if (entity.player != null) {
                entity.player.getPrayer().deactivateAll();
                entity.player.sendMessage(Color.RED.wrap("Your prayers have been disabled!"));
                entity.player.graphics(1473, 55, 0);
            }
        }));
    }

    private void fireArcAttack() {
        npc.animate(7910);
        Position targetPosition = target.getPosition().copy();
        int clientDelay = FIRE_ARC_ATTACK.send(npc, targetPosition);
        int tickDelay = ((25 * clientDelay) / 600) - 2;
        World.sendGraphics(1466, 0, clientDelay, targetPosition);
        npc.addEvent(event -> {
            event.setCancelCondition(() -> isDead() || target == null);
            event.delay(tickDelay);
            int distance = target.player.getPosition().distance(targetPosition);
            if (distance <= 1) { // One tile away is still half damage
                target.hit(new Hit().randDamage(distance == 1 ? 30 : 60, distance == 1 ? 50 : 109).ignorePrayer().ignoreDefence());
            }
        });
    }

    private void fireTrapAttack() {
        npc.animate(7910);
        for (int[] offsetCoordinates : FIRE_TRAP_OFFSETS) {
            int offsetX = offsetCoordinates[0];
            int offsetY = offsetCoordinates[1];
            Position destination = npc.getPosition().copy().translate(offsetX, offsetY, 0);

            if (offsetY == -3) {
                Projectile projectile = new Projectile(1495, 105, 15, 40, 50, 9, 40, 64);
                projectile.send(npc, destination);
            } else if (offsetY == -7) {
                Projectile projectile = new Projectile(1495, 105, 15, 40, 50, 5, 40, 64);
                projectile.send(npc, destination);
            } else if (offsetY == -11) {
                Projectile projectile = new Projectile(1495, 105, 15, 40, 50, 3, 40, 64);
                projectile.send(npc, destination);
            } else if (offsetY == -15) {
                Projectile projectile = new Projectile(1495, 105, 15, 40, 50, 2, 40, 64);
                projectile.send(npc, destination);
            }

            npc.addEvent(event -> {
                event.setCancelCondition(() -> this.isDead() || target == null || target.getCombat().isDead());
                event.delay(2);
                GameObject obj = GameObject.spawnUnclipped(FIRE_TRAP, destination.getX(), destination.getY(), 2, 10, 0);
                for (int i = 0; i < 30; i++) {
                    if (form != GalvekForm.FIRE) {
                        obj.remove();
                        World.sendGraphics(1466, 0, 0, destination);
                        fireTrapsActive = false;
                        return;
                    }

                    if (target != null && Misc.getDistance(target.player.getPosition(), destination) <= 1) {
                        target.player.hit(new Hit().fixedDamage(Math.min(200, target.player.getHp())).clientDelay(0).ignorePrayer());
                        obj.remove();
                        World.sendGraphics(1466, 0, 0, destination);
                        break;
                    }

                    event.delay(1);
                }

                if (!obj.isRemoved()) {
                    obj.remove();
                    World.sendGraphics(1466, 0, 0, destination);
                    fireTrapsActive = false;
                }
            });
        }
    }

    private void drainAttack() {
        npc.animate(form.basicAttackAnimation);
        int clientDelay = DRAIN.send(npc, target);
        int tickDelay = (25 * clientDelay) / 600;
        npc.addEvent(event -> {
            event.setCancelCondition(() -> isDead() || target == null || target.getCombat().isDead());
            event.delay(tickDelay);
            target.player.getPrayer().drain(Random.get(2, 5)); // Not sure on exact amount, something similar to this
            target.player.getMovement().drainEnergy(Random.get(20, 50)); // Again, not sure on exact amount
            target.player.sendMessage(Color.RED.wrap("You feel drained!"));
        });
    }

    private void tsunamiAttack() {
        npc.animate(7910);
        TsunamiDirection direction = TsunamiDirection.random();

        int gap = Random.get(0, 10);
        for (int i = 0; i < 11; i++) {
            if (i == gap) {
                continue;
            }

            Position pos = npc.getPosition().copy().translate(direction.offsetX + i, direction.offsetY, 0);
            Projectile projectile = new Projectile(1497, 105, 15, 40, 115 + (2 / (i + 1)), 0, 30, 64);
            projectile.send(npc, pos);

            NPC tsunami = new NPC(TSUNAMI);
            tsunami.getDef().isMinimapVisible = false;

            npc.addEvent(event -> {
                event.delay(2);
                tsunami.spawn(pos.getX(), pos.getY(), 2, direction == TsunamiDirection.NORTHERN ? Direction.SOUTH : Direction.NORTH, 0);

                tsunami.addEvent(tsunamiEvent -> {
                    tsunami.lock();
                    for (int j = 0; j < 20; j++) {
                        if (isDead() || form != GalvekForm.WATER || target == null || target.getCombat().isDead()) {
                            break;
                        }

                        tsunami.step(0, direction == TsunamiDirection.NORTHERN ? -1 : 1, StepType.FORCE_WALK);
                        if (target != null && target.player.getPosition().equals(tsunami.getPosition())) {
                            target.player.hit(new Hit().randDamage(109, 132));
                            break;
                        }

                        tsunamiEvent.delay(1);
                    }
                    tsunami.animate(7879);
                    tsunamiEvent.delay(1); // Delay so wave collapse animation can finish
                    tsunamiActive = false;
                    tsunami.unlock();
                    tsunami.remove();
                });
            });
        }
    }

    private void boulderAttack() {
        npc.animate(form.basicAttackAnimation);
        Position targetPosition = target.getPosition().copy();
        int clientDelay = BOULDER_PROJECTILE.send(npc, targetPosition);
        int tickDelay = ((25 * clientDelay) / 600) - 2;
        npc.addEvent(event -> {
            event.setCancelCondition(() -> this.isDead() || target == null || target.getCombat().isDead());
            event.delay(tickDelay);
            if (target.player.getPosition().equals(targetPosition)) {
                World.sendGraphics(1494, 0, 0, targetPosition);
                target.stun(3, true);
            }
        });
    }

    @Override
    public boolean allowRetaliate(Entity attacker) {
        return !transitionDelay.isDelayed() && super.allowRetaliate(attacker);
    }

    private void onDeath(Player player) {
        form = GalvekForm.FIRE;
        if (player != null && player.galvekTimer != null) {
            player.galvekBestTime = player.galvekTimer.stop(player, player.galvekBestTime);
        }
    }

    public static void enter(Player player) {
        DynamicMap map = new DynamicMap().build(6489, 3);
        NPC galvek = new NPC(GalvekForm.FIRE.npcId);
        galvek.spawn(map.swRegion.baseX + 28, map.swRegion.baseY + 37, 2);
        map.addNpc(galvek);

        player.addEvent(event -> {
            player.lock();
            player.getPacketSender().fadeOut();
            event.delay(2);
            player.getMovement().teleport(map.convertPosition(START));
            map.assignListener(player).onExit((p, logout) -> {
                if (logout) {
                    p.getMovement().teleport(1665, 10048, 0);
                }
                map.destroy();
            });
            event.delay(1);
            player.getPacketSender().fadeIn();
            player.sendMessage("The dragon embodies the element of fire.");
            player.unlock();
        });
    }

    private static GameObject ROW_BOAT = GameObject.spawn(27066, 3386, 3646, 0, 10, 0);

    static {
        ObjectAction.register(ROW_BOAT, 1, (player, obj) -> {
            player.dialogue(new OptionsDialogue("Enter Galvek's boat?",
                    new Option("Yes", () -> {
                        enter(player);
                    }),
                    new Option(("No"))));
        });
    }
}
