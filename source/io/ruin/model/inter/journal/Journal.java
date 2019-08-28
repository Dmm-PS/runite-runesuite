package io.ruin.model.inter.journal;

import io.ruin.api.utils.ListUtils;
import io.ruin.model.World;
import io.ruin.model.achievements.Achievement;
import io.ruin.model.achievements.AchievementCategory;
import io.ruin.model.activities.tasks.DailyTask;
import io.ruin.model.activities.wilderness.*;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.shared.listeners.LoginListener;
import io.ruin.model.inter.journal.bestiary.BestiarySearchDrop;
import io.ruin.model.inter.journal.bestiary.BestiarySearchMonster;
import io.ruin.model.inter.journal.bestiary.BestiarySearchResult;
import io.ruin.model.inter.journal.main.*;
import io.ruin.model.inter.journal.presets.PresetCustom;
import io.ruin.model.inter.journal.presets.main.Hybrid;
import io.ruin.model.inter.journal.presets.main.MainNoHonorTribrid;
import io.ruin.model.inter.journal.presets.main.Melee;
import io.ruin.model.inter.journal.presets.pure.PureMelee;
import io.ruin.model.inter.journal.presets.pure.PureNoHonorTribrid;
import io.ruin.model.inter.journal.presets.pure.RangeDDS;
import io.ruin.model.inter.journal.toggles.*;

import java.util.ArrayList;
import java.util.Arrays;

public enum Journal {

    MAIN,
    ACHIEVEMENTS,
    PRESETS,
    TOGGLES,
    BESTIARY;

    /**
     * Categories
     */

    private ArrayList<JournalCategory> categories = new ArrayList<>();

    private JournalCategory lastCategory;

    private void addCategory(String name) {
        categories.add(lastCategory = new JournalCategory(name));
        categories.trimToSize(); //only ok because it's on startup
    }

    /**
     * Entries
     */

    private ArrayList<JournalEntry> entries = new ArrayList<>();

    private void addEntry(JournalEntry entry) {
        if(lastCategory == null)
            addCategory("");
        entry.childId = entries.size();
        entries.add(entry);
        entries.trimToSize(); //only ok because it's on startup
        lastCategory.count++;
    }

    /**
     * Sending
     */

    public void send(Player player) {
        player.journal = this;
        if(this == BESTIARY && player.bestiarySearchResults != null) {
            JournalCategory results = new JournalCategory("Results");
            results.count = player.bestiarySearchResults.size() - 2; //minus two because of the search entries
            player.getPacketSender().sendJournal(ordinal(), ListUtils.toList(this.categories.get(0), results));
            for(JournalEntry entry : player.bestiarySearchResults)
                entry.send(player);
        } else {
            player.bestiarySearchResults = null;
            player.getPacketSender().sendJournal(ordinal(), categories);
            for(JournalEntry entry : entries)
                entry.send(player);
        }
    }

    /**
     * Selecting
     */

    public void select(Player player, int childId) {
        if(this == BESTIARY && player.bestiarySearchResults != null) {
            if(childId < 0 || childId >= player.bestiarySearchResults.size())
                return;
            player.bestiarySearchResults.get(childId).select(player);
        } else {
            if(childId < 0 || childId >= entries.size())
                return;
            entries.get(childId).select(player);
        }
    }

