package io.ruin.model.item.actions.impl;

import io.ruin.cache.ItemDef;
import io.ruin.model.activities.duelarena.DuelRule;
import io.ruin.model.activities.nightmarezone.NightmareZoneDream;
import io.ruin.model.activities.raids.xeric.ChambersOfXeric;
import io.ruin.model.combat.Hit;
import io.ruin.model.entity.player.Player;
import io.ruin.model.inter.Widget;
import io.ruin.model.inter.utils.Config;
import io.ruin.model.item.Item;
import io.ruin.model.item.actions.ItemAction;
import io.ruin.model.item.containers.Equipment;
import io.ruin.model.skills.herblore.Potion;
import io.ruin.model.stat.Stat;
import io.ruin.model.stat.StatType;

import java.util.function.Consumer;

public class Consumable {

    /**
     * Eating
     */

    static {
        registerEat(1942, 1, p -> p.sendFilteredMessage("You eat the potato. Yuck!"));
        registerEat(1965, 1, p -> p.sendFilteredMessage("You eat the cabbage. Yuck!"));

        registerEat(1963, 2, "banana");
        registerEat(2162, 2, "king worm");
        registerEat(6883, 8, "peach");

        registerEat(2309, 5, "bread");
        registerCake(1891, 1893, 1895, 12, "cake");
        registerCake(1897, 1899, 1901, 15, "chocolate cake");

        registerPizza(2289, 2291, 14, "pizza");
        registerPizza(2293, 2295, 16, "meat pizza");
        registerPizza(2297, 2299, 18, "anchovy pizza");
        registerPizza(2301, 2303, 22, "pineapple pizza");

        registerPie(2325, 2333, 10, "redberry pie", null);
        registerPie(2327, 2331, 12, "meat pie", null);
        registerPie(2323, 2335, 14, "apple pie", null);
        registerPie(7178, 7180, 12, "garden pie", p -> p.getStats().get(StatType.Farming).boost(3, 0.0));
        registerPie(7188, 7190, 12, "fish pie", p -> p.getStats().get(StatType.Fishing).boost(3, 0.0));
        registerPie(7198, 7200, 16, "admiral pie", p -> p.getStats().get(StatType.Fishing).boost(5, 0.0));
        registerPie(7218, 7220, 22, "summer pie", p -> p.getStats().get(StatType.Agility).boost(5, 0.0));
        registerPie(7208, 7210, 22, "wild pie", p -> {
            p.getStats().get(StatType.Ranged).boost(4, 0.0);
            p.getStats().get(StatType.Slayer).boost(5, 0.0);
        });

        registerEat(7082, 1923, 5, "fried mushrooms");
        registerEat(2011, 1923, 19, "curry");

        registerEat(7054, 14, "chilli potato");
        registerEat(7058, 20, "mushroom potato");
        registerEat(6705, 16, "potato with cheese");
        registerEat(7060, 22, "tuna potato");

        registerEat(2140, 4, "chicken");
        registerEat(2142, 4, "meat");

        registerEat(315, 3, "shrimps");
        registerEat(325, 4, "sardine");
        registerEat(347, 5, "herring");
        registerEat(355, 6, "mackerel");
        registerEat(333, 7, "trout");
        registerEat(339, 7, "cod");
        registerEat(351, 8, "pike");
        registerEat(329, 9, "salmon");
        registerEat(361, 10, "tuna");
        registerEat(379, 12, "lobster");
        registerEat(365, 13, "bass");
        registerEat(373, 14, "swordfish");
        registerEat(7946, 16, "monkfish");
        registerEat(385, 20, "shark");
        registerEat(397, 21, "sea turtle");
        registerEat(391, 22, "manta ray");
        registerEat(11936, 22, "dark crab");

        registerEat(20856, 4, "pysk");
        registerEat(20858, 8, "suphi");
        registerEat(20860, 8, "leckish");
        registerEat(20862, 12, "brawk");
        registerEat(20864, 17, "mycril");
        registerEat(20866, 20, "roqed");
        registerEat(20868, 23, "kyren");

        registerEat(20871, 4, "guanic");
        registerEat(20873, 8, "praeal");
        registerEat(20875, 8, "giral");
        registerEat(20877, 12, "phluxia");
        registerEat(20879, 17, "kryket");
        registerEat(20881, 20, "murng");
        registerEat(20883, 23, "psykk");

        ItemDef.get(3144).consumable = true;
        ItemAction.registerInventory(3144, "eat", (player, item) -> {
            if(eatKaram(player, item))
                player.sendFilteredMessage("You eat the karambwan.");
        });

        ItemDef.get(13441).consumable = true;
        ItemAction.registerInventory(13441, "eat", (player, item) -> {
            if(eatAngler(player, item))
                player.sendFilteredMessage("You eat the anglerfish.");
        });

        ItemAction.registerInventory(7510, actions -> {
            actions[1] = null; //eat
            actions[3] = null; //guzzle
        });
    }

