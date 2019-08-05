import io.ruin.Server;
import io.ruin.api.database.Database;
import io.ruin.api.database.DatabaseUtils;
import io.ruin.api.filestore.FileStore;
import io.ruin.api.utils.FileUtils;
import io.ruin.api.utils.JsonUtils;
import io.ruin.api.utils.NumberUtils;
import io.ruin.api.utils.ServerWrapper;
import io.ruin.cache.ItemDef;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class Test2 {

    public static void main(String[] args) throws IOException {
        ServerWrapper.init(Server.class);
        System.out.println("Loading server settings...");
        Properties properties = new Properties();
        try(InputStream in = new FileInputStream(Server.getPath() + File.separator + "server.properties")) {
            properties.load(in);
        } catch(IOException e) {
            System.out.println("Failed to load server settings!");
            return;
        }
        System.out.println("loading items");
        Server.fileStore = new FileStore(properties.getProperty("cache_path"));
        Server.dataFolder = FileUtils.get(properties.getProperty("data_path"));
        ItemDef.load();
        System.out.println("Connecting to SQL databases...");
        DatabaseUtils.connect(new Database[]{Server.gameDb}, errors -> {
            if(!errors.isEmpty()) {
                for(Throwable t : errors)
                    System.out.println(t.getMessage());
                System.exit(1);
            }
        });
        System.out.println("Connected");
        Map<String, Integer> totalAmount = new HashMap<>();
        Map<String, Integer> totalKills = new HashMap<>();
        AtomicInteger totalBM = new AtomicInteger();
        Server.gameDb.execute(connection -> {
            ResultSet rs = connection.createStatement().executeQuery("SELECT `user_name`, `killer_name`, `user_ip`, `killer_ip`, `items_lost`, `items_kept` FROM logs_dangerous_deaths WHERE killer_id != -1 AND `items_lost` LIKE '%Blood money%' AND `world_stage` LIKE 'LIVE'");
            while (rs.next()) {
                LogItem[] lost = JsonUtils.GSON.fromJson(rs.getString("items_lost"), LogItem[].class);
                LogItem[] kept = JsonUtils.GSON.fromJson(rs.getString("items_kept"), LogItem[].class);
                int bloodMoney = Arrays.stream(lost).filter(i -> i.id == 13307).mapToInt(i -> i.amount).sum();
                totalBM.addAndGet(bloodMoney);
                String key = rs.getString("killer_name") + "[ip=" + rs.getString("killer_ip") + "]|" + rs.getString("user_name") + "[ip=" + rs.getString("user_ip") + "]";
                totalKills.compute(key, (s, integer) -> integer == null ? 1 : integer + 1);
                if (Arrays.stream(kept).map(LogItem::getDef).anyMatch(def -> !def.tradeable)) {
                    totalAmount.compute(key, (s, integer) -> integer == null ? bloodMoney : integer + bloodMoney);
                }
            }
            totalAmount.forEach((key, amount) -> {
                String[] names = key.split("\\|");
                int killCount = totalKills.getOrDefault(key, 0);
                if (amount > 20000 || killCount >= 10)
                    System.out.println("\"" + names[0] + "\" killed \"" + names[1] +"\" " + killCount + " times for a total of " + NumberUtils.formatNumber(amount) + " blood money");
            });
            System.out.println("Done. Total BM: " + NumberUtils.formatNumber(totalBM.get()));
        });


    }


    class LogItem {
        int id;
        String name;
        int amount;

        ItemDef getDef() {
            return ItemDef.get(id);
        }
    }
}
