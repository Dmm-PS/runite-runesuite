package io.ruin.model.inter.handlers;

import io.ruin.cache.Color;
import io.ruin.model.World;
import io.ruin.model.entity.npc.actions.edgeville.CreditManager;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.player.PlayerGroup;
import io.ruin.model.entity.shared.listeners.LoginListener;
import io.ruin.model.inter.Interface;
import io.ruin.model.inter.InterfaceHandler;
import io.ruin.model.inter.actions.SimpleAction;
import io.ruin.model.inter.dialogue.ItemDialogue;
import io.ruin.model.inter.dialogue.OptionsDialogue;
import io.ruin.model.inter.utils.Option;
import io.ruin.model.item.Item;
import io.ruin.services.XenUsername;

public class TabAccountManagement {

    private static final int OSPVP_CREDITS = 13190;
    private static final String FORUM_INBOX_URL = World.type.getWebsiteUrl() + "/forum/index.php?conversations/";
    private static final String VOTE_URL = World.type.getWebsiteUrl() + "/vote";
    private static final String HISCORES = World.type.getWebsiteUrl() + "/hiscores";

    static {
        /**
         * Send the interface when our player logs in
         */
        LoginListener.register(player -> player.getPacketSender().sendAccountManagement(getDonatorRank(player), getUsername(player), player.getUnreadPMs()));

        /**
         * Interface buttons
         */
        InterfaceHandler.register(Interface.ACCOUNT_MANAGEMENT, h -> {
            h.actions[3] = (SimpleAction) p -> p.dialogue(
                    new OptionsDialogue("Would you like to open our store?",
                            new Option("Yes", () -> p.openUrl(World.type.getWorldName() + " Store", CreditManager.STORE_URL)),
                            new Option("No", p::closeDialogue)
                    )
            );
            h.actions[8] = (SimpleAction) p -> p.dialogue(
                    new OptionsDialogue("Would you like to open the credit purchase page?",
                            new Option("Yes", () -> p.openUrl(World.type.getWorldName() + " Store", CreditManager.STORE_URL)),
                            new Option("No", p::closeDialogue)
                    )
            );
            h.actions[15] = (SimpleAction) p -> p.dialogue(
                    new OptionsDialogue("Would you like to open your forum inbox?",
                            new Option("Yes", () -> p.openUrl(World.type.getWorldName() + " Inbox", FORUM_INBOX_URL)),
                            new Option("No", p::closeDialogue)
                    )
            );
            h.actions[22] = (SimpleAction) p -> {
              Item credits = p.getInventory().findItem(OSPVP_CREDITS);
              if(credits == null || credits.getAmount() < 50) {
                  p.dialogue(new ItemDialogue().one(OSPVP_CREDITS, "You need at least " + Color.COOL_BLUE.wrap("50 " + World.type.getWorldName() + " Credits") + " to change your username. You can purchase credits from our store." +
                          "<br>Would you like to view the store?"),
                          new OptionsDialogue("Would you like to open our store?",
                                  new Option("Yes", () -> p.openUrl(World.type.getWorldName() + " Store", CreditManager.STORE_URL)),
                                  new Option("No", p::closeDialogue)
                          ));
                  return;
              }
              p.nameInput("What would you like to change your display name to?", reqName -> {
                  XenUsername.requestNameChange(p, reqName);
              });
            };
            h.actions[29] = (SimpleAction) p -> p.dialogue(new OptionsDialogue("Would you like to vote now?",
                    new Option("Yes", () -> p.openUrl(World.type.getWorldName() + " Vote", VOTE_URL)),
                    new Option("No", p::closeDialogue)
                    )
            );
            h.actions[32] = (SimpleAction) p -> p.dialogue(new OptionsDialogue("Would you like to view the hiscores?",
                            new Option("Yes", () -> p.openUrl(World.type.getWorldName() + " Hiscores", HISCORES)),
                            new Option("No", p::closeDialogue)
                    )
            );
        });
    }

    public static String getUsername(Player player) {
        PlayerGroup clientGroup = player.getClientGroup();
        return (clientGroup.clientImgId != -1 ? clientGroup.tag() + " " : "") + player.getName();
    }

    public static String getDonatorRank(Player player) {
        if (player.isGroup(PlayerGroup.GODLIKE_DONATOR)) {
            return PlayerGroup.GODLIKE_DONATOR.tag() + " Godlike";
        } else if (player.isGroup(PlayerGroup.UBER_DONATOR)) {
            return PlayerGroup.UBER_DONATOR.tag() + " MASTER";
        } else if (player.isGroup(PlayerGroup.LEGENDARY_DONATOR)) {
            return PlayerGroup.LEGENDARY_DONATOR.tag() + " Ultimate";
        } else if (player.isGroup(PlayerGroup.EXTREME_DONATOR)) {
            return PlayerGroup.EXTREME_DONATOR.tag() + " Extreme";
        } else if (player.isGroup(PlayerGroup.SUPER_DONATOR)) {
            return PlayerGroup.SUPER_DONATOR.tag() + " Super";
        } else if (player.isGroup(PlayerGroup.DONATOR)) {
            return PlayerGroup.DONATOR.tag() + " Regular";
        } else {
            return "Unranked";
        }
    }

}
