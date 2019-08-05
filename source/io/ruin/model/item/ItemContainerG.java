package io.ruin.model.item;

import com.google.gson.annotations.Expose;
import io.ruin.cache.ItemDef;
import io.ruin.model.entity.player.Player;

import java.util.ArrayList;
import java.util.function.Predicate;

public abstract class ItemContainerG<I extends Item> {

    @Expose protected I[] items;

    protected int itemCount;

    protected int updatedCount;

    protected boolean[] updatedSlots;

    public boolean sendAll;

    public Player player;

    public int interfaceHash;

    public int containerId;

    public boolean forceStack;

    protected abstract I newItem(int id, int amount, int uniqueValue);

    protected abstract I[] newArray(int size);

    public void init(Player player, int size, int parentId, int childId, int containerId, boolean forceStack) {
        if(items == null)
            items = newArray(size);
        else if(items.length != size) {
            I[] newItems = newArray(size);
            System.arraycopy(items, 0, newItems, 0, Math.min(items.length, newItems.length));
            items = newItems;
        }
        updatedSlots = new boolean[size];
        for(int slot = 0; slot < size; slot++) {
            Item item = items[slot];
            if(item != null) {
                itemCount++;
                item.setSlot(slot);
                item.setContainer(this);
            }
        }
        this.player = player;
        this.interfaceHash = parentId << 16 | childId;
        this.containerId = containerId;
        this.forceStack = forceStack;
    }

    public void init(int size, boolean forceStack) {
        init(null, size, -1, -1, -1, forceStack);
    }

    public void clear() {
        for(int slot = 0; slot < items.length; slot++)
            set(slot, null);
    }

    public boolean validateSlots(int... slots) {
        for(int slot : slots) {
            if(slot < 0 || slot >= items.length)
                return false;
        }
        return true;
    }

    public void set(int slot, I item) {
        Item pre = items[slot];
        if(item == null) {
            if(pre == null)
                return;
            pre.setContainer(null);
            items[slot] = null;
            itemCount--;
        } else {
            item.setSlot(slot);
            item.setContainer(this);
            items[slot] = item;
            if(pre == null)
                itemCount++;
        }
        update(slot);
    }

    public I get(int slot) {
        return items[slot];
    }

    public I getSafe(int slot) {
        if(slot < 0 || slot >= items.length)
            return null;
        return items[slot];
    }

    public I get(int slot, int itemId) {
        I item = getSafe(slot);
        if(item == null || item.getId() != itemId)
            return null;
        return item;
    }

    public I[] getItems() {
        return items;
    }

    public int getId(int slot) {
        Item item = items[slot];
        return item == null ? -1 : item.getId();
    }

    public ItemDef getDef(int slot) {
        I item = get(slot);
        return item == null ? null : item.getDef();
    }

    public int getFreeSlots() {
        return items.length - itemCount;
    }

    public int getCount() {
        return itemCount;
    }

    public boolean isEmpty() {
        return itemCount == 0;
    }

    public boolean isFull() {
        return getFreeSlots() == 0;
    }

    /**
     * Adding
     */

    public int add(I item) {
        return add(item.getId(), item.getAmount(), item.getUniqueValue());
    }

    public int add(int id, int amount) {
        return add(id, amount, 0);
    }

    public int add(int id, int amount, int uniqueValue) {
        if(amount <= 0) {
            System.err.println("Invalid add amount: " + amount + " | " + new Throwable().getStackTrace()[1].toString());
            return 0;
        }
        ItemDef def = ItemDef.get(id);
        if(def == null) {
            System.err.println("Failed to add non-existing item: " + id + " | " + new Throwable().getStackTrace()[1].toString());
            return 0;
        }
        boolean stack = false;
        if(uniqueValue == 0) {
            if(def.stackable) {
                /**
                 * Regular stackable item
                 */
                Item stackItem = findItem(id);
                if(stackItem != null) {
                    stackItem.incrementAmount(amount);
                    return amount;
                }
                stack = true;
            } else if(forceStack) {
                /**
                 * Not a stackable item
                 */
                Item stackItem = findItem(id);
                if(stackItem != null && stackItem.getUniqueValue() == 0) {
                    stackItem.incrementAmount(amount);
                    return amount;
                }
                stack = true;
            }
        }
        if(amount == 1 || stack) {
            int freeSlot = freeSlot();
            if(freeSlot == -1) {
                /**
                 * No free slot
                 */
                return 0;
            }
            set(freeSlot, newItem(id, amount, uniqueValue));
            return amount;
        }
        int added = 0;
        while(amount-- > 0) {
            int freeSlot = freeSlot();
            if(freeSlot == -1) {
                /**
                 * No free slot
                 */
                break;
            }
            set(freeSlot, newItem(id, 1, uniqueValue));
            added++;
        }
        return added;
    }

