package io.ruin.network.incoming.handlers;

import com.google.gson.GsonBuilder;
import io.ruin.api.buffer.InBuffer;
import io.ruin.api.utils.NumberUtils;
import io.ruin.api.utils.TimeUtils;
import io.ruin.cache.*;
import io.ruin.cache.EnumMap;
import io.ruin.data.DataFile;
import io.ruin.data.impl.help;
import io.ruin.data.impl.items.item_info;
import io.ruin.data.impl.items.shield_types;
import io.ruin.data.impl.items.weapon_types;
import io.ruin.data.impl.login_set;
import io.ruin.data.impl.npcs.npc_drops;
import io.ruin.data.impl.npcs.npc_shops;
import io.ruin.data.impl.npcs.npc_spawns;
import io.ruin.data.impl.teleports;
import io.ruin.model.World;
import io.ruin.model.activities.grandexchange.GrandExchange;
import io.ruin.model.activities.inferno.Inferno;
import io.ruin.model.activities.nightmarezone.NightmareZonePregame;
import io.ruin.model.activities.raids.xeric.ChambersOfXeric;
import io.ruin.model.activities.raids.xeric.chamber.Chamber;
import io.ruin.model.activities.raids.xeric.chamber.ChamberDefinition;
import io.ruin.model.activities.raids.xeric.chamber.impl.BlankHandler;
import io.ruin.model.activities.raids.xeric.chamber.impl.CheckpointChamber;
import io.ruin.model.activities.raids.xeric.party.Party;
import io.ruin.model.activities.tournament.Tournament;
import io.ruin.model.activities.tournament.TournamentSchedule;
import io.ruin.model.activities.tournamentv2.Tournament2;
import io.ruin.model.activities.wilderness.StaffBounty;
import io.ruin.model.combat.Hit;
import io.ruin.model.entity.npc.NPC;
import io.ruin.model.entity.npc.actions.edgeville.Nurse;
import io.ruin.model.entity.npc.actions.edgeville.Rustin;
import io.ruin.model.entity.player.*;
import io.ruin.model.entity.player.ai.AIPlayer;
import io.ruin.model.entity.player.ai.scripts.GoblinKiller;
import io.ruin.model.inter.Interface;
import io.ruin.model.inter.InterfaceType;
import io.ruin.model.inter.dialogue.MessageDialogue;
import io.ruin.model.inter.dialogue.OptionsDialogue;
import io.ruin.model.inter.dialogue.skill.SkillDialogue;
import io.ruin.model.inter.dialogue.skill.SkillItem;
import io.ruin.model.inter.handlers.OptionScroll;
import io.ruin.model.inter.handlers.TabStats;
import io.ruin.model.inter.journal.presets.PresetCustom;
import io.ruin.model.inter.utils.Config;
import io.ruin.model.inter.utils.Option;
import io.ruin.model.inter.utils.Unlock;
import io.ruin.model.item.Item;
import io.ruin.model.item.ItemContainer;
import io.ruin.model.item.actions.impl.GoldCasket;
import io.ruin.model.item.containers.Equipment;
import io.ruin.model.item.containers.bank.BankItem;
import io.ruin.model.map.*;
import io.ruin.model.map.dynamic.DynamicMap;
import io.ruin.model.map.ground.GroundItem;
import io.ruin.model.map.object.GameObject;
import io.ruin.model.map.object.actions.impl.PrayerAltar;
import io.ruin.model.map.object.actions.impl.edgeville.Giveaway;
import io.ruin.model.map.route.RouteFinder;
import io.ruin.model.skills.construction.*;
import io.ruin.model.skills.construction.actions.Costume;
import io.ruin.model.skills.construction.actions.CostumeStorage;
import io.ruin.model.skills.magic.SpellBook;
import io.ruin.model.skills.magic.rune.Rune;
import io.ruin.model.skills.mining.Mining;
import io.ruin.model.skills.mining.Pickaxe;
import io.ruin.model.skills.mining.Rock;
import io.ruin.model.skills.slayer.Slayer;
import io.ruin.model.skills.slayer.SlayerTask;
import io.ruin.model.stat.Stat;
import io.ruin.model.stat.StatType;
import io.ruin.network.incoming.Incoming;
import io.ruin.services.Loggers;
import io.ruin.services.Punishment;
import io.ruin.services.Referral;
import io.ruin.services.discord.impl.TournamentEmbedMessage;
import io.ruin.utility.IdHolder;
import io.ruin.utility.OfflineMode;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@IdHolder(ids = {90})
public class CommandHandler implements Incoming {

    private static final Bounds EDGEVILLE = new Bounds(3036, 3478, 3144, 3524, -1);

    private static final Bounds MAGE_BANK = new Bounds(2527, 4708, 2551, 4727, 0);

    private static Position relativeBase;

    @Override
    public void handle(Player player, InBuffer in, int opcode) {
        String query = in.readString();
        handle(player, query);
    }

    public static void handle(Player player, String query) {
        if((query = query.trim()).isEmpty())
            return;
        if(!query.contains("yell") && !query.contains("Yell"))
            player.sendFilteredMessage("<col=cc0000>::" + query);
        Loggers.logCommand(player.getUserId(), player.getName(), player.getIp(), query);
        String command;
        String[] args;
        int spaceIndex = query.indexOf(' ');
        if(spaceIndex == -1) {
            command = query;
            args = null;
        } else {
            command = query.substring(0, spaceIndex);
            args = query.substring(spaceIndex + 1).split(" ");
        }
        try {
            command = command.toLowerCase();
            if(handleAdmin(player, query, command, args))
                return;
            if(player.isLocked()) {
                player.sendMessage("Please finish what you're doing first.");
                return;
            }
            if(handleSupport(player, query, command, args))
                return;
            if(handleMod(player, query, command, args))
                return;
            if(handleSeniorMod(player, query, command, args))
                return;
            if(handleRegular(player, query, command, args))
                return;
            player.sendMessage("Sorry, that command does not exist.");
        } catch(Throwable t) {
            t.printStackTrace();
            player.sendMessage("Error handling command '" + query + "': " + t.getMessage());
        }
    }

