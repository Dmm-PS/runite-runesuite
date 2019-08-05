package io.ruin.model.entity.shared.masks;

import io.ruin.api.buffer.OutBuffer;
import io.ruin.api.filestore.utility.Huffman;
import io.ruin.api.utils.StringUtils;
import io.ruin.model.entity.shared.UpdateMask;

public class ChatUpdate extends UpdateMask {

    private boolean shadow;

    private int type;

    private int effects;

    private int rankId;

    private byte[] messageData;

    public void set(boolean shadow, int effects, int rankId, int type, String message) {
        this.shadow = shadow;
        this.effects = effects;
        this.rankId = rankId;
        this.type = type;
        message = StringUtils.fixCaps(message);
        OutBuffer out = new OutBuffer(255);
        Huffman.encrypt(out, message);
        this.messageData = out.toByteArray();
    }

    @Override
    public void reset() {
        messageData = null;
    }

    @Override
    public boolean hasUpdate(boolean added) {
        return messageData != null;
    }

    @Override
    public void send(OutBuffer out, boolean playerUpdate) {
        out.addLEShort(effects);
        out.addByteA(rankId);
        //todo re-add (This was custom on Runite) out.addByteC(shadow ? 1 : 0);
        out.addByteC(type);
        out.addByte(messageData.length);
        out.addBytes(messageData);

        /*

				int_5 = packetbuffer_0.readUnsignedLEShort();
				final Permission permission_0 = (Permission) class76.forOrdinal(WorldMapData.method216(),
				        packetbuffer_0.readUnsignedByteA());
				final boolean bool_0 = packetbuffer_0.readUnsignedByteC() == 1;
				int_7 = packetbuffer_0.readUnsignedByte();
         */
    }

    @Override
    public int get(boolean playerUpdate) {
        return 0x40;
    }

}