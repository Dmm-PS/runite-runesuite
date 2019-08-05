package io.ruin.utility;

import io.ruin.cache.Icon;
import io.ruin.model.World;
import io.ruin.model.entity.player.Player;
import io.ruin.network.central.CentralClient;

import java.util.function.BiConsumer;

public enum Broadcast {

    FRIENDS(Icon.GREEN_INFO_BADGE, (player, message) -> CentralClient.sendGlobalMessage(player.getUserId(), message)),
    WORLD(Icon.BLUE_INFO_BADGE, (player, message) -> World.players.forEach(p -> p.sendMessage(message))),
    GLOBAL(Icon.YELLOW_INFO_BADGE, (player, message) -> CentralClient.sendGlobalMessage(-1, message));

    private final Icon newsIcon;


    private final BiConsumer<Player, String> consumer;

    Broadcast(Icon newsIcon, BiConsumer<Player, String> consumer) {
        this.newsIcon = newsIcon;
        this.consumer = consumer;
    }

    /**
     * Send the given message as is.
     */

    public void sendPlain(String message) {
        sendPlain(null, message);
    }

    public void sendPlain(Player player, String message) {
        //Player only required to be set when this == FRIENDS.
        consumer.accept(player, message);
    }

    /**
     * Send the given message with a news portion added to it.
     */

    public void sendNews(String message) {
        sendNews(null, null, null, message);
    }

    public void sendNews(Player player, String message) {
        sendNews(player, null, null, message);
    }

    public void sendNews(Icon overrideIcon, String title, String message) {
        sendNews(null, overrideIcon, title, message);
    }

    public void sendNews(Icon overrideIcon, String message) {
        sendNews(null, overrideIcon, null, message);
    }

    public void sendNews(Player player, Icon overrideIcon, String title, String message) {

        //Player only required to be set when this == FRIENDS.
      consumer.accept(player, (overrideIcon == null ? newsIcon.tag() : overrideIcon.tag()) + "<col=006600> " +
             (title == null ? "News:" : title + ":") + " <col=1e44b3>" + message);
    }

}