    private static void registerEat(int id, int heal, String name) {
        registerEat(id, -1, heal, 3, p -> p.sendFilteredMessage("You eat the " + name + "."));
    }

    private static void registerEat(int id, int newId, int heal, String name) {
        registerEat(id, newId, heal, 3, p -> p.sendFilteredMessage("You eat the " + name + "."));
    }

    private static void registerEat(int id, int heal, Consumer<Player> eatAction) {
        registerEat(id, -1, heal, 3, eatAction);
    }

    private static void registerEat(int id, int newId, int heal, int ticks, Consumer<Player> eatAction) {
        ItemDef.get(id).consumable = true;
        ItemAction.registerInventory(id, "eat", (player, item) -> {
            if(eat(player, item, newId, heal, ticks))
                eatAction.accept(player);
        });
    }

    private static void registerCake(int firstId, int secondId, int thirdId, int heal, String name) {
        heal /= 3;
        registerEat(firstId, secondId, heal, 2, p -> p.sendFilteredMessage("You eat part of the " + name + "."));
        registerEat(secondId, thirdId, heal, 2, p -> p.sendFilteredMessage("You eat some more of the " + name + "."));
        registerEat(thirdId, -1, heal, 3, p -> p.sendFilteredMessage("You eat the slice of " + name + "."));
    }

    private static void registerPizza(int fullId, int halfId, int heal, String name) {
        heal /= 2;
        registerEat(fullId, halfId, heal, 1, p -> p.sendFilteredMessage("You eat half of the " + name + "."));
        registerEat(halfId, -1, heal, 2, p -> p.sendFilteredMessage("You eat the remaining " + name + "."));
    }

    private static void registerPie(int fullId, int halfId, int heal, String name, Consumer<Player> postEffect) {
        heal /= 2;
        registerEat(fullId, halfId, heal, 1, p -> {
            p.sendFilteredMessage("You eat half of the " + name + ".");
            if(postEffect != null)
                postEffect.accept(p);
        });
        registerEat(halfId, -1, heal, 2, p -> {
            p.sendFilteredMessage("You eat the remaining " + name + ".");
            if(postEffect != null)
                postEffect.accept(p);
        });
    }

    private static boolean eat(Player player, Item item, int newId, int heal, int ticks) {
        if(player.isLocked() || player.isStunned())
            return false;
        if(player.eatDelay.isDelayed() || player.karamDelay.isDelayed() || player.potDelay.isDelayed())
            return false;
        if(DuelRule.NO_FOOD.isToggled(player)) {
            player.sendMessage("Food has been disabled for this duel!");
            return false;
        }
        if(newId == -1)
            item.remove();
        else
            item.setId(newId);
        animEat(player);
        player.incrementHp(heal);
        player.eatDelay.delay(ticks);
        player.getCombat().delayAttack(3);
        return true;
    }

    private static boolean eatKaram(Player player, Item item) {
        if(player.isLocked() || player.isStunned())
            return false;
        if(player.karamDelay.isDelayed())
            return false;
        if(DuelRule.NO_FOOD.isToggled(player)) {
            player.sendMessage("Food has been disabled for this duel!");
            return false;
        }
        item.remove();
        animEat(player);
        player.incrementHp(18);
        player.karamDelay.delay(3);
        player.getCombat().delayAttack(player.eatDelay.isDelayed() ? 1 : 2); //delays combat 1 tick less than other food on rs
        return true;
    }

    private static boolean eatAngler(Player player, Item item) {
        if(player.isLocked() || player.isStunned())
            return false;
        if(player.eatDelay.isDelayed() || player.karamDelay.isDelayed() || player.potDelay.isDelayed())
            return false;
        if(DuelRule.NO_FOOD.isToggled(player)) {
            player.sendMessage("Food has been disabled for this duel!");
            return false;
        }
        item.remove();
        animEat(player);
        int hp = player.getHp();
        int maxHp = player.getMaxHp();
        int c;
        if(maxHp <= 24)
            c = 2;
        else if(maxHp <= 49)
            c = 4;
        else if(maxHp <= 74)
            c = 6;
        else if(maxHp <= 92)
            c = 8;
        else
            c = 13;
        int restore = (maxHp / 10) + c;
        int newHp = Math.min(hp + restore, maxHp + restore);
        player.setHp(newHp);
        player.eatDelay.delay(3);
        player.getCombat().delayAttack(3);
        return true;
    }

