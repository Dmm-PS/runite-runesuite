package io.ruin.network.incoming.handlers;

import io.ruin.api.buffer.InBuffer;
import io.ruin.model.World;
import io.ruin.model.entity.Entity;
import io.ruin.model.entity.player.Player;
import io.ruin.model.inter.InterfaceAction;
import io.ruin.model.inter.InterfaceHandler;
import io.ruin.model.map.route.routes.TargetRoute;
import io.ruin.network.incoming.Incoming;
import io.ruin.utility.IdHolder;

public class InterfaceOnEntityHandler {

    @IdHolder(ids = {54})
    public static final class ItemOnPlayer implements Incoming {

        @Override
        public void handle(Player player, InBuffer in, int opcode) {
            int slot = in.readLEShortA();
            int itemId = in.readLEShort();
            int interfaceHash = in.readInt1();
            int ctrlRun = in.readByteS();
            int targetIndex = in.readShort();
            handleAction(player, World.getPlayer(targetIndex), interfaceHash, slot, itemId, ctrlRun);
        }

    }

    @IdHolder(ids = {77})
    public static final class InterfaceOnPlayer implements Incoming {

        @Override
        public void handle(Player player, InBuffer in, int opcode) {
            int interfaceHash = in.readLEInt();
            int targetIndex = in.readLEShort();
            int slot = in.readShortA();
            int ctrlRun = in.readByteA();
            handleAction(player, World.getPlayer(targetIndex), interfaceHash, slot, -1, ctrlRun);
        }

    }

    @IdHolder(ids = {43})
    public static final class ItemOnNpc implements Incoming {

        @Override
        public void handle(Player player, InBuffer in, int opcode) {
            int slot = in.readUnsignedShort();
            int ctrlRun = in.readByteC();
            int targetIndex = in.readUnsignedLEShort();
            int itemId = in.readUnsignedShortA();
            int interfaceHash = in.readInt2();
            handleAction(player, World.getNpc(targetIndex), interfaceHash, slot, itemId, ctrlRun);
        }

    }

    @IdHolder(ids = {0})
    public static final class InterfaceOnNpc implements Incoming {

        @Override
        public void handle(Player player, InBuffer in, int opcode) {
            int interfaceHash = in.readInt();
            int slot = in.readShortA();
            int targetIndex = in.readLEShort();
            int ctrlRun = in.readByte();
            handleAction(player, World.getNpc(targetIndex), interfaceHash, slot, -1, ctrlRun);
        }

    }

    protected static void handleAction(Player player, Entity target, int interfaceHash, int slot, int itemId, int ctrlRun) {
        if(target == null || player.isLocked())
            return;
        player.resetActions(true, true, true);
        player.face(target);
        player.getMovement().setCtrlRun(ctrlRun == 1);
        if(itemId == -1)
            action(player, target, interfaceHash, slot, itemId);
        else
            TargetRoute.set(player, target, () -> action(player, target, interfaceHash, slot, itemId));
    }

    private static void action(Player player, Entity target, int interfaceHash, int slot, int itemId) {
        InterfaceAction action = InterfaceHandler.getAction(player, interfaceHash);
        if(action == null)
            return;
        if(slot == 65535)
            slot = -1;
        if(itemId == 65535)
            itemId = -1;
        action.handleOnEntity(player, target, slot, itemId);
    }

}