package io.ruin.model;

import io.ruin.Server;
import io.ruin.api.protocol.world.WorldFlag;
import io.ruin.api.protocol.world.WorldStage;
import io.ruin.api.protocol.world.WorldType;
import io.ruin.cache.Color;
import io.ruin.cache.Icon;
import io.ruin.model.activities.pvminstances.PVMInstance;
import io.ruin.model.entity.EntityList;
import io.ruin.model.entity.npc.NPC;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.player.PlayerFile;
import io.ruin.model.map.Position;
import io.ruin.model.map.Region;
import io.ruin.process.event.EventWorker;
import io.ruin.utility.Broadcast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class World extends EventWorker {

    public static int id;

    public static String name;

    public static WorldStage stage;

    public static WorldType type;

    public static WorldFlag flag;

    public static int settings;

    public static String address;

    public static String centralAdress;

    public static int centralPort;

    public static long centralAuth;


    public static String gameDbHost;
    public static int gameDbPort;
    public static String gameDbUser;
    public static String gameDbPassword;
    public static String gameDb;

    public static String webDbHost;
    public static int webDbPort;
    public static String webDbUser;
    public static String webDbPassword;
    public static String webDB;

    public static boolean debug;



    public static boolean isDev() {
        return stage == WorldStage.DEV;
    }

    public static boolean isBeta() {
        return stage == WorldStage.BETA;
    }

    public static boolean isLive() {
        return stage == WorldStage.LIVE;
    }

    public static boolean isPVP() {
        return type == WorldType.PVP;
    }

    public static boolean isEco() {
        return type == WorldType.ECO;
    }

    /**
     * Players
     */

    public static final EntityList<Player> players = new EntityList<>(new Player[1000]);

    public static Player getPlayer(int index) {
        return players.get(index);
    }

    public static Player getPlayer(String name) {
        for(Player player : players) {
            if(player.getName().equalsIgnoreCase(name))
                return player;
        }
        return null;
    }

    public static Player getPlayer(int userId, boolean onlineReq) {
        if(onlineReq) {
            for(Player player : players) {
                if(player != null && player.getUserId() == userId)
                    return player;
            }
        } else {
            for(Player player : players.entityList) {
                if(player != null && player.getUserId() == userId)
                    return player;
            }
        }
        return null;
    }

    public static void sendGraphics(int id, int height, int delay, Position dest) {
        sendGraphics(id, height, delay, dest.getX(), dest.getY(), dest.getZ());
    }

    public static void sendGraphics(int id, int height, int delay, int x, int y, int z) {
        for(Player p : Region.get(x, y).players)
            p.getPacketSender().sendGraphics(id, height, delay, x, y, z);
    }

    /**
     * Npcs
     */

    public static final EntityList<NPC> npcs = new EntityList<>(new NPC[1000]);

    public static NPC getNpc(int index) {
        return npcs.get(index);
    }

    /**
     * PLAYER SAVERS
     */

    public static boolean dropRateBonus = false;

    public static boolean doubleDrops;

    public static int xpMultiplier = 0;

    public static boolean doublePestControl;

    public static int bmMultiplier = 0;

    public static boolean weekendExpBoost = false;

    public static void toggleDropRateBonus() {
        dropRateBonus = !dropRateBonus;
        String message = dropRateBonus ? "now" : "no longer";
        Broadcast.WORLD.sendNews(Icon.RED_INFO_BADGE, "The server is " + message + " experiencing a 15% drop rate bonus!");
    }

    public static void toggleDoubleDrops() {
        doubleDrops = !doubleDrops;
        Broadcast.WORLD.sendNews(Icon.RED_INFO_BADGE, "Double drops have been " + (doubleDrops ? "enabled" : "disabled") + ".");
    }

    public static void boostXp(int multiplier) {
        xpMultiplier = multiplier;
        if(xpMultiplier == 1)
            Broadcast.WORLD.sendNews(Icon.RED_INFO_BADGE, "Experience is now normal. (x1)");
        else if(xpMultiplier == 2)
            Broadcast.WORLD.sendNews(Icon.RED_INFO_BADGE, "Experience is now being doubled! (x2)");
        else if(xpMultiplier == 3)
            Broadcast.WORLD.sendNews(Icon.RED_INFO_BADGE, "Experience is now being tripled! (x3)");
        else if(xpMultiplier == 4)
            Broadcast.WORLD.sendNews(Icon.RED_INFO_BADGE, "Experience is now being quadrupled! (x4)");
        else
            Broadcast.WORLD.sendNews(Icon.RED_INFO_BADGE, "Experience is now boosted! (x" + multiplier + ")");
    }

    public static void toggleDoublePestControl() {
        doublePestControl = !doublePestControl;
        if (doublePestControl) {
            Broadcast.WORLD.sendNews("Pest Control reward points are currently being doubled! (x2)");
        } else {
            Broadcast.WORLD.sendNews("Pest Control reward points are no longer being doubled.");
        }
    }

    public static void toggleWeekendExpBoost() {
        weekendExpBoost = !weekendExpBoost;
        if(weekendExpBoost) {
            Broadcast.WORLD.sendNews(Icon.RED_INFO_BADGE, "The 25% weekend experience boost is now activated!");
        } else {
            Broadcast.WORLD.sendNews(Icon.RED_INFO_BADGE, "The 25% weekend experience boost is now deactivated!");
        }
    }

    public static void boostBM(int multiplier) {
        bmMultiplier = multiplier;
        if(bmMultiplier == 1)
            Broadcast.WORLD.sendNews(Icon.RED_INFO_BADGE, "Blood money drops from player kills are now normal. (x1)");
        else if(bmMultiplier == 2)
            Broadcast.WORLD.sendNews(Icon.RED_INFO_BADGE, "Blood money drops from player kills are now being doubled! (x2)");
        else if(bmMultiplier == 3)
            Broadcast.WORLD.sendNews(Icon.RED_INFO_BADGE, "Blood money drops from player kills are now being tripled! (x3)");
        else if(bmMultiplier == 4)
            Broadcast.WORLD.sendNews(Icon.RED_INFO_BADGE, "Blood money drops from player kills are now being quadrupled! (x4)");
        else
            Broadcast.WORLD.sendNews(Icon.RED_INFO_BADGE, "Blood money drops from player kills are now boosted! (x" + multiplier + ")");
    }

    public static void sendLoginMessages(Player player) {
        if(doubleDrops)
            player.sendMessage(Color.ORANGE_RED.tag() + "Npc drops are currently being doubled!");
        if(xpMultiplier == 2)
            player.sendMessage(Color.ORANGE_RED.tag() + "Experience is currently being doubled! (x2)");
        else if(xpMultiplier == 3)
            player.sendMessage(Color.ORANGE_RED.tag() + "Experience is currently being tripled! (x3)");
        else if(xpMultiplier == 4)
            player.sendMessage(Color.ORANGE_RED.tag() + "Experience is currently being quadrupled! (x4)");
    }

    public static boolean wildernessKeyEvent = false;

    public static void toggleWildernessKeyEvent() {
        wildernessKeyEvent = !wildernessKeyEvent;
    }

    /**
     * Updating
     */

    public static boolean updating = false;

    public static boolean update(int minutes) { //todo come back and clean this bad boy up
        if(minutes <= 0) {
            updating = false;
            for(Player player : players)
                player.getPacketSender().sendSystemUpdate(0);
            System.out.println("System Update Cancelled");
            return true;
        }
        if(updating)
            return false;
        updating = true;
        System.out.println("System Update: " + minutes + " minutes");
        for(Player player : players)
            player.getPacketSender().sendSystemUpdate(minutes * 60);
        startEvent(e -> {
            int ticks = minutes * 100;
            while(updating) {
                if(--ticks <= 0 && removeBots() && removePlayers())
                    return;
                e.delay(1);
            }
        });
        return true;
    }

    public static boolean removePlayers() {
        int pCount = players.count();
        if(pCount > 0) {
            System.out.println("Attempting to remove " + pCount + " players...");
            for(Player player : players)
                player.forceLogout();
            return false;
        }
        PVMInstance.destroyAll();
        System.out.println("Players removed from world successfully!");
        return true;
    }

    private static boolean removeBots() {
        for(Player p : World.players) {
            if(p.getChannel().id() == null)
                p.logoutStage = -1;
        }
        return true;
    }

    /**
     * Holliday themes
     */
    public static boolean halloween;

    public static boolean isHalloween() {
        return halloween;
    }

    public static boolean christmas;

    public static boolean isChristmas() {
        return christmas;
    }

    /**
     * Save event
     */

    static {
        startEvent(e -> {
            while(true) {
                e.delay(100); //every 1 minute just in case..
                for(Player player : players)
                    PlayerFile.save(player, -1);
            }
        });
    }

    /**
     * Announcement event
     */

    static {
        Server.afterData.add(() -> {
            List<String> announcements;
            if(isPVP()) {
                announcements = Arrays.asList(
                        //todo shorten messages that break into two lines because they look bad :c
                        //@Nick ask Matt if he can maybe? :)
                        "Need some assistance? Try using the ::help command!",
                        "Type ::commands to view a list of our in game commands!",
                        "Every 24 hours you'll get a bonus 500 blood money for your first kill!",
                        "Running low on blood money? Type ::store to restock!",
                        "You gain an additional 10% blood money for killing your Bounty Target!",
                        "If you have a bank pin and 2FA you get an additional 10% blood money per kill!",
                        "Rustin will exchange all your Mysterious Emblems for blood money!",
                        "You can toggle a variety of settings by going to the quest tab and clicking the purple button!",
                        "We offer preset gear! View your quest tab and click the red button!",
                        "Feeling lucky? Might be time to pick up a Mystery box at the ::store!",
                        "Running low on blood money? Try killing the event bosses!",
                        "Please take the time to vote for us. It helps us out and takes two seconds! ::vote"
                );
            } else {
                //todo
                announcements = Arrays.asList(
                        "Need some assistance? Try using the ::help command!",
                        "Type ::commands to view a list of our in game commands!",
                        "Looking for a challenge? Create a new account and become an Ironman!",
                        "Remember, rings of wealth increase the odds of receiving coin casket drops!",
                        "Feeling lucky? Check out our ::store for Super Mystery Boxes!",
                        "We host Daily 50m+ OSRS GP Tournaments! You can participate North East of Edgeville."
                );
            }
            Collections.shuffle(announcements);
      /*      startEvent(e -> {
                int offset = 0;
                while(true) {
                    e.delay(500); //5 minutes
                    Broadcast.WORLD.sendNews(Icon.ANNOUNCEMENT, announcements.get(offset));
                    if(++offset >= announcements.size())
                        offset = 0;
                }
            });*/
        });
    }
}