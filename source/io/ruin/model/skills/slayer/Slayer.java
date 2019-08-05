package io.ruin.model.skills.slayer;

import io.ruin.api.utils.Random;
import io.ruin.cache.ItemDef;
import io.ruin.model.World;
import io.ruin.model.entity.npc.NPC;
import io.ruin.model.entity.player.Player;
import io.ruin.model.entity.player.PlayerCounter;
import io.ruin.model.inter.utils.Config;
import io.ruin.model.item.containers.Equipment;
import io.ruin.model.stat.StatType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Slayer {

    static {
        Arrays.stream(ItemDef.LOADED).filter(def -> def.name.toLowerCase().contains("slayer helm")).forEach(def -> def.slayerHelm = true);
    }

    public static void reset(Player player) {
        player.slayerTask = null;
        player.slayerTaskName = null;
        player.slayerTaskRemaining = 0;
        player.slayerTaskDangerous = false;
        player.slayerTaskAmountAssigned = 0;
    }

    public static void test(Player player) {
        List<SlayerTask> tasks = getPossibleTasks(player, SlayerTask.Type.HARD, false);
        tasks.forEach(it -> {
            player.sendMessage(it.name + " " + it.level);
        });
    }

    private static List<SlayerTask> getPossibleTasks(Player player, SlayerTask.Type type, Boolean preferWilderness) {
        return Arrays.stream(type.tasks)
                .filter(task -> player.getStats().get(StatType.Slayer).fixedLevel >= task.level) // has level
                .filter(task -> !task.disable) // task not disabled
                .filter(task -> task.additionalRequirement == null || task.additionalRequirement.test(player)) // has additional req
                .filter(task -> task.unlockConfig == null || task.unlockConfig.get(player) == 1) // unlocked (if required)
                .filter(task -> preferWilderness ? task.wildernessSpawns > 0 : task.mainSpawns > 0) // has relevant spawns
                .filter(task -> !isBlockedTask(player, task))
                .collect(Collectors.toList());
    }

    public static void set(Player player, SlayerTask.Type type, Boolean preferWilderness) {
        List<SlayerTask> tasks = getPossibleTasks(player, type, preferWilderness);
        int totalWeight = tasks.stream().mapToInt(task -> task.weight).sum();
        SlayerTask task = null;
        int roll = Random.get(totalWeight);
        for (SlayerTask t : tasks) {
            roll -= t.weight;
            if (roll <= 0) {
                task = t;
                break;
            }
        }
        if (task == null) { // should never happen, but just in case..
            task = Random.get(tasks);
        }
        player.slayerTask = task;
        player.slayerTaskName = task.name;
        int taskAmount = task.type[0] == SlayerTask.Type.BOSS ? -1 : Random.get(task.min, task.max);
        player.slayerTaskRemaining = taskAmount;
        player.slayerTaskAmountAssigned = taskAmount;
        if (task.extensionConfig != null && task.extensionConfig.get(player) == 1)
            player.slayerTaskRemaining *= 1.35;
        if(World.isPVP() && preferWilderness)
            player.slayerTaskDangerous = true;
    }

    public static SlayerTask getTask(Player player) {
        if(player.slayerTask != null)
            return player.slayerTask;
        if(player.slayerTaskName == null)
            return null;
        SlayerTask task = SlayerTask.TASKS.get(player.slayerTaskName);
        if(task == null) {
            reset(player);
            return null;
        }
        return player.slayerTask = task;
    }

    public static boolean hasSlayerHelmEquipped(Player player) {
        ItemDef def = player.getEquipment().getDef(Equipment.SLOT_HAT);
        return def != null && def.slayerHelm;
    }

    public static void onNPCKill(Player player, NPC npc) {
        if (isTask(player, npc)) {
            player.slayerTaskRemaining--;
            player.getStats().addXp(StatType.Slayer, npc.getCombat().getInfo().slayer_xp, true);
            if (player.slayerTaskRemaining <= 0) {
                finishTask(player);
            }
        }
    }

    public static boolean isTask(Player player, NPC npc) {
        SlayerTask playerTask = getTask(player);
        if (playerTask == null || npc.getCombat().getInfo().slayerTasks == null) {
            return false;
        }
        for (SlayerTask task : npc.getCombat().getInfo().slayerTasks) {
            if (task == playerTask)
                return true;
        }
        return false;
    }

    private static void finishTask(Player player) {
        SlayerTask task = getTask(player);
        PlayerCounter.SLAYER_TASKS_COMPLETED.increment(player, 1);
        int reward = getPointsReward(task, player.slayerTasksCompleted);
        reward *= task.getHighestType().modifier; // TODO fix this
        Config.SLAYER_POINTS.set(player, reward + Config.SLAYER_POINTS.get(player));
        reset(player);
        int bloodMoneyReward = (player.slayerTaskDangerous && player.wildernessLevel > 0 ? 75 : 50) * player.slayerTaskAmountAssigned;
        if(World.isPVP())
            player.getInventory().addOrDrop(13307, bloodMoneyReward);
        player.sendMessage("Your slayer task is now complete."); // TODO add colors
        player.sendMessage("You've completed a total of " + PlayerCounter.SLAYER_TASKS_COMPLETED.get(player) + " tasks, earning " + reward + " points" +
                (World.isPVP() ? (" and " + bloodMoneyReward + " blood money") : "") +
                ". You now have a total of " + Config.SLAYER_POINTS.get(player) + " points.");
        if(World.isPVP()) {
            if(player.slayerSpree++ >= 4) {
                player.slayerSpree = 0;
                int bloodMoney = Random.get(2500, 5000);
                player.sendMessage("You've completed 5 tasks in a row and have been rewarded with " + bloodMoney + " blood money.");
                player.getInventory().addOrDrop(13307, bloodMoney);
            }
        }
    }

    public static int getPointsReward(SlayerTask task, int tasks) {
        int base = 30;
        if (tasks % 1000 == 0)
            return base * 50;
        else if (tasks % 250 == 0)
            return base * 35;
        else if (tasks % 100 == 0)
            return base * 25;
        else if (tasks % 50 == 0)
            return base * 15;
        else if (tasks % 10 == 0)
            return base * 5;
        return base;
    }


    static void sendTaskInfo(Player player) {
        SlayerTask task = getTask(player);
        if(task != null) {
            if(task.type[0] == SlayerTask.Type.BOSS) {
                Config.SLAYER_TASK_1.set(player, 98);
                Config.SLAYER_TASK_2.set(player, task.key);
            } else {
                Config.SLAYER_TASK_1.set(player, task.key);
            }
        }
        Config.SLAYER_TASK_AMOUNT.set(player, player.slayerTaskRemaining);
    }

    public static void sendRewardInfo(Player player) {
        Config.UNLOCK_BLOCK_TASK_SIX.set(player, 1);
        if(player.slayerBlockedTasks != null) {
            for(int i = 0; i < player.slayerBlockedTasks.size(); i++) {
                SlayerTask blocked = SlayerTask.TASKS.get(player.slayerBlockedTasks.get(i));
                Config.BLOCKED_TASKS[i].set(player, blocked != null ? blocked.key : 0);
            }
        }
    }

    public static boolean isBlockedTask(Player player, SlayerTask task) {
        if(player.slayerBlockedTasks != null) {
            for(int i = 0; i < player.slayerBlockedTasks.size(); i++) {
                SlayerTask blocked = SlayerTask.TASKS.get(player.slayerBlockedTasks.get(i));
                if(blocked == task) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void check(Player player) {
        SlayerTask task = Slayer.getTask(player);
        if (task == null) {
            player.sendMessage("You do not currently have a slayer assignment. Talk to Krystilia in Edgeville to receive one.");
        } else {
            player.sendMessage("Your current slayer assignment is " + task.name + ". Only " + player.slayerTaskRemaining + " left to go.");
        }
    }
}