    private static boolean handleRegular(Player player, String query, String command, String[] args) {
        switch(command) {
            case "commands": {
                if(World.isPVP()) {
                    player.sendScroll("<col=800000>Commands</col>",
                            "<col=800000>Teleport Commands:</col>",
                            "::home", "::stake", "::mb",
                            "::cammypvp", "::edgepvp", "::fallypvp", "::lumbpvp",
                            "",
                            "<col=800000>Misc Commands:</col>",
                            "::help", "::yell", "::bestiary",
                            "",
                            "<col=800000>Website Commands:</col>",
                            "::store", "::vote", "::guides", "::support", "::forums", "::scores", "::discord", "::thread #"
                    );
                } else {
                    player.sendScroll("<col=800000>Commands</col>",
                            "<col=800000>Teleport Commands:</col>",
                            "::home", "::stake", "::mb",
                            "::cammypvp", "::edgepvp", "::fallypvp", "::lumbpvp",
                            "",
                            "<col=800000>Misc Commands:</col>",
                            "::help", "::yell",
                            "::char", "::skull",
                            "::journal", "::presets", "::toggles", "::bestiary",
                            "",
                            "<col=800000>Website Commands:</col>",
                            "::store", "::vote", "::guides", "::support", "::forums", "::scores", "::discord", "::thread #", "::member #"
                    );
                }
                return true;
            }

            case "scout": {
                if (!ChambersOfXeric.isRaiding(player)) {
                    player.sendMessage("You are not in a raids party for which you can scout!");
                    return true;
                }

                ArrayList<String> chambers = new ArrayList<>();

                for (int i = 0; i < player.raidsParty.getRaid().getChambers().length; i++) {
                    for (int j = 0; j < player.raidsParty.getRaid().getChambers()[i].length; j++) {
                        for (int k = 0; k < player.raidsParty.getRaid().getChambers()[i][j].length; k++) {
                            if (player.raidsParty.getRaid().getChambers()[i][j][k] != null && !(player.raidsParty.getRaid().getChambers()[i][j][k] instanceof
                                    CheckpointChamber) && !(player.raidsParty.getRaid().getChambers()[i][j][k] instanceof BlankHandler)) {
                                chambers.add(player.raidsParty.getRaid().getChambers()[i][j][k].getClass().getSimpleName());
                            }
                        }
                    }
                }

                player.sendScroll("Chambers in this raid", chambers.toArray(new String[0]));

                return true;
            }

            case "barrage": {
                if(World.isEco()) {
                    return true;
                }
                if(!player.getPosition().inBounds(EDGEVILLE) && !player.getPosition().inBounds(MAGE_BANK) && player.pvpInstancePosition == null) {
                    player.dialogue(new MessageDialogue("You can only use this command near a bank or around Edgeville."));
                    return true;
                }
                if (player.getPosition().inBounds(Tournament.JOIN_BOUNDS)) {
                    player.dialogue(new MessageDialogue("You can't use this command here."));
                    return true;
                }
                player.getInventory().addOrDrop(555, 16000);
                player.getInventory().addOrDrop(560, 16000);
                player.getInventory().addOrDrop(565, 16000);
                PrayerAltar.switchBook(player, SpellBook.ANCIENT, false);
                return true;
            }

            case "tb":
            case "teleblock": {
                if(World.isEco())
                    return true;
                if(!player.getPosition().inBounds(EDGEVILLE) && !player.getPosition().inBounds(MAGE_BANK) && player.pvpInstancePosition == null) {
                    player.dialogue(new MessageDialogue("You can only use this command inside a bank or around Edgeville."));
                    return true;
                }
                if (player.getPosition().inBounds(Tournament.JOIN_BOUNDS)) {
                    player.dialogue(new MessageDialogue("You can't use this command here."));
                    return true;
                }
                player.getInventory().addOrDrop(562, 1000);
                player.getInventory().addOrDrop(563, 1000);
                player.getInventory().addOrDrop(560, 1000);
                PrayerAltar.switchBook(player, SpellBook.MODERN, false);
                return true;
            }

            case "veng": {
                if(World.isEco()) {
                    return true;
                }
                if(!player.getPosition().inBounds(EDGEVILLE) && !player.getPosition().inBounds(MAGE_BANK) && player.pvpInstancePosition == null) {
                    player.dialogue(new MessageDialogue("You can only use this command inside a bank or around Edgeville."));
                    return true;
                }
                if (player.getPosition().inBounds(Tournament.JOIN_BOUNDS)) {
                    player.dialogue(new MessageDialogue("You can't use this command here."));
                    return true;
                }
                player.getInventory().addOrDrop(560, 1000);
                player.getInventory().addOrDrop(9075, 1000);
                player.getInventory().addOrDrop(557, 1000);
                PrayerAltar.switchBook(player, SpellBook.LUNAR, false);
                return true;
            }

            case "food": {
                if(World.isEco())
                    return true;
                if(!player.getPosition().inBounds(EDGEVILLE) && !player.getPosition().inBounds(MAGE_BANK) && player.pvpInstancePosition == null) {
                    player.dialogue(new MessageDialogue("You can only use this command inside a bank or around Edgeville."));
                    return true;
                }
                if (player.getPosition().inBounds(Tournament.JOIN_BOUNDS)) {
                    player.dialogue(new MessageDialogue("You can't use this command here."));
                    return true;
                }
                player.getInventory().add(385, 28);
                return true;
            }

            case "karam": {
                if(World.isEco())
                    return true;
                if(!player.getPosition().inBounds(EDGEVILLE) && !player.getPosition().inBounds(MAGE_BANK) && player.pvpInstancePosition == null) {
                    player.dialogue(new MessageDialogue("You can only use this command inside a bank or around Edgeville."));
                    return true;
                }
                if (player.getPosition().inBounds(Tournament.JOIN_BOUNDS)) {
                    player.dialogue(new MessageDialogue("You can't use this command here."));
                    return true;
                }
                player.getInventory().add(3144, 3);
                return true;
            }

            case "setlvls":
            case "setlvl":
            case "setlevels":
            case "setlevel": {
                if(World.isEco()) {
                    player.dialogue(new MessageDialogue("You can't set your stats unless you're playing the PK gamemode."));
                    return true;
                }
                player.dialogue(
                        new OptionsDialogue(
                                new Option("Set Attack", () -> TabStats.setLevel(player, StatType.Attack)),
                                new Option("Set Strength", () -> TabStats.setLevel(player, StatType.Strength)),
                                new Option("Set Defence", () -> TabStats.setLevel(player, StatType.Defence)),
                                new Option("Set Ranged", () -> TabStats.setLevel(player, StatType.Ranged)),
                                new Option("Next Page", () -> {
                                    player.dialogue(
                                            new OptionsDialogue(
                                                    new Option("Set Prayer", () -> TabStats.setLevel(player, StatType.Prayer)),
                                                    new Option("Set Magic", () -> TabStats.setLevel(player, StatType.Magic)),
                                                    new Option("Set Hitpoints", () -> TabStats.setLevel(player, StatType.Hitpoints))
                                            )
                                    );
                                })
                        )
                );
                return true;
            }

            case "edge":
            case "home": {
                teleport(player, 3096, 3487, 0);
                return true;
            }

            case "arena":
            case "stake":
            case "duel":
            case "da": {
                teleport(player, 3367, 3265, 0);
                return true;
            }

            case "magebank":
            case "mb": {
                teleport(player, 2539, 4716, 0);
                return true;
            }

            case "revs": {
                teleportDangerous(player, 3069, 3651, 0);
                return true;
            }

            case "easts": {
                teleportDangerous(player, 3364, 3666, 0);
                return true;
            }

            case "wests": {
                teleportDangerous(player, 2983, 3598, 0);
                return true;
            }

            case "50":
            case "50s":{
                teleportDangerous(player, 3314, 3912, 0);
                return true;
            }

            case "44":
            case "44s": {
                teleportDangerous(player, 2972, 3869, 0);
                return true;
            }

            case "chins": {
                teleportDangerous(player, 3129, 3777, 0);
                return true;
            }

            case "graves": {
                teleportDangerous(player, 3143, 3677, 0);
                return true;
            }

            case "cammypvp": {
                teleport(player, 134, 87, 0);
                return true;
            }

            case "edgepvp": {
                teleport(player, 88, 233, 0);
                return true;
            }

            case "fallypvp": {
                teleport(player, 129, 362, 0);
                return true;
            }

            case "lumbpvp": {
                teleport(player, 87, 467, 0);
                return true;
            }

            case "tournament": {
                teleport(player, 3112, 3514, 0);
               // if(World.isEco())
               //     teleport(player, 3114, 3514, 0);
               // else
               //     teleport(player, 3084, 3477, 0);
                return true;
            }

            case "help": {
                if(World.isEco())
                    return true;
                if(args != null && args.length > 0) {
                    try {
                        help.open(player, Integer.valueOf(args[0]));
                    } catch(Exception e) {
                        player.sendMessage("Invalid command usage. Example: [::help 1]");
                    }
                    return true;
                }
                help.open(player);
                return true;
            }

            case "heal": {
                if(World.isEco())
                    return true;
                if(!player.isAdmin() && !player.isNearBank() && !player.getPosition().inBounds(EDGEVILLE) || player.wildernessLevel > 0 || player.pvpAttackZone) {
                    player.dialogue(new MessageDialogue("You can only use this command near a bank or around Edgeville."));
                    return true;
                }
                if(!player.getCombat().isDead())
                    Nurse.heal(player, null);
                return true;
            }

            case "skull": {
                if(World.isEco())
                    return true;
                if(player.wildernessLevel > 0 || player.pvpAttackZone) {
                    player.dialogue(new MessageDialogue("You can't use this command from where you're standing."));
                    return true;
                }
                if(!player.getCombat().isDead())
                    Rustin.skull(player);
                return true;
            }

            case "preset": {
                if(World.isEco())
                    return true;
                try {
                    int id = Integer.valueOf(args[0]);
                    int index = id - 1;
                    PresetCustom preset;
                    if(index < 0 || index >= player.customPresets.length || (preset = player.customPresets[index]) == null)
                        player.sendMessage("Preset #" + id + " does not exist.");
                    else if(preset.allowSelect(player)) {
                        player.closeDialogue();
                        preset.selectFinish(player);
                    }
                } catch(Exception e) {
                    player.sendMessage("Invalid command usage. Example: [::preset 1]");
                }
                return true;
            }

            case "yell": {
                boolean shadow = false;
                if(Punishment.isMuted(player)) {
                    if(!player.shadowMute) {
                        player.sendMessage("You're muted and can't talk.");
                        return true;
                    }
                    shadow = true;
                }
                String message;
                if(query.length() < 5 || (message = query.substring(5).trim()).isEmpty()) {
                    player.sendMessage("You can't yell an empty message.");
                    return true;
                }
                if (message.toLowerCase().contains("nigger")) {
                    player.muteEnd = System.currentTimeMillis() + (900 * 1000);
                    player.sendMessage("You have been muted for 15 minutes for offensive language.");
                    return true;
                }
                if (message.contains("<col=") || message.contains("<img=")) {
                    player.sendMessage("You can't use color or image tags inside your yell!");
                    return true;
                }
                long ms = System.currentTimeMillis(); //ew oh well
                long delay = player.yellDelay - ms;
                if(delay > 0) {
                    long seconds = delay / 1000L;
                    if(seconds <= 1)
                        player.sendMessage("You need to wait 1 more second before yelling again.");
                    else
                        player.sendMessage("You need to wait " + seconds + " more seconds before yelling again.");
                    return true;
                }
                boolean bypassFilter; //basically disallows players to filter staff yells.
                int delaySeconds; //be sure this is set in ascending order.
                if (player.isAdmin() || player.isSupport() || player.isModerator() || player.isSeniorModerator()) {
                    bypassFilter = true;
                    delaySeconds = 0;
                } else if(player.isGroup(PlayerGroup.GODLIKE_DONATOR)) {
                    bypassFilter = false;
                    delaySeconds = 15;
                } else if(player.isGroup(PlayerGroup.UBER_DONATOR) || player.isGroup(PlayerGroup.YOUTUBER) || player.isGroup(PlayerGroup.BETA_TESTER) || player.isGroup(PlayerGroup.DICE_HOST)) {
                    bypassFilter = false;
                    delaySeconds = 30;
                } else if(player.isGroup(PlayerGroup.LEGENDARY_DONATOR)) {
                    bypassFilter = false;
                    delaySeconds = 45;
                } else if(player.isGroup(PlayerGroup.EXTREME_DONATOR)) {
                    bypassFilter = false;
                    delaySeconds = 60;
                } else if(player.isGroup(PlayerGroup.SUPER_DONATOR)) {
                    bypassFilter = false;
                    delaySeconds = 75;
                } else if(player.isGroup(PlayerGroup.DONATOR)) {
                    bypassFilter = false;
                    delaySeconds = 90;
                } else {
                    help.open(player, "yell");
                    return true;
                }

                final boolean shadowText = player.isAdmin() || player.isDonator();
                String color = Color.BLUE.tag(); // Default yell color

                if (player.isAdmin()) {
                    color = "<col=ffff00>";
                } else if (player.isGroup(PlayerGroup.GODLIKE_DONATOR)) {
                    color = Color.WHITE.tag();
                } else if (player.isGroup(PlayerGroup.UBER_DONATOR)) {
                    color = "<col=ffff00>";
                } else if (player.isGroup(PlayerGroup.LEGENDARY_DONATOR)) {
                    color = Color.RAID_PURPLE.tag();
                } else if (player.isGroup(PlayerGroup.EXTREME_DONATOR)) {
                    color = Color.DARK_GREEN.tag();
                } else if (player.isGroup(PlayerGroup.SUPER_DONATOR)) {
                    color = Color.BABY_BLUE.tag();
                } else if (player.isGroup(PlayerGroup.DONATOR) || player.isGroup(PlayerGroup.YOUTUBER)) {
                    color = Color.RED.tag();
                }

                PlayerGroup clientGroup = player.getClientGroup();

                StringBuilder builder = new StringBuilder();
                builder.append(color);
                if (shadowText) {
                    builder.append("<shad=000000>");
                }
                builder.append("[");
                if (clientGroup.clientImgId != -1) {
                    builder.append(clientGroup.tag() + " ");
                }
                builder.append(player.getName());
                builder.append("]");
                builder.append("</col>");
                if (shadowText) {
                    builder.append("</shad>");
                }
                builder.append(": ");
                builder.append(message);

                System.out.println("Color = " + color);

                message = builder.toString();

                player.yellDelay = ms + (delaySeconds * 1000L);

                if(shadow) {
                    player.sendMessage(message);
                    return true;
                }

                for(Player p : World.players) {
                    if(!bypassFilter && p.yellFilter && p.getUserId() != player.getUserId())
                        continue;

                    p.sendMessage(message);
                }

                Loggers.logYell(player.getUserId(), player.getName(), player.getIp(), message);
                return true;
            }

            case "staff":
            case "staffonline": {
                List<String> text = new LinkedList<>();
                List<String> admins = new LinkedList<>();
                List<String> mods = new LinkedList<>();
                List<String> slaves = new LinkedList<>();
                World.players.forEach(p -> {
                    if (p.isAdmin()) admins.add(p.getName());
                    else if (p.isModerator()) mods.add(p.getName());
                    else if (p.isSupport()) slaves.add(p.getName());
                });

                text.add("<img=1><col=bbbb00><shad=0000000> Administrators</col></shad>");
                if (admins.size() == 0) text.add("None online!");
                else text.addAll(admins);
                text.add("");

                text.add("<img=0><col=b2b2b2><shad=0000000> Moderators<col></shad>");
                if (mods.size() == 0) text.add("None online!");
                else text.addAll(mods);
                text.add("");

                text.add("<img=27><col=5bccc4><shad=0000000> Server Supports</col></shad>");
                if (slaves.size() == 0) text.add("None online!");
                else text.addAll(slaves);

                player.sendScroll("Staff Online", text.toArray(new String[0]));
                return true;
            }

            case "pure":
            case "hybrid":
            case "master": {
                if(World.isEco())
                    return true;
                player.dialogue(new MessageDialogue("To select presets, go to your quest tab and click the red button."));
                return true;
            }

            case "store": {
                player.openUrl(World.type.getWorldName() + " Store", World.type.getWebsiteUrl() + "/store");
                return true;
            }

            case "updates": {
                player.openUrl(World.type.getWorldName() + " Updates", World.type.getWebsiteUrl() + "/forum/index.php?forums/development-updates.3/");
                return true;
            }

            case "rules": {
                player.openUrl(World.type.getWorldName() + " Rules", World.type.getWebsiteUrl() + "/forum/index.php?threads/community-rules.1/");
                return true;
            }

            case "vote": {
                player.openUrl(World.type.getWorldName() + " Voting", World.type.getWebsiteUrl() + "/vote");
                return true;
            }

            case "guides": {
                player.openUrl(World.type.getWorldName() + " Guides", World.type.getWebsiteUrl() + "/forum/index.php?forums/guides.7/");
                return true;
            }

            case "support": {
                player.openUrl(World.type.getWorldName() + " Support", World.type.getWebsiteUrl() + "/forum/index.php?forums/general-support.16/");
                return true;
            }

            case "forums": {
                player.openUrl(World.type.getWorldName() + " Forums", World.type.getWebsiteUrl() + "/forum/index.php");
                return true;
            }

            case "hiscores":
            case "scores": {
                player.openUrl(World.type.getWorldName() + " Hiscores", World.type.getWebsiteUrl() + "/hiscores/");
                return true;
            }

            case "discord": {
                player.openUrl("Official " + World.type.getWorldName() + " Discord Server", "https://discord.gg/SSeKYkk");
                return true;
            }

            case "thread": {
                int id;
                try {
                    id = Integer.valueOf(args[0]);
                } catch(Exception e) {
                    player.sendMessage("Invalid topic # entered, please try again.");
                    return true;
                }
                player.openUrl(World.type.getWorldName() + " Thread #" + id, World.type.getWebsiteUrl() + "/forum/index.php?threads/" + id);
                return true;
            }

            case "referral": {
                player.stringInput("Please enter the referral code you'd like to claim:", referralCode -> Referral.checkClaimed(player, referralCode, alreadyClaimed -> {
                    if(alreadyClaimed) {
                        player.dialogue(new MessageDialogue("You've already claimed this referral code."));
                        return;
                    }
                    Referral.claim(player, referralCode, success -> {
                        if(success)
                            player.dialogue(new MessageDialogue("You've successfully claimed the referral code."));
                        else
                            player.dialogue(new MessageDialogue("Error claiming referral code. Please try again later."));
                    });
                }));

                return true;
            }
        }
        return false;
    }