    public static void animEat(Player player) {
        if(player.getEquipment().getId(Equipment.SLOT_WEAPON) == 4084)
            player.animate(1469);
        else if (player.seat != null)
            player.animate(player.seat.getEatAnimation(player));
        else
            player.animate(829);
        player.privateSound(2393);
        player.resetActions(true, player.getMovement().following != null, true);
    }

    /**
     * Drinking
     */

    static {
        registerPotion(Potion.ATTACK, p -> p.getStats().get(StatType.Attack).boost(3, 0.10));
        registerPotion(Potion.STRENGTH, p -> p.getStats().get(StatType.Strength).boost(3, 0.10));
        registerPotion(Potion.DEFENCE, p -> p.getStats().get(StatType.Defence).boost(3, 0.10));
        registerPotion(Potion.COMBAT, p -> {
            p.getStats().get(StatType.Attack).boost(3, 0.10);
            p.getStats().get(StatType.Strength).boost(3, 0.10);
        });

        registerPotion(Potion.SUPER_ATTACK, p -> p.getStats().get(StatType.Attack).boost(5, 0.15));
        registerPotion(Potion.SUPER_STRENGTH, p -> p.getStats().get(StatType.Strength).boost(5, 0.15));
        registerPotion(Potion.SUPER_DEFENCE, p -> p.getStats().get(StatType.Defence).boost(5, 0.15));
        registerPotion(Potion.SUPER_COMBAT, p -> {
            p.getStats().get(StatType.Attack).boost(5, 0.15);
            p.getStats().get(StatType.Strength).boost(5, 0.15);
            p.getStats().get(StatType.Defence).boost(5, 0.15);
        });

        registerPotion(Potion.RANGING, p -> p.getStats().get(StatType.Ranged).boost(4, 0.10));
        registerPotion(Potion.MAGIC, p -> p.getStats().get(StatType.Magic).boost(5, 0.0));

        registerPotion(Potion.AGILITY, p -> p.getStats().get(StatType.Agility).boost(3, 0.0));
        registerPotion(Potion.FISHING, p -> p.getStats().get(StatType.Fishing).boost(3, 0.0));
        registerPotion(Potion.HUNTER, p -> p.getStats().get(StatType.Hunter).boost(3, 0.0));

        registerPotion(Potion.ANTIPOISON, p -> p.curePoison((90 * 1000) / 600));
        registerPotion(Potion.RESTORE, p -> restore(p, false));
        registerPotion(Potion.ENERGY, p -> p.getMovement().restoreEnergy(10));
        registerPotion(Potion.PRAYER, p -> {
            Stat stat = p.getStats().get(StatType.Prayer);
            if(p.getEquipment().getId(Equipment.SLOT_RING) == 13202)
                stat.restore(7, 0.27);
            else
                stat.restore(7, 0.25);
            p.getStats().get(StatType.Prayer).alter(stat.currentLevel);
        });

        registerPotion(Potion.SUPER_ANTIPOISON, p -> p.curePoison((360 * 1000) / 600));
        registerPotion(Potion.SUPER_ENERGY, p -> p.getMovement().restoreEnergy(2000));
        registerPotion(Potion.SUPER_RESTORE, p -> restore(p, true));
        registerPotion(Potion.SANFEW_SERUM, p -> {
            restore(p, true);
             p.curePoison((90 * 1000) / 600);
        });
        registerPotion(Potion.ANTIDOTE_PLUS, p -> p.curePoison((540 * 1000) / 600));
        registerPotion(Potion.ANTIFIRE, p -> {
            p.antifireTicks = 600;
            p.getPacketSender().sendWidget(Widget.ANTIFIRE, (int) (600 * 0.6));
        });
        registerPotion(Potion.SUPER_ANTIFIRE, p -> {
            p.superAntifireTicks = 300;
            p.getPacketSender().sendWidget(Widget.ANTIFIRE, (int) (300 * 0.6));
        });

        registerPotion(Potion.STAMINA, p -> {
            p.getMovement().restoreEnergy(20);
            Config.STAMINA_POTION.set(p, 1);
            p.staminaTicks = 200;
            p.getPacketSender().sendWidget(Widget.STAMINA, 120);
        });
        registerPotion(Potion.ANTIDOTE_PLUS_PLUS, p -> p.curePoison((730 * 1000) / 600));
        registerPotion(Potion.SARADOMIN_BREW, p -> {
            p.getStats().get(StatType.Hitpoints).boost(2, 0.15);
            p.getStats().get(StatType.Defence).boost(2, 0.20);
            p.getStats().get(StatType.Attack).drain(0.10);
            p.getStats().get(StatType.Strength).drain(0.10);
            p.getStats().get(StatType.Ranged).drain(0.10);
            p.getStats().get(StatType.Magic).drain(0.10);
        });

        registerPotion(Potion.ZAMORAK_BREW, p -> {
            p.getStats().get(StatType.Attack).boost(2, 0.2);
            p.getStats().get(StatType.Strength).boost(2, 0.12);
            p.getStats().get(StatType.Defence).drain((int)(2 + (p.getStats().get(StatType.Defence).fixedLevel * 0.1)));
            p.hit(new Hit().fixedDamage((int) (2 + (p.getMaxHp() * 0.1))));
        });

        registerPotion(Potion.EXTENDED_ANTIFIRE, p -> {
            p.antifireTicks = 1200;
            p.getPacketSender().sendWidget(Widget.EXTENDED_ANTIFIRE, (int) (1200 * 0.6));
        });
        registerPotion(Potion.MAGIC_ESSENCE, p -> p.getStats().get(StatType.Magic).boost(3, 0));
        registerPotion(Potion.ANTI_VENOM, p -> p.cureVenom(0));
        registerPotion(Potion.SUPER_ANTI_VENOM, p -> p.cureVenom(300));

        registerPotion(Potion.GUTHIX_REST, p -> { //todo give this it's own method with it's own correct drink messages
            p.getStats().get(StatType.Hitpoints).boost(5, 0.0);
            p.getMovement().restoreEnergy(5);
            if(p.isPoisoned()) {
                p.curePoison(0);
                p.sendFilteredMessage("You tea dilutes some of the poison.");
            }
        });

        /**
         * Raids potions
         */
        registerPotion(Potion.ELDER_MINUS, p -> {
            p.getStats().get(StatType.Attack).boost(4, 0.10);
            p.getStats().get(StatType.Strength).boost(4, 0.10);
            p.getStats().get(StatType.Defence).boost(4, 0.10);
        });
        registerPotion(Potion.ELDER_REGULAR, p -> {
            p.getStats().get(StatType.Attack).boost(5, 0.13);
            p.getStats().get(StatType.Strength).boost(5, 0.13);
            p.getStats().get(StatType.Defence).boost(5, 0.13);
        });
        registerPotion(Potion.ELDER_PLUS, p -> {
            p.getStats().get(StatType.Attack).boost(6, 0.16);
            p.getStats().get(StatType.Strength).boost(6, 0.16);
            p.getStats().get(StatType.Defence).boost(6, 0.16);
        });

        registerPotion(Potion.TWISTED_MINUS, p -> {
            p.getStats().get(StatType.Ranged).boost(4, 0.10);
        });
        registerPotion(Potion.TWISTED_REGULAR, p -> {
            p.getStats().get(StatType.Ranged).boost(5, 0.13);
        });
        registerPotion(Potion.TWISTED_PLUS, p -> {
            p.getStats().get(StatType.Ranged).boost(6, 0.16);
        });

        registerPotion(Potion.KODAI_MINUS, p -> {
            p.getStats().get(StatType.Magic).boost(4, 0.10);
        });
        registerPotion(Potion.KODAI_REGULAR, p -> {
            p.getStats().get(StatType.Magic).boost(5, 0.13);
        });
        registerPotion(Potion.KODAI_PLUS, p -> {
            p.getStats().get(StatType.Magic).boost(6, 0.16);
        });

        registerPotion(Potion.REVITALISATION_MINUS, p -> restore(p, 5, 0.15, 5, 0.15));
        registerPotion(Potion.REVITALISATION_REGULAR, p -> restore(p, 7, 0.22, 7, 0.22));
        registerPotion(Potion.REVITALISATION_PLUS, p -> restore(p, 11, 0.30, 11, 0.30));

        registerPotion(Potion.PRAYER_ENHANCE_MINUS, p -> prayerEnhance(p,10));
        registerPotion(Potion.PRAYER_ENHANCE_REGULAR, p -> prayerEnhance(p,8));
        registerPotion(Potion.PRAYER_ENHANCE_PLUS, p -> prayerEnhance(p,6));

        registerPotion(Potion.XERIC_AID_MINUS, p -> {
            p.getStats().get(StatType.Hitpoints).boost(2, 0.5);
            p.getStats().get(StatType.Defence).boost(2, 0.10);
            p.getStats().get(StatType.Attack).drain(0.15);
            p.getStats().get(StatType.Strength).drain(0.15);
            p.getStats().get(StatType.Ranged).drain(0.15);
            p.getStats().get(StatType.Magic).drain(0.15);
        });
        registerPotion(Potion.XERIC_AID_REGULAR, p -> {
            p.getStats().get(StatType.Hitpoints).boost(2, 0.10);
            p.getStats().get(StatType.Defence).boost(2, 0.15);
            p.getStats().get(StatType.Attack).drain(0.15);
            p.getStats().get(StatType.Strength).drain(0.15);
            p.getStats().get(StatType.Ranged).drain(0.15);
            p.getStats().get(StatType.Magic).drain(0.15);
        });
        registerPotion(Potion.XERIC_AID_PLUS, p -> {
            p.getStats().get(StatType.Hitpoints).boost(5, 0.15);
            p.getStats().get(StatType.Defence).boost(5, 0.20);
            p.getStats().get(StatType.Attack).drain(0.10);
            p.getStats().get(StatType.Strength).drain(0.10);
            p.getStats().get(StatType.Ranged).drain(0.10);
            p.getStats().get(StatType.Magic).drain(0.10);
        });

        registerPotion(Potion.OVERLOAD_MINUS, p -> {
            overload(p, 4, 0.10);
            p.getStats().get(StatType.Attack).boost(4, 0.10);
            p.getStats().get(StatType.Strength).boost(4, 0.10);
            p.getStats().get(StatType.Defence).boost(4, 0.10);
            p.getStats().get(StatType.Ranged).boost(4, 0.10);
            p.getStats().get(StatType.Magic).boost(4, 0.10);
        });
        registerPotion(Potion.OVERLOAD_REGULAR, p -> {
            overload(p, 5, 0.13);
            p.getStats().get(StatType.Attack).boost(5, 0.13);
            p.getStats().get(StatType.Strength).boost(5, 0.13);
            p.getStats().get(StatType.Defence).boost(5, 0.13);
            p.getStats().get(StatType.Ranged).boost(5, 0.13);
            p.getStats().get(StatType.Magic).boost(5, 0.13);
        });
        registerPotion(Potion.OVERLOAD_PLUS, p -> {
            overload(p, 6, 0.16);
            p.getStats().get(StatType.Attack).boost(6, 0.16);
            p.getStats().get(StatType.Strength).boost(6, 0.16);
            p.getStats().get(StatType.Defence).boost(6, 0.16);
            p.getStats().get(StatType.Ranged).boost(6, 0.16);
            p.getStats().get(StatType.Magic).boost(6, 0.16);
        });

        registerPotion(Potion.ABSORPTION, p -> {
           NightmareZoneDream dream = p.get("nmz");
           if (dream == null) {
               p.sendMessage("You can only drink this potion during a dream in the Nightmare Zone.");
               return;
           }
           dream.absorptionPoints += 50;
           Config.NMZ_ABSORPTION.set(p, dream.absorptionPoints);
        });
    }

