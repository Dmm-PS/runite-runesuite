package io.ruin.model.activities.lastmanstanding;

import io.ruin.model.inter.journal.presets.Preset;
import io.ruin.model.item.Item;
import io.ruin.model.item.containers.Equipment;
import io.ruin.model.skills.magic.SpellBook;

public final class LastManStandingPreset extends Preset {

    public LastManStandingPreset() {
        free = true;
        name = "Last Man Standing";
        attack = level(99);
        strength = level(99);
        defence = level(99);
        ranged = level(99);
        prayer = level(99);
        magic = level(99);
        hitpoints = level(99);
        spellBook = SpellBook.ANCIENT;
        invItems[0] = new Item(BLACK_DRAGONHIDE_BODY, 1);
        invItems[1] = new Item(4093, 1);
        invItems[2] = new Item(SPIRIT_SHIELD, 1);
        invItems[3] = new Item(ANCIENT_STAFF, 1);
        invItems[4] = new Item(RUNE_CROSSBOW, 1);
        invItems[5] = new Item(DRAGON_DAGGER, 1);
        invItems[6] = new Item(AVAS_ACCUMULATOR, 1);
        invItems[7] = new Item(SARADOMIN_BREW_4, 1);
        invItems[8] = new Item(SUPER_RESTORE_POTION_4, 1);
        invItems[9] = new Item(SANFEW_SERUM, 1);
        invItems[10] = new Item(RANGING_POTION_4, 1);
        invItems[11] = new Item(SUPER_COMBAT_POTION, 1);
        invItems[12] = new Item(12625, 1);
        invItems[13] = new Item(SHARK, 1);
        invItems[14] = new Item(SHARK, 1);
        invItems[15] = new Item(SHARK, 1);
        invItems[16] = new Item(SHARK, 1);
        invItems[17] = new Item(SHARK, 1);
        invItems[18] = new Item(SHARK, 1);
        invItems[19] = new Item(SHARK, 1);
        invItems[20] = new Item(SHARK, 1);
        invItems[21] = new Item(SHARK, 1);
        invItems[22] = new Item(SHARK, 1);
        invItems[23] = new Item(SHARK, 1);
        invItems[24] = new Item(SHARK, 1);
        invItems[25] = new Item(COOKED_KARAMBWAN, 1);
        invItems[26] = new Item(COOKED_KARAMBWAN, 1);
        invItems[27] = new Item(12791, 1);
        equipItems[Equipment.SLOT_HAT] = new Item(HELM_OF_NEITIZNOT, 1);
        equipItems[Equipment.SLOT_WEAPON] = new Item(ABYSSAL_WHIP, 1);
        equipItems[Equipment.SLOT_AMULET] = new Item(AMULET_OF_GLORY_6, 1);
        equipItems[Equipment.SLOT_SHIELD] = new Item(DRAGON_DEFENDER, 1);
        equipItems[Equipment.SLOT_FEET] = new Item(CLIMBING_BOOTS, 1);
        equipItems[Equipment.SLOT_RING] = new Item(BERSERKER_RING_IMBUE, 1);
        equipItems[Equipment.SLOT_HANDS] = new Item(BARROWS_GLOVES, 1);
        equipItems[Equipment.SLOT_LEGS] = new Item(RUNE_PLATELEGS, 1);
        equipItems[Equipment.SLOT_CAPE] = new Item(21793, 1);
        equipItems[Equipment.SLOT_CHEST] = new Item(4091, 1);
    }
}