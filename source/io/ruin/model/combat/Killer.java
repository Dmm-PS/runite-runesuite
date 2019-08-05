package io.ruin.model.combat;

import io.ruin.api.utils.NumberUtils;
import io.ruin.api.utils.Random;
import io.ruin.api.utils.TimeUtils;
import io.ruin.cache.Color;
import io.ruin.cache.Icon;
import io.ruin.model.World;
import io.ruin.model.activities.tasks.DailyTask;
import io.ruin.model.activities.wilderness.Hotspot;
import io.ruin.model.activities.wilderness.StaffBounty;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.player.PlayerGroup;
import io.ruin.model.inter.journal.main.WildernessPoints;
import io.ruin.model.inter.utils.Config;
import io.ruin.model.item.Item;
import io.ruin.model.item.actions.impl.WildernessKey;
import io.ruin.model.item.actions.impl.boxes.WildernessRewardBox;
import io.ruin.model.map.Bounds;
import io.ruin.model.stat.StatType;
import io.ruin.services.Loggers;
import io.ruin.utility.Broadcast;

public class Killer {

//    private static final Bounds EDGEVILLE_FARM_SKIP_BOUNDS = new Bounds(2996, 3430, 3187, 3599, 0);
    public static final Bounds EDGEVILLE_FARM_SKIP_BOUNDS = new Bounds(3034, 3523, 3125, 3574, 0);

    private static final int BASE_BM_REWARD = 150;

    public Player player;

    public int damage;

