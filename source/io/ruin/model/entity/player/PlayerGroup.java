package io.ruin.model.entity.player;

import io.ruin.api.utils.XenPost;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Descending order from highest priority group
 */
public enum PlayerGroup {

    /* Staff */
    OWNER(23, 28, 1),
    COMMUNITY_MANAGER(28, 21, 127),
    ADMINISTRATOR(3, 22, 124),
    DEVELOPER(13, 13, 1),
    DISCORD_MANAGER(26, 26, 125),
    ADVISOR(25, 23, 130),
    SENIOR_MODERATOR(17, 22, 28),
    MODERATOR(4, 24, 0),
    SUPPORT(5, 25, 131),

    /* Special Accounts */
    YOUTUBER(15, 16, 29),
    BETA_TESTER(14, 15, 92),
    DICE_HOST(22, 18, 119), // TODO should be ironman?

    /* Donators */
    GODLIKE_DONATOR(9, 11, 25),
    UBER_DONATOR(6, 10, 24),
    LEGENDARY_DONATOR(11, 9, 23),
    EXTREME_DONATOR(10, 8, 22),
    SUPER_DONATOR(12, 7, 21),
    DONATOR(7, 6, 20),

    /* Standard */
    CLASSIC_MODE(30, 29, 132),
    REGISTERED(2, 0, -1),
    BANNED(31, 0, -1);

    public final int id;

    public final int clientId;

    public final int clientImgId;

    PlayerGroup(int id, int clientId, int clientImgId) {
        this.id = id;
        this.clientId = clientId;
        this.clientImgId = clientImgId;
    }

    public void sync(Player player, String type) {
        sync(player, type, null);
    }

    public void sync(Player player, String type, Runnable successAction) {
        CompletableFuture.runAsync(() -> {
            Map<Object, Object> map = new HashMap<>();
            map.put("userId", player.getUserId());
            map.put("type", type);
            map.put("groupId", id);
            String result = XenPost.post("add_group", map);
            if(successAction != null && "1".equals(result))
                successAction.run();
        });
    }

    public String tag() {
        return "<img=" + clientImgId + ">";
    }

    public static final PlayerGroup[] GROUPS_BY_ID;

    static {
        int highestGroupId = 0;
        for(PlayerGroup group : values()) {
            if(group.id > highestGroupId)
                highestGroupId = group.id;
        }
        GROUPS_BY_ID = new PlayerGroup[highestGroupId + 1];
        for(PlayerGroup group : values())
            GROUPS_BY_ID[group.id] = group;
    }

}