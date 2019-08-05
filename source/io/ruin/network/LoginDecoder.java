package io.ruin.network;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.ruin.api.buffer.InBuffer;
import io.ruin.api.netty.MessageDecoder;
import io.ruin.api.protocol.Protocol;
import io.ruin.api.protocol.Response;
import io.ruin.api.protocol.login.LoginInfo;
import io.ruin.api.utils.IPAddress;
import io.ruin.api.utils.Random;
import io.ruin.model.World;
import io.ruin.model.activities.wilderness.Wilderness;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.player.PlayerLogin;
import io.ruin.process.LoginWorker;

import java.math.BigInteger;

public class LoginDecoder extends MessageDecoder<Channel> {

    public LoginDecoder() {
        super(null, false);
    }

    @Override
    protected void handle(Channel channel, InBuffer in, int opcode) {
        if(opcode == 14) {
            /**
             * Login handshake request
             */
            confirmHandshake(channel);
        } else if(opcode == 16 || opcode == 18) {
            /**
             * World login request
             */

            LoginInfo loginInfo = decode(channel, in, World.id);
            if(loginInfo != null)
                LoginWorker.add(new PlayerLogin(loginInfo));
        }
    }

    @Override
    protected int getSize(int opcode) {
        switch(opcode) {
            case 14: return 0;          //login handshake request
            case 16: return VAR_SHORT;  //world login request
            case 18: return VAR_SHORT;  //world login request (from dc)
        }
        return UNHANDLED;
    }

    /**
     * Separator
     */

    private static final int SUB_BUILD = 24;

    private static final int BUILD_HASH = Protocol.CLIENT_BUILD << 16 | SUB_BUILD;

    private static final BigInteger EXPONENT = new BigInteger("83923922839219287732917015149195732073695536052984669036231778384894490439511932467277679466826619631057457711190000931928848746974072996674449163992099423404866521840525018174269929252811697346444185514151969525075984167323004389765704130745055780821036980587747264383960689940905074229888881011617738648897");

    private static final BigInteger MODULUS = new BigInteger("94210824259843347324509385276594109263523823612210415282840685497179394322370180677069205378760490069724955139827325518162089726630921395369270393801925644637806226306156731189625154078707248525519618118185550146216513714101970726787284175941436804270501308516733103597242337227056455402809871503542425244523");

    private static void confirmHandshake(Channel channel) {
        channel.writeAndFlush(Unpooled.buffer(9).writeByte(0).writeLong(Random.getLong()));
    }

    private static LoginInfo decode(Channel channel, InBuffer in, int worldId) {
        if (World.isPVP() && isAtWildernessLimitForIP(channel)) {
            /**
             * Multiple of the same IP is already in the wilderness.
             */
            Response.CONNECTION_LIMIT.send(channel);
            return null;
        }
        if(in.readInt() != BUILD_HASH) {
            /**
             * Invalid client version.
             */
            Response.GAME_UPDATED.send(channel);
            return null;
        }
        byte[] rsaPayload = new byte[in.readUnsignedShort()];
        in.readBytes(rsaPayload);
        InBuffer rsaBuffer = new InBuffer(new BigInteger(rsaPayload).modPow(EXPONENT, MODULUS).toByteArray());
        if(rsaBuffer.readByte() != 1) {
            /**
             * Invalid RSA header key.
             */
            Response.BAD_SESSION_ID.send(channel);
            return null;
        }
        int loginType = rsaBuffer.readByte();
        int[] keys = new int[]{rsaBuffer.readInt(), rsaBuffer.readInt(), rsaBuffer.readInt(), rsaBuffer.readInt()};
        int tfaCode = 0;
        boolean tfaTrust = false;
        int tfaTrustHash = 0;
        String email = "";
        if(loginType == 1 || loginType == 3) {
            /**
             * Login attempt with 2fa code
             */
            tfaCode = rsaBuffer.readMedium();
            tfaTrust = loginType == 1;
            rsaBuffer.skip(5);
        } else if(loginType == 0) {
            /**
             * Login attempt with a 2fa trust key
             */
            tfaTrustHash = rsaBuffer.readInt();
            rsaBuffer.skip(4);
        } else if(loginType == 2){
            /**
             * No code no trust key
             */
            rsaBuffer.skip(8);
        } else {
            /**
             * Login attempt with email
             */
            email = rsaBuffer.readString();
        }
        String password = rsaBuffer.readString();

        byte[] bytes = new byte[in.remaining()];
        in.readBytes(bytes);
        in = new InBuffer(bytes);
        in.decode(keys, 0, bytes.length);
        String name = in.readString();
        if(name == null || name.length() > 12 || (name = name.trim()).isEmpty() || password == null || (password = password.trim()).isEmpty()) {
            /**
             * Invalid login
             */
            Response.INVALID_LOGIN.send(channel);
            return null;
        }
        int idk1 = in.readUnsignedByte();
        boolean someBool1 = (idk1 >> 1) == 1;
        boolean someBool2 = (idk1 & 0xff) == 1;
        int someSize1 = in.readUnsignedShort();
        int someSize2 = in.readUnsignedShort();

        in.skip(24); //not 100% sure what this is

        String someString = in.readString(); //from params
        int someInt = in.readInt(); //from params

        /* device info */
        in.readByte(); //always 6
        int osType = in.readUnsignedByte();
        int bit64 = in.readUnsignedByte();
        int osVersionType = in.readUnsignedByte();
        //there is more, but we don't need it.

        /* skipping lots of data */

        if(tfaTrustHash != 0) {
            /**
             * Check if 2fa trust hash matches
             */
            int osTypeStored = (tfaTrustHash >> 24) & 0xff;
            int bit64Stored = (tfaTrustHash >> 16) & 0xff;
            int osVersionTypeStored = (tfaTrustHash >> 8) & 0xff;
            if(osTypeStored != osType || bit64Stored != bit64 || osVersionTypeStored != osVersionType)
                tfaTrustHash = 0; //device doesn't match up
        } else if(tfaTrust) {
            /**
             * Create a new 2fa trust hash
             */
            tfaTrustHash = osType << 24 | bit64 << 16 | osVersionType << 8 | Random.get(1, 254);
        }

        return new LoginInfo(channel, name, password, email, tfaCode, tfaTrust, tfaTrustHash, worldId, keys);
    }

    /**
     * Checks if the {@code channel}'s remote ip address has 2 or more players already in the wilderness.
     */
    private static boolean isAtWildernessLimitForIP(Channel channel) {
        int ip = IPAddress.toInt(IPAddress.get(channel)),
            count = 0;

        for (Player player : Wilderness.players) {
            if (player.getIpInt() == ip && ++count >= 2) {
                return true;
            }
        }

        return false;
    }

}