package io.ruin.model.skills.herblore;

import io.ruin.model.item.actions.ItemItemAction;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PoisonableWeapon {
    DRAGON_DAGGER(1215, 1231, 5680, 5698, "dagger"),
    ABYSSAL_DAGGER(13265, 13267, 13269, 13271, "dagger");

    private static final PoisonableWeapon[] WEAPONS = values();

    private static final int WEAPON_POISON = 187;
    private static final int WEAPON_POISON_PLUS = 5937;
    private static final int WEAPON_POISON_PLUS_PLUS = 5940;

    private final int unpoisoned;
    private final int poisoned;
    private final int poisonedPlus;
    private final int poisonedPlusPlus;
    private final String message;

    static {
        for (PoisonableWeapon weapon : WEAPONS) {
            ItemItemAction.register(WEAPON_POISON, weapon.unpoisoned, ((player, primary, secondary) -> {
                primary.setId(229);
                secondary.setId(weapon.poisoned);
                player.sendMessage("You poison the " + weapon.message + ".");
            }));

            ItemItemAction.register(WEAPON_POISON_PLUS, weapon.unpoisoned, ((player, primary, secondary) -> {
                primary.setId(229);
                secondary.setId(weapon.poisonedPlus);
                player.sendMessage("You poison the " + weapon.message + ".");
            }));

            ItemItemAction.register(WEAPON_POISON_PLUS_PLUS, weapon.unpoisoned, ((player, primary, secondary) -> {
                primary.setId(229);
                secondary.setId(weapon.poisonedPlusPlus);
                player.sendMessage("You poison the " + weapon.message + ".");
            }));

        }
    }
}
