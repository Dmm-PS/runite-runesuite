package io.ruin.model.inter.journal;

import io.ruin.cache.Color;
import io.ruin.model.entity.player.Player;

public abstract class JournalEntry {

    public int childId;

    protected final void send(Player player, String text) {
        player.getPacketSender().sendJournalEntry(childId, text, -1);
    }

    protected final void send(Player player, String text, Color color) {
        player.getPacketSender().sendJournalEntry(childId, text, color.ordinal());
    }

    protected final void send(Player player, String key, String value, Color color) {
        player.getPacketSender().sendJournalEntry(childId, ("<col=D37E2A>" + key + ":</col> " + value), color.ordinal());
    }

    protected final void send(Player player, String key, int value, Color color) {
        player.getPacketSender().sendJournalEntry(childId, ("<col=D37E2A>" + key + ":</col> " + value), color.ordinal());
    }

    public abstract void send(Player player);

    public abstract void select(Player player);

}