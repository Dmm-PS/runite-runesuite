package io.ruin.model.inter.handlers;

import io.ruin.model.inter.Interface;
import io.ruin.model.inter.InterfaceHandler;
import io.ruin.model.inter.InterfaceType;
import io.ruin.model.inter.actions.DefaultAction;
import io.ruin.model.inter.actions.OptionAction;
import io.ruin.model.inter.actions.SimpleAction;
import io.ruin.model.inter.dialogue.MessageDialogue;
import io.ruin.model.inter.dialogue.OptionsDialogue;
import io.ruin.model.inter.utils.Config;
import io.ruin.model.inter.utils.Option;

public class MainFrame {

    static {
        InterfaceHandler.register(Interface.ORBS, h -> {
            h.actions[1] = (OptionAction) XpCounter::select;
            h.actions[14] = (OptionAction) (p, option) -> {
                if(option == 1)
                    p.getPrayer().toggleQuickPrayers();
                else
                    TabPrayer.setupQuickPrayers(p, true);
            };
            h.actions[22] = (SimpleAction) p -> p.getMovement().toggleRunning();
            h.actions[42] = (SimpleAction) p -> {
                p.getPacketSender().sendClientScript(1749, "c", p.getPosition().getTileHash());
                p.openInterface(InterfaceType.WORLD_MAP, Interface.WORLD_MAP);
                p.getPacketSender().sendAccessMask(595, 17, 0, 4, 2);
            };
        });
        InterfaceHandler.register(Interface.CHAT_BAR, h -> {
            h.actions[7] = (OptionAction) (player, option) -> {
                if(option == 2)
                    Config.GAME_FILTER.toggle(player);
                else if(option == 3) {
                    player.dialogue(
                            new MessageDialogue("Would you like to filter yells from non-staff members?"),
                            new OptionsDialogue(
                                    new Option("Yes", () -> {
                                        player.yellFilter = true;
                                        player.sendMessage("Yells from non-staff members will now be hidden when your game chat is set to filtered.");
                                    }),
                                    new Option("No", () -> {
                                        player.yellFilter = false;
                                        player.sendMessage("Yells from non-staff members will now show even when your game chat is set to filtered.");
                                    })
                            )
                    );
                }
            };
        });
        InterfaceHandler.register(Interface.FIXED_SCREEN, actions -> {
            actions.actions[54] = (DefaultAction) (player, option, slot, itemId) -> {
                if(option == 2)
                    Config.DISABLE_SPELL_FILTERING.toggle(player);
            };
        });
        InterfaceHandler.register(Interface.RESIZED_SCREEN, actions -> {
            actions.actions[57] = (DefaultAction) (player, option, slot, itemId) -> {
                if(option == 2)
                    Config.DISABLE_SPELL_FILTERING.toggle(player);
            };
            //todo
        });
        InterfaceHandler.register(Interface.RESIZED_STACKED_SCREEN, actions -> {
            actions.actions[56] = (DefaultAction) (player, option, slot, itemId) -> {
                if(option == 2)
                    Config.DISABLE_SPELL_FILTERING.toggle(player);
            };
        });
    }

}