    private static void prayerEnhance(Player player, int frequency) {
        player.prayerEnhanceTicks = 500;
        player.prayerEnhanceFrequency = frequency;
    }

    private static void restore(Player player, int flatRestore, double percentRestore, int prayerFlat, double prayerPercent) {
        for(StatType type : StatType.values()) {
            if(type == StatType.Hitpoints)
                continue;
            Stat stat = player.getStats().get(type);
            if(stat.currentLevel < stat.fixedLevel) {
                if(type == StatType.Prayer && (prayerFlat > 0 || prayerPercent > 0)) {
                    stat.restore(prayerFlat, prayerPercent);
                } else if (type != StatType.Prayer)
                    stat.restore(flatRestore, percentRestore);
            }
        }
    }

    private static void restore(Player player, boolean superEffect) {
        restore(player, superEffect ? 8 : 10, superEffect ? 0.25 : 0.30, superEffect ? 8 : 0,
                superEffect ? (player.getEquipment().getId(Equipment.SLOT_RING) == 13202 ? 0.27 : 0.25) : 0);
    }

    private static void registerDrink(Potion potion, int id, int newId, Consumer<Player> effect) {
        ItemDef.get(id).consumable = true;
        ItemAction.registerInventory(id, "drink", (player, item) -> {
            if(drink(player, potion, item, newId))
                effect.accept(player);
        });
    }