    /**
     * Removing
     */

    public int remove(int id, int amount) {
        return remove(id, amount, false);
    }

    /**
     * Removing
     */

    public int remove(int id, int amount, boolean acceptNoted) {
        if(amount <= 0) {
            System.err.println("Invalid remove amount: " + amount + " | " + new Throwable().getStackTrace()[1].toString());
            return 0;
        }
        ItemDef definition = ItemDef.get(id);
        if(definition == null) {
            System.err.println("Failed to remove non-existing item: " + id + " | " + new Throwable().getStackTrace()[1].toString());
            return 0;
        }
        int removed = 0;
        while(amount > 0) {
            Item stack = findItem(id);
            if (stack == null && acceptNoted && definition.notedId > 0) {
                stack = findItem(definition.notedId);
            }
            if (stack == null)
                break;
            int fromThisStack = Math.min(stack.getAmount(), amount);
            stack.incrementAmount(-fromThisStack);
            removed += fromThisStack;
            amount -= fromThisStack;
        }
        return removed;
    }

    /**
     * Mirroring
     */

    private int mirrorInterfaceHash, mirrorContainerId;

    public void mirror(int parentId, int childId, int containerId) {
        mirrorInterfaceHash = parentId << 16 | childId;
        mirrorContainerId = containerId;
    }

    /**
     * Updating
     */

    public void update(int slot) {
        if(updatedSlots[slot])
            return;
        updatedSlots[slot] = true;
        updatedCount++;
    }

    public boolean sendUpdates() {
        return sendUpdates(null);
    }

    public boolean sendUpdates(ItemContainerG mirrorContainer) {
        if(updatedCount == 0 && !sendAll)
            return false;
        if(sendAll) {
            if(player != null)
                player.getPacketSender().sendItems(interfaceHash, containerId, items, items.length);
            if(mirrorContainer != null)
                mirrorContainer.player.getPacketSender().sendItems(mirrorContainer.mirrorInterfaceHash, mirrorContainer.mirrorContainerId, items, items.length);
        } else {
            if(player != null)
                player.getPacketSender().updateItems(interfaceHash, containerId, items, updatedSlots, updatedCount);
            if(mirrorContainer != null)
                mirrorContainer.player.getPacketSender().updateItems(mirrorContainer.mirrorInterfaceHash, mirrorContainer.mirrorContainerId, items, updatedSlots, updatedCount);
        }
        for(int i = 0; i < updatedSlots.length; i++)
            updatedSlots[i] = false;
        updatedCount = 0;
        sendAll = false;
        return true;
    }

    public void send(Player player) {
        player.getPacketSender().sendItems(interfaceHash, containerId, items, items.length);
    }

    /**
     * Misc
     */

    public ArrayList<I> collectItems(int... ids) {
        ArrayList<I> list = new ArrayList<>(ids.length);
        for(I item : items) {
            if(item == null)
                continue;
            for(int id : ids) {
                if(item.getId() == id)
                    list.add(item);
            }
        }
        return list.isEmpty() ? null : list;
    }

    public ArrayList<I> collectOneOfEach(int... ids) {
        ArrayList<I> list = new ArrayList<>(ids.length);
        i: for(int id : ids) {
            for(I item : items) {
                if(item != null && item.getId() == id) {
                    list.add(item);
                    continue i;
                }
            }
        }
        return list.size() != ids.length ? null : list;
    }

    public ArrayList<I> findItems(int id, int amount) {
        ArrayList<I> list = new ArrayList<>(amount);
        for(I item : items) {
            if(item == null || item.getId() != id)
                continue;
            list.add(item);
            if(list.size() >= amount)
                break;
        }
        return list.size() < amount ? null : list;
    }

