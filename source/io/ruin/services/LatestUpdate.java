package io.ruin.services;

import io.ruin.Server;
import io.ruin.model.World;

import java.sql.ResultSet;
import java.sql.Statement;

public class LatestUpdate {

    public static String LATEST_UPDATE_URL = "https://runite.io/forum/index.php?threads/update-6-grand-exchange-new-home-and-more.1069/";
    public static String LATEST_UPDATE_TITLE = "Update #6 - Grand Exchange, New Home, and more!";

    public static void fetch() {
        Server.forumDb.execute(con -> {
            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT xf_thread.title, xf_thread.thread_id " +
                    "FROM xf_thread " +
                    "INNER JOIN xf_post " +
                    "ON xf_thread.thread_id = xf_post.thread_id AND xf_post.position = 0 " +
                    "WHERE xf_thread.node_id = 9 ORDER BY xf_thread.post_date DESC LIMIT 1");
            while(resultSet.next()) {
                LATEST_UPDATE_URL = World.type.getWebsiteUrl() + "/forum/index.php?threads/" + resultSet.getInt("thread_id");
                LATEST_UPDATE_TITLE = resultSet.getString("title");
            }

        });
    }
}
