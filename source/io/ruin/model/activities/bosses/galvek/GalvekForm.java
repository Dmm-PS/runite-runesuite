package io.ruin.model.activities.bosses.galvek;

/**
 * An enumerated type whose elements correspond to each form in a Galvek fight.
 *
 * @author Andys1814
 */
public enum GalvekForm {
    FIRE("fire", 8095, 0, 0, 7901),
    WIND("wind", 8097, -12, -12, 7904),
    WATER("water", 8096, 23, 0, 7904),
    EARTH("earth", 8098, -11, 0, 7901);

    public final String element;
    public final int npcId;
    public final int xOffset;
    public final int yOffset;
    public final int basicAttackAnimation;

    GalvekForm(String element, int npcId, int xOffset, int yOffset, int basicAttackAnimation) {
        this.element = element;
        this.npcId = npcId;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.basicAttackAnimation = basicAttackAnimation;
    }
}
