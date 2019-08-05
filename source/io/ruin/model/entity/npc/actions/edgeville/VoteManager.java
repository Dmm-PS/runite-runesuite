package io.ruin.model.entity.npc.actions.edgeville;

import io.ruin.cache.Color;
import io.ruin.cache.Icon;
import io.ruin.model.World;
import io.ruin.model.entity.npc.NPCAction;
import io.ruin.model.inter.dialogue.NPCDialogue;
import io.ruin.model.inter.dialogue.OptionsDialogue;
import io.ruin.model.inter.utils.Option;
import io.ruin.services.Votes;
import io.ruin.utility.Broadcast;

import static io.ruin.process.event.EventWorker.startEvent;

public class VoteManager {

    private static final String VOTE_URL = World.type.getWebsiteUrl() + "/vote";

    private static int voteMysteryBoxesClaimed = 0;

    static {
        NPCAction.register(1815, "cast-votes", (player, npc) -> {
            player.dialogue(
                    new OptionsDialogue("Would you like to open our voting page?",
                            new Option("Yes", () -> player.openUrl("Voting Page", VOTE_URL)),
                            new Option("No", player::closeDialogue)
                    )
            );
        });
        NPCAction.register(1815, "claim-votes", (player, npc) -> {
            Votes.claim(player, npc, claimed -> {
                if(claimed == -1) {
                    player.dialogue(new NPCDialogue(npc, "Error claiming votes, please try again."));
                    return;
                }
                if(claimed == 0) {
                    player.dialogue(new NPCDialogue(npc, "No unclaimed votes found."));
                    return;
                }
                player.getInventory().add(4067, claimed * 3);
                player.claimedVotes += claimed;
                player.dialogue(new NPCDialogue(npc, "You've successfully claimed " + claimed + " vote" + (claimed > 1 ? "s" : "") + "!"));
                player.sendFilteredMessage(Color.COOL_BLUE.wrap("You receive " + claimed + " vote ticket" + (claimed > 1 ? "s" : "") + " for voting."));
                player.voteMysteryBoxReward += claimed;
                if(player.voteMysteryBoxReward >= 3) {
                    voteMysteryBoxesClaimed += 1;
                    player.voteMysteryBoxReward -= 3;
                    boolean bank = player.getInventory().isFull();
                    if(bank)
                        player.getBank().add(6829, 1);
                    else
                        player.getInventory().add(6829, 1);
                    player.sendMessage(Color.COOL_BLUE.wrap("You receive a voting mystery box for voting on all 3 sites" + (bank ? " which has been deposited into your bank" : "") + "!"));
                }
            });
        });
        startEvent(e -> {
            while(true) {
                e.delay(3000); //30 minutes
                if(voteMysteryBoxesClaimed > 1) {
                    Broadcast.WORLD.sendNews(Icon.ANNOUNCEMENT, "Another " + voteMysteryBoxesClaimed + " players have claimed their FREE Voting Mystery Box! Type ::vote and claim yours now!");
                    voteMysteryBoxesClaimed = 0;
                }
            }
        });
    }

}
