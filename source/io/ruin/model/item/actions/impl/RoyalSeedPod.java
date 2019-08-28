package io.ruin.model.item.actions.impl;

import io.ruin.model.World;
import io.ruin.model.entity.shared.LockType;
import io.ruin.model.inter.dialogue.MessageDialogue;
import io.ruin.model.item.actions.ItemAction;
import io.ruin.model.map.Position;
import io.ruin.model.skills.magic.spells.HomeTeleport;
import io.ruin.model.stat.StatType;

public class RoyalSeedPod {

    static {
        ItemAction.registerInventory(19564, "commune", (player, item) -> {
            player.getMovement().startTeleport(30, event -> {
                player.lock(LockType.FULL_NULLIFY_DAMAGE);
                player.graphics(767);
                player.animate(4544);
                event.delay(3);
                player.getAppearance().setNpcId(716);
                event.delay(3);
                Position override = HomeTeleport.getHomeTeleportOverride(player);
                if (override == null) {
                    player.getMovement().teleport(3096, 3487, 0);
                } else {
                    player.getMovement().teleport(override);
                }
                event.delay(2);
                player.graphics(769);
                event.delay(2);
                player.getAppearance().setNpcId(-1);
                if(World.isEco()) {
                    player.getStats().get(StatType.Farming).drain(5);
                    player.dialogue(new MessageDialogue("Plants seem hostile to you for killing seeds from the Grand Tree. <br>Your current farming level has been reduced by 5."));
                }
                player.unlock();
            });
        });
    }

}