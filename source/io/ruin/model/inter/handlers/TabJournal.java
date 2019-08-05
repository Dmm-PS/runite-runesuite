package io.ruin.model.inter.handlers;

import io.ruin.cache.EnumMap;
import io.ruin.model.World;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.shared.listeners.DeathListener;
import io.ruin.model.inter.InterfaceHandler;
import io.ruin.model.inter.actions.SimpleAction;
import io.ruin.model.inter.actions.SlotAction;
import io.ruin.model.inter.journal.Journal;
import io.ruin.network.incoming.handlers.DisplayHandler;

public class TabJournal {

    static {
        InterfaceHandler.register(259, h -> {
            h.actions[8] = (SimpleAction) Journal.MAIN::send;
            h.actions[9] = World.isEco() ? (SimpleAction) Journal.ACHIEVEMENTS::send : (SimpleAction) Journal.PRESETS::send;
            h.actions[10] = World.isEco() ? (SimpleAction) Journal.PRESETS::send : (SimpleAction) Journal.TOGGLES::send;
            h.actions[11] = World.isEco() ? (SimpleAction) Journal.TOGGLES::send : (SimpleAction) Journal.BESTIARY::send;
            h.actions[12] = World.isEco() ? (SimpleAction) Journal.BESTIARY::send : null;
            h.actions[4] = (SlotAction) (p, slot) -> p.journal.select(p, slot);
        });
    }

    public static void swap(Player player, int interfaceId) {
        if(player.isFixedScreen())
            player.getPacketSender().sendInterface(interfaceId, 548, 68, 1);
        else if(player.getGameFrameId() == 164)
            player.getPacketSender().sendInterface(interfaceId, 164, 70, 1);
        else
            player.getPacketSender().sendInterface(interfaceId, 161, 70, 1);
    }

    public static void restore(Player player) {
        swap(player, 259);
        player.journal.send(player);
    }

}