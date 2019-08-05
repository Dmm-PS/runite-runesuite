package io.ruin.services.discord.impl;

import io.ruin.model.World;
import io.ruin.services.discord.Webhook;
import io.ruin.services.discord.util.Embed;
import io.ruin.services.discord.util.Footer;
import io.ruin.services.discord.util.Message;
import io.ruin.services.discord.util.Thumbnail;

public class RareDropEmbedMessage {

    public static void sendDiscordMessage(String discordMessage, String npcDescriptiveName, int itemId) {
        if(World.isLive() || World.isEco()) {
            return;
        }
        try {
            Webhook webhook = new Webhook();
            Message message = new Message();

            Embed embedMessage = new Embed();
            embedMessage.setTitle("NPC Rare Drop");
            embedMessage.setDescription(discordMessage + " from " + npcDescriptiveName + "!");
            embedMessage.setColor(8917522);

            /**
             * Thumbnail
             */
            Thumbnail thumbnail = new Thumbnail();
            thumbnail.setUrl("https://ospvp.com/extra/items/" + itemId + ".png");
            embedMessage.setThumbnail(thumbnail);

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
