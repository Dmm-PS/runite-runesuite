package io.ruin.model.inter.handlers;

import io.ruin.model.entity.player.Player;
import io.ruin.model.inter.Interface;
import io.ruin.model.inter.InterfaceHandler;
import io.ruin.model.inter.actions.SimpleAction;
import io.ruin.model.inter.actions.SlotAction;
import io.ruin.model.inter.utils.Config;
import io.ruin.model.item.containers.Equipment;
import io.ruin.model.skills.magic.SpellBook;
import io.ruin.model.skills.magic.spells.TargetSpell;

public class TabCombat {

    static {
        InterfaceHandler.register(Interface.COMBAT_OPTIONS, h -> {
            h.actions[3] = (SimpleAction) p -> p.getCombat().changeAttackSet(0);
            h.actions[7] = (SimpleAction) p -> p.getCombat().changeAttackSet(1);
            h.actions[11] = (SimpleAction) p -> p.getCombat().changeAttackSet(2);
            h.actions[15] = (SimpleAction) p -> p.getCombat().changeAttackSet(3);
            h.actions[20] = (SimpleAction) p -> openAutocast(p, true);
            h.actions[25] = (SimpleAction) p -> openAutocast(p, false);
            h.actions[29] = (SimpleAction) Config.AUTO_RETALIATE::toggle;
            h.actions[35] = (SimpleAction) p -> p.getCombat().toggleSpecial();
        });
        InterfaceHandler.register(Interface.AUTOCAST_SELECTION, h -> {
            h.actions[1] = (SlotAction) TabCombat::selectAutocast;
        });
    }

    private static void open(Player player, int interfaceId) {//meehhhh (Todo better interface positioning system..)
        int parentId = player.getGameFrameId(), childId;
        if(parentId == 548)
            childId = 66;
        else if(parentId == 164)
            childId = 67;
        else
            childId = 68;
        player.getPacketSender().sendInterface(interfaceId, parentId, childId, 1);
    }

    private static void openAutocast(Player player, boolean defensive) {
        Integer autocastId = getAutocastId(player);
        if(autocastId == null) {
            player.sendMessage("Your staff can't autocast with that spellbook.");
            return;
        }
        open(player, Interface.AUTOCAST_SELECTION);
        player.getPacketSender().sendAccessMask(Interface.AUTOCAST_SELECTION, 1, 0, 52, 2);
        Config.AUTOCAST_SET.set(player, autocastId);
        Config.DEFENSIVE_CAST.set(player, defensive ? 1 : 0);
    }

    public static void updateAutocast(Player player, boolean login) {
        if(login) {
            int index = Config.AUTOCAST.get(player);
            player.getCombat().autocastSpell = TargetSpell.AUTOCASTS[index];
        } else {
            if(player.isVisibleInterface(Interface.AUTOCAST_SELECTION))
                open(player, Interface.COMBAT_OPTIONS);
            resetAutocast(player);
        }
    }

    public static void resetAutocast(Player player) {
        if(player.getCombat().autocastSpell != null) {
            player.getCombat().autocastSpell = null;
            Config.AUTOCAST.set(player, 0);
            player.getCombat().updateCombatLevel();
        }
    }

    private static void selectAutocast(Player player, int slot) {
        if(slot < 0 || slot >= TargetSpell.AUTOCASTS.length)
            return;
        if(slot != 0) {
            player.getCombat().autocastSpell = TargetSpell.AUTOCASTS[slot];
            Config.AUTOCAST.set(player, slot);
        }
        open(player, Interface.COMBAT_OPTIONS);
        player.getCombat().updateWeapon(true);
        player.getCombat().updateCombatLevel();
    }

    private static Integer getAutocastId(Player player) {
        int staffId = player.getEquipment().getId(Equipment.SLOT_WEAPON);
        if(staffId == -1) //shouldn't happen
            return null;
        if(staffId == 4675) //ancient staff
            return SpellBook.ANCIENT.isActive(player) ? 4675 : null;
        if(staffId == 6914) //master wand
            return SpellBook.ANCIENT.isActive(player) ? 4675 : staffId;
        if(staffId == 11791 || staffId == 12904) //staff of the dead
            return SpellBook.MODERN.isActive(player) ? 11791 : null;
        if(staffId == 21006 && SpellBook.ANCIENT.isActive(player)) //kodai wand
            return 4675;
        if(staffId == 22296) //staff of light
            return SpellBook.MODERN.isActive(player) ? 22296 : null;
        return SpellBook.MODERN.isActive(player) ? -1 : null;
    }

}