    private static void registerEmpty(int id) {
        ItemDef def = ItemDef.get(id);
        if(def == null)
            return;
        if(!def.hasOption("empty"))
            return;
        ItemAction.registerInventory(id, "empty", (player, item) -> {
            item.setId(229);
            player.sendMessage("You empty the contents of the vial on the floor.");
        });
    }

    private static void registerPotion(Potion potion, Consumer<Player> effect) {
        registerDrink(potion, potion.vialIds[3], potion.vialIds[2], p -> {
            effect.accept(p);
            p.sendFilteredMessage("You drink some of your " + potion.potionName + ".");
            p.sendFilteredMessage("You have 3 doses of potion left.");
        });
        registerDrink(potion, potion.vialIds[2], potion.vialIds[1], p -> {
            effect.accept(p);
            p.sendFilteredMessage("You drink some of your " + potion.potionName + ".");
            p.sendFilteredMessage("You have 2 doses of potion left.");
        });
        registerDrink(potion, potion.vialIds[1], potion.vialIds[0], p -> {
            effect.accept(p);
            p.sendFilteredMessage("You drink some of your " + potion.potionName + ".");
            p.sendFilteredMessage("You have 1 dose of potion left.");
        });
        registerDrink(potion, potion.vialIds[0], 229, p -> {
            effect.accept(p);
            p.sendFilteredMessage("You drink some of your " + potion.potionName + ".");
            p.sendFilteredMessage("You have finished your potion.");
        });
        for(int vial : potion.vialIds) {
            registerEmpty(vial);
        }
    }