    private static boolean handleSupport(Player player, String query, String command, String[] args) {
        if(!player.isSupport() && !player.isModerator() && !player.isSeniorModerator() && !player.isAdmin())
            return false;
        switch(command) {
            case "staffcommands": {
                break;
            }

            case "jail": {
                forPlayerInt(player, query, "::jail playerName rockCount", (p2, ores) -> Punishment.jail(player, p2, ores));
                return true;
            }

            case "unjail": {
                forPlayer(player, query, "::unjail playerName", p2 -> Punishment.unjail(player, p2));
                return true;
            }

            case "mute": {
                forPlayerTime(player, query, "::mute playerName #d/#h/perm", (p2, time) -> Punishment.mute(player, p2, time, false));
                return true;
            }

            case "unmute": {
                forPlayer(player, query, "::unmute playerName", p2 -> Punishment.unmute(player, p2));
                return true;
            }
        }
        return false;
    }

    private static boolean handleMod(Player player, String query, String command, String[] args) {
        if(!player.isModerator() && !player.isSeniorModerator() && !player.isAdmin())
            return false;
        switch(command) {
            case "kick": {
                forPlayer(player, query, "::kick playerName", p2 -> Punishment.kick(player, p2, false));
                return true;
            }

            case "fkick": {
                forPlayer(player, query, "::kick playerName", p2 -> Punishment.kick(player, p2, true));
                return true;
            }

            case "ban": {
                forPlayer(player, query, "::ban playerName", p2 -> Punishment.ban(player, p2));
                return true;
            }

            case "removedicerank": {
                String name = query.substring(command.length() + 1);
                Player p2 = World.getPlayer(name);
                if(p2 == null)
                    player.sendMessage("Could not find player: " + name);
                else
                    p2.diceHost = false;
                return true;
            }

            case "teleto": {
                String name = query.substring(command.length() + 1);
                Player p2 = World.getPlayer(name);
                if(p2 == null)
                    player.sendMessage("Could not find player: " + name);
                else {
                    if(p2.isAdmin() && !player.isAdmin()) {
                        player.sendMessage("You can't teleport to an administrator.");
                        p2.sendMessage(player.getName() + " has just attempted to teleport to you.");
                        return false;
                    }
                    if(p2.joinedTournament) {
                        player.sendMessage("You can't teleport to a player who's in a tournament.");
                        return false;
                    }
                    player.getMovement().teleport(p2.getPosition());
                }
                return true;
            }

            case "teletome": {
                String name = query.substring(command.length() + 1);
                Player p2 = World.getPlayer(name);
                if(p2 == null)
                    player.sendMessage("Could not find player: " + name);
                else {
                    if(p2.isAdmin() && !player.isAdmin()) {
                        player.sendMessage("You can't teleport an administrator to you.");
                        p2.sendMessage(player.getName() + " has just attempted to teleport you to them.");
                        return false;
                    }
                    if(player.joinedTournament) {
                        player.sendMessage("You can't do this while inside a tournament.");
                        return false;
                    }
                    p2.getMovement().teleport(player.getPosition());
                }
                return true;
            }

        }
        return false;
    }

    private static boolean handleSeniorMod(Player player, String query, String command, String[] args) {
        if(!player.isSeniorModerator() && !player.isAdmin())
            return false;
        switch(command) {

        }
        return false;
    }


    private static final List<String> enabledDevCmds = Arrays.asList(
            "item",
            "pickup",
            "empty",

            "fi",
            "fitem",

            "master",
            "lvl",

            "copyinv",
            "copyarm",

            "inter",
            "heal",
            "debug"
    );

    private static Tournament2 tournament2 = new Tournament2();

