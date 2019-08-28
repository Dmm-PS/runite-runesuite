package io.ruin.network;

import io.netty.channel.ChannelFutureListener;
import io.ruin.Server;
import io.ruin.api.buffer.OutBuffer;
import io.ruin.api.protocol.Protocol;
import io.ruin.api.protocol.login.LoginInfo;
import io.ruin.api.utils.ISAACCipher;
import io.ruin.data.impl.teleports;
import io.ruin.model.World;
import io.ruin.model.entity.Entity;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.player.ai.AIPlayer;
import io.ruin.model.inter.InterfaceType;
import io.ruin.model.inter.Widget;
import io.ruin.model.inter.handlers.BuyCredits;
import io.ruin.model.inter.journal.JournalCategory;
import io.ruin.model.item.Item;
import io.ruin.model.item.containers.shop.ShopItem;
import io.ruin.model.map.Position;
import io.ruin.model.map.Region;
import io.ruin.model.map.ground.GroundItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PacketSender {

    private final Player player;

    private final ISAACCipher cipher;

    public PacketSender(Player player, ISAACCipher cipher) {
        this.player = player;
        this.cipher = cipher;
    }

    public void write(OutBuffer out) { //this has to be called by main thread or else gg because of cipher
        if (player instanceof AIPlayer) {
            return;
        }
        if(!Thread.currentThread().getName().equals("server-worker #1")) //todo remove after testing
            Server.getLogger("warnings").log(new Throwable(player.getName() + " wrote packet off main thread!"));
        player.getChannel().write(out.encode(cipher).toBuffer());
    }

    /**
     * Packets
     */

    public void sendLogin(LoginInfo info) {
        if (player instanceof AIPlayer) {
            return;
        }
        OutBuffer out = new OutBuffer(14).sendFixedPacket(2);
        if(info.tfaTrust) {
            out.addByte(1);
            out.startEncrypt().addInt(info.tfaTrustValue).stopEncrypt();
            out.encrypt(cipher);
        } else {
            out.addByte(0);
            out.skip(4);
        }
        out.addByte((World.isDev() || player.isAdmin()) ? 2 : player.isModerator() ? 1 : 0)
                .addByte(0)
                .addShort(player.getIndex())
                .addByte(0);
        player.getChannel().write(out.toBuffer()); //no encryption needed!
        sendRegion(true);
        Region.update(player);
    }

    public void sendLogout() {
        //if (player instanceof AIPlayer) {
        //    return;
        //}
        OutBuffer out = new OutBuffer(1).sendFixedPacket(68);
        player.getChannel().writeAndFlush(out.encode(cipher).toBuffer()).addListener(ChannelFutureListener.CLOSE);
    }

    public void sendRegion(boolean login) {
        player.removeFromRegions();
        Position position = player.getPosition();
        Region region = player.lastRegion = position.getRegion();
        int chunkX = position.getChunkX();
        int chunkY = position.getChunkY();
        int depth = Region.CLIENT_SIZE >> 4;
        boolean dynamic = region.dynamicData != null;
        OutBuffer out = new OutBuffer(255);
        if(login || !dynamic) {
            /**
             * Regular map
             */
            out.sendVarShortPacket(36);
            if(login)
                player.getUpdater().init(out);
            if(dynamic) {
                /**
                 * Dynamic region must be sent after regular region on login. //todo check if this is still the case in 171 since they changed how this packet is sent
                 */
                player.getUpdater().updateRegion = true;
                chunkX = chunkY = 0;
            }
            out.addShortA(chunkX);
            out.addLEShortA(chunkY);
            int countPos = out.position();
            out.addShort(0);
            boolean forceSend = false; //Resets the landscape client sided
            if((chunkX / 8 == 48 || chunkX / 8 == 49) && chunkY / 8 == 48)
                forceSend = true;
            if(chunkX / 8 == 48 && chunkY / 8 == 148)
                forceSend = true;
            int regionCount = 0;
            for(int xCalc = (chunkX - depth) / 8; xCalc <= (chunkX + depth) / 8; xCalc++) {
                for(int yCalc = (chunkY - depth) / 8; yCalc <= (chunkY + depth) / 8; yCalc++) {
                    int regionId = yCalc + (xCalc << 8);
                    if(!forceSend || (yCalc != 49 && yCalc != 149 && yCalc != 147 && xCalc != 50 && (xCalc != 49 || yCalc != 47))) {
                        Region r = Region.get(regionId);
                        if(r.keys == null)
                            out.skip(16);
                        else
                            out.addInt(r.keys[0]).addInt(r.keys[1]).addInt(r.keys[2]).addInt(r.keys[3]);
                        player.addRegion(r);
                        regionCount++;
                    }
                }
            }
            int curPos = out.position();
            out.position(countPos);
            out.addShort(regionCount);
            out.position(curPos);
        } else {
            /**
             * Dynamic map
             */
            out.sendVarShortPacket(81)
                    .addLEShort(chunkX)
                    .addLEShort(chunkY)
                    .addByte(0); //force refresh;
            int startPos = out.position();
            out.addShort(0);

            ArrayList<Integer> chunkRegionIds = new ArrayList<>();
            out.initBitAccess();
            for(int pointZ = 0; pointZ < 4; pointZ++) {
                for(int xCalc = (chunkX - depth); xCalc <= (chunkX + depth); xCalc++) {
                    for(int yCalc = (chunkY - depth); yCalc <= (chunkY + depth); yCalc++) {
                        Region r = Region.LOADED[(xCalc / 8) << 8 | (yCalc / 8)];
                        if(r == null || r.dynamicData == null || r.dynamicIndex != region.dynamicIndex) {
                            out.addBits(1, 0);
                            continue;
                        }
                        int[] chunkData = r.dynamicData[pointZ][xCalc % 8][yCalc % 8];
                        int chunkHash = chunkData[0];
                        int chunkRegionId = chunkData[1];
                        if(chunkHash == 0 || chunkRegionId == 0) {
                            out.addBits(1, 0);
                            continue;
                        }
                        out.addBits(1, 1);
                        out.addBits(26, chunkHash);
                        if(!chunkRegionIds.contains(chunkRegionId))
                            chunkRegionIds.add(chunkRegionId);
                        if(!player.getRegions().contains(r))
                            player.addRegion(r);
                    }
                }
            }
            out.finishBitAccess();

            int endPos = out.position();
            out.position(startPos);
            out.addShort(chunkRegionIds.size());
            out.position(endPos);

            for(int id : chunkRegionIds) {
                Region r = Region.LOADED[id];
                if(r.keys == null)
                    out.skip(16);
                else
                    out.addInt(r.keys[0]).addInt(r.keys[1]).addInt(r.keys[2]).addInt(r.keys[3]);
            }
        }
        write(out);
    }

    public void sendGameFrame(int id) {
        player.setGameFrameId(id);
        OutBuffer out = new OutBuffer(3).sendFixedPacket(16)
                .addShort(id);
        write(out);
    }

    public void refreshGameFrame() {
        OutBuffer out = new OutBuffer(1).sendFixedPacket(45);
        write(out);
    }

    public void sendUrl(String url, boolean copy) {
        OutBuffer out = new OutBuffer(2 + Protocol.strLen(url) + 1).sendVarBytePacket(85)
                .addString(url)
                .addByte(copy ? 1 : 0);
        write(out);
    }

    public void sendInterface(int interfaceId, int parentId, int childId, int overlayType) {
        player.setVisibleInterface(interfaceId, parentId, childId);
        OutBuffer out = new OutBuffer(8).sendFixedPacket(79)
                .addByteS(overlayType)
                .addLEInt(parentId << 16 | childId)
                .addShortA(interfaceId);
        write(out);
    }

    public void removeInterface(int parentId, int childId) {
        player.removeVisibleInterface(parentId, childId);
        OutBuffer out = new OutBuffer(5).sendFixedPacket(11)
                .addInt(parentId << 16 | childId);
        write(out);
    }

    public void moveInterface(int fromParentId, int fromChildId, int toParentId, int toChildId) {
        player.moveVisibleInterface(fromParentId, fromChildId, toParentId, toChildId);
        OutBuffer out = new OutBuffer(9).sendFixedPacket(14)
                .addInt2(fromParentId << 16 | fromChildId)
                .addInt1(toParentId << 16 | toChildId);
        write(out);
    }

    public void sendString(int interfaceId, int childId, String string) {
        OutBuffer out = new OutBuffer(3 + 4 + Protocol.strLen(string)).sendVarShortPacket(37)
                .addInt(interfaceId << 16 | childId)
                .addString(string);
        write(out);
    }

    public void setHidden(int interfaceId, int childId, boolean hide) {
        OutBuffer out = new OutBuffer(6).sendFixedPacket(20)
                .addInt2(interfaceId << 16 | childId)
                .addByteC(hide ? 1 : 0);
        write(out);
    }

    public void sendItem(int parentId, int childId, int itemId, int amount) {
        OutBuffer out = new OutBuffer(11).sendFixedPacket(54)
                .addShortA(itemId)
                .addInt(parentId << 16 | childId)
                .addLEInt(amount);
        write(out);
    }

    public void setAlignment(int parentId, int childId, int x, int y) {
        OutBuffer out = new OutBuffer(9).sendFixedPacket(41)
                .addShort(y)
                .addLEInt(parentId << 16 | childId)
                .addLEShort(x);
        write(out);
    }

    public void sendAccessMask(int interfaceId, int childParentId, int minChildId, int maxChildId, int mask) {
        OutBuffer out = new OutBuffer(13).sendFixedPacket(35)
                .addShortA(minChildId)
                .addInt2(interfaceId << 16 | childParentId)
                .addInt1(mask)
                .addLEShortA(maxChildId);
        write(out);
    }

    public void sendClientScript(int id, String type, Object... params) {
        OutBuffer out = new OutBuffer(3 + Protocol.strLen(type) + (params.length * 4)).sendVarShortPacket(3)
                .addString(type);
        for(int i = type.length() - 1; i >= 0; i--) {
            Object param = params[i];
            if(param instanceof String)
                out.addString((String) param);
            else
                out.addInt((Integer) param);
        }
        out.addInt(id);
        write(out);
    }

    public void sendSystemUpdate(int time) {
        OutBuffer out = new OutBuffer(3).sendFixedPacket(5)
                .addLEShort(time * 50 / 30);
        write(out);
    }

    public void setTextStyle(int parentId, int childId, int horizontalAlignment, int verticalAlignment, int lineHeight) {
        sendClientScript(600, "iiiI", horizontalAlignment, verticalAlignment, lineHeight, parentId << 16 | childId);
    }

    public void fadeIn() {
        sendClientScript(948, "iiiii", 0, 0, 0, 255, 50);
    }

    public void fadeOut() {
        InterfaceType.SECONDARY_OVERLAY.open(player, 174);
        sendClientScript(951, "");
    }

    public void clearFade() {
        sendClientScript(948, "iiiii", 0, 0, 0, 255, 0);
    }

    public void sendMessage(String message, String extension, int type) {
        OutBuffer out = Protocol.messagePacket(message, extension, type);
        write(out);
    }

    public void sendVarp(int id, int value) {
        OutBuffer out;
        if(value < Byte.MIN_VALUE || value > Byte.MAX_VALUE)
            out = new OutBuffer(7).sendFixedPacket(50).addLEInt(value).addLEShort(id);
        else
            out = new OutBuffer(4).sendFixedPacket(49).addByteS(value).addLEShort(id);
        write(out);
    }

    public void sendItems(int parentId, int childId, int containerId, Item... items) {
        sendItems(parentId << 16 | childId, containerId, items, items.length);
    }

    public void sendItems(int parentId, int childId, int containerId, Item[] items, int length) {
        sendItems(parentId << 16 | childId, containerId, items, length);
    }

    public void sendItems(int interfaceHash, int containerId, Item[] items, int length) {
        OutBuffer out = new OutBuffer(10 + (length * 10)).sendVarShortPacket(7)
                .addInt(interfaceHash)
                .addShort(containerId)
                .addShort(length);
        for(int slot = 0; slot < length; slot++) {
            Item item = items[slot];
            if(item == null || item.getId() < 0) {
                out.addByte(0);
                out.addShort(0);
            } else {
                if(item.getAmount() < 255) {
                    out.addByte(item.getAmount());
                } else {
                    out.addByte(255);
                    out.addInt2(item.getAmount());
                }
                out.addShort(item.getId() + 1);
            }
        }
        write(out);
    }

    public void updateItems(int interfaceHash, int containerId, Item[] items, boolean[] updatedSlots, int updatedCount) {
        OutBuffer out = new OutBuffer(10 + (updatedCount * 10)).sendVarShortPacket(22)
                .addInt(interfaceHash)
                .addShort(containerId);
        for(int slot = 0; slot < items.length; slot++) {
            if(updatedSlots[slot]) {
                Item item = items[slot];
                out.addSmart(slot);
                if(item == null || item.getId() < 0) {
                    out.addShort(0);
                } else {
                    out.addShort(item.getId() + 1);
                    if(item.getAmount() < 255) {
                        out.addByte(item.getAmount());
                    } else {
                        out.addByte(255);
                        out.addInt(item.getAmount());
                    }
                }
            }
        }
        write(out);
    }

    public void unlinkItems(int containerId) {
        OutBuffer out = new OutBuffer(3).sendFixedPacket(28)
                .addShort(containerId);
        write(out);
    }

    public void sendStat(int id, int currentLevel, int experience) {
        OutBuffer out = new OutBuffer(7).sendFixedPacket(10)
                .addInt1(experience)
                .addByteA(id)
                .addByteC(currentLevel);
        write(out);
    }

    public void sendRunEnergy(int energy) {
        OutBuffer out = new OutBuffer(2).sendFixedPacket(46)
                .addByte(energy);
        write(out);
    }

    public void sendWeight(int weight) {
        OutBuffer out = new OutBuffer(3).sendFixedPacket(53)
                .addShort(weight);
        write(out);
    }

    public void sendPlayerAction(String name, boolean top, int option) {
        OutBuffer out = new OutBuffer(4 + Protocol.strLen(name)).sendVarBytePacket(17)
                .addByteS(top ? 1 : 0)
                .addByteC(option)
                .addString(name);
        write(out);
    }

    public void worldHop(String host, int id, int flags) {
        OutBuffer out = new OutBuffer(50).sendFixedPacket(76)
                .addString(host)
                .addShort(id)
                .addInt(flags);
        //todo@@ write(out);
    }

    public void resetMapFlag() {
        OutBuffer out = new OutBuffer(2).sendFixedPacket(26)
                .addByte(-1)
                .addByte(-1);
        write(out);
    }

    public void clearChunks() {
        OutBuffer out = new OutBuffer(3).sendFixedPacket(80)
                .addByteS(-1)
                .addByteA(-1);
        write(out);
    }

    public void clearChunk(int chunkAbsX, int chunkAbsY) {
        int x = Position.getLocal(chunkAbsX, player.getPosition().getFirstChunkX());
        int y = Position.getLocal(chunkAbsY, player.getPosition().getFirstChunkY());
        OutBuffer out = new OutBuffer(3).sendFixedPacket(80)
                .addByteS(x)
                .addByteA(y);
        write(out);
    }

    private void sendMapPacket(int x, int y, int z, Function<Integer, OutBuffer> write) {
        if(player.getHeight() != z)
            return;
        int chunkAbsX = (x >> 3) << 3;
        int chunkAbsY = (y >> 3) << 3;
        int targetLocalX = x - chunkAbsX;
        int targetLocalY = y - chunkAbsY;
        int playerLocalX = Position.getLocal(chunkAbsX, player.getPosition().getFirstChunkX());
        int playerLocalY = Position.getLocal(chunkAbsY, player.getPosition().getFirstChunkY());
        if(playerLocalX >= 0 && playerLocalX < 104 && playerLocalY >= 0 && playerLocalY < 104) {
            write(new OutBuffer(3).sendFixedPacket(62).addByteA(playerLocalY).addByteS(playerLocalX));
            write(write.apply((targetLocalX & 0x7) << 4 | (targetLocalY & 0x7)));
        }
    }

    public void sendGroundItem(GroundItem groundItem) {
        sendMapPacket(groundItem.x, groundItem.y, groundItem.z, offsetHash ->
                new OutBuffer(6).sendFixedPacket(61)
                        .addByteS(offsetHash)
                        .addShortA(groundItem.id)
                        .addLEShort(groundItem.amount)
        );
    }

    public void sendRemoveGroundItem(GroundItem groundItem) {
        sendMapPacket(groundItem.x, groundItem.y, groundItem.z, offsetHash ->
                new OutBuffer(4).sendFixedPacket(75)
                        .addLEShortA(groundItem.id)
                        .addByteA(offsetHash)

        );
    }

    public void sendCreateObject(int id, int x, int y, int z, int type, int dir) {
        sendMapPacket(x, y, z, offsetHash ->
                new OutBuffer(5).sendFixedPacket(76)
                        .addByteC(offsetHash)
                        .addByteS(type << 2 | dir)
                        .addShortA(id)
        );
    }

    public void sendRemoveObject(int x, int y, int z, int type, int dir) {
        sendMapPacket(x, y, z, offsetHash ->
                new OutBuffer(3).sendFixedPacket(58)
                        .addByte(type << 2 | dir)
                        .addByteS(offsetHash)
        );
    }

    public void sendObjectAnimation(int x, int y, int z, int type, int dir, int animationId) {
        sendMapPacket(x, y, z, offsetHash ->
                new OutBuffer(5).sendFixedPacket(12)
                        .addLEShortA(animationId)
                        .addByte(type << 2 | dir)
                        .addByte(offsetHash)
        );
    }

    public void sendProjectile(int projectileId, int startX, int startY, int destX, int destY, int targetIndex, int startHeight, int endHeight, int delay, int duration, int curve, int something) {
        sendMapPacket(startX, startY, player.getHeight(), offsetHash ->
                new OutBuffer(16).sendFixedPacket(9)
                        .addShort(duration)
                        .addShort(targetIndex)
                        .addByte((destX - startX))
                        .addByteA((destY - startY))
                        .addByte(endHeight)
                        .addByteS(curve)
                        .addByteS(something)
                        .addByteS(offsetHash)
                        .addShortA(projectileId)
                        .addByteA(startHeight)
                        .addShort(delay)
        );
    }

    public void sendGraphics(int id, int height, int delay, int x, int y, int z) {
        sendMapPacket(x, y, z, offsetHash ->
                new OutBuffer(7).sendFixedPacket(32)
                        .addShort(id)
                        .addShort(delay)
                        .addByteS(offsetHash)
                        .addByte(height)

        );
    }

    public void sendAreaSound(int id, int type, int delay, int x, int y, int distance) {
        sendMapPacket(x, y, player.getHeight(), offsetHash ->
                new OutBuffer(6).sendFixedPacket(43)
                        .addByteC(delay)
                        .addByte(distance << 4 | type)
                        .addByteC(offsetHash)
                        .addLEShortA(id)
        );
    }

    public void sendSoundEffect(int id, int type, int delay) {
        OutBuffer out = new OutBuffer(6).sendFixedPacket(78)
                .addShort(id)
                .addByte(type)
                .addShort(delay);
        write(out);
    }

    public void sendMusic(int id) {
        OutBuffer out = new OutBuffer(3).sendFixedPacket(29)
                .addShortA(id);
        write(out);
    }

    public void sendJournal(int type, List<JournalCategory> categories) {
        OutBuffer out = new OutBuffer(255).sendVarShortPacket(86)
                .addByte(type);
        for(JournalCategory category : categories) {
            out.addString(category.name);
            out.addSmart(category.count);
        }
        write(out);
    }

    public void sendJournalEntry(int childId, String text, int colorIndex) {
        OutBuffer out = new OutBuffer(2 + Protocol.strLen(text) + 1).sendVarBytePacket(87)
                .addSmart(childId)
                .addString(text)
                .addByte(colorIndex);
        write(out);
    }

    public void sendAccountManagement(String donatorStatus, String username, int unreadPMs) {
        OutBuffer out = new OutBuffer(2 + Protocol.strLen(donatorStatus) + 5).sendVarShortPacket(92)
                .addString(donatorStatus)
                .addString(username).
                addByte(unreadPMs);
        write(out);
    }

    public void sendSkillinterface(String donatorStatus) {
        OutBuffer out = new OutBuffer(2 + Protocol.strLen(donatorStatus)).sendVarShortPacket(95)
                .addString(donatorStatus);
        write(out);
    }

    public void sendDiscordPresence(String discordStatus) {
        OutBuffer out = new OutBuffer(2 + Protocol.strLen(discordStatus)).sendVarShortPacket(94)
                .addString(discordStatus);
        write(out);
    }


    public void sendShop(String title, ShopItem... items) {
        OutBuffer out = new OutBuffer(3 + Protocol.strLen(title) + 2 + (items.length * 6)).sendVarShortPacket(89)
                .addString(title)
                .addShort(items.length);
        for(ShopItem item : items) {
            out.addShort(item.getDisplayId(player));
            out.addInt(item.price);
            out.addInt(item.remaining);
        }
        write(out);
    }

    public void sendTeleports(String title,
                              int selectedCategoryIndex, teleports.Category[] categories,
                              int selectedSubcategoryIndex, teleports.Subcategory[] subcategories,
                              teleports.Teleport[] teleports) {

        OutBuffer out = new OutBuffer(255).sendVarShortPacket(90);

        if(title != null)
            out.addString(title);
        else
            out.addByte(0);

        out.addByte(selectedCategoryIndex);
        if(categories != null) {
            out.addByte(categories.length);
            for(teleports.Category c : categories)
                out.addString(c.name);
        } else {
            out.addByte(0);
        }

        out.addByte(selectedSubcategoryIndex);
        if(subcategories != null) {
            out.addByte(subcategories.length);
            for(teleports.Subcategory c : subcategories)
                out.addString(c.name);
        } else {
            out.addByte(0);
        }

        if(teleports != null) {
            for(teleports.Teleport t : teleports)
                out.addString(t.name);
        }

        write(out);
    }

    public void sendDropTable(String name, int petId, int petAverage, List<Integer[]> drops) {
        OutBuffer out = new OutBuffer(3 + Protocol.strLen(name) + 4 + (drops.size() * 13)).sendVarShortPacket(91)
                .addString(name)
                .addShort(petId)
                .addShort(petAverage);
        for(Integer[] drop : drops) {
            out.addShort(drop[0]);  //id
            out.addByte(drop[1]);   //broadcast
            out.addInt(drop[2]);    //min amount
            out.addInt(drop[3]);    //max amount
            out.addShort(drop[4]);  //average
        }
        write(out);
    }

    public void sendLoyaltyRewards(int dayReward, int currentSpree, int highestSpree, int totalClaimedRewards, Item... loyaltyRewards) {
        OutBuffer out = new OutBuffer(3 + 1 + 12 + (loyaltyRewards.length * 8)).sendVarShortPacket(93)
                .addByte(dayReward)
                .addInt(currentSpree)
                .addInt(highestSpree)
                .addInt(totalClaimedRewards);
        for(Item reward : loyaltyRewards) {
            out.addInt(reward.getId());
            out.addInt(reward.getAmount());
        }
        write(out);
    }

/*    public void sendBuyCredits(String message, int discountPercent, int selectedCreditPack, int selectedPaymentMethod, BuyCredits... packs) {
        OutBuffer out = new OutBuffer(3 + Protocol.strLen(message) + 3 + (packs.length * 12)).sendVarShortPacket(12)
                .addString(message)
                .addByte(discountPercent)
                .addByte(selectedCreditPack)
                .addByte(selectedPaymentMethod);
        for(BuyCredits pack : packs) {
            out.addInt(pack.purchasePrice);
            out.addInt(pack.purchaseAmount);
            out.addInt(pack.freeAmount);
        }
        //todo@@ write(out);
    }*/

    public void sendWidget(Widget widget, int seconds) {
        OutBuffer out = new OutBuffer(4).sendVarShortPacket(88)
                .addByte(widget.getSpriteId())
                .addShort(seconds * 50)
                .addString(widget.getName())
                .addString(widget.getDescription());
        write(out);
    }

    public void sendPlayerHead(int parentId, int childId) {
        OutBuffer out = new OutBuffer(5).sendFixedPacket(70)
                .addInt2(parentId << 16 | childId);
        write(out);
    }

    public void sendNpcHead(int parentId, int childId, int npcId) {
        OutBuffer out = new OutBuffer(7).sendFixedPacket(0)
                .addInt1(parentId << 16 | childId)
                .addShortA(npcId);
        write(out);
    }

    public void animateInterface(int parentId, int childId, int animationId) {
        OutBuffer out = new OutBuffer(7).sendFixedPacket(65)
                .addLEShort(animationId)
                .addInt2(parentId << 16 | childId);
        write(out);
    }

    public void sendMapState(int state) {
        OutBuffer out = new OutBuffer(2).sendFixedPacket(82)
                .addByte(state);
        write(out);
    }

    public void sendHintIcon(Entity target) {
        OutBuffer out = new OutBuffer(8).sendFixedPacket(60)
                .addByte(target.player == null ? 1 : 10)
                .addShort(target.getIndex())
                .skip(3);
        write(out);
    }

    public void sendHintIcon(int x, int y) {
        OutBuffer out = new OutBuffer(8).sendFixedPacket(60)
                .addByte(2)
                .addShort(x)
                .addShort(y)
                .addByte(1);
        write(out);
    }


    public void resetHintIcon(boolean npcType) {
        OutBuffer out = new OutBuffer(8).sendFixedPacket(60)
                .addByte(npcType ? 1 : 10)
                .addShort(-1)
                .skip(3);
        write(out);
    }

    public void turnCameraToLocation(int x, int y, int cameraHeight, int constantSpeed, int variableSpeed) {
        Position pos = new Position(x, y, 0);
        int posX = pos.getSceneX(player.getPosition());
        int posY = pos.getSceneY(player.getPosition());
        OutBuffer out = new OutBuffer(7).sendFixedPacket(48)
                .addByte(posX)
                .addByte(posY)
                .addShort(cameraHeight)
                .addByte(constantSpeed)
                .addByte(variableSpeed);
        write(out);
    }

    public void moveCameraToLocation(int x, int y, int cameraHeight, int constantSpeed, int variableSpeed) {
        Position pos = new Position(x, y, 0);
        int posX = pos.getSceneX(player.getPosition());
        int posY = pos.getSceneY(player.getPosition());
        OutBuffer out = new OutBuffer(7).sendFixedPacket(67)
                .addByte(posX)
                .addByte(posY)
                .addShort(cameraHeight)
                .addByte(constantSpeed)
                .addByte(variableSpeed);
        write(out);
    }

    /*
            int chunkAbsX = (x >> 3) << 3;
        int chunkAbsY = (y >> 3) << 3;
        int targetLocalX = x - chunkAbsX;
        int targetLocalY = y - chunkAbsY;
        int playerLocalX = Position.getLocal(chunkAbsX, player.getPosition().getFirstChunkX());
        int playerLocalY = Position.getLocal(chunkAbsY, player.getPosition().getFirstChunkY());
        if(playerLocalX >= 0 && playerLocalX < 104 && playerLocalY >= 0 && playerLocalY < 104) {
            write(new OutBuffer(3).sendFixedPacket(70).addByteA(playerLocalY).addByteC(playerLocalX));
            write(write.apply((targetLocalX & 0x7) << 4 | (targetLocalY & 0x7)));
     */

    /**
     * @param shakeType 0 shakes X, 1 shakes Z, 2 shakes Y, 3 shakes Yaw, 4 shakes Pitch
     */
    public void shakeCamera(int shakeType, int intensity) {
        OutBuffer out = new OutBuffer(5).sendFixedPacket(69)
                .addByte(shakeType)
                .addByte(intensity)
                .addByte(intensity)
                .addByte(intensity);
        write(out);
    }

    public void resetCamera() {
        OutBuffer out = new OutBuffer(1).sendFixedPacket(51);
        write(out);
    }

    public void setHeatmap(boolean heatmap) {
        OutBuffer out = new OutBuffer(2).sendFixedPacket(73);
        out.addByte(heatmap ? 1 : 0);
        write(out);
    }

    public void sendGrandExchangeUpdate(int state, int slot, int itemId, int price, int amount, int amountSold, int moneyReceived) {
        OutBuffer out = new OutBuffer(21).sendFixedPacket(31);
        out.addByte(slot);
        out.addByte(state);
        out.addShort(itemId);
        out.addInt(price);
        out.addInt(amount);
        out.addInt(amountSold);
        out.addInt(moneyReceived);
        write(out);
    }

}