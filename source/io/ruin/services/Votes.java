package io.ruin.services;

import io.ruin.Server;
import io.ruin.api.database.DatabaseStatement;
import io.ruin.model.entity.npc.NPC;
import io.ruin.model.entity.player.Player;
import io.ruin.model.inter.dialogue.NPCDialogue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;

public class Votes {

    public static void claim(Player player, NPC npc, Consumer<Integer> consumer) {
        if(player.getInventory().isFull()) {
            player.dialogue(new NPCDialogue(npc, "You need at least 1 inventory slot to claim your voting rewards."));
            return;
        }
        player.lock();
        player.dialogue(new NPCDialogue(npc, "Attempting to claim vote tickets, please wait...").hideContinue());
        Server.gameDb.execute(new DatabaseStatement() {
            @Override
            public void execute(Connection connection) throws SQLException {
                try(Statement statement = connection.createStatement()) {
                    int claimed = statement.executeUpdate("UPDATE votes SET claimed = 1 WHERE userid = " + player.getUserId() + " AND claimed = 0 AND timestamp IS NOT NULL");
                    finish(claimed);
                }
            }
            @Override
            public void failed(Throwable t) {
                finish(-1);
                Server.logError(t); //todo exclude timeouts
            }
            private void finish(int claimed) {
                Server.worker.execute(() -> {
                    consumer.accept(claimed);
                    player.unlock();
                });
            }
        });
    }

}
