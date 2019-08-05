package io.ruin.model.inter.journal.main;

import io.ruin.cache.Color;
import io.ruin.model.World;
import io.ruin.model.entity.player.Player;
import io.ruin.model.inter.journal.JournalEntry;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class StaffOnline extends JournalEntry {

    public static final StaffOnline INSTANCE = new StaffOnline();

    @Override
    public void send(Player player) {
        send(player, "Staff Online",  staffOnline(), Color.GREEN);
    }

    @Override
    public void select(Player player) {
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
    }

    public int staffOnline() {
        int total = 0;
        for (Player player : World.players) {
            if (player.isAdmin() || player.isModerator() || player.isSupport()) {
                total++;
            }
        }
        return total;
    }

}