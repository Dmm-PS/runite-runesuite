package io.ruin.services.discord.impl;

import io.ruin.model.World;
import io.ruin.services.discord.Webhook;
import io.ruin.services.discord.util.Embed;
import io.ruin.services.discord.util.Footer;
import io.ruin.services.discord.util.Message;

public class KillingSpreeEmbedMessage {

    public static void sendDiscordMessage(String spreeMessage) {

        if(World.isLive() || World.isEco()) {
            return;
        }
        try {
            Webhook webhook = new Webhook();
            Message message = new Message();

            Embed embedMessage = new Embed();
            embedMessage.setTitle("KillSpree Announcement");
            embedMessage.setDescription(spreeMessage);
            embedMessage.setColor(8917522);

            /**
             * Footer
             */
            Footer footer = new Footer();
            footer.setText("OSPvP - Jump into battle");
            embedMessage.setFooter(footer);

            /**
             * Fire the message
             */
            message.setEmbeds(embedMessage);
            webhook.sendMessage(message.toJson());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
