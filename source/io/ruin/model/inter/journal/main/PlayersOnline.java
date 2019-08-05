package io.ruin.model.inter.journal.main;

import io.ruin.cache.Color;
import io.ruin.model.World;
import io.ruin.model.entity.player.Player;
import io.ruin.model.inter.journal.JournalEntry;

public class PlayersOnline extends JournalEntry {

    public static final PlayersOnline INSTANCE = new PlayersOnline();

    @Override
    public void send(Player player) {
        send(player, "Players Online",  (int) (World.players.count() * 1.6), Color.GREEN);
    }

    @Override
    public void select(Player player) {
        player.sendMessage("There are currently " + (int) (World.players.count() * 1.6) + " players online.");
    }

}