    private static boolean drink(Player player, Potion potion, Item item, int newId) {
        if(player.isLocked() || player.isStunned())
            return false;
        if(player.potDelay.isDelayed() || player.karamDelay.isDelayed())
            return false;
        if (item.getDef().coxItem && !ChambersOfXeric.isRaiding(player)) {
            player.sendMessage("This potion cannot be used outside of the Chambers of Xeric.");
            return false;
        }
        if(DuelRule.NO_DRINKS.isToggled(player)) {
            player.sendMessage("Drinks have been disabled for this duel!");
            return false;
        }
        if((potion == Potion.SARADOMIN_BREW || potion == Potion.GUTHIX_REST) && DuelRule.NO_FOOD.isToggled(player)) {
            player.sendMessage("Food has been disabled for this duel!");
            return false;
        }
        if (potion == Potion.OVERLOAD_MINUS || potion == Potion.OVERLOAD_REGULAR || potion == Potion.OVERLOAD_PLUS) {
            if (player.overloadTicks > 0) {
                player.sendMessage("You already have an overload potion active!");
                return false;
            } else if (player.getHp() < 50) {
                player.sendMessage("You are low on health, drinking an overload would kill you!");
                return false;
            }
        }
        if(newId == -1 || (newId == 229 && player.breakVials))
            item.remove();
        else if (potion == Potion.GUTHIX_REST)
            item.setId(newId == 229 ? 1980 : newId);
        else if(potion.raidsPotion)
            item.setId(newId == 229 ? 20800 : newId);
        else
            item.setId(newId);
        animDrink(player);
        player.potDelay.delay(3);
        return true;
    }

    private static void overload(Player player, int flatBoost, double percentBoost) {
        player.addEvent(event -> {
            for(int i = 0; i < 5; i ++) {
                player.animate(3170);
                player.graphics(560);
                player.hit(new Hit().fixedDamage(10));
                event.delay(2);
            }
            player.overloadFlatBoost = flatBoost;
            player.overloadPercentBoost = percentBoost;
            player.overloadTicks = 500;
        });
    }

    private static void animDrink(Player player) {
        if(player.getEquipment().getId(Equipment.SLOT_WEAPON) == 4084)
            player.animate(1469);
        else if (player.seat != null)
            player.animate(player.seat.getEatAnimation(player));
        else
            player.animate(829);
        player.privateSound(2401);
        player.resetActions(true, player.getMovement().following != null, true);
    }

}