package io.ruin.model.item.containers.shop;

import io.ruin.model.entity.player.Player;
import io.ruin.model.item.Item;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum ShopCurrency {

    COINS(995),
    TOKKUL(6529),
    BONDS(13190),
    BLOOD_MONEY(13307),
    CREDITS(13190),
    VOTE_TICKETS(4067),
    WARRIOR_GUILD_TOKEN(8851),
    //SLAYER_POINTS(p -> p.slayerPoints, (p, amt) -> p.slayerPoints -= amt),
    MARKS_OF_GRACE(11849),
    UNIDENTIFIED_MINERALS(21341),
    MAGE_ARENA_POINTS(p -> p.mageArenaPoints, (p, amt) -> p.mageArenaPoints -= amt),
    GOLDEN_NUGGETS(12012),
    TOURNAMENT_TICKETS(5023),
    WILDERNESS_POINTS(p -> p.wildernessPoints, (p, amt) -> p.wildernessPoints -= amt),
    TASK_POINTS(p -> p.dailyTaskPoints, (p, amt) -> p.dailyTaskPoints -= amt),
    SNOWBALL_POINTS(p -> p.snowballPoints, (p, amt) -> p.snowballPoints -= amt),
    LOYALTY_POINTS(p -> p.appreciationPoints, (p, amt) -> p.appreciationPoints -= amt),
    VOID_KNIGHT_COMMENDATION_POINTS(p -> p.voidKnightCommendationPoints, (p, amt) -> p.voidKnightCommendationPoints -= amt),
    MOLCH_PEARLS(22820),
    EASTER_EGGS(11028);

    private final Function<Player, Integer> amountAction;

    private final BiConsumer<Player, Integer> removeAction;

    public final String name;

    ShopCurrency(int itemId) { //only supports stackables!
        this(p -> {
                    Item item = p.getInventory().findItem(itemId);
                    return item == null ? 0 : item.getAmount();
                },
                (p, amt) -> {
                    Item item = p.getInventory().findItem(itemId);
                    item.remove(amt); //item should NEVER be null!
                }
        );
    }

    ShopCurrency(Function<Player, Integer> getAction, BiConsumer<Player, Integer> removeAction) {
        this.amountAction = getAction;
        this.removeAction = removeAction;
        this.name = name().toLowerCase().replace("_", " ");
    }

    public int getAmount(Player player) {
        return amountAction.apply(player);
    }

    public void remove(Player player, int amount) {
        removeAction.accept(player, amount);
    }

}