    public void reward(Player pKilled, boolean bhTarget) {
        /**
         * Farming checks
         */
        long ms = System.currentTimeMillis();
        if(player.getIpInt() == pKilled.getIpInt()) //don't reward bm for killing someone on your same ip
            return;
        if(!pKilled.getPosition().inBounds(EDGEVILLE_FARM_SKIP_BOUNDS)) {
            boolean logged = player.bmIpLogs.contains(pKilled.getIpInt(), ms, 5L)
                    || player.bmUserLogs.contains(pKilled.getUserId(), ms, 5L);
            player.bmIpLogs.add(pKilled.getIpInt(), ms);
            player.bmUserLogs.add(pKilled.getUserId(), ms);
            if(logged)
                return;
        }
        /**
         * Increment killer kills & spree.
         */
        Config.PVP_KILLS.increment(player, 1);
        int killerSpree = ++player.currentKillSpree;
        if(killerSpree > player.highestKillSpree)
            player.highestKillSpree = killerSpree;
        if(killerSpree > 1) {
            player.sendMessage("You are currently on a killing spree of " + killerSpree + "!");
            if(World.isPVP()) {
                if(killerSpree % 5 == 0 || killerSpree > 15) {
                    String spreeMessage = player.getName() + " is on a killing spree of " + killerSpree + ". Kill "
                            + (player.getAppearance().isMale() ? "him" : "her") + " for a bounty reward of " + (BASE_BM_REWARD + bountyValue(killerSpree)) + " Blood money!";
                    Broadcast.WORLD.sendPlain(KillingSpree.imgTag(killerSpree) + Color.DARK_GREEN.tag() + " " + spreeMessage);
                    //KillingSpreeEmbedMessage.sendDiscordMessage(spreeMessage);
                }
                if(player.getCombat().isSkulled()) //Overheads start at sprees of 2, so this fits here.
                    player.getAppearance().setSkullIcon(KillingSpree.overheadId(player));
            }
        }
        /**
         * Check for shutdown
         */
        int targetSpree = pKilled.currentKillSpree; //Spree is set to 0 in death method. (After this method)
        if(World.isPVP() && targetSpree >= 5) {
            String shutdownMessage = KillingSpree.shutdownMessage(player.getName(), pKilled.getName(), targetSpree);
            Broadcast.WORLD.sendPlain("<img=36> " + Color.DARK_GREEN.tag() + shutdownMessage);
            //ShutdownEmbedMessage.sendDiscordMessage(shutdownMessage);
            if(targetSpree > player.highestShutdown)
                player.highestShutdown = targetSpree;
        }
        /**
         * Reward blood money
         */
        int bmAmount = BASE_BM_REWARD;
        if(World.isPVP()) {
            bmAmount += bountyValue(targetSpree);
            bmAmount += streakValue(killerSpree);
            bmAmount *= donatorBonus(player);
            bmAmount += player.wildernessLevel * 1.5;
            if (bhTarget)
                bmAmount *= 1.10;
            if (ms >= player.nextWildernessBonus) {
                player.nextWildernessBonus = (ms + TimeUtils.getHoursToMillis(12));
                player.sendFilteredMessage("<col=6f0000>You get an additional 250 Blood money for your first kill of the day!");
                bmAmount += 250;
            }
            if (player.getBankPin().hasPin() && player.tfa)
                bmAmount *= 1.10;
            else if (Random.rollDie(10))
                player.sendFilteredMessage("<col=6f0000>Set 2-FA and a bank pin to earn an additional 10% Blood money!");
            if (player.getPosition().inBounds(Hotspot.ACTIVE.bounds)) {
                bmAmount *= 2.0;
                player.sendFilteredMessage("<col=6f0000>You get double blood money for killing a player in a hotspot!");
            }

            /**
             * Check if we're doubling blood money for the weekend event
             */
            int bmMultiplier = World.bmMultiplier;
            if (bmMultiplier != 0) {
                bmAmount *= bmMultiplier;
            }

            /**
             * Check for staff bounty event
             */
            if (StaffBounty.EVENT_ACTIVE) {
                if (pKilled.isSupport()) {
                    StaffBounty.incrementStaffBounty("support_deaths");
                    Broadcast.WORLD.sendNews(Icon.WILDERNESS, "Staff Bounty", pKilled.getName() + " has been slain! " + player.getName() +
                            " has claimed " + StaffBounty.SUPPORT_KILL_VALUE + " for their bounty! The hunt continues...");

                }
                if (pKilled.isModerator()) {
                    bmAmount += StaffBounty.MODERATOR_KILL_VALUE;
                    StaffBounty.incrementStaffBounty("moderator_deaths");
                    Broadcast.WORLD.sendNews(Icon.WILDERNESS, "Staff Bounty", pKilled.getName() + " has been slain! " + player.getName() +
                            " has claimed " + StaffBounty.MODERATOR_KILL_VALUE + " for their bounty! The hunt continues...");
                }
                if (pKilled.isAdmin()) {
                    bmAmount += StaffBounty.ADMINISTRATOR_KILL_VALUE;
                    StaffBounty.incrementStaffBounty("administrator_deaths");
                    Broadcast.WORLD.sendNews(Icon.WILDERNESS, "Staff Bounty", pKilled.getName() + " has been slain! " + player.getName() +
                            " has claimed " + StaffBounty.ADMINISTRATOR_KILL_VALUE + " for their bounty! The hunt continues...");
                }
                Loggers.logStaffBountyKill(player, pKilled);
            }


            /**
             * Finally reward the player the blood money and refill their special attack
             */
            if (player.getPosition().isWithinDistance(pKilled.getPosition(), 32)) {
                // only restore special if they're within a 32 tile radius
                // this was orig being abused to refill special in different places of the world by getting alt accounts low
                // health and then killing them with an alt to receive the special restore
                player.getCombat().restoreSpecial(100);
            }

            player.rewardBm(pKilled, bmAmount);
        }

        /**
         * Roll for the wilderness reward box
         */
        if(World.isPVP()) {
            int wildernessRewardBox = WildernessRewardBox.rollForDrop(player);
            if (wildernessRewardBox != -1) {
                player.getInventory().addOrDrop(new Item(wildernessRewardBox, 1));
            }
        }

        /**
         * Add wilderness points
         */
        int wildernessPoints = Random.get(200, 300);
        wildernessPoints *= (WildernessPoints.wildernessLevelModifier(player) + WildernessPoints.playerGroupModifier(player));
        player.wildernessPoints += wildernessPoints;
        player.sendFilteredMessage("You receive <col=6f0000>" + wildernessPoints + "</col> wilderness points for killing " + pKilled.getName() + ".");

        /**
         * Drop gold on the ground if this is an economy kill
         */
        if(World.isEco()) {
            int goldAmount = Random.get(20_000, 50_000);
            player.getInventory().addOrDrop(995, goldAmount);
            player.sendFilteredMessage("<col=6f0000>You receive " + NumberUtils.formatNumber(goldAmount) + " gold coins for killing " + pKilled.getName() + "!");
        }

        /**
         * Roll for a wilderness key which can be exchanged for OSRS gold
         */
        if(World.isEco() && World.wildernessKeyEvent) {
            WildernessKey.rollForPlayerKill(player, pKilled);
        }

        if(World.isPVP()) {
            if (player.insideWildernessAgilityCourse) {
                player.getStats().addXp(StatType.Agility, 50000, false);
                player.getInventory().addOrDrop(11849, 10);
                player.sendFilteredMessage("<col=6f0000> You receive 50,000 agility experience and 10 marks of grace for killing a player inside the Agility course.");
            }

   /*         if (player.getPosition().inBounds(ResourceArea.BOUNDS)) {
                player.getInventory().addOrDrop(SkillLamp.SKILL_CAMP, 1);
                player.sendFilteredMessage("<col=6f0000> You receive a skill lamp for killing a player inside the Resource Area.");
            }*/
        }

        /**
         * Adjust wilderness elo
         */
        WildernessRating.adjustEloRatings(pKilled, player);

        /**
         * Check daily
         */
        if(World.isPVP())
            DailyTask.checkPlayerKill(player, pKilled);
    }

    private static double donatorBonus(Player player) {
        if (player.isGroup(PlayerGroup.GODLIKE_DONATOR)) {
            return 1.20;
        } else if (player.isGroup(PlayerGroup.UBER_DONATOR)) {
            return 1.15;
        } else if (player.isGroup(PlayerGroup.LEGENDARY_DONATOR)) {
            return 1.12;
        } else if (player.isGroup(PlayerGroup.EXTREME_DONATOR)) {
            return 1.10;
        } else if (player.isGroup(PlayerGroup.SUPER_DONATOR)) {
            return 1.07;
        } else if (player.isGroup(PlayerGroup.DONATOR)) {
            return 1.05;
        } else {
            return 1.00;
        }
    }

    private static int bountyValue(int spree) {
        return (10 * spree + 50 * (spree / 10));
    }

    private static int streakValue(int spree) {
        return 5 * spree;
    }


}