    public ArrayList<I> findItems(Predicate<Item> predicate) {
        ArrayList<I> list = new ArrayList<>();
        for(I item : items) {
            if(item != null && predicate.test(item))
                list.add(item);
        }
        return list;
    }

    public I findItem(int id) {
        return findItem(id, false);
    }

    public I findItem(int id, boolean acceptNoted) {
        for(I item : items) {
            if(item != null && (item.getId() == id || (acceptNoted && ItemDef.get(id).notedId != -1 && item.getId() == ItemDef.get(id).notedId)))
                return item;
        }
        return null;
    }

    public I findFirst(int... ids) {
        for(I item : items) {
            if(item == null)
                continue;
            for(int id : ids) {
                if(id == item.getId())
                    return item;
            }
        }
        return null;
    }

    public int freeSlot() {
        for(int slot = 0; slot < items.length; slot++) {
            if(items[slot] == null)
                return slot;
        }
        return -1;
    }

    public int getSlot(int id) {
        return getSlot(id, false);
    }

    public int getSlot(int id, boolean acceptNoted) {
        Item item = findItem(id, acceptNoted);
        return item == null ? -1 : item.getSlot();
    }

    public int getAmount(int id) {
        ItemDef definition = ItemDef.get(id);
        if(definition == null) {
            System.err.println("Failed to get amount for non-existing item: " + id + " | " + new Throwable().getStackTrace()[1].toString());
            return 0;
        }
        if(definition.stackable) {
            Item stacked = findItem(id);
            if(stacked != null)
                return stacked.getAmount();
            return 0;
        }
        return count(id);
    }

    public int count(int id) {
        return count(id, false);
    }

    public int count(int id, boolean acceptNoted) {
        int amount = 0;
        ItemDef def = ItemDef.get(id);
        for(Item item : items) {
            if (item == null)
                continue;
            if(item.getId() == id)
                amount += item.getAmount();
            else if (acceptNoted && def.notedId != -1 && item.getId() == def.notedId)
                amount += item.getAmount();
        }
        return amount;
    }

    public boolean contains(Item item) {
        return contains(item, false);
    }

    public boolean contains(Item item, boolean acceptNoted) {
        return contains(item.getId(), item.getAmount(), acceptNoted);
    }

    public boolean contains(int id, int amount) {
        return contains(id, amount, false);
    }

    public boolean contains(int id, int amount, boolean acceptNoted) {
        return count(id, acceptNoted) >= amount;
    }

    public boolean hasId(int id) {
        return findItem(id) != null;
    }

    public boolean hasItem(int id, int amount) {
        Item item = findItem(id);
        return item != null && item.getAmount() >= amount;
    }

    public boolean hasMultiple(int id) {
        int amount = 0;
        for(Item item : items) {
            if(item == null || item.getId() != id)
                continue;
            if((amount += item.getAmount()) > 1)
                return true;
        }
        return false;
    }

    public boolean hasMultiple(int... itemIds) {
        int[] amounts = new int[itemIds.length];
        int multiples = 0;
        for(Item item : items) {
            if(item == null)
                continue;
            for(int i = 0; i < itemIds.length; i++) {
                if(itemIds[i] != item.getId())
                    continue;
                if(amounts[i] > 1 || (amounts[i] += item.getAmount()) < 2)
                    continue;
                if(++multiples >= itemIds.length)
                    return true;
            }
        }
        return false;
    }

    public boolean hasRoomFor(int id) {
        ItemDef def = ItemDef.get(id);
        return getFreeSlots() > 0 || (def != null && def.stackable && hasId(id));
    }

    public boolean hasFreeSlots(int slots) {
        return getFreeSlots() >= slots;
    }

    public boolean containsAll(boolean acceptNoted, I... items) {
        for (Item item: items) {
            if (!contains(item, acceptNoted))
                return false;
        }
        return true;
    }

    public int removeAll(boolean acceptNoted, I... items) {
        int removed = 0;
        for (Item item: items)
            removed += remove(item.getId(), item.getAmount(), acceptNoted);
        return removed;

    }

}