    static { //This is going to get ugly, but it's on startup so who cares lol!
        /**
         * Main
         */
        MAIN.addCategory("Runite");
        MAIN.addEntry(Uptime.INSTANCE);
        MAIN.addEntry(PlayersOnline.INSTANCE);
        MAIN.addEntry(StaffOnline.INSTANCE);
        MAIN.addEntry(WildernessCount.INSTANCE);
        MAIN.addCategory(" ");

        BossEvent[] bossEvents = new BossEvent[BossEvent.BOSSES.length];
        MAIN.addCategory("Events");
        for (int i = 0; i < bossEvents.length; i++) {
            MAIN.addEntry(bossEvents[i] = new BossEvent(i));
        }
        MAIN.addCategory(" ");

        MAIN.addCategory("Personal");
        MAIN.addEntry(TwoFactorAuthentication.INSTANCE);
        MAIN.addEntry(TotalSpent.INSTANCE);
        MAIN.addEntry(PlayTime.INSTANCE);
        if (World.isEco()) {
            //MAIN.addEntry(new CombatXP());
            MAIN.addEntry(AppreciationPoints.INSTANCE);
        }
        MAIN.addCategory(" ");

        if (World.isEco()) {
            MAIN.addCategory("Scroll Boosts");
            MAIN.addEntry(ExpBonusTime.INSTANCE);
            MAIN.addEntry(DropBoostTime.INSTANCE);
            MAIN.addEntry(PetDropBoostTime.INSTANCE);
        }
        MAIN.addCategory(" ");

        if (World.isEco()) {
            MAIN.addCategory("Wilderness");
        }
        //MAIN.addEntry(SnowballPoints.INSTANCE);
        MAIN.addEntry(PKRating.INSTANCE);
        MAIN.addEntry(WildernessPoints.INSTANCE);
        MAIN.addEntry(new TotalKills());
        MAIN.addEntry(new TotalDeaths());
        MAIN.addEntry(new KillDeathRatio());
        MAIN.addEntry(new KillingSpree());
        MAIN.addEntry(new HighestKillingSpree());
        MAIN.addEntry(new HighestShutdown());
        /**
         * Achievements
         */
        if (World.isEco()) {
            for (AchievementCategory cat : AchievementCategory.values()) {
                ACHIEVEMENTS.addCategory(cat.name());
                Arrays.stream(Achievement.values())
                        .filter(ach -> ach.getCategory() == cat)
                        .sorted((a1, a2) -> a1.getListener().name().compareToIgnoreCase(a2.getListener().name()))
                        .map(Achievement::toEntry)
                        .forEachOrdered(ACHIEVEMENTS::addEntry);
            }
        }
        /**
         * Presets
         */
        if(World.isPVP()) {
            PRESETS.addCategory("Main");
            PRESETS.addEntry(new Melee());
            PRESETS.addEntry(new Hybrid());
            PRESETS.addEntry(new MainNoHonorTribrid());
            PRESETS.addCategory("Pure");
            PRESETS.addEntry(new PureMelee());
            PRESETS.addEntry(new RangeDDS());
            PRESETS.addEntry(new PureNoHonorTribrid());
            PRESETS.addCategory("Custom");
            for(PresetCustom preset : PresetCustom.ENTRIES)
                PRESETS.addEntry(preset);
        } else {
            PRESETS.addCategory("Presets");
            for(PresetCustom preset : PresetCustom.getEntries())
                PRESETS.addEntry(preset);
        }
        /**
         * Toggles
         */
        TOGGLES.addCategory("Combat");
        //TOGGLES.addEntry(new TargetOverlay());
        TOGGLES.addEntry(new KDOverlay());
        if (World.isPVP())
            TOGGLES.addEntry(new ExpCounter());

        if (World.isPVP()) {
            TOGGLES.addCategory("Edgeville Blacklist");
            for (EdgevilleBlacklist blacklistedUsers : EdgevilleBlacklist.ENTRIES)
                TOGGLES.addEntry(blacklistedUsers);
        }
        TOGGLES.addCategory("Bounty Hunter");
        TOGGLES.addEntry(new BountyHunterTargeting());
        TOGGLES.addEntry(new BountyHunterStreaks());

        //if(World.isPVP()) {
        //    TOGGLES.addCategory("Broadcasts");
        //    TOGGLES.addEntry(new BroadcastBossEvent());
        //    TOGGLES.addEntry(new BroadcastActiveVolcano());
        //    TOGGLES.addEntry(new BroadcastHotspot());
        //    TOGGLES.addEntry(new BroadcastPvPSupplyChest());
        //    TOGGLES.addEntry(new BroadcastBloodyMerchant());
        //}

        TOGGLES.addCategory("Miscellaneous");
        TOGGLES.addEntry(new BreakVials());
        if(World.isEco())
            TOGGLES.addEntry(new DiscardBuckets());
        TOGGLES.addEntry(new DragSetting());
        TOGGLES.addEntry(new HideIcon());
        TOGGLES.addEntry(new SwapMagePrayers());
        TOGGLES.addEntry(new SwapRangePrayers());
        TOGGLES.addEntry(new SwitchGrading());
        TOGGLES.addEntry(new ShowWidgets());
        TOGGLES.addEntry(new HideTitles());
        if (World.isPVP())
            TOGGLES.addEntry(new HideFreeItems());
        TOGGLES.addEntry(new ColorValuedItems());
        if (World.isPVP())
            TOGGLES.addEntry(RiskProtection.INSTANCE);
        TOGGLES.addEntry(new HideYells());
        /**
         * Bestiary
         */
        BESTIARY.addCategory("Search");
        BESTIARY.addEntry(BestiarySearchDrop.INSTANCE);
        BESTIARY.addEntry(BestiarySearchMonster.INSTANCE);
        BESTIARY.addCategory("Popular");
        BESTIARY.addEntry(new BestiarySearchResult(8095)); //Galvek
        BESTIARY.addEntry(new BestiarySearchResult(415)); //Abyssal demon
        BESTIARY.addEntry(new BestiarySearchResult(5862)); //Cerberus
        BESTIARY.addEntry(new BestiarySearchResult(3162)); //Kree'arra
        BESTIARY.addEntry(new BestiarySearchResult(2042)); //Zulrah
        BESTIARY.addEntry(new BestiarySearchResult(2215)); //General Graardor
        BESTIARY.addEntry(new BestiarySearchResult(319)); //Corporeal Beast
        BESTIARY.addEntry(new BestiarySearchResult(6619)); //Chaos Fanatic
        BESTIARY.addEntry(new BestiarySearchResult(3129)); //K'ril Tsutsaroth
        BESTIARY.addEntry(new BestiarySearchResult(2265)); //Dagannoth Supreme
        /**
         * Updating
         */
        LoginListener.register(player -> {
            TOGGLES.entries.forEach(entry -> entry.send(player)); // Sending these on login forces the custom varp based settings to be updated
            player.journal.send(player);
            player.addEvent(event -> {
                while(true) {
                    if (player.journal == MAIN) {
                        Uptime.INSTANCE.send(player);
                        PlayersOnline.INSTANCE.send(player);
                        StaffOnline.INSTANCE.send(player);
                        WildernessCount.INSTANCE.send(player);
                        TwoFactorAuthentication.INSTANCE.send(player);
                        TotalSpent.INSTANCE.send(player);
                        PlayTime.INSTANCE.send(player);
                        if (World.isPVP()) {
                            PVPInstanceCount.INSTANCE.send(player);
                            //ActiveVolcano.Entry.INSTANCE.send(player);
                            Hotspot.Entry.INSTANCE.send(player);
                            SupplyChest.Entry.INSTANCE.send(player);
                            BloodyMerchant.Entry.INSTANCE.send(player);
                        } else {
                            ExpBonusTime.INSTANCE.send(player);
                            DropBoostTime.INSTANCE.send(player);
                            PetDropBoostTime.INSTANCE.send(player);
                            AppreciationPoints.INSTANCE.send(player);
                        }
                        WildernessPoints.INSTANCE.send(player);
                        for (BossEvent bossEvent : bossEvents) bossEvent.send(player);
                    }
                    event.delay(10);
                }
            });
        });
    }

    public ArrayList<JournalEntry> getEntries() {
        return entries;
    }
}