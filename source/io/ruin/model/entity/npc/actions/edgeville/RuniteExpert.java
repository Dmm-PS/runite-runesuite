package io.ruin.model.entity.npc.actions.edgeville;

import io.ruin.api.utils.XenPost;
import io.ruin.cache.Color;
import io.ruin.cache.NPCDef;
import io.ruin.data.impl.help;
import io.ruin.model.World;
import io.ruin.model.entity.npc.NPC;
import io.ruin.model.entity.npc.NPCAction;
import io.ruin.model.entity.player.GameMode;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.player.PlayerGroup;
import io.ruin.model.entity.shared.LockType;
import io.ruin.model.entity.shared.listeners.LoginListener;
import io.ruin.model.entity.shared.listeners.SpawnListener;
import io.ruin.model.inter.Interface;
import io.ruin.model.inter.InterfaceType;
import io.ruin.model.inter.dialogue.*;
import io.ruin.model.inter.journal.Journal;
import io.ruin.model.inter.utils.Config;
import io.ruin.model.inter.utils.Option;
import io.ruin.model.map.Direction;
import io.ruin.utility.Broadcast;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class RuniteExpert {

    private static final NPC RUNITE_EXPERT = SpawnListener.first(306);

    private static final NPC WIZARD = null;

    static {
        //TODO: Good idea but a bit annoying because of raidus.. perhaps reduce it a bit? Also, I think there's an exploit with claiming coins.
        // TODO: for some odd reason, it prompts you to claim coins more than once for one achievement. (not sure what triggers this but it 100% happens)
   /*     SpawnListener.register(306, npc -> {
            npc.getPosition().area(8, pos -> ProjectileRoute.allow(npc, pos)).forEach(pos -> {
                pos.getTile().addPlayerTrigger(p -> {
                    if (p.guideCoins <= 0) {
                        return;
                    }
                    long lastNotify = p.get("GUIDE_COINS_ATTEMPT", 0L);
                    if (System.currentTimeMillis() - lastNotify >= 1000 * 60 * 10) {
                        p.addEvent(event -> {
                            p.lock();
                            p.set("GUIDE_COINS_ATTEMPT", System.currentTimeMillis());
                            p.faceTemp(npc);
                            p.getPacketSender().sendHintIcon(npc);
                            p.dialogue(new NPCDialogue(npc, "Hey there, " + p.getName() + "! I have some money for you!<br>Come talk to me."));
                            event.delay(2);
                            p.unlock();
                            event.delay(20);
                            p.getPacketSender().resetHintIcon(true);
                        });
                    }
                });
            });
        });*/
        NPCDef.get(307).ignoreOccupiedTiles = true;
        NPCAction.register(RUNITE_EXPERT, "view-help", (player, npc) -> help.open(player));
        NPCAction.register(RUNITE_EXPERT, "view-guide", (player, npc) -> {
            player.dialogue(
                    new OptionsDialogue("Watch the guide?",
                            new Option("Yes", () -> tutorial(player, npc, false)),
                            new Option("No", player::closeDialogue))
            );
        });
        NPCAction.register(RUNITE_EXPERT, "talk-to", RuniteExpert::optionsDialogue);


        LoginListener.register(player -> {

                if (player.newPlayer) {
                    ecoTutorial(player);
                } else {
                    player.dialogue(new ItemDialogue().one(9711, "If you have a question or are stuck on something, try visiting Runite's ::forums or ::discord. " +
                            "There's plenty of information for the new and experienced player alike.").lineHeight(17));
                    //player.getPacketSender().sendMessage("Latest Update: " + LatestUpdate.LATEST_UPDATE_TITLE + "|" + LatestUpdate.LATEST_UPDATE_URL, "", 14);
                }
        });
    }

    private static void optionsDialogue(Player player, NPC npc) {
        if (World.isPVP()) {
            player.dialogue(new NPCDialogue(npc, "Hello " + player.getName() + ", is there something I could assist you with?"),
                    new OptionsDialogue(
                            new Option("View help pages", () -> help.open(player)),
                            new Option("Replay tutorial", () -> ecoTutorial(player))
                    ));
        } else {
            player.dialogue(new NPCDialogue(npc, "Hello " + player.getName() + ", is there something I could assist you with?"),
                    new OptionsDialogue(
                            new Option("Replay tutorial", () -> ecoTutorial(player))
                    ));
        }
    }


    private static void ecoTutorial(Player player) {
        boolean actuallyNew = player.newPlayer;
        player.inTutorial = true;
        player.startEvent(event -> {
            player.lock(LockType.FULL_ALLOW_LOGOUT);
            player.getMovement().teleport(3096, 3486, 0);
            player.face(Direction.WEST);
            if (actuallyNew) {
                player.openInterface(InterfaceType.MAIN, Interface.APPEARANCE_CUSTOMIZATION);
                while (player.isVisibleInterface(Interface.APPEARANCE_CUSTOMIZATION)) {
                    event.delay(1);
                }
            }

            NPC guide = RUNITE_EXPERT;

            if (actuallyNew) {
                guide.forceText("Hello " + player.getName() + ", and welcome to Runite!");
            }

            player.getPacketSender().sendHintIcon(guide);
            player.face(guide);
            player.dialogue(new NPCDialogue(guide, "Greetings, " + player.getName() + "! Welcome to " + World.type.getWorldName() + "."));
            if (actuallyNew) {
                player.dialogue(
                        new NPCDialogue(guide, "Before I let you go, I need to ask you a question."),
                        new NPCDialogue(guide, "Do you want to see the options for Iron Man modes?"),
                        new OptionsDialogue("View Iron Man options?",
                                new Option("Yes", () -> {
                                    GameMode.openSelection(player);
                                    player.unsafeDialogue(new MessageDialogue("Close the interface once you're happy with your selection.<br><br><col=ff0000>WARNING:</col> This is the ONLY chance to choose your Iron Man mode.").hideContinue());
                                }),
                                new Option("No", player::closeDialogue)));
                event.waitForDialogue(player);

                String text = "You want to be a part of the economy, then? Great!";
                if (player.getGameMode() == GameMode.IRONMAN) {
                    text = "Iron Man, huh? Self-sufficiency is quite a challenge, good luck!";
                } else if (player.getGameMode() == GameMode.HARDCORE_IRONMAN) {
                    text = "Hardcore?! You only live once... make it count!";
                } else if (player.getGameMode() == GameMode.ULTIMATE_IRONMAN) {
                    text = "Ultimate Iron Man... Up for quite the challenge, aren't you?";
                }

                if (player.getGameMode().isIronMan()) {
                    player.dialogue(new NPCDialogue(guide, text),
                            new NPCDialogue(guide, "I'll give you a few items to help get you started..."),
                            new NPCDialogue(guide, "There you go, some basic stuff. If you need anything else, remember to check Sigmund's shop.") {
                                @Override
                                public void open(Player player) {
                                    giveEcoStarter(player);
                                    player.newPlayer = false;
                                    super.open(player);
                                }
                            });
                    Broadcast.WORLD.sendNews(player.getName() + " has just joined " + World.type.getWorldName() + "!");
                } else {
                    player.dialogue(
                            new NPCDialogue(guide, "Not interested in any ironman modes? No problem!<br>Select which game mode you would like to play as."),
                            new OptionsDialogue("Select a game mode",
                                    new Option("Regular Mode <col=880088>(150x combat, 50x skills)", () -> {
                                        player.dialogue(
                                                new NPCDialogue(guide, "Excellent decision! Here are a few items to get you started." +
                                                        "If you need anything else, remember to check out Sigmund's shop.") {
                                                    @Override
                                                    public void open(Player player) {
                                                        giveEcoStarter(player);
                                                        player.newPlayer = false;
                                                        super.open(player);
                                                    }
                                                }
                                        );
                                        Broadcast.WORLD.sendNews(player.getName() + " has just joined " + World.type.getWorldName() + "!");
                                    }),
                                    new Option("Classic Mode <col=880088>(5x combat, 5x skills)", () -> {
                                        player.dialogue(
                                                new NPCDialogue(guide, "I see that you're a fan of the grind! Excellent.<br>Here are a few items to get you started. " +
                                                        "If you need anything else, remember to check out Sigmund's shop.") {
                                                    @Override
                                                    public void open(Player player) {
                                                        giveEcoStarter(player);
                                                        PlayerGroup.CLASSIC_MODE.sync(player, "mode");
                                                        player.combatXpRate = 5;
                                                        player.join(PlayerGroup.CLASSIC_MODE);
                                                        player.newPlayer = false;
                                                        super.open(player);
                                                    }
                                                }
                                        );
                                        Broadcast.WORLD.sendNews(player.getName() + " has just joined " + World.type.getWorldName() + "!");
                                    }))
                    );
                }
            }
            event.waitForDialogue(player);
            player.dialogue(
                    new NPCDialogue(guide, "I have one last piece of advice for you."),
                    new NPCDialogue(guide, "To learn more about " + World.type.getWorldName() + ", have a look at the Introductory achievements. They are basic tasks that will reward you with some gold and teach you more about this world.") {
                        @Override
                        public void open(Player player) {
                            super.open(player);
                            player.getPacketSender().sendClientScript(915, "i", 2);
                            // Journal.ACHIEVEMENTS.send(player);
                        }
                    },
                    new NPCDialogue(guide, "You can always find me here if you have more questions."));

            player.sendMessage("If you have any questions please join the clan chat 'Runite'.");
            event.waitForDialogue(player);
            guide.animate(863);
            player.inTutorial = false;
            player.unlock();
            player.getPacketSender().resetHintIcon(true);
        });
    }

    public static void changeForumsGroup(Player player, int mode) {
        CompletableFuture.runAsync(() -> {
            Map<Object, Object> map = new HashMap<>();
            map.put("userId", player.getUserId());
            map.put("type", "donator");
            map.put("groupId", mode);
            XenPost.post("add_group", map);
        });
    }

    private static void pvpTutorial(Player player) {
        boolean actuallyNew = player.newPlayer;
        player.inTutorial = true;
        player.startEvent(event -> {
            player.lock(LockType.FULL_ALLOW_LOGOUT);
            player.getMovement().teleport(3087, 3502, 0);
            if (actuallyNew) {
                player.openInterface(InterfaceType.MAIN, Interface.APPEARANCE_CUSTOMIZATION);
                while (player.isVisibleInterface(Interface.APPEARANCE_CUSTOMIZATION)) {
                    event.delay(1);
                }
            }
            NPC guide = new NPC(307).spawn(3087, 3503, 0, Direction.SOUTH, 0);
            player.getPacketSender().sendHintIcon(guide);
            player.face(guide);
            player.dialogue(new NPCDialogue(guide, "Greetings, " + player.getName() + "! Welcome to " + World.type.getWorldName() + "."),
                    new NPCDialogue(guide, "Before you begin playing, please take a second to learn a bit about the server."));
            event.waitForDialogue(player);
            if (actuallyNew) {
                player.dialogue(new OptionsDialogue("What keybinds would you like to use?",
                        new Option("Use OSRS (2007) Keybinds", () -> {
                            for (Config c : Config.KEYBINDS)
                                c.reset(player);
                            Config.ESCAPE_CLOSES.reset(player);
                            setDrag(player);
                        }),
                        new Option("Use Pre-EoC (2011) Keybinds", () -> {
                            for (int i = 0; i < Config.KEYBINDS.length; i++) {
                                Config c = Config.KEYBINDS[i];
                                if (i == 0)
                                    c.set(player, 5);
                                else if (i >= 3 && i <= 6)
                                    c.set(player, i - 2);
                                else
                                    c.set(player, 0);
                            }
                            Config.ESCAPE_CLOSES.reset(player);
                            setDrag(player);
                        })
                ));
                event.waitForDialogue(player);

                player.dialogue(
                        new NPCDialogue(guide, "We offer a variety of toggles which may drastically change your experience at " + World.type.getWorldName() + ". They can be found by selecting the quest tab, and clicking the "
                                + Color.PURPLE.wrap("purple") + " button.") {
                            @Override
                            public void open(Player player) {
                                player.getPacketSender().sendClientScript(915, "i", 2);
                                Journal.TOGGLES.send(player);
                                super.open(player);
                            }
                        });
                event.waitForDialogue(player);

                player.dialogue(
                        new NPCDialogue(guide, "You can also select and setup custom presets by clicking the " + Color.DARK_RED.wrap("red") + " button.") {
                            @Override
                            public void open(Player player) {
                                Journal.PRESETS.send(player);
                                player.getPacketSender().sendClientScript(915, "i", 2);
                                super.open(player);
                            }
                        });
                event.waitForDialogue(player);

                player.dialogue(
                        new NPCDialogue(guide, "And finally, the " + Color.GOLD.wrap("yellow") + " tab to search monster drop tables.") {
                            @Override
                            public void open(Player player) {
                                Journal.BESTIARY.send(player);
                                player.getPacketSender().sendClientScript(915, "i", 2);
                                super.open(player);
                            }
                        });
                event.waitForDialogue(player);

                player.dialogue(
                        new NPCDialogue(guide, "Your bank has all the essentials you'll need to dive into the wilderness.") {
                            @Override
                            public void open(Player player) {
                                addItemToBank(player);
                                super.open(player);
                            }
                        });
            }
            event.waitForDialogue(player);
            player.dialogue(new NPCDialogue(guide, "You can find me near the bank if you have more questions. Good luck and thank you for playing " + World.type.getWorldName() + "!"));
            event.waitForDialogue(player);
            if(actuallyNew) {
                Broadcast.WORLD.sendNews(player.getName() + " has just joined " + World.type.getWorldName() + "!");
            }
            guide.animate(863);
            player.newPlayer = false;
            player.inTutorial = false;
            player.unlock();
            guide.addEvent(e -> {
                e.delay(2);
                World.sendGraphics(86, 50, 0, guide.getPosition());
                guide.remove();
            });
        });
    }


    private static void giveEcoStarter(Player player) {
        player.getInventory().add(995, 5000); // gp
        player.getInventory().add(1349, 1); // iron axe
        player.getInventory().add(1267, 1); // iron pickaxe
        player.getInventory().add(1323, 1); // iron scim
        player.getInventory().add(1381, 1); // air staff
        player.getInventory().add(558, 100); // mind rune
        player.getInventory().add(841, 1); // shortbow
        player.getInventory().add(884, 100); // iron arrow
        player.getInventory().add(334, 50); // trout
        switch (player.getGameMode()) {
            case IRONMAN:
                player.getInventory().add(12810, 1);
                player.getInventory().add(12811, 1);
                player.getInventory().add(12812, 1);
                break;
            case ULTIMATE_IRONMAN:
                player.getInventory().add(12813, 1);
                player.getInventory().add(12814, 1);
                player.getInventory().add(12815, 1);
                break;
            case HARDCORE_IRONMAN:
                player.getInventory().add(20792, 1);
                player.getInventory().add(20794, 1);
                player.getInventory().add(20796, 1);
                break;
            case STANDARD:
                player.getInventory().add(995, 15000);
                break;
        }

    }


    private static NPC find(Player player, int id) {
        for (NPC n : player.localNpcs()) {
            if (n.getId() == id)
                return n;
        }
        throw new IllegalArgumentException();
    }

    private static void starterDialogue(Player player) { //todo redo lol
        player.lock(LockType.FULL_ALLOW_LOGOUT);
        player.dialogue(
                new OptionsDialogue("What keybinds would you like to use?",
                        new Option("Use OSRS (2007) Keybinds", () -> {
                            for (Config c : Config.KEYBINDS)
                                c.reset(player);
                            Config.ESCAPE_CLOSES.reset(player);
                            setDrag(player);
                        }),
                        new Option("Use Pre-EoC (2011) Keybinds", () -> {
                            for (int i = 0; i < Config.KEYBINDS.length; i++) {
                                Config c = Config.KEYBINDS[i];
                                if (i == 0)
                                    c.set(player, 5);
                                else if (i >= 3 && i <= 6)
                                    c.set(player, i - 2);
                                else
                                    c.set(player, 0);
                            }
                            Config.ESCAPE_CLOSES.reset(player);
                            setDrag(player);
                        })
                )
        );
    }

    private static void setDrag(Player player) {
        player.dialogue(
                new OptionsDialogue("What drag setting would you like to use?",
                        new Option("5 (OSRS) (2007) Drag", () -> setDrag(player, 5)),
                        new Option("10 (Pre-EoC) (2011) Drag", () -> setDrag(player, 10))
                )
        );
    }

    private static void setDrag(Player player, int drag) {
        player.dragSetting = drag;
    }

    private static void tutorial(Player player, NPC npc, boolean intro) {
        if (World.isEco()) {
            ecoTutorial(player);
            return;
        }
        if (true) {
            player.dialogue(
                    new NPCDialogue(npc, "Hi " + player.getName() + " and welcome to <col=0040ff>" + World.type.getWorldName() + "</col>! I'm going to give you a quick rundown of the server.").lineHeight(24),
                    new NPCDialogue(npc, "The purpose of this world is to fight, fight and fight some more. The best way to obtain <col=6f0000>Blood money</col>, which is used to purchase items from the shops, is through the wilderness."),
                    new NPCDialogue(npc, "You can also obtain <col=6f0000>Blood money</col> from killing bosses, but I suggest sticking to the wilderness... safe monsters like Zulrah and Kraken, are just not as profitable!"),
                    new NPCDialogue(npc, "Good luck!")
            );
            return;
        }
        //todo todo todo todo todo
        if (intro) {
            player.lock();
            player.getMovement().teleport(3095, 3512, 0);
            player.getPacketSender().sendHintIcon(RUNITE_EXPERT);
            player.face(RUNITE_EXPERT);
            player.dialogue(new MessageDialogue("Welcome to " + World.type.getWorldName() + " <br><br> Please talk-to the <col=0040ff>" + World.type.getWorldName() + " Expert</col> to begin the tutorial.").hideContinue());
        } else {
            player.dialogue(
                    new NPCDialogue(npc, "Hi " + player.getName() + " and welcome to <col=0040ff>" + World.type.getWorldName() + "</col>! I'm going to give you a quick rundown of the server.").lineHeight(24),
                    new NPCDialogue(npc, "The purpose of this world is to fight, fight and fight some more. The best way to obtain <col=6f0000>Blood money</col>, which is used to purchase items from the shops, is through the wilderness."),
                    new NPCDialogue(npc, "You can also obtain <col=6f0000>Blood money</col> from killing bosses, but I suggest sticking to the wilderness... safe monsters like Zulrah and Kraken, are just not as profitable!"),
                    new ActionDialogue(() -> {
                        player.getMovement().teleport(3080, 3510, 0);
                        player.dialogue(
                                new NPCDialogue(npc, "This is the where all of the shops are located. Here, you can spend your blood money to upgrade your gear and supplies."),
                                new ActionDialogue(() -> {
                                    player.getPacketSender().sendHintIcon(3084, 3551);
                                    player.dialogue(
                                            new NPCDialogue(npc, "This is the Loyalty chest. Here, you can collect a reward every 24 hours for simply playing! We suggest you do this after the tutorial."),
                                            new ActionDialogue(() -> {
                                                player.getMovement().teleport(3086, 3501);
                                                player.getPacketSender().sendHintIcon(WIZARD);
                                                player.dialogue(
                                                        new NPCDialogue(npc, "The Wizard offers a variety of teleports including paid & free boss teleports, wilderness teleports, PVP instance teleports and more."),
                                                        new ActionDialogue(() -> {
                                                            player.getPacketSender().resetHintIcon(false);
                                                            player.getMovement().teleport(3095, 3512);
                                                            player.dialogue(
                                                                    new NPCDialogue(npc, "The last thing I want to talk to you about is some of the Player Journal. You can get there by selecting the quest tab."),
                                                                    new NPCDialogue(npc, "Your player journal offers a variety of information. The default tab, blue, contains general information about your character and the server."),
                                                                    new NPCDialogue(npc, "The red button contains presets which can be selected to change your inventory, spell book, stats, prayer and equipment."),
                                                                    new NPCDialogue(npc, "The purple button contains a ton of game toggles. Here you can change your drag, prayer book toggles, overlays, and more."),
                                                                    new NPCDialogue(npc, "If you have any other questions please feel free to ask around or visit our forums."),
                                                                    new NPCDialogue(npc, "Enjoy your stay at " + World.type.getWorldName() + " and most importantly.. have fun!"),
                                                                    new ActionDialogue(() -> {
                                                                        if (player.newPlayer) {
                                                                            addItemToBank(player);
                                                                            Journal.PRESETS.send(player);
                                                                        }
                                                                        player.newPlayer = false;
                                                                        player.unlock();
                                                                    }));
                                                        })
                                                );
                                            })
                                    );
                                })
                        );
                    })
            );
        }
    }

    public static void addItemToBank(Player player) {
        player.getBank().add(19625, 50000); // Home teleport
        player.getBank().add(385, 50000); // Sharks
        player.getBank().add(3144, 50000); // Karambwans
        player.getBank().add(560, 50000); // Death runes
        player.getBank().add(565, 50000); // Blood runes
        player.getBank().add(555, 50000); // Water runes
        player.getBank().add(562, 50000); // Chaos runes
        player.getBank().add(557, 50000); // Earth runes
        player.getBank().add(11696, 50000); // Cosmic runes
        player.getBank().add(554, 50000); // Fire runes
        player.getBank().add(9075, 50000); // Astral runes
        player.getBank().add(556, 50000); // Air runes
        player.getBank().add(563, 50000); // Law runes
        player.getBank().add(559, 50000); // Body runes
        player.getBank().add(566, 50000); // Soul runes
        player.getBank().add(561, 50000); // Nature runes
        player.getBank().add(2436, 50000); // pots
        player.getBank().add(2440, 50000); // pots
        player.getBank().add(2442, 50000); // pots
        player.getBank().add(2444, 50000); // pots
        player.getBank().add(3040, 50000); // pots
        player.getBank().add(10925, 50000); // pots
        player.getBank().add(3024, 50000); // pots
        player.getBank().add(6685, 50000); // pots
        player.getBank().add(145, 50000); // pots
        player.getBank().add(157, 50000); // pots
        player.getBank().add(163, 50000); // pots
        player.getBank().add(169, 50000); // pots
        player.getBank().add(3042, 50000); // pots
        player.getBank().add(10927, 50000); // pots
        player.getBank().add(3026, 50000); // pots
        player.getBank().add(6689, 50000); // pots
        player.getBank().add(147, 50000); // pots
        player.getBank().add(159, 50000); // pots
        player.getBank().add(165, 50000); // pots
        player.getBank().add(171, 50000); // pots
        player.getBank().add(3044, 50000); // pots
        player.getBank().add(10929, 50000); // pots
        player.getBank().add(3028, 50000); // pots
        player.getBank().add(6687, 50000); // pots
        player.getBank().add(149, 50000); // pots
        player.getBank().add(161, 50000); // pots
        player.getBank().add(167, 50000); // pots
        player.getBank().add(173, 50000); // pots
        player.getBank().add(3046, 50000); // pots
        player.getBank().add(10931, 50000); // pots
        player.getBank().add(3030, 50000); // pots
        player.getBank().add(6691, 50000); // pots
        player.getBank().add(9241, 20000); // bolts
        player.getBank().add(9244, 20000); // bolts
        player.getBank().add(9245, 20000); // bolts
        player.getBank().add(9243, 20000); // bolts
        player.getBank().add(9242, 20000); // bolts
        player.getBank().add(892, 20000); // rune arrows
        player.getBank().add(868, 20000); // rune knife
        player.getBank().add(811, 20000); // rune dart
        player.getBank().add(4587, 20000); // Scim
        player.getBank().add(1215, 20000); // Dagger
        player.getBank().add(4089, 20000); // Mystic
        player.getBank().add(4109, 20000); // Mystic
        player.getBank().add(4099, 20000); // Mystic
        player.getBank().add(10828, 20000); // neit helm
        player.getBank().add(3755, 20000); // farseer helm
        player.getBank().add(1163, 20000); // rune full helm
        player.getBank().add(1305, 20000); // d long
        player.getBank().add(4675, 20000); // ancient staff
        player.getBank().add(4091, 20000); // Mystic
        player.getBank().add(4111, 20000); // Mystic
        player.getBank().add(4101, 20000); // Mystic
        player.getBank().add(2414, 20000); // zamy god cape
        player.getBank().add(3751, 20000); // hat
        player.getBank().add(1127, 20000); // rune
        player.getBank().add(1434, 20000); // mace
        player.getBank().add(9185, 20000); // crossbow
        player.getBank().add(4093, 20000); // Mystic
        player.getBank().add(4113, 20000); // Mystic
        player.getBank().add(4103, 20000); // Mystic
        player.getBank().add(11978, 20000); // glory (6)
        player.getBank().add(3753, 20000); // helm
        player.getBank().add(1079, 20000); // rune
        player.getBank().add(5698, 20000); // dagger
        player.getBank().add(10499, 20000); // avas
        player.getBank().add(4097, 20000); // Mystic
        player.getBank().add(4117, 20000); // Mystic
        player.getBank().add(4107, 20000); // Mystic
        player.getBank().add(2579, 20000); // wiz boots
        player.getBank().add(3749, 20000); // helm
        player.getBank().add(4131, 20000); // rune boots
        player.getBank().add(2503, 20000); // hides
        player.getBank().add(2497, 20000); // hides
        player.getBank().add(3105, 20000); // climbers
        player.getBank().add(7458, 20000); // mithril gloves for pures
        player.getBank().add(7462, 20000); // gloves
        player.getBank().add(3842, 20000); // god book
        player.getBank().add(1093, 20000); // rune
        player.getBank().add(1201, 20000); // rune
    }

    public static void addPKModeItemToBank(Player player) {
        player.getBank().add(19625, 50000); // Home teleport
        player.getBank().add(385, 50000); // Sharks
        player.getBank().add(3144, 50000); // Karambwans
        player.getBank().add(2436, 50000); // pots
        player.getBank().add(2440, 50000); // pots
        player.getBank().add(2444, 50000); // pots
        player.getBank().add(3024, 50000); // pots
        player.getBank().add(6685, 50000); // pots
        player.getBank().add(560, 50000); // Death runes
        player.getBank().add(565, 50000); // Blood runes
        player.getBank().add(561, 50000); // Nature runes

        player.getBank().add(145, 50000); // pots
        player.getBank().add(157, 50000); // pots
        player.getBank().add(169, 50000); // pots
        player.getBank().add(3026, 50000); // pots
        player.getBank().add(6689, 50000); // pots
        player.getBank().add(9075, 50000); // Astral runes
        player.getBank().add(555, 50000); // Water runes
        player.getBank().add(557, 50000); // Earth runes

        player.getBank().add(147, 50000); // pots
        player.getBank().add(159, 50000); // pots
        player.getBank().add(171, 50000); // pots
        player.getBank().add(3028, 50000); // pots
        player.getBank().add(6687, 50000); // pots
        player.getBank().add(7458, 20000); // mithril gloves for pures
        player.getBank().add(7462, 20000); // gloves
        player.getBank().add(3842, 20000); // god book

        player.getBank().add(149, 50000); // pots
        player.getBank().add(161, 50000); // pots
        player.getBank().add(173, 50000); // pots
        player.getBank().add(3030, 50000); // pots
        player.getBank().add(6691, 50000); // pots


        player.getBank().add(9144, 20000); // bolts
        player.getBank().add(2503, 20000); // hides
        player.getBank().add(4099, 20000); // Mystic
        player.getBank().add(2414, 20000); // zamy god cape
        player.getBank().add(10828, 20000); // neit helm
        player.getBank().add(1215, 20000); // Dagger
        player.getBank().add(1163, 20000); // rune full helm
        player.getBank().add(11978, 20000); // glory (6)

        player.getBank().add(892, 20000); // rune arrows
        player.getBank().add(2497, 20000); // hides
        player.getBank().add(4101, 20000); // Mystic
        player.getBank().add(4675, 20000); // ancient staff
        player.getBank().add(3751, 20000); // hat
        player.getBank().add(5698, 20000); // dagger
        player.getBank().add(1127, 20000); // rune
        player.getBank().add(1079, 20000); // rune

        player.getBank().add(9185, 20000); // crossbow
        player.getBank().add(10499, 20000); // avas
        player.getBank().add(4103, 20000); // Mystic
        player.getBank().add(4107, 20000); // Mystic
        player.getBank().add(3105, 20000); // climbers
        player.getBank().add(4587, 20000); // Scim
        player.getBank().add(1093, 20000); // rune
        player.getBank().add(1201, 20000); // rune
    }

}