    private static boolean handleAdmin(Player player, String query, String command, String[] args) {
        if(!player.isAdmin()) {
            if(!OfflineMode.enabled && (!World.isDev() || !enabledDevCmds.contains(command)))
                return false;
        }
        switch(command) {
            case "killother": {
                Player p2 = World.getPlayer(String.join(" ", args));
                if(p2 == null) {
                    player.sendMessage("Player can't be found.");
                    return true;
                }
                p2.hit(new Hit().fixedDamage(p2.getHp()));
                return true;
            }

            case "disablege": {
                GrandExchange.ENABLED = false;
                player.sendMessage("Grand Exchange " + (GrandExchange.ENABLED ? "enabled" : "disabled") + ".");
                return true;
            }

            case "heatmap": {
                player.heatMap = !player.heatMap;
                player.getPacketSender().setHeatmap(player.heatMap);
                player.sendMessage("Heatmap " + (player.heatMap ? "enabled" : "disabled") + ".");
                return true;
            }

            case "controlnpc": {
                if (args == null || args.length == 0) {
                    player.remove("CONTROLLING_NPC");
                    player.sendMessage("NPC control cleared.");
                    return true;
                } else {
                    int index = Integer.parseInt(args[0]);
                    NPC npc = World.getNpc(index);
                    if (npc == null) {
                        player.sendMessage("Invalid NPC. Use index");
                        return true;
                    } else {
                        player.set("CONTROLLING_NPC", npc);
                        player.sendMessage("You're now controlling " + npc.getDef().name + ".");
                    }
                    return true;
                }
            }

            case "tbuild": {
                for (int i = 0; i < 512; i++) {
                    AIPlayer aip = new AIPlayer("Test " + i, new Position(0, 0, 0));
                    aip.init();

                    tournament2.join(aip);
                }

                tournament2.join(player);
                return true;
            }

            case "tstart": {
                tournament2.start();
                return true;
            }

            case "tjoin": {
                tournament2.join(player);
                return true;
            }

            case "tdestroy": {
                tournament2.destroy();
                tournament2 = new Tournament2();
                return true;
            }

            case "ipban": {
                forPlayer(player, query, "::ban playerName", p2 -> Punishment.IPBan(player, p2));
                return true;
            }

            case "godoverload": {
                player.overloadTicks = 10000;
                player.overloadFlatBoost = 255;
                return true;
            }

            case "resetoverload": {
                player.overloadTicks = 1;
                return true;
            }

            case "inferno": {
                new Inferno(player, Integer.parseInt(args[0]), false).start(true);
                return true;
            }

            case "test2": {
                PlayerCounter.SLAYER_TASKS_COMPLETED.increment(player, 1);
                return true;
            }

            case "dailyreset": {
                player.dailyReset();
                return true;
            }

            case "starttourney": {
                Consumer<TournamentSchedule> run = schedule -> {
                    player.dialogue(new OptionsDialogue("How many minutes until start?",
                            new Option("30 minutes", () -> {
                                Tournament.forceStart(player, schedule, 1800);
                            }),
                            new Option("15 minutes", () -> {
                                Tournament.forceStart(player, schedule, 900);
                            }),
                            new Option("10 minutes", () -> {
                                Tournament.forceStart(player, schedule, 600);
                            }),
                            new Option("5 minutes", () -> {
                                Tournament.forceStart(player, schedule, 300);
                            }),
                            new Option("Cancel")
                    ));
                };
                OptionScroll.open(player, "Select a tournament type", true, Arrays.stream(TournamentSchedule.values()).map(schedule -> new Option(schedule.presetName, () -> run.accept(schedule))).collect(Collectors.toList()));
                return true;
            }

            case "staffbounty": {
                player.dialogue(new OptionsDialogue("Would you like to toggle the event on or off?",
                        new Option("Toggle on", () -> StaffBounty.startEvent(player)),
                        new Option("Toggle off", () -> StaffBounty.stopEvent(player))
                ));
                return true;
            }

            case "tourneybots": {

                return true;
            }

            case "aiplayer":
                player.sendMessage("Spawning AI Player...");
                AIPlayer aip = new AIPlayer("Test AI", new Position(3092, 3494, 0));
                aip.init();
                GoblinKiller goblinKiller = new GoblinKiller(aip);
                aip.runScript(goblinKiller);
                return true;
            case "broadcast":
            case "bc": {
                String message = String.join(" ", args);
                World.players.forEach(p -> {
                    p.getPacketSender().sendMessage(message, "", 14);
                });
                return true;
            }

            case "clearcostumeroom": {
                for (CostumeStorage s : CostumeStorage.values()) {
                    Map<Costume, int[]> stored = s.getSets(player);
                    stored.clear();
                }
                return true;
            }

            case "fillcostumeroom": {
                int count = 0;
                for (CostumeStorage s : CostumeStorage.values()) {
                    s.getSets(player).clear();
                }
                for (CostumeStorage s : CostumeStorage.values()) {
                    Map<Costume, int[]> stored = s.getSets(player);
                    for (Costume costume : s.getCostumes()) {
                        if (stored.get(costume) != null)
                            continue;
                        int[] set = new int[costume.getPieces().length];
                        for (int i = 0; i < costume.getPieces().length; i++) {
                            set[i] = costume.getPieces()[i][0];
                        }
                        stored.put(costume, set);
                        count++;
                    }
                }
                player.sendMessage("Added " + count + " sets.");
                return true;
            }

            case "house": {
                player.house = new House();
                return true;
            }

            case "conenum": {
                int id = Integer.parseInt(args[0]);
                for (int i = 0; i < 2000; i++) {
                    EnumMap map = EnumMap.get(i);
                    if (map.keys != null && map.intValues != null && map.length > 0
                            && (map.ints().containsValue(id) || map.ints().containsKey(id))) {
                        player.sendMessage("Found in enum " + i);
                    }
                }
                return true;
            }

            case "enum": {
                EnumMap map = EnumMap.get(Integer.parseInt(args[0]));
                System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(map));
                return true;
            }

            case "housestyle": {
                player.house.setStyle(HouseStyle.valueOf(String.join("_", args).toUpperCase()));
                player.sendMessage("set!");
                return true;
            }

            case "resethouse": {
                player.house = new House();
                player.sendMessage("House reset");
                return true;
            }

            case "refreshregion": {
                player.getUpdater().updateRegion = true;
                return true;
            }

            case "testbuild": {
                Buildable[] objs = new Buildable[] {Buildable.CRUDE_WOODEN_CHAIR, Buildable.CRUDE_WOODEN_CHAIR, Buildable.CRUDE_WOODEN_CHAIR, Buildable.CRUDE_WOODEN_CHAIR, Buildable.CRUDE_WOODEN_CHAIR, Buildable.CRUDE_WOODEN_CHAIR};
                int count = 1;
                for (Buildable b: objs) {
                    player.getPacketSender().sendClientScript(1404, "iiisi", count++, b.getItemId(), b.getLevelReq(), b.getCreationMenuString(), b.hasLevelAndMaterials(player) ? 1 : 0);
                }
                player.getPacketSender().sendClientScript(1406, "ii", count - 1, 0);
                player.openInterface(InterfaceType.MAIN, Interface.CONSTRUCTION_FURNITURE_CREATION);
                return true;
            }

            case "materials": { // spawns materials for the given object
                Buildable b = Buildable.valueOf(String.join("_", args).toUpperCase());
                b.getMaterials().forEach(player.getInventory()::addOrDrop);
                return true;
            }

            case "allmats": { // spawns materials for all objects in the given room type (last entry in the list, typically the highest level object)
                RoomDefinition def = RoomDefinition.valueOf(String.join("_", args).toUpperCase());
                for (Hotspot hotspot : def.getHotspots()) {
                    if (hotspot != Hotspot.EMPTY)
                        hotspot.getBuildables()[hotspot.getBuildables().length - 1].getMaterials().forEach(player.getInventory()::addOrDrop);
                }
                return true;
            }

            case "conobj": {
                int id = Integer.parseInt(args[0]);
                ItemDef itemDef = ItemDef.get(id);
                player.sendMessage("Looking for objects for item " + itemDef.id + "...");
                List<ObjectDef> found = Arrays.stream(ObjectDef.LOADED)
                        .filter(objectDef -> objectDef != null && objectDef.modelIds != null && Arrays.stream(objectDef.modelIds).anyMatch(model -> model == itemDef.inventoryModel))
                        .collect(Collectors.toList());
                if (found.size() == 0) {
                    player.sendMessage("No matches!");
                } else {
                    found.forEach(def -> {
                        player.sendMessage("Object[" + def.id +"]: \"" + def.name + "\"; Options=" + Arrays.toString(def.options));
                    });
                }
                return true;
            }

            case "fconobj": {
                String name = String.join(" ", args).toLowerCase();
                ObjectDef.forEach(def -> {
                    if (def != null && def.name != null && def.name.toLowerCase().contains(name) && def.options != null && def.options.length >= 5 && def.options[4] != null && def.options[4].equalsIgnoreCase("remove")) {
                        player.sendMessage(def.id + " - " + def.name + " " + Arrays.toString(def.options));
                    }
                });
                return true;
            }

            case "dbolts": {
                for (String gem: Arrays.asList("opal", "jade", "pearl", "topaz", "sapphire", "emerald", "ruby", "diamond", "dragonstone", "onyx")) {
                    String type = gem + " dragon bolts";
                    for (ItemDef def : ItemDef.LOADED) {
                        if (def == null || def.name == null || def.isPlaceholder() || def.isNote()) continue;
                        if (def.name.toLowerCase().startsWith(type.toLowerCase())) {
                            boolean enchanted = def.name.contains("(e)");
                            item_info.Temp inf = new item_info.Temp();
                            inf.id = def.id;
                            inf.tradeable = true;
                            if (enchanted)
                                inf.examine = "Enchanted dragon crossbow bolts, tipped with " + gem + ".";
                            else
                                inf.examine = "Dragon crossbow bolts, tipped with " + gem + ".";
                            inf.ranged_strength_bonus = 122;
                            inf.equip_slot = Equipment.SLOT_AMMO;
                            inf.ranged_level = 64;
                            inf.protect_value = 600;
                            inf.ranged_ammo = enchanted ? "DRAGON_" + gem.toUpperCase() + "_BOLTS" : "DRAGON_BOLTS";
                            System.out.print(new GsonBuilder().setPrettyPrinting().create().toJson(inf));
                            System.out.println(", #" + def.name);
                        }
                    }
                }
                return true;
            }

            case "customtitle": {
                String[] parts = String.join(" ", args).split("\\|");
                player.title = new Title().prefix(parts[0]).suffix(parts[1]);
                player.getAppearance().update();
                return true;
            }

            case "title": {
                if (args == null || args.length == 0) {
                    Title.openSelection(player, true);
                    return true;
                }
                int id = Integer.parseInt(args[0]);
                player.titleId = id;
                player.title = Title.get(id); // bypasses requirements
                player.getAppearance().update();
                return true;
            }

            case "wikidrops": {
                npc_drops.dump(String.join("_", args));
                player.sendMessage("Dumped!");
                return true;
            }

            case "enterraid": {
                if (player.raidsParty == null)
                    player.raidsParty = new Party(player);
                ChambersOfXeric.createChamber(player, player.raidsParty);
                return true;
            }

            case "raid": {
                ChambersOfXeric raid = args == null ? new ChambersOfXeric() : new ChambersOfXeric(String.join(" ", args));
                try {
                    Party party = new Party(player);
                    raid.setParty(party);
                    party.setRaid(raid);
                    raid.generate();
                    raid.startRaid();
                    player.raidsParty = party;
                    player.getMovement().teleport(raid.getStartingChamber().getPosition(15, 15));
                } catch (Exception e) {
                    player.sendMessage("Failed to generate raid: " + e.getMessage());
                    e.printStackTrace();
                }
                return true;
            }

            case "raidroom": {
                int rotation = 0;
                int layout = 0;
                if (args != null && args.length > 0)
                    rotation = Integer.parseInt(args[0]);
                if (args != null && args.length > 1)
                    layout = Integer.parseInt(args[1]);
                int finalRotation = rotation;
                int finalLayout = layout;
                Consumer<ChamberDefinition> run = definition -> {
                    Chamber chamber = definition.newChamber();
                    if (chamber == null) {
                        player.sendMessage("Failed to generate room");
                        return;
                    }
                    ChambersOfXeric raid = new ChambersOfXeric();
                    Party party = new Party(player);
                    player.raidsParty = party;
                    raid.setParty(party);
                    party.setRaid(raid);
                    chamber.setRaid(raid);
                    chamber.setRotation(finalRotation);
                    chamber.setLayout(finalLayout);
                    chamber.setLocation(0, 0, 0);
                    DynamicMap map = new DynamicMap().build(chamber.getChunks());
                    raid.setMap(map);
                    chamber.setBasePosition(new Position(map.swRegion.baseX, map.swRegion.baseY, 0));
                    chamber.onBuild();
                    chamber.onRaidStart();
                    player.getMovement().teleport(chamber.getPosition(15, 15));
                };
                OptionScroll.open(player, "Select a room type", true,
                        Arrays.stream(ChamberDefinition.values()).map(cd -> new Option(cd.getName(), () -> run.accept(cd))).collect(Collectors.toList()));
                return true;
            }

            case "hit":
            case "hitme": {
                player.hit(new Hit().fixedDamage(Integer.parseInt(args[0])).delay(0));
                return true;
            }

            case "tutorial": {
                player.newPlayer = true;
                return true;
            }

            case "debug": {
                player.sendMessage("Debug: " + ((player.debug = !player.debug) ? "ON" : "OFF"));
                return true;
            }

            case "update": {
                World.update(Integer.valueOf(args[0]));
                return true;
            }

            case "objanim": {
                int id = Integer.parseInt(args[0]);
                ObjectDef def = ObjectDef.get(id);
                if (def == null) {
                    player.sendMessage("Invalid id.");
                    return true;
                }
                player.sendMessage("Object uses animation " + def.unknownOpcode24);
                return true;
            }

            case "animateobj": {
                Tile.getObject(-1, player.getAbsX(), player.getAbsY(), player.getHeight(), 10, -1).animate(Integer.parseInt(args[0]));
                return true;
            }

            case "hide": {
                if(player.isHidden()) {
                    player.setHidden(false);
                    player.sendMessage("You are now visible.");
                } else {
                    player.setHidden(true);
                    player.sendMessage("You are now hidden.");
                }
                return true;
            }

            case "kill": {
                player.hit(new Hit().fixedDamage(player.getHp()));
                return true;
            }

            case "killnpcs": {
                for (NPC npc : player.localNpcs()) {
                    if (npc.getCombat() == null)
                        continue;
                    if (player.getCombat().canAttack(npc, true)) {
                        npc.hit(new Hit(player).fixedDamage(npc.getHp()).delay(0));
                    }
                }
                return true;
            }

            case "pvpmagicaccuracy": {
                Hit.PVP_MAGIC_ACCURACY_MODIFIER = Double.valueOf(args[0]);
                player.sendMessage("PVP_MAGIC_ACCURACY_MODIFIER = " + Hit.PVP_MAGIC_ACCURACY_MODIFIER + ";");
                return true;
            }

            case "pvpmeleeaccuracy": {
                Hit.PVP_MELEE_ACCURACY_MODIFIER = Double.valueOf(args[0]);
                player.sendMessage("PVP_MELEE_ACCURACY_MODIFIER = " + Hit.PVP_MELEE_ACCURACY_MODIFIER + ";");
                return true;
            }

            case "settask": {
                if (args == null) {
                    OptionScroll.open(player, "Choose a task", SlayerTask.TASKS.entrySet().stream().map(e -> new Option(e.getKey(), () -> {
                        player.slayerTask = e.getValue();
                        player.slayerTaskRemaining = 100;
                        player.sendMessage("Task set to \"" + e.getKey() + "\"!");
                    })).sorted(Comparator.comparing(o -> o.name)).collect(Collectors.toList()));
                } else {
                    SlayerTask task = SlayerTask.TASKS.get(String.join(" ", args));
                    player.slayerTask = task;
                    player.slayerTaskRemaining = 100;
                    player.sendMessage("Task set to " + task.name + "!");
                }
                return true;
            }

            case "rune": {
                Rune r = Rune.valueOf(args[0].toUpperCase());
                player.getInventory().add(r.getId(), Integer.MAX_VALUE);
                return true;
            }

            case "setbase": {
                relativeBase = player.getPosition().copy();
                player.sendMessage("Base set to: " + relativeBase.toString());
                return true;
            }

            case "rel":
            case "relative": {
                int x = player.getAbsX() - relativeBase.getX();
                int y = player.getAbsY() - relativeBase.getY();
                System.out.println("{" + x + ", " + y + "},");
                return true;
            }

            case "shake": {
                player.getPacketSender().shakeCamera(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                return true;
            }

            case "zulrahdeath": {
                player.getPacketSender().sendItems(-1, 0, 525, new Item(4151, 1));
                Unlock unlock = new Unlock(602, 3, 0, 89);
                unlock.unlockRange(player, 0, 10);
                player.openInterface(InterfaceType.MAIN, 602);
                return true;
            }

            case "calcmining": {
                Pickaxe pick =  Pickaxe.find(player);
                if (pick == null) {
                    player.sendMessage("Equip a pickaxe!!");
                    return true;
                }
                for (Rock rock : Rock.values()) {
                    if (rock.multiOre != null)
                        continue;
                    double chance = Mining.chance(Mining.getEffectiveLevel(player), rock, pick) / 100;
                    double oresPerTick = chance / 2;
                    double oresPerHour = oresPerTick * 60 * 100;
                    double xpPerHour = oresPerHour * rock.experience * StatType.Mining.defaultXpMultiplier;
                    System.out.println(rock + ": ores/h=" + NumberUtils.formatNumber((long) oresPerHour) + " xp/h=" + NumberUtils.formatNumber((long) xpPerHour) + " chance=" + NumberUtils.formatTwoPlaces(chance));
                }
                return true;
            }

            case "oldcasket": {
                NPCDef def = NPCDef.get(Integer.parseInt(args[0]));
                if (def == null) {
                    return true;
                }
                double baseChance = 0.3;
                double largeChance = Math.min(def.combatLevel, 100 * 2 * 2) / (250.0 * 2 * 2);
                double medChance = Math.min(def.combatLevel, 100 * 2) / (250.0 * 2) * (1 - largeChance);
                double smallChance = (1 - largeChance) * (1 - medChance);
                largeChance *= baseChance;
                medChance *= baseChance;
                smallChance *= baseChance;
                player.sendMessage("small=" + NumberUtils.formatTwoPlaces(smallChance) + ", medium=" + NumberUtils.formatTwoPlaces(medChance) + ", large=" + NumberUtils.formatTwoPlaces(largeChance));
                int smallReturn = (int) (((15000 + 35000) / 2) * smallChance);
                int mediumReturn = (int) (((35000 + 5000) / 2) * medChance);
                int largeReturn = (int) (((50000 + 75000) / 2) * largeChance);
                int perKill = (smallReturn + mediumReturn + largeReturn);
                player.sendMessage("smallgp=" + smallReturn + ", medgp=" + mediumReturn +", largegp=" + largeReturn);
                player.sendMessage("avg/kill=" + perKill + ", with wealth=" + (perKill * 1.5) + ", wealth+wild=" + (perKill * 2.25));
                return true;
            }

            case "newcasket": {
                NPCDef def = NPCDef.get(Integer.parseInt(args[0]));
                if (def == null) {
                    return true;
                }
                NPC npc = new NPC(def.id);
                GoldCasket casket = GoldCasket.getCasket(npc);
                if (casket == null) {
                    player.sendMessage("none");
                    return true;
                }
                int averageGold = (casket.getMinAmount() + casket.getMaxAmount()) / 2;
                double chance = casket.getDropChance().apply(npc);
                int goldPerKill = (int) (chance * averageGold);
                player.sendMessage("type=" + casket.toString() + ", chance=" + NumberUtils.formatTwoPlaces(chance) + ", perKill=" + goldPerKill);
                return true;
            }

            case "img": {
                player.sendMessage("<img=" + Integer.parseInt(args[0]) + ">");
                return true;
            }

            case "webhook": {
                TournamentEmbedMessage.sendDiscordMessage("Dharok", "15 minutes");
                return true;
            }

            case "sproj": {
                new Projectile(Integer.parseInt(args[0]), 60, 60, 0, 300, 20, 55, 64).send(player.getAbsX(), player.getAbsY(), player.getAbsX() - 5, player.getAbsY());
                return true;
            }

            case "projloop": {
                player.startEvent(event -> {
                    int id = 0;
                    if (args.length > 0)
                        id = Integer.parseInt(args[0]);
                    while (id < GfxDef.LOADED.length) {
                        new Projectile(id, 60, 60, 0, 300, 20, 0, 64).send(player.getAbsX(), player.getAbsY(), player.getAbsX() - 5, player.getAbsY());
                        player.sendMessage("Sending: " + id);
                        id++;
                        event.delay(1);
                    }
                });
                return true;
            }

            case "removeplayers": {
                World.players.forEach(Player::forceLogout);
                return true;
            }

            case "resetslayertask": {
                Player p2 = World.getPlayer(String.join(" ", args));
                if(p2 == null) {
                    player.sendMessage("Player can't be found.");
                    return true;
                }
                Slayer.reset(p2);
                p2.sendMessage("Your slayer task has been reset.");
                player.sendMessage("You have reset " + p2.getName() + "'s task.");
                return true;
            }

            case "checkclue": {
                Player p2 = World.getPlayer(String.join(" ", args));
                if(p2.easyClue != null)
                    player.sendMessage("Easy[" + p2.easyClue.id + "]");
                if(p2.medClue != null)
                    player.sendMessage("Med[" + p2.medClue.id + "]");
                if(p2.hardClue != null)
                    player.sendMessage("Hard[" + p2.hardClue.id + "]");
                if(p2.eliteClue != null)
                    player.sendMessage("Elite[" + p2.eliteClue.id + "]");
                if(p2.masterClue != null)
                    player.sendMessage("MASTER[" + p2.masterClue.id + "]");
                return true;
            }

            case "map": {
                player.getPacketSender().sendMapState(Integer.valueOf(args[0]));
                return true;
            }

            case "lms": {
                DynamicMap lmsMap = new DynamicMap()
                        .buildSw(13658, 1)
                        .buildNw(13659, 1)
                        .buildSe(13914, 1)
                        .buildNe(13915, 1);
                player.getMovement().teleport(lmsMap.swRegion.baseX, lmsMap.swRegion.baseY, 0);
                return true;
            }

            case "attribs": {
                int id = Integer.valueOf(args[0]);
                ItemDef def = ItemDef.get(id);
                if(def == null) {
                    player.sendMessage("Item " + id + " not found!");
                    return true;
                }
                if(def.attributes == null) {
                    player.sendMessage("Item " + id + " has no attributes!");
                    return true;
                }
                System.out.println("Attributes for item " + id + ":");
                def.attributes.forEach((key, value) -> System.out.println("    " + key + "=" + value));
                return true;
            }

            case "save": {
                player.sendMessage("Saving online players...");
                for(Player p : World.players)
                    PlayerFile.save(p, -1);
                player.sendMessage("DONE!");
                return true;
            }

            case "addbots": {
                int amount = Integer.valueOf(args[0]);
                int range = Integer.valueOf(args[1]);
                Bounds bounds = new Bounds(player.getPosition(), range);
                for(int i = 0; i < amount; i++)
                    PlayerBot.create(new Position(bounds.randomX(), bounds.randomY(), bounds.z), bot -> {});
                return true;
            }

            case "removebots": {
                int remove = args != null && args.length >= 1 ? Integer.valueOf(args[0]) : Integer.MAX_VALUE;
                for(Player p : World.players) {
                    if(p.getChannel().id() == null && remove-- > 0)
                        p.logoutStage = -1;
                }
                return true;
            }

            case "osw":
            case "oswiki": {
                player.getPacketSender().sendUrl("https://oldschool.runescape.wiki/?search=" + String.join("+", args), false);
                return true;
            }

            case "sound": {
                int id = Integer.valueOf(args[0]);
                int type = args.length >= 2 ? Integer.valueOf(args[1]) : 1;
                int delay = args.length >= 3 ? Integer.valueOf(args[2]) : 0;
                player.privateSound(id, type, delay);
                return true;
            }

            case "interface":
            case "inter": {
                int interfaceId = Integer.valueOf(args[0]);
                InterfaceType type = InterfaceType.MAIN;
                if(args.length == 2)
                    type = InterfaceType.valueOf(args[1].toUpperCase());
                player.temp.put("last_inter_cmd", interfaceId);
                player.openInterface(type, interfaceId);
                return true;
            }

            case "inters": {
                InterfaceType type = InterfaceType.MAIN;
                if(args != null && args.length == 1)
                    type = InterfaceType.valueOf(args[0].toUpperCase());
                int interfaceId = (int) player.temp.getOrDefault("last_inter_cmd", 0);
                if(interfaceId == 548 || interfaceId == 161 || interfaceId == 164) //main screen
                    interfaceId++;
                if(interfaceId == Interface.CHAT_BAR) //chat box
                    interfaceId++;
                if(interfaceId == 156) //annoying
                    interfaceId++;
                player.temp.put("last_inter_cmd", interfaceId + 1);
                player.openInterface(type, interfaceId);
                player.sendFilteredMessage("Interface: " + interfaceId);
                return true;
            }

            case "ic":
            case "iconf": {
                int interfaceId = Integer.valueOf(args[0]);
                boolean recursiveSearch = args.length >= 2 && Integer.valueOf(args[1]) == 1;
                InterfaceDef.printConfigs(interfaceId, recursiveSearch);
                return true;
            }

            case "findinterscript": {
                int scriptId = Integer.valueOf(args[0]);
                boolean recursiveSearch = args.length >= 2 && Integer.valueOf(args[1]) == 1;
                for (int interId = 0; interId < InterfaceDef.COUNTS.length; interId++) {
                    Set<ScriptDef> s = InterfaceDef.getScripts(interId, recursiveSearch);
                    if (s != null && s.stream().anyMatch(def -> def.id == scriptId)) {
                        player.sendMessage("Inter " + interId + " uses script " + scriptId +"!");
                    }
                }
                return true;
            }

            case "bits": {
                int id = Integer.valueOf(args[0]);
                Varp varp = Varp.get(id);
                if(varp == null) {
                    player.sendFilteredMessage("Varp " + id + " has no bits!");
                    return true;
                }
                System.out.println("Varp: " + id);
                for(Varpbit bit : varp.bits)
                    System.out.println("    bit: " + bit.id + "  shift: " + bit.leastSigBit);
                return true;
            }

            case "v":
            case "varp": {
                int id = Integer.valueOf(args[0]);
                int value = Integer.valueOf(args[1]);
                if(id < 0 || id >= 2000) {
                    player.sendFilteredMessage("Varp " + id + " does not exist.");
                    return true;
                }
                Config.create(id, null, false, false).set(player, value);
                player.sendFilteredMessage("Updated varp " + id + "!");
                return true;
            }

            case "vb":
            case "varpbit": {
                int id = Integer.valueOf(args[0]);
                int value = Integer.valueOf(args[1]);
                Varpbit bit = Varpbit.get(id);
                if(bit == null) {
                    player.sendFilteredMessage("Varpbit " + id + " does not exist.");
                    return true;
                }
                Config.create(id, bit, false, false).set(player, value);
                player.sendFilteredMessage("Updated varp " + bit.varpId + " with varpbit " + id + "!");
                return true;
            }

            case "vbs":
            case "varpbits": {
                int minId = Integer.valueOf(args[0]);
                int maxId = Integer.valueOf(args[1]);
                int value = Integer.valueOf(args[2]);
                if(minId < 0 || minId >= Varpbit.LOADED.length || maxId < 0 || maxId >= Varpbit.LOADED.length) {
                    player.sendFilteredMessage("Invalid values! Please use values between 0 and " + (Varpbit.LOADED.length - 1) + "!");
                    return true;
                }
                for(int i = minId; i <= maxId; i++) {
                    Varpbit bit = Varpbit.get(i);
                    if(bit == null)
                        continue;
                    Config.create(i, bit, false, false).set(player, value);
                }
                return true;
            }

            case "string": {
                StringBuilder sb = new StringBuilder();
                for(int i = 2; i < args.length; i++)
                    sb.append(args[i]).append(" ");
                player.getPacketSender().sendString(Integer.valueOf(args[0]), Integer.valueOf(args[1]), sb.toString());
                return true;
            }
            case "strings": {
                int interfaceId = Integer.valueOf(args[0]);
                for(int i = 0; i < InterfaceDef.COUNTS[interfaceId]; i++) {
                    player.getPacketSender().sendString(interfaceId, i, "" + i);
                    player.getPacketSender().setHidden(interfaceId, i, false);
                }
                return true;
            }

            case "ichide": {
                int parentId = Integer.valueOf(args[0]);
                int minChildId = Integer.valueOf(args[1]);
                int maxChildId = args.length > 2 ? Integer.valueOf(args[2]) : minChildId;
                for(int childId = minChildId; childId <= maxChildId; childId++)
                    player.getPacketSender().setHidden(parentId, childId, true);
                return true;
            }

            case "icshow": {
                int parentId = Integer.valueOf(args[0]);
                int minChildId = Integer.valueOf(args[1]);
                int maxChildId = args.length > 2 ? Integer.valueOf(args[2]) : minChildId;
                for(int childId = minChildId; childId <= maxChildId; childId++)
                    player.getPacketSender().setHidden(parentId, childId, false);
                return true;
            }

            case "si": {
                int itemId = Integer.valueOf(args[0]);
                SkillDialogue.make(player, new SkillItem(itemId).addReq(p -> false));
                return true;
            }

            case "script": {
                int id = Integer.valueOf(args[0]);
                ScriptDef def = ScriptDef.get(id);
                if(def == null) {
                    System.err.println("Script " + id + " does not exist!");
                    return true;
                }
                def.print(System.out);
                return true;
            }

            case "dumpscripts": {
                for(int i = 0; i < 65535; i++) {
                    ScriptDef def = ScriptDef.get(i);
                    if(def == null)
                        continue;
                    try(PrintStream ps = new PrintStream(System.getProperty("user.home") + "/Desktop/script_instructions/" + i + ".txt")) {
                        def.print(ps);
                        ps.flush();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }

            case "findintinscript": {
                int search = Integer.parseInt(args[0]);
                for (ScriptDef def : ScriptDef.LOADED) {
                    if (def == null)
                        continue;
                    if (def.intArgs == null)
                        continue;
                    for (int i : def.intArgs)
                        if (i == search)
                            System.out.println("Found in " + def.id);
                }
                return true;
            }

            case "findstringinscript": {
                String search = String.join(" ", args).toLowerCase();
                for (ScriptDef def : ScriptDef.LOADED) {
                    if (def == null)
                        continue;
                    if (def.stringArgs == null)
                        continue;
                    for (String s : def.stringArgs) {
                        if (s == null)
                            continue;
                        if (s.toLowerCase().contains(search))
                            System.out.println("Found " + s + " in " + def.id);
                    }
                }
                return true;
            }

            case "findvarcinscript": {
                int id = Integer.parseInt(args[0]);
                for (ScriptDef def : ScriptDef.LOADED) {
                    if (def == null)
                        continue;
                    if (def.intArgs == null)
                        continue;
                    for (int i = 0; i < def.instructions.length; i++) {
                        if (def.instructions[i] == 43 && def.intArgs[i] == id) {
                            player.sendMessage("Script " + def.id + " sets varc " + id);
                        }
                    }
                }
                return true;
            }

            case "npc": {
                int npcId = Integer.valueOf(args[0]);
                NPCDef def = NPCDef.get(npcId);
                if(def == null) {
                    player.sendMessage("Invalid npc id: " + npcId);
                    return true;
                }
                new NPC(npcId).spawn(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
                return true;
            }

            case "fnpc": {
                String search = query.substring(5);
                int combat = -1;
                if(search.contains(":")) {
                    String[] s = search.split(":");
                    search = s[0];
                    combat = Integer.valueOf(s[1]);
                }
                for(NPCDef def : NPCDef.LOADED) {
                    if(def != null && def.name.toLowerCase().contains(search.toLowerCase()) && (combat == -1 || def.combatLevel == combat))
                        player.sendMessage(def.id + " (" + def.name + "): combat=" + def.combatLevel + " options=" + Arrays.toString(def.options) +" size=" + def.size);
                }
                return true;
            }

            case "pnpc": {
                int npcId = Integer.valueOf(args[0]);
                if(npcId > 0) {
                    NPCDef def = NPCDef.get(npcId);
                    if(def == null) {
                        player.sendMessage("Invalid npc id: " + npcId);
                        return true;
                    }
                    player.temp.put("LAST_PNPC", npcId);
                    player.getAppearance().setNpcId(npcId);
                    player.sendMessage(def.name + " " + def.size);
                } else {
                    player.getAppearance().setNpcId(-1);
                }
                player.getAppearance().update();
                return true;
            }

            case "pnpcs": {
                Integer lastId = (Integer) player.temp.get("LAST_PNPC");
                if(lastId == null)
                    lastId = 0;
                NPCDef def = NPCDef.get(lastId);
                if(def == null) {
                    player.sendMessage("Invalid npc id: " + lastId);
                    return true;
                }
                player.getAppearance().setNpcId(lastId);
                player.sendMessage("pnpc: " + lastId);
                player.temp.put("LAST_PNPC", lastId + 1);
                player.getAppearance().update();
                return true;
            }

            case "removenpc": {
                int id = Integer.parseInt(args[0]);
                int count = 0;
                for (NPC npc : player.localNpcs()) {
                    if (npc.getId() == id && !npc.defaultSpawn) {
                        npc.remove();
                        count++;
                    }
                }
                player.sendMessage("Removed " + count + " NPCs with id " + id + ".");
                return true;
            }

            case "calc":
            case "calculate": {
                int id = Integer.valueOf(args[0]);
                NPCDef def = NPCDef.get(id);
                if(def == null) {
                    player.sendMessage("Invalid npc id: " + id);
                    return true;
                }
                if(def.lootTable == null) {
                    player.sendMessage(def.name + " doesn't have a loot table.");
                    return true;
                }
                def.lootTable.calculate(def.name + " Loot Probability Table");
                return true;
            }

            case "ispawn": {
                int id = Integer.valueOf(args[0]);
                ItemDef def = ItemDef.get(id);
                if (def == null) {
                    player.sendMessage("Invalid id!");
                    return true;
                }
                System.out.println("  {\"id\": " + id + ", \"x\": " + player.getAbsX() +", \"y\": " + player.getAbsY() + ", \"z\": " + player.getHeight() + "}, // " + def.name);
                new GroundItem(new Item(id, 1)).position(player.getPosition()).spawnWithRespawn(2);
                return true;
            }

            case "addnpc": { // TODO support more options
                int id = Integer.valueOf(args[0]);
                NPCDef def = NPCDef.get(id);
                if(def == null) {
                    player.sendMessage("Invalid npc id: " + id);
                    return true;
                }
                int range = args.length > 1 ? Integer.valueOf(args[1]) : 3;
                System.out.println("{\"id\": " + id + ", \"x\": " + player.getAbsX() + ", \"y\": " + player.getAbsY() + ", \"z\": " + player.getHeight() +", \"walkRange\": " + range + "}, // " + def.name);
                new NPC(id).spawn(player.getPosition());
                return true;
            }

            case "findspawnednpc": {
                int id = Integer.parseInt(args[0]);
                World.npcs.forEach(npc -> {
                    if(npc.getId() == id) {
                        player.sendMessage("Found at " + npc.getPosition());
                    }
                });
                return true;
            }

            case "npcanims": {
                int sourceId = Integer.parseInt(args[0]);
                NPCDef sourceDef = NPCDef.get(sourceId);
                if(sourceDef == null) {
                    player.sendMessage("Invalid NPC!");
                    return true;
                }
                player.sendMessage("Stand: " + sourceDef.standAnimation + " Walk: " + sourceDef.walkAnimation);
                SortedSet<Integer> results = AnimDef.findAnimationsWithSameRigging(sourceDef.walkAnimation, sourceDef.standAnimation, sourceDef.walkBackAnimation, sourceDef.walkLeftAnimation, sourceDef.walkRightAnimation);
                if(results == null) {
                    player.sendMessage("Nothing found!");
                    return true;
                }
                System.out.println(Arrays.toString(results.toArray()));
                return true;
            }

            case "similaranims": {
                int sourceId = Integer.parseInt(args[0]);
                AnimDef source = AnimDef.LOADED[sourceId];
                SortedSet<Integer> results = AnimDef.findAnimationsWithSameRigging(sourceId);
                if(results == null) {
                    player.sendMessage("Nothing found!");
                    return true;
                }
                System.out.println("Same rigging: " + Arrays.toString(results.toArray()));
                results.clear();
                for (int id = 0; id < AnimDef.LOADED.length; id++) {
                    AnimDef def = AnimDef.LOADED[id];
                    if (def == null || def.frameData == null) continue;
                    if (def.frameData[0] == source.frameData[0]) { // TODO consider checking other frames and outputting a % match?
                        results.add(id);
                    }
                }
                System.out.println("Similar frames: " + Arrays.toString(results.toArray()));
                return true;
            }

            case "dumpnpcanims": {
                try(BufferedWriter bw = new BufferedWriter(new FileWriter("npcanims.txt"))) {
                    bw.write("id\tname\tanims");
                    bw.newLine();
                    for(NPCDef def : NPCDef.LOADED) {
                        bw.write(String.valueOf(def.id));
                        bw.write("\t");
                        bw.write(def.name);
                        bw.write("\t");
                        SortedSet<Integer> anims = AnimDef.findAnimationsWithSameRigging(def.walkAnimation, def.standAnimation, def.walkBackAnimation, def.walkLeftAnimation, def.walkRightAnimation);
                        if(anims == null)
                            bw.write("[none found]");
                        else
                            bw.write(Arrays.toString(anims.toArray()));
                        bw.newLine();
                        bw.flush();
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
                player.sendMessage("Done");
            }

            case "reloadnpcs": {
                World.npcs.forEach(NPC::remove); //todo fix this
                DataFile.reload(player, npc_spawns.class);
                return true;
            }

            case "dumpdrops": {
                npc_drops.dump(String.join("_", args));
                return true;
            }

            case "reloaddrops": {
                NPCDef.forEach(def -> def.lootTable = null);
                DataFile.reload(player, npc_drops.class);
                return true;
            }

            case "clear":
            case "empty": {
                player.getInventory().clear();
                return true;
            }

            case "item":
            case "pickup": {
                int[] ids = NumberUtils.toIntArray(args[0]);
                int amount = args.length > 1 ? NumberUtils.intValue(args[1]) : 1;
                for(int id : ids) {
                    if(id != -1)
                        player.getInventory().add(id, amount);
                }
                return true;
            }

            case "fi":
            case "find":
            case "fitem": {
                int l = command.length() + 1;
                if(query.length() > l) {
                    String search = query.substring(l).toLowerCase();
                    int found = 0;
                    ItemDef exactMatch = null;
                    for(ItemDef def : ItemDef.LOADED) {
                        if(def == null || def.name == null)
                            continue;
                        if(def.isNote() || def.isPlaceholder())
                            continue;
                        String name = def.name.toLowerCase();
                        if(name.contains(search)) {
                            player.sendFilteredMessage("    " + def.id + ": " + def.name);
                        }
                        if(name.equals(search)) {
                            if(exactMatch == null)
                                exactMatch = def;
                        }
                    }
                    if(exactMatch != null) {
                        player.sendFilteredMessage("Most relevant result for '" + search + "':");
                        player.sendFilteredMessage("    " + exactMatch.id + ": " + exactMatch.name);
                        player.getInventory().add(exactMatch.id, 1);
                    }
                    return true;
                }
                player.itemSearch("Select an item to spawn", false, itemId -> {
                    Item item = new Item(itemId, 1);
                    player.integerInput("How many would you like to spawn:", amt -> {
                        if(item.getDef().stackable)
                            player.getInventory().add(itemId, amt);
                        else if(item.getDef().notedId != -1 && amt > 1)
                            player.getInventory().add(item.getDef().notedId, amt);
                        else
                            player.getInventory().add(itemId, amt);

                        player.sendFilteredMessage("Spawned " + amt + "x " + item.getDef().name);
                    });
                });
                return true;
            }

            case "b":
            case "bank":
            case "openbank": {
                player.getBank().open();
                return true;
            }

            case "reloaditems": {
                DataFile.reload(player, shield_types.class);
                DataFile.reload(player, weapon_types.class);
                DataFile.reload(player, item_info.class);
                return true;
            }

            case "reloadshops": {
                DataFile.reload(player, npc_shops.class);
                return true;
            }

            case "pinv": {
                StringBuilder sb = new StringBuilder();
                for(Item item : player.getInventory().getItems()) {
                    if(item != null)
                        sb.append(item.getId()).append(",");
                }
                System.out.println(sb.substring(0, sb.length() - 1));
                return true;
            }

            case "wipe": {
                String name = query.substring(command.length() + 1);
                Player p2 = World.getPlayer(name);
                if(p2 == null) {
                    player.sendMessage("Could not find player: " + name);
                    return true;
                }
                player.dialogue(
                        new MessageDialogue("Are you sure you want to wipe player: " + p2.getName() + "?"),
                        new OptionsDialogue(
                                new Option("Yes", () -> { //todo log this
                                    if(!p2.isOnline()) {
                                        player.sendMessage(p2.getName() + " is no longer online!");
                                        return;
                                    }
                                    p2.getInventory().clear();
                                    p2.getEquipment().clear();
                                    p2.getBank().clear();
                                }),
                                new Option("No", player::closeDialogue)
                        )
                );
                return true;
            }

            case "pos": {
                System.out.println("new Position(" + player.getAbsX() + ", " + player.getAbsY() + ", " + player.getHeight() + "),");
                return true;
            }

            case "coords": {
                player.sendMessage("Abs: " + player.getPosition().getX() + ", " + player.getPosition().getY() + ", " + player.getPosition().getZ());
                System.out.println("{" + (player.getAbsX() - player.getPosition().getRegion().baseX) + ", " + (player.getAbsY() - player.getPosition().getRegion().baseY) + "},");
                return true;
            }

            case "chunk": {
                int chunkX = player.getPosition().getChunkX();
                int chunkY = player.getPosition().getChunkY();
                int chunkAbsX = chunkX << 3;
                int chunkAbsY = chunkY << 3;
                int localX = player.getPosition().getX() - chunkAbsX;
                int localY = player.getPosition().getY() - chunkAbsY;
                Region region = Region.get(chunkAbsX, chunkAbsY);
                int pointX = (player.getPosition().getX() - region.baseX) / 8;
                int pointY = (player.getPosition().getY() - region.baseY) / 8;
                player.sendMessage("Chunk: " + chunkX + ", " + chunkY);
                player.sendMessage("    abs = " + chunkAbsX + ", " + chunkAbsY);
                player.sendMessage("    local = " + localX + ", " + localY);
                player.sendMessage("    points =  " + pointX + ", " + pointY);
                return true;
            }

            case "region": {
                Region region;
                if(args == null || args.length == 0)
                    region = player.getPosition().getRegion();
                else
                    region = Region.get(Integer.valueOf(args[0]));
                player.sendMessage("Region: " + region.id);
                player.sendMessage("    base = " + region.baseX + "," + region.baseY);
                player.sendMessage("    empty = " + region.empty);
                return true;
            }

            case "clipping": {
                Tile tile = Tile.get(player.getAbsX(), player.getAbsY(), player.getHeight(), false);
                player.sendMessage("Clipping: " + (tile == null ? -1 : tile.clipping));
                System.out.println(tile.clipping & ~RouteFinder.UNMOVABLE_MASK);
                return true;
            }

            case "tele":
            case "teleport": {
                if(args == null || args.length == 0) {
                    player.teleportsWizard = null;
                    teleports.open(player);
                    return true;
                }
                int x, y, z;
                try {
                    x = Integer.valueOf(args[0]);
                    y = Integer.valueOf(args[1]);
                    if(args.length > 2)
                        z = Math.max(0, Math.min(3, Integer.valueOf(args[2])));
                    else
                        z = player.getPosition().getZ();
                } catch(Exception e) {
                    int l = command.length() + 1;
                    if(query.length() <= l)
                        return true;
                    String loc = query.substring(l).trim();
                    Location location = Location.find(loc);
                    if(location == null) {
                        player.sendMessage("Invalid teleport location: " + loc);
                        return true;
                    }
                    x = location.x;
                    y = location.y;
                    z = location.z;
                }
                int regionId = Region.getId(x, y);
                if(regionId < 0 || regionId >= Region.LOADED.length) {
                    player.sendMessage("Invalid teleport coordinates: " + x + ", " + y + ", " + z);
                    return true;
                }
                player.getMovement().teleport(x, y, z);
                return true;
            }

            case "height": {
                int z = Integer.valueOf(args[0]);
                if(z < 0)
                    z = 0;
                else if(z > 3)
                    z = 3;
                player.getMovement().teleport(player.getAbsX(), player.getAbsY(), z);
                return true;
            }

            case "down": {
                player.getMovement().teleport(player.getAbsX(), player.getAbsY(), Math.max(0, player.getHeight() - 1));
                return true;
            }

            case "up": {
                player.getMovement().teleport(player.getAbsX(), player.getAbsY(), Math.min(3, player.getHeight() + 1));
                return true;
            }

            case "ix": {
                int increment = Integer.valueOf(args[0]);
                int x = player.getPosition().getX() + increment;
                int y = player.getPosition().getY();
                int z = player.getPosition().getZ();
                player.getMovement().teleport(x, y, z);
                return true;
            }

            case "iy": {
                int increment = Integer.valueOf(args[0]);
                int x = player.getPosition().getX();
                int y = player.getPosition().getY() + increment;
                int z = player.getPosition().getZ();
                player.getMovement().teleport(x, y, z);
                return true;
            }

            case "iz": {
                int increment = Integer.valueOf(args[0]);
                int x = player.getPosition().getX();
                int y = player.getPosition().getY();
                int z = player.getPosition().getZ() + increment;
                player.getMovement().teleport(x, y, z);
                return true;
            }

            case "todung": {
                player.getMovement().teleport(player.getAbsX(), player.getAbsY() + 6400, player.getHeight());
                return true;
            }

            case "fromdung": {
                player.getMovement().teleport(player.getAbsX(), player.getAbsY() - 6400, player.getHeight());
                return true;
            }

            case "obj": {
                int id = Integer.valueOf(args[0]);
                int type = 10;
                if(args.length > 1)
                    type = Integer.valueOf(args[1]);
                int direction = 0;
                if(args.length > 2)
                    direction = Integer.valueOf(args[2]);
                player.getPacketSender().sendCreateObject(id, player.getAbsX(), player.getAbsY(), player.getHeight(), type, direction);
                return true;
            }

            case "addobj": {
                int id = Integer.valueOf(args[0]);
                int type = 10;
                if(args.length > 1)
                    type = Integer.valueOf(args[1]);
                int direction = 0;
                if(args.length > 2)
                    direction = Integer.valueOf(args[2]);
                GameObject.spawn(id, player.getPosition(), type, direction);
                return true;
            }

            case "objs": {
                Tile tile = Tile.get(player.getAbsX(), player.getAbsY(), player.getHeight(), false);
                if(tile == null || tile.gameObjects == null) {
                    player.sendMessage("No objects.");
                    return true;
                }
                if(tile.gameObjects.isEmpty()) {
                    player.sendMessage("No objects?");
                    return true;
                }
                tile.checkActive();
                player.sendMessage("Tile active: " + tile.isActive());
                for(GameObject object : tile.gameObjects) {
                    int varpId;
                    int varpbitId;
                    if(object.id == -1) {
                        varpId = -1;
                        varpbitId = -1;
                    } else {
                        varpId = object.getDef().varpId;
                        varpbitId = object.getDef().varpBitId;
                    }
                    player.sendMessage("id=" + object.id + "  x=" + object.x + "  y=" + object.y + "  z=" + object.z + "  type=" + object.type + "  dir=" + object.direction + " varpbitId=" + varpbitId + " varpId=" + varpId + " clipType="+ object.getDef().clipType);
                    //System.out.println("{" + object.id + ", " + object.x + ", " + object.y + ", " + object.z + ", " + object.type + ", " + object.direction + "},");
                   // System.out.println("obj(" + object.id + ", " + object.x + ", " + object.y + ", " + object.z + ", " + object.type + ", " + object.direction + ").remove();");
                }
                return true;
            }

            case "fobj": {
                String search = query.substring(5);
                int number = -1;
                try {
                    number = Integer.parseInt(search);
                } catch (Exception e) {

                }
                int finalNumber = number;
                ObjectDef.forEach(def -> {
                    if(def != null && def.name != null && def.name.toLowerCase().contains(search))
                        player.sendMessage(def.id + " (" + def.name + ") options=" + Arrays.toString(def.options));
                    if(finalNumber != -1 && def != null && def.unknownOpcode24 == finalNumber)
                        player.sendMessage(def.id + " uses anim " + search);
                    if (finalNumber != -1 && def.modelIds != null && Arrays.stream(def.modelIds).anyMatch(i -> finalNumber == i))
                        player.sendMessage(def.id + " uses model " + search);
                });
                return true;
            }

            case "findinregion": {
                int id = Integer.parseInt(args[0]);
                for (Region region : player.getRegions())
                    for(int x = 0; x < 64; x++)
                        for(int y = 0; y < 64; y++)
                            for(int z = 0; z < 4; z++) {
                                Tile t = region.getTile(region.baseX + x, region.baseY + y, z, false);
                                if(t == null)
                                    continue;
                                if(t.gameObjects != null && t.gameObjects.stream().anyMatch(o -> o.id == id)) {
                                    player.sendMessage("Found at " + (region.baseX + x) + "," + (region.baseY + y) + "," + z);
                                }
                                if(t.gameObjects != null && t.gameObjects.stream().anyMatch(o -> o.id != -1 && o.getDef().showIds != null && Arrays.stream(o.getDef().showIds).anyMatch(i -> i == id))) {
                                    player.sendMessage("Found <col=ff0000>in container</col> at " + (region.baseX + x) + "," + (region.baseY + y) +"," + z);
                                }
                            }
                return true;
            }

            case "findinmap": {
                int id = Integer.parseInt(args[0]);
                CompletableFuture.runAsync(() -> {
                    for(Region region : Region.LOADED) {
                        if(region == null)
                            continue;
                        for(int x = 0; x < 64; x++)
                            for(int y = 0; y < 64; y++)
                                for(int z = 0; z < 4; z++) {
                                    Tile t = region.getTile(region.baseX + x, region.baseY + y, z, false);
                                    if(t == null)
                                        continue;
                                    if(t.gameObjects != null && t.gameObjects.stream().anyMatch(o -> o.id == id)) {
                                        player.sendMessage("Found at " + (region.baseX + x) + "," + (region.baseY + y) + "," + z);
                                    }
                                    if(t.gameObjects != null && t.gameObjects.stream().anyMatch(o -> o.getDef().showIds != null && Arrays.stream(o.getDef().showIds).anyMatch(i -> i == id))) {
                                        player.sendMessage("Found <col=ff0000>in container</col> at " + (region.baseX + x) + "," + (region.baseY + y) +"," + z);
                                    }
                                }
                    }
                    player.sendMessage("Finished.");
                });
                return true;
            }

            case "containerobjs": {
                ObjectDef def = ObjectDef.get(Integer.valueOf(args[0]));
                if(def == null)
                    return true;
                for(int i = 0; i < def.showIds.length; i++) {
                    int id = def.showIds[i];
                    ObjectDef obj = ObjectDef.get(id);
                    if(obj == null)
                        continue;
                    System.out.println("[" + i + "]: \"" + obj.name + "\" #" + id + "; options=" + Arrays.toString(obj.options));
                }
                return true;
            }

            case "master": {
                int xp = Stat.xpForLevel(99);
                for(int i = 0; i < (World.isPVP() ? 7 : StatType.values().length); i ++) {
                    Stat stat = player.getStats().get(i);
                    stat.currentLevel = stat.fixedLevel = 99;
                    stat.experience = xp;
                    stat.updated = true;
                }

                player.getCombat().updateLevel();
                player.getAppearance().update();
                return true;
            }

            case "hp": {
                int amount = Integer.parseInt(args[0]);
                player.setHp(amount);
                player.sendMessage("HP set to " + amount + ".");
                return true;
            }

            case "lvl": {
                StatType type = StatType.get(args[0]);
                int id = type == null ? Integer.valueOf(args[0]) : type.ordinal();
                int level = Integer.valueOf(args[1]);
                if(World.isPVP() && (id < 0 || id > 6)) {
                    player.sendMessage("You can only set your combat stats! ID 1->6.");
                    return true;
                }
                if(level < 1 || level > 255 || (id == 3 && level < 10)) {
                    player.sendMessage("Invalid level!");
                    return true;
                }
                Stat stat = player.getStats().get(id);
                stat.currentLevel = level;
                stat.fixedLevel = Math.min(99, level);
                stat.experience = Stat.xpForLevel(Math.min(99, level));
                stat.updated = true;
                if(id == 5)
                    player.getPrayer().deactivateAll();
                player.getCombat().updateLevel();
                //not needed? Item wep = player.getEquipment().get(3);
                //not needed? if(wep != null)
                //not needed?     wep.update();
                return true;
            }

            case "poison": {
                player.poison(6);
                return true;
            }

            case "anim":
            case "emote": {
                int id = Integer.valueOf(args[0]);
                //if(id != -1 && AnimationDefinition.get(id) == null) {
                //    player.sendMessage("Invalid Animation: " + id);
                //    return true;
                //}
                int delay = 0;
                if(args.length > 1)
                    delay = Integer.valueOf(args[1]);
                player.animate(id, delay);
                return true;
            }

            case "animloop": {
                player.startEvent(event -> {
                    int id = 0;
                    if (args.length > 0)
                        id = Integer.parseInt(args[0]);
                    while (id < AnimDef.LOADED.length) {
                        player.animate(id);
                        player.sendMessage("Sending: " + id);
                        id++;
                        event.delay(2);
                        player.resetAnimation();
                        event.delay(1);
                    }
                });
                return true;
            }

            case "sgfx":
            case "gfx":
            case "graphics": {
                int id = Integer.valueOf(args[0]);
                //if(id != -1 && GfxDefinition.get(id) == null) {
                //    player.sendMessage("Invalid Graphics: " + id + ". max valid: " + (GfxDefinition.LOADED.length - 1));
                //    return true;
                //}
                int height = 0;
                if(args.length > 2)
                    height = Integer.valueOf(args[1]);
                int delay = 0;
                if(args.length > 1)
                    delay = Integer.valueOf(args[2]);
                if (command.startsWith("s"))
                    World.sendGraphics(id, height, delay, player.getPosition());
                else
                    player.graphics(id, height, delay);
                return true;
            }

            case "iteminfo": {
                ItemDef def = ItemDef.get(Integer.parseInt(args[0]));
                if (def == null) {
                    player.sendMessage("Invalid id!");
                    return true;
                }
                player.sendMessage("inventory=" +def.inventoryModel);
                player.sendMessage("origcolors=" + Arrays.toString(def.colorFind));
                player.sendMessage("replacecolors=" + Arrays.toString(def.colorReplace));
                player.sendMessage("model=" + def.anInt1504);
                return true;
            }

            case "gfxanim": {
                GfxDef def = GfxDef.get(Integer.valueOf(args[0]));
                if(def == null) {
                    player.sendMessage("Invalid id.");
                    return true;
                }
                player.sendMessage("Gfx " + def.id + " uses animation " + def.animationId);
                return true;
            }

            case "gfxmodel": {
                GfxDef def = GfxDef.get(Integer.valueOf(args[0]));
                if(def == null) {
                    player.sendMessage("Invalid id.");
                    return true;
                }
                player.sendMessage("Gfx " + def.id + " uses model " + def.modelId);
                return true;
            }

            case "findgfxa": {
                int animId = Integer.valueOf(args[0]);
                player.sendMessage("Finding gfx using anim " + animId + "...");
                Arrays.stream(GfxDef.LOADED)
                        .filter(Objects::nonNull)
                        .filter(def -> def.animationId == animId)
                        .forEachOrdered(def -> player.sendMessage("Found: " + def.id));
                return true;
            }

            case "findgfxm": {
                int model = Integer.valueOf(args[0]);
                player.sendMessage("Finding gfx using model " + model + "...");
                Arrays.stream(GfxDef.LOADED)
                        .filter(Objects::nonNull)
                        .filter(def -> def.modelId == model)
                        .forEachOrdered(def -> player.sendMessage("Found: " + def.id));
                return true;
            }

            case "objmodels": {
                ObjectDef obj = ObjectDef.get(Integer.parseInt(args[0]));
                if (obj == null) {
                    player.sendMessage("Invalid id!");
                    return true;
                }
                player.sendMessage(Arrays.toString(obj.modelIds));
                return true;
            }

            case "itemanim": {
                int id = Integer.parseInt(args[0]);
                player.sendMessage("Finding animation that uses item " + id + "...");
                Arrays.stream(AnimDef.LOADED)
                        .filter(Objects::nonNull)
                        .filter(def -> def.rightHandItem - 512 == id)
                        .forEachOrdered(def -> player.sendMessage("Found: " + def.id));
                return true;
            }

            case "animitem": {
                AnimDef anim = AnimDef.get(Integer.parseInt(args[0]));
                if (anim.rightHandItem == -1)
                    player.sendMessage("Animation does not use an item");
                else
                    player.sendMessage("Animation uses item " + (anim.rightHandItem - 512) + ".");
                return true;
            }

            case "ag": {
                int animation = Integer.valueOf(args[0]);
                //if(animation != -1 && AnimationDefinition.get(animation) == null) {
                //    player.sendMessage("Invalid Animation: " + animation);
                //    return true;
                //}
                int gfx = Integer.valueOf(args[1]);
                //if(gfx != -1 && GfxDefinition.get(gfx) == null) {
                //    player.sendMessage("Invalid Graphics: " + gfx);
                //    return true;
                //}
                player.animate(animation);
                player.graphics(gfx, 0, 0);
                return true;
            }

            case "projectile":
            case "printprojectile": {
                Projectile.print(Integer.valueOf(args[0]));
                return true;
            }

            case "picon": {
                player.getAppearance().setPrayerIcon(Integer.valueOf(args[0]));
                return true;
            }

            case "sicon": {
                player.getAppearance().setSkullIcon(Integer.valueOf(args[0]));
                return true;
            }

            case "copyinv": {
                String name = query.substring(query.indexOf(" ") + 1);
                Player p2 = World.getPlayer(name);
                if(p2 == null) {
                    player.sendMessage(name + " could not be found.");
                    return true;
                }
                for(int slot = 0; slot < player.getInventory().getItems().length; slot++) {
                    Item item = p2.getInventory().get(slot);
                    if(item == null)
                        player.getInventory().set(slot, null);
                    else
                        player.getInventory().set(slot, item.copy());
                }
                player.sendMessage("You have copied " + name + "'s inventory.");
                return true;
            }

            case "copyarm": {
                String name = query.substring(query.indexOf(" ") + 1);
                Player p2 = World.getPlayer(name);
                if(p2 == null) {
                    player.sendMessage(name + " could not be found.");
                    return true;
                }
                for(int slot = 0; slot < player.getEquipment().getItems().length; slot++) {
                    Item item = p2.getEquipment().get(slot);
                    if(item == null)
                        player.getEquipment().set(slot, null);
                    else
                        player.getEquipment().set(slot, item.copy());
                }
                player.getAppearance().update();
                player.sendMessage("You have copied " + name + "'s armor.");
                return true;
            }

            case "copystats": {
                String name = query.substring(query.indexOf(" ") + 1);
                Player p2 = World.getPlayer(name);
                if(p2 == null) {
                    player.sendMessage(name + " could not be found.");
                    return true;
                }
                for(int statId = 0; statId < StatType.values().length; statId++) {
                    Stat stat = player.getStats().get(statId);
                    Stat stat2 = p2.getStats().get(statId);
                    stat.currentLevel = stat2.currentLevel;
                    stat.fixedLevel = stat2.fixedLevel;
                    stat.experience = stat2.experience;
                    stat.updated = true;
                }
                player.getCombat().updateLevel();
                player.getAppearance().update();
                player.sendMessage("You have copied " + name + "'s stats.");
                return true;
            }

            case "copybank": {
                String name = query.substring(query.indexOf(" ") + 1);
                Player p2 = World.getPlayer(name);
                if(p2 == null) {
                    player.sendMessage(name + " could not be found.");
                    return true;
                }
                for(int slot = 0; slot < player.getBank().getItems().length; slot++) {
                    BankItem item = p2.getBank().getItems()[slot];
                    if(item == null)
                        player.getBank().set(slot, null);
                    else
                        player.getBank().set(slot, item.copy());
                }
                player.sendMessage("You have copied " + name + "'s bank.");
                return true;
            }

            case "resetcamera": {
                player.getPacketSender().resetCamera();
                return true;
            }

            case "movecamera": {
                player.getPacketSender().moveCameraToLocation(3071, 3515, 400, 0, 15);
                player.getPacketSender().turnCameraToLocation(3068, 3517, 0, 0, 25);
                return true;
            }

            case "movecamera2": {
                player.getPacketSender().moveCameraToLocation(3080, 3499, 800, 0, 15);
                player.getPacketSender().turnCameraToLocation(3084, 3504, 0, 0, 25);
                return true;
            }

            case "rotatecamera": {
                player.getPacketSender().turnCameraToLocation(3079, 3487, 30, 0, 30);
                return true;
            }

            case "doubledrops": {
                World.toggleDoubleDrops();
                return true;
            }

            case "droprate": {
                World.toggleDropRateBonus();
                return true;
            }

            case "doublepc": {
                World.toggleDoublePestControl();
                return true;
            }

            case "bmb":
            case "bmboost": {
                int multiplier = Integer.valueOf(args[0]);
                if(multiplier < 1) {
                    player.sendMessage("Blood money multiplier cannot be less than 1.");
                    multiplier = 1;
                } else if(multiplier > 4) {
                    player.sendMessage("Blood money multiplier cannot be greater than 4.");
                    multiplier = 4;
                }
                World.boostBM(multiplier);
                return true;
            }

            case "getbmboost": {
                player.sendMessage("The bloody money multiplier is currently: " + World.bmMultiplier);
                return true;
            }

            case "xpb":
            case "xpboost": {
                int multiplier = Integer.valueOf(args[0]);
                if(multiplier < 1) {
                    player.sendMessage("Experience multiplier cannot be less than 1.");
                    multiplier = 1;
                } else if(multiplier > 4) {
                    player.sendMessage("Experience multiplier cannot be greater than 4.");
                    multiplier = 4;
                }
                World.boostXp(multiplier);
                return true;
            }

            case "doublexpweekend": {
                World.toggleWeekendExpBoost();
                return true;
            }

            case "togglewildernesskeyevent": {
                World.toggleWildernessKeyEvent();
                boolean active = World.wildernessKeyEvent;
                player.sendMessage("The wilderness key event is now " + (active ? "enabled" : "disabled") + ".");
                return true;
            }

            case "loginset": {
                forName(player, query, "::loginset live", s -> login_set.setActive(player, s));
                return true;
            }

            case "reloadteles":
            case "reloadteleports": {
                DataFile.reload(player, teleports.class);
                return true;
            }

            case "reloadhelp": {
                DataFile.reload(player, help.class);
                return true;
            }

            case "unlock": {
                forPlayer(player, query, "::unlock playerName", p2 -> {
                    if(!p2.isLocked()) {
                        player.sendMessage(p2.getName() + " is not locked.");
                    } else {
                        p2.unlock();
                        player.sendMessage("Unlocked " + p2.getName() + ".");
                    }
                });
                return true;
            }

            case "smute": {
                forPlayerTime(player, query, "::smute playerName #d/#h/perm", (p2, time) -> Punishment.mute(player, p2, time, true));
                return true;
            }

            case "resetbankpin": {
                forPlayer(player, query, "::resetbankpin playerName", p2 -> {
                    p2.getBankPin().setPin(-1);
                    player.sendMessage("Reset bankpin for " + p2.getName() + ".");
                });
                return true;
            }
        }
        return false;
    }

    /**
     * Utils
     */

    private static void forName(Player player, String cmdQuery, String exampleUsage, Consumer<String> consumer) {
        try {
            String name = cmdQuery.substring(cmdQuery.indexOf(" ") + 1).trim();
            consumer.accept(name);
        } catch(Exception e) {
            player.sendMessage("Invalid command usage. Example: [" + exampleUsage + "]");
        }
    }

    private static void forNameString(Player player, String cmdQuery, String exampleUsage, BiConsumer<String, String> consumer) {
        try {
            String s = cmdQuery.substring(cmdQuery.indexOf(" ") + 1).trim();
            int i = s.lastIndexOf(" ");
            String name = s.substring(0, i).trim();
            String string = s.substring(i).trim();
            consumer.accept(name, string);
        } catch(Exception e) {
            player.sendMessage("Invalid command usage. Example: [" + exampleUsage + "]");
        }
    }

    private static void forNameTime(Player player, String cmdQuery, String exampleUsage, BiConsumer<String, Long> consumer) {
        forNameString(player, cmdQuery, exampleUsage, (name, string) -> {
            try {
                if(string.equalsIgnoreCase("perm")) {
                    consumer.accept(name, -1L);
                    return;
                }
                long time = Long.valueOf(string.substring(0, string.length() - 1));
                String unit = string.substring(string.length() - 1).toLowerCase();
                if(unit.equals("h"))
                    time = TimeUtils.getHoursToMillis(time);
                else if(unit.equals("d"))
                    time = TimeUtils.getDaysToMillis(time);
                else
                    throw new IOException("Invalid time unit: " + unit);
                consumer.accept(name, System.currentTimeMillis() + time);
            } catch(Exception e) {
                e.printStackTrace();
                player.sendMessage("Invalid command usage. Example: [" + exampleUsage + "]");
            }
        });
    }

    private static void forPlayer(Player player, String cmdQuery, String exampleUsage, Consumer<Player> consumer) {
        forName(player, cmdQuery, exampleUsage, name -> {
            try {
                Player p = getOnlinePlayer(player, name);
                if(p != null)
                    consumer.accept(p);
            } catch(Exception e) {
                player.sendMessage("Invalid command usage. Example: [" + exampleUsage + "]");
            }
        });
    }

    private static void forPlayerString(Player player, String cmdQuery, String exampleUsage, BiConsumer<Player, String> consumer) {
        forNameString(player, cmdQuery, exampleUsage, (name, string) -> {
            try {
                Player p = getOnlinePlayer(player, name);
                if(p != null)
                    consumer.accept(p, string);
            } catch(Exception e) {
                player.sendMessage("Invalid command usage. Example: [" + exampleUsage + "]");
            }
        });
    }

    private static void forPlayerInt(Player player, String cmdQuery, String exampleUsage, BiConsumer<Player, Integer> consumer) {
        forNameString(player, cmdQuery, exampleUsage, (name, string) -> {
            try {
                Player p = getOnlinePlayer(player, name);
                if(p != null)
                    consumer.accept(p, Integer.valueOf(string));
            } catch(Exception e) {
                player.sendMessage("Invalid command usage. Example: [" + exampleUsage + "]");
            }
        });
    }

    private static void forPlayerTime(Player player, String cmdQuery, String exampleUsage, BiConsumer<Player, Long> consumer) {
        forNameTime(player, cmdQuery, exampleUsage, (name, time) -> {
            try {
                Player p = getOnlinePlayer(player, name);
                if(p != null)
                    consumer.accept(p, time);
            } catch(Exception e) {
                player.sendMessage("Invalid command usage. Example: [" + exampleUsage + "]");
            }
        });
    }

    private static Player getOnlinePlayer(Player player, String name) {
        Player p = World.getPlayer(name);
        if(p == null)
            player.sendMessage("User '" + name + "' is not online.");
        return p;
    }

    private static void teleportDangerous(Player player, int x, int y, int z) {
        if (player.wildernessLevel != 0 || player.pvpAttackZone) {
            player.sendMessage("You can't use this command from where you are standing.");
            return;
        }
        player.dialogue(
                new MessageDialogue("<col=ff0000>Warning:</col> This teleport is inside the wilderness.<br> Are you sure you want to do this?").lineHeight(24),
                new OptionsDialogue(
                        new Option("Yes", () -> teleport(player, x, y, z)),
                        new Option("No")
                )
        );
    }

    private static void teleport(Player player, int x, int y, int z) {
        if (player.wildernessLevel != 0 || player.pvpAttackZone) {
            player.sendMessage("You can't use this command from where you are standing.");
            return;
        }
        player.getMovement().startTeleport(event -> {
            player.animate(3864);
            player.graphics(1039);
            player.privateSound(200, 0, 10);
            event.delay(2);
            player.getMovement().teleport(x, y, z);
        });
    }

    private static void sendItems(Player player, ItemContainer container, int scriptId) {
        Object[] ids = new Object[container.getItems().length];
        StringBuilder sb = new StringBuilder(ids.length);
        for(int i = 0; i < ids.length; i++) {
            ids[i] = container.getId(i);
            sb.append("i");
        }
        player.getPacketSender().sendClientScript(scriptId, sb.toString(), ids);
    }
}