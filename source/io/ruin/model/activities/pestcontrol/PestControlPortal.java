package io.ruin.model.activities.pestcontrol;

import io.ruin.cache.NPCDef;
import io.ruin.data.impl.npcs.npc_combat;
import io.ruin.model.activities.miscpvm.PassiveCombat;
import io.ruin.model.map.Position;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * An enumerated type whose elements correspond to each of the in-game portal types.
 *
 * @author Andys1814
 */
@Getter @RequiredArgsConstructor
public enum PestControlPortal {
    WEST("western", "purple", 1743, new Position(2628, 2591, 0), new Position(2631, 2592, 0), 27, 23),
    EAST("eastern", "blue", 1744, new Position(2680, 2588, 0), new Position(2679, 2589, 0), 29, 24),
    SOUTHEAST("southeastern", "yellow", 1745, new Position(2669, 2570, 0), new Position(2670, 2573, 0), 31, 25),
    SOUTHWEST("southwestern", "red", 1746, new Position(2645, 2569, 0), new Position(2646, 2572, 0), 33, 26);

    private final String name;
    private final String color;
    private final int npcId;
    private final Position position;
    private final Position pestSpawnPosition;
    private final int shieldIconChildId;
    private final int healthChildId;

//    PestControlPortal(String name, String color, int npcId, Position position, Position pestSpawnPosition, int shieldIconChildId, int healthChildId) {
//        this.name = name;
//        this.color = color;
//        this.npcId = npcId;
//        this.position = position;
//        this.pestSpawnPosition = pestSpawnPosition;
//        this.shieldIconChildId = shieldIconChildId;
//        this.healthChildId = healthChildId;
//    }

    public static final PestControlPortal[] VALUES = values();

    static {
        for (PestControlPortal portal : VALUES) {
            NPCDef def = NPCDef.get(portal.npcId);
            def.combatHandlerClass = PassiveCombat.class;
            def.combatInfo = new npc_combat.Info();
            def.combatInfo.hitpoints = PestControlLander.NOVICE.portalHitpoints;
            def.ignoreMultiCheck = true;
            def.combatInfo.defend_animation = -1;
            def.combatInfo.spawn_animation = -1;
//
//            NPCDef def2 = NPCDef.get(portal.npcId - 4);
//            def2.combatHandlerClass = PassiveCombat.class;
//            def2.combatInfo = new npc_combat.Info();
//            def2.combatInfo.hitpoints = PestControlLander.NOVICE.portalHitpoints;
//            def2.ignoreMultiCheck = true;
//            def2.combatInfo.defend_animation = -1;
//            def2.combatInfo.spawn_animation = -1;
        }
    }

}
