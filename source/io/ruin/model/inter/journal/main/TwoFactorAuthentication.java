package io.ruin.model.inter.journal.main;

import io.ruin.cache.Color;
import io.ruin.model.World;
import io.ruin.model.entity.player.Player;
import io.ruin.model.inter.journal.JournalEntry;

import java.util.LinkedList;
import java.util.List;

public class TwoFactorAuthentication extends JournalEntry {

    public static final TwoFactorAuthentication INSTANCE = new TwoFactorAuthentication();

    @Override
    public void send(Player player) {
        send(player, "Two-Factor Auth",  player.tfa ? "Yes" : "No", player.tfa ? Color.GREEN : Color.RED);
    }

    @Override
    public void select(Player player) {
        if (player.tfa) {
            player.sendMessage("It looks like you have 2FA setup - good job for being secure!");
        } else {
            player.sendMessage("You do not currently have 2FA setup - please look into doing this!");
        }
    }

}