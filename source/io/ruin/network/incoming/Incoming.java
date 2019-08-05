package io.ruin.network.incoming;

import io.ruin.api.buffer.InBuffer;
import io.ruin.api.utils.PackageLoader;
import io.ruin.model.entity.player.Player;
import io.ruin.utility.IdHolder;

public interface Incoming {

    Incoming[] HANDLERS = new Incoming[256];

    int[] OPTIONS = new int[256];

    boolean[] IGNORED = new boolean[256];

    int[] SIZES = new int[256];

    static void load() throws Exception {
        for(Class c : PackageLoader.load("io.ruin.network.incoming.handlers", Incoming.class)) {
            Incoming incoming = (Incoming) c.newInstance();
            IdHolder idHolder = (IdHolder) c.getAnnotation(IdHolder.class);
            if(idHolder == null) {
                /* handler is disabled, most likely for upgrading */
                continue;
            }
            int option = 1;
            for(int id : idHolder.ids()) {
                HANDLERS[id] = incoming;
                OPTIONS[id] = option++;
            }
        }
        /**
         * Ignored
         */
        int[] ignored = {
                76,     //ping
                93,     //something with screen?
                62,     //screen minimized/restored
                83,     //key pressed
                50,     //camera movement with arrow keys
                53,     //click
                17,     //idle

        };
        for(int opcode : ignored)
            IGNORED[opcode] = true;
        /**
         * Sizes
         */
        for(int i = 0; i < SIZES.length; i++)
            SIZES[i] = Byte.MIN_VALUE;
        SIZES[0] = 9;
        SIZES[1] = -1;
        SIZES[2] = 3;
        SIZES[3] = -1;
        SIZES[4] = 7;
        SIZES[5] = 2;
        SIZES[6] = -1;
        SIZES[7] = 0;
        SIZES[8] = 9;
        SIZES[9] = 8;
        SIZES[10] = -1;
        SIZES[11] = 8;
        SIZES[12] = 7;
        SIZES[13] = 4;
        SIZES[14] = 8;
        SIZES[15] = 4;
        SIZES[16] = 7;
        SIZES[17] = 0;
        SIZES[18] = -1;
        SIZES[19] = -2;
        SIZES[20] = 8;
        SIZES[21] = 3;
        SIZES[22] = 8;
        SIZES[23] = 8;
        SIZES[24] = 7;
        SIZES[25] = 8;
        SIZES[26] = 7;
        SIZES[27] = 15;
        SIZES[28] = 3;
        SIZES[29] = 16;
        SIZES[30] = 2;
        SIZES[31] = 8;
        SIZES[32] = -2;
        SIZES[33] = 2;
        SIZES[34] = 7;
        SIZES[35] = 9;
        SIZES[36] = 7;
        SIZES[37] = 13;
        SIZES[38] = 13;
        SIZES[39] = 8;
        SIZES[40] = 8;
        SIZES[41] = 10;
        SIZES[42] = 8;
        SIZES[43] = 11;
        SIZES[44] = -1;
        SIZES[45] = 8;
        SIZES[46] = 3;
        SIZES[47] = 7;
        SIZES[48] = -1;
        SIZES[49] = 14;
        SIZES[50] = 4;
        SIZES[51] = 7;
        SIZES[52] = 4;
        SIZES[53] = 6;
        SIZES[54] = 11;
        SIZES[55] = 13;
        SIZES[56] = 8;
        SIZES[57] = 3;
        SIZES[58] = 8;
        SIZES[59] = -1;
        SIZES[60] = 16;
        SIZES[61] = 16;
        SIZES[62] = 1;
        SIZES[63] = 8;
        SIZES[64] = 15;
        SIZES[65] = 8;
        SIZES[66] = -1;
        SIZES[67] = 8;
        SIZES[68] = -1;
        SIZES[69] = 0;
        SIZES[70] = 0;
        SIZES[71] = 3;
        SIZES[72] = 3;
        SIZES[73] = 3;
        SIZES[74] = -1;
        SIZES[75] = -1;
        SIZES[76] = 0;
        SIZES[77] = 9;
        SIZES[78] = 8;
        SIZES[79] = 7;
        SIZES[80] = 5;
        SIZES[81] = 2;
        SIZES[82] = 3;
        SIZES[83] = -2;
        SIZES[84] = -1;
        SIZES[85] = 3;
        SIZES[86] = -1;
        SIZES[87] = 3;
        SIZES[88] = 3;
        SIZES[89] = 3;
        SIZES[90] = -1;
        SIZES[91] = 8;
        SIZES[92] = 6;
        SIZES[93] = -1;
        SIZES[94] = 3;
        SIZES[95] = 4;
        SIZES[96] = 8;
        SIZES[97] = -1;
    }

    /**
     * Separator
     */

    void handle(Player player, InBuffer in, int opcode);

}
