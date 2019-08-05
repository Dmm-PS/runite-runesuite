package io.ruin.model.combat.special.melee;

import io.ruin.cache.ItemDef;
import io.ruin.model.combat.AttackStyle;
import io.ruin.model.combat.AttackType;
import io.ruin.model.combat.Hit;
import io.ruin.model.combat.special.Special;
import io.ruin.model.entity.Entity;
import io.ruin.model.entity.player.Player;

//The Vesta's longsword special attack, Feint, costs 25% of the wielder's special attack energy.
//Upon a successful hit it will deal anywhere between 20% and 120% of the wielder's standard max hit, the accuracy of this special attack is rolled against 25% of the opponent's defence.
public class VestasLongsword implements Special {

    @Override
    public boolean accept(ItemDef def, String name) {
        return name.contains("vesta's longsword");
    }

    @Override
    public boolean handle(Player player, Entity target, AttackStyle attackStyle, AttackType attackType, int maxDamage) {
        player.animate(7515);
        //can't find the gfx (if there is one)
        target.hit(new Hit(player, attackStyle, attackType).randDamage((int)(maxDamage * 0.20), (int) (maxDamage * 1.20)));
        return true;
    }

    @Override
    public int getDrainAmount() {
        return 25;
    }

}