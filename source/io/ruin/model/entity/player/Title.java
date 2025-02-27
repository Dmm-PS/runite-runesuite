package io.ruin.model.entity.player;

import io.ruin.api.protocol.world.WorldType;
import io.ruin.api.utils.NumberUtils;
import io.ruin.cache.Color;
import io.ruin.model.World;
import io.ruin.model.achievements.Achievement;
import io.ruin.model.inter.dialogue.MessageDialogue;
import io.ruin.model.inter.dialogue.NPCDialogue;
import io.ruin.model.inter.handlers.OptionScroll;
import io.ruin.model.inter.utils.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Title {

    /**
     * !!! BE CAREFUL EDITING THIS! DO NOT REMOVE ENTRIES FROM THIS ARRAY !!!
     * Player files save an index to it. If you want to remove a title, set its entry to null.
     * ALWAYS add new ones at the BOTTOM!
     * Remember to close tags and add spaces where necessary :)
     */
    public static final Title[] PRESET_TITLES = {
            prefixTitle("<col=ffff00><shad=000000>Admin </col></shad>").setPredicate(p -> p.isGroup(PlayerGroup.ADMINISTRATOR)).setHidden(),
            prefixTitle("<col=a239db><shad=000000><img=31>Developer </shad></col>").setPredicate(p -> p.isGroup(PlayerGroup.ADMINISTRATOR)).setHidden(),
            prefixTitle(colorAndShadow("d6daff", "000000", "Moderator ")).setPredicate(p -> p.isGroup(PlayerGroup.MODERATOR)).setHidden(),
            prefixTitle(colorAndShadow("0033cc", "000000", "Support ")).setPredicate(p -> p.isGroup(PlayerGroup.SUPPORT)).setHidden(),
            prefixTitle(colorAndShadow("9e9e9e", "000000", "Ironman ")).setPredicate(p -> p.getGameMode() == GameMode.IRONMAN).setHidden(),
            prefixTitle(colorAndShadow("681212", "000000", "Hardcore Ironman ")).setPredicate(p -> p.getGameMode().isHardcoreIronman()).setHidden(),
            prefixTitle(colorAndShadow("bfbfbf", "000000",  "Ultimate Ironman ")).setPredicate(p -> p.getGameMode().isUltimateIronman()).setHidden(),
            prefixTitle(color("13a5f9", "Newbie ")),
            prefixTitle(colorAndShadow("c1a900", "000000", "Champion ")).setPredicate(p -> p.tournamentWins > 0).setUnlockDescription("Win a Tournament").pvpWorld(),
            prefixTitle(colorAndShadow("bfbfbf", "000000", "<img=119>Dice Host ")).setPredicate(p -> p.diceHost).setUnlockDescription("Purchase a Dice Bag"),
            prefixTitle(colorAndShadow("cc0000", "00000", "<img=91>Loyal ")).setPredicate(p -> p.loyaltyChestCount >= 30).setUnlockDescription("Open the loyalty chest 30 times"),
            prefixTitle(colorAndShadow("858585", "00000", "Ironman ")).setPredicate(p -> p.getGameMode() == GameMode.IRONMAN).setUnlockDescription("Play as an Ironman").ecoWorld(),
            prefixTitle(colorAndShadow("d2d2d2", "00000", "Ultimate ")).setPredicate(p -> p.getGameMode() == GameMode.ULTIMATE_IRONMAN).setUnlockDescription("Play as an Ultimate Ironman").ecoWorld(),
            prefixTitle(colorAndShadow("5e2121", "00000", "Hardcore ")).setPredicate(p -> p.getGameMode() == GameMode.HARDCORE_IRONMAN).setUnlockDescription("Play as a Hardcore Ironman").ecoWorld(),
            prefixTitle(colorAndShadow("b36b00", "00000", "JalYt ")).setPredicate(p -> p.zukKills.getKills() > 0).setUnlockDescription("Complete the Inferno minigame"),
            prefixTitle(colorAndShadow("a01111", "00000", "Slayer MASTER ")).setPredicate(p-> p.slayerTasksCompleted > 50).setUnlockDescription("Complete 50 slayer tasks").ecoWorld(),
            prefixTitle(colorAndShadow("c1a900", "00000", "Champion")).setPredicate(p -> p.tournamentWins >= 10).setUnlockDescription("Place first in 10 tournaments").ecoWorld()
    };

    public static String color(String color, String text) {
        return "<col=" + color + ">" + text + "</col>";
    }

    public static String shadow(String color, String text) {
        return "<shad=" + color + ">" + text + "</shad>";
    }

    public static String colorAndShadow(String color, String shadow, String text) {
        return color(color, shadow(shadow, text));
    }

    private static List<Title> sortedList = new ArrayList<>();
    static {
        for (int i = 0; i < PRESET_TITLES.length; i++) {
            if (PRESET_TITLES[i] == null)
                continue;
            PRESET_TITLES[i].id = i;
            sortedList.add(PRESET_TITLES[i]);
        }
        sortedList.sort(Title::compare);
    }

    public static void openSelection(Player player, boolean showLocked) {
        List<Option> options = sortedList.stream()
                .filter(t -> (showLocked && !t.hidden) || t.canUse(player))
                .filter(Title::activeOnThisWorld)
                .map(t -> new Option((t.getPrefix() == null ? "" : t.getPrefix())
                        + player.getName()
                        + (t.getSuffix() == null ? "" : t.getSuffix())
                        + (t.unlockDescription != null ? (" (" + t.unlockDescription + ")") : ""), () -> {
                    select(player, t);
                })).collect(Collectors.toList());
        if (options.size() == 0) {
            player.dialogue(new NPCDialogue(1307, "You haven't unlocked any titles yet!"));
            return;
        }
        OptionScroll.open(player, "Choose your title", false, options);
    }

    private static void select(Player player, Title title) {
        if (!title.canUse(player)) {
            player.dialogue(new MessageDialogue("You don't have access to this title.<br><br>Unlock condition: " + title.unlockDescription));
            return;
        }
        player.titleId = title.id;
        player.title = title;
        player.getAppearance().update();
        player.dialogue(new MessageDialogue("Your title has been updated."));
    }

    public static void clearTitle(Player player) {
        player.titleId = -1;
        player.title = null;
        player.getAppearance().update();
    }

    private static int compare(Title t1, Title t2) {
        try {
            if (t1.getPrefix() != null && t2.getPrefix() == null)
                return -1;
            else if (t2.getPrefix() != null && t1.getPrefix() == null)
                return 1;
            else if (t1.getPrefix() != null && t2.getPrefix() != null)
                return t1.strippedPrefix.compareToIgnoreCase(t2.strippedPrefix);
            else
                return t1.strippedSuffix.compareToIgnoreCase(t2.strippedSuffix);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static Title prefixTitle(String prefix) {
        Title title = new Title();
        return title.prefix(prefix);
    }


    public static Title suffixTitle(String suffix) {
        Title title = new Title();
        return title.suffix(suffix);
    }

    public Title prefix(String prefix) {
        this.prefix = prefix;
        strippedPrefix = prefix.replaceAll("<(.*)>", "");
        return this;
    }

    public Title suffix(String suffix) {
        this.suffix = suffix;
        strippedSuffix = suffix.replaceAll("<(.*)>", "");
        return this;
    }

    private String prefix, suffix;
    private String strippedPrefix, strippedSuffix;

    private int id;

    private boolean hidden;

    public Title pvpWorld() {
        this.worldType = WorldType.PVP;
        return this;
    }

    public Title ecoWorld() {
        this.worldType = WorldType.ECO;
        return this;
    }

    private WorldType worldType;

    public Title setPredicate(Predicate<Player> predicate) {
        this.predicate = predicate;
        return this;
    }

    private Predicate<Player> predicate;

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public Predicate<Player> getPredicate() {
        return predicate;
    }

    public String getUnlockDescription() {
        return unlockDescription;
    }

    public Title setUnlockDescription(String unlockDescription) {
        this.unlockDescription = unlockDescription;
        return this;
    }

    private String unlockDescription;

    public boolean canUse(Player player) {
        return activeOnThisWorld() && (predicate == null || predicate.test(player));
    }

    public boolean activeOnThisWorld() {
        return worldType == null || worldType == World.type;
    }

    public Title achievementUnlock(Achievement achievement) {
        predicate = achievement::isFinished;
        unlockDescription = "Achievement: " + achievement.getListener().name();
        return this;
    }

    public static Title get(int titleId) {
        if (titleId < 0 || titleId >= PRESET_TITLES.length)
            return null;
        return PRESET_TITLES[titleId];
    }

    public static void load(Player player) {
        if (player.titleId == -1) {
            return;
        }
        Title title = get(player.titleId);
        if (title != null && !title.canUse(player)) {
            player.sendMessage(Color.RED.wrap("Your title has been removed as you no longer meet the requirements to use it."));
        } else {
            player.title = title;
        }
    }

    /**
     * Hidden titles only show if unlocked, even if the player selects to show locked titles
     */
    public Title setHidden() {
        hidden = true;
        return this;
    }

    /**
     * Purchasable titles
     */
    private enum PurchasableTitles {
        /* credit titles */
        DESPERADO("Desperado", 50, -1),
        PRINCE("Prince", 50, -1),
        PRINCESS("Princess", 50, -1),
        LION_HEART("Lion Heart", 100, -1),
        THE_AWESOME("The Awesome", 100, -1),
        THE_STRANGE("The Strange", 100, -1),
        THE_MAGNIFICENT("The Magnificent", 100, -1),
        THE_FALLEN("The Fallen", 100, -1),
        THE_WARRIOR("The Warrior", 100, -1),
        THE_MYSTERIOUS("The Mysterious", 100, -1),
        EMPEROR("Emperor", 250, -1),
        EMPRESS("Empress", 250, -1),
        SWAGMASTER("Swagmaster", 500, -1),
        HELLRAISER("Hellrasier", 500, -1),
        GOD_OF_WAR("God Of War", 500, -1),
        KING("King", 1000, -1),
        QUEEN("Queen", 1000, -1),

        /* blood money titles */
        LORD("Lord", -1, 5000),
        LADY("Lady", -1, 5000),
        SIR("Sir", -1, 5000),
        GRUMPY("Grumpy", -1, 7500),
        CHEERFUL("Cheerful", -1, 7500),
        CRUSADER("Crusader", -1, 10000),
        DOCTOR("Doctor", -1, 10000),
        BARON("Baron", -1, 10000),
        OVERLORD("<col=a01111><shad=000000>Overlord</shad></col>", -1, 25000),
        MASTER("MASTER", -1, 25000),
        THE_UNDEFEATED("The Undefeated", -1, 50000);

        private String title;
        private int creditCost, bloodyMoneyCost;

        PurchasableTitles(String title, int creditCost, int bloodyMoneyCost) {
            this.title = title;
            this.creditCost = creditCost;
            this.bloodyMoneyCost = bloodyMoneyCost;
        }

        public static Stream<PurchasableTitles> stream() {
            return Stream.of(PurchasableTitles.values());
        }
    }

    public static void openPurchasableTitles(Player player) {
        List<Option> options = PurchasableTitles.stream()
                .map(t -> new Option(t.title + " "
                        + player.getName()
                        + (t.creditCost == -1 ? (" (Unlock for " + NumberUtils.formatNumber(t.bloodyMoneyCost) + " Blood money)") : " (Unlock for " + NumberUtils.formatNumber(t.creditCost) + " Runite credits) "), () -> {
                    selectPurchasable(player, t);
                })).collect(Collectors.toList());
        if (options.size() == 0) {
            player.dialogue(new NPCDialogue(1307, "You haven't unlocked any titles yet!"));
            return;
        }
        OptionScroll.open(player, "Choose your title", false, options);
    }

    private static void selectPurchasable(Player player, PurchasableTitles purchasableTitle) {
        player.sendMessage("Selected: " + purchasableTitle.title);
    }
}
