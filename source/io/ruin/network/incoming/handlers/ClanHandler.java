package io.ruin.network.incoming.handlers;

import io.ruin.api.buffer.InBuffer;
import io.ruin.model.entity.player.Player;
import io.ruin.network.central.CentralClient;
import io.ruin.network.incoming.Incoming;
import io.ruin.utility.IdHolder;

@IdHolder(ids = {6, 18})
public class ClanHandler implements Incoming {

    @Override
    public void handle(Player player, InBuffer in, int opcode) {
        String username = in.readString();
        if(opcode == 6) {
            /**
             * Join / Leave
             */
            CentralClient.sendClanRequest(player.getUserId(), username);
            return;
        }
        if(opcode == 18) {
            /**
             * Kick
             */
            CentralClient.sendClanKick(player.getUserId(), username);
            return;
        }
    }

}