package io.ruin;

import io.ruin.api.database.Database;
import io.ruin.api.database.DatabaseUtils;
import io.ruin.api.filestore.FileStore;
import io.ruin.api.netty.NettyServer;
import io.ruin.api.process.ProcessWorker;
import io.ruin.api.protocol.world.WorldFlag;
import io.ruin.api.protocol.world.WorldSetting;
import io.ruin.api.protocol.world.WorldStage;
import io.ruin.api.protocol.world.WorldType;
import io.ruin.api.utils.*;
import io.ruin.cache.*;
import io.ruin.data.DataFile;
import io.ruin.data.impl.login_set;
import io.ruin.model.World;
import io.ruin.model.combat.special.Special;
import io.ruin.model.map.object.actions.impl.edgeville.Giveaway;
import io.ruin.network.LoginDecoder;
import io.ruin.network.central.CentralClient;
import io.ruin.network.incoming.Incoming;
import io.ruin.process.CoreWorker;
import io.ruin.process.LoginWorker;
import io.ruin.process.event.EventWorker;
import io.ruin.services.LatestUpdate;
import io.ruin.services.Loggers;
import io.ruin.services.PlayersOnline;
import io.ruin.services.livedata.LiveData;
import io.ruin.utility.OfflineMode;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Server extends ServerWrapper {

    public static final ProcessWorker worker = newWorker("server-worker", 600L, Thread.NORM_PRIORITY + 1);

    public static Database gameDb;

    public static final Database dumpsDb = new Database("", "", "", "");

    public static String WEBSITE_URL;

    public static Database forumDb;

    public static List<Runnable> afterData = new ArrayList<>();

    public static FileStore fileStore;

    public static File dataFolder;

    static {
        init(Server.class);
    }

    public static void main(String[] args) throws Throwable {
        /**
         * Server properties
         */
        println("Loading server settings...", false);
        Properties properties = new Properties();
        try(InputStream in = new FileInputStream(getPath() + File.separator + "server.properties")) {
            properties.load(in);
        } catch(IOException e) {
            println("Failed to load server settings!", true);
            logError(e);
            throw e;
        }
        /**
         * World information
         */
        World.id = Integer.valueOf(properties.getProperty("world_id"));
        World.name = properties.getProperty("world_name");
        World.stage = WorldStage.valueOf(properties.getProperty("world_stage"));
        World.type = WorldType.ECO;
        World.flag = WorldFlag.valueOf(properties.getProperty("world_flag"));
        World.centralAdress = properties.getProperty("central_address");
        World.debug = Boolean.parseBoolean(properties.getProperty("world_debug"));
        World.centralAuth = Long.parseLong(properties.getProperty("central_auth"));
        World.centralPort = Integer.parseInt(properties.getProperty("central_port"));


        World.gameDbHost = properties.getProperty("game_host");
        World.gameDbPort = Integer.parseInt(properties.getProperty("game_port"));
        World.gameDbUser = properties.getProperty("game_user");
        World.gameDbPassword = properties.getProperty("game_password");
        World.gameDb = properties.getProperty("game_db");

        World.webDbHost = properties.getProperty("web_host");
        World.webDbPort = Integer.parseInt(properties.getProperty("web_port"));
        World.webDbUser = properties.getProperty("web_user");
        World.webDbPassword = properties.getProperty("web_password");
        World.webDB = properties.getProperty("web_db");


        World.halloween = Boolean.valueOf(properties.getProperty("halloween"));
        World.christmas = Boolean.valueOf(properties.getProperty("christmas"));
        String worldSettings = properties.getProperty("world_settings");
        for(String s : worldSettings.split(",")) {
            if(s == null || (s = s.trim()).isEmpty())
                continue;
            WorldSetting setting;
            try {
                setting = WorldSetting.valueOf(s);
            } catch(Exception e) {
                println("INVALID WORLD SETTING: " + s, true);
                continue;
            }
            World.settings |= setting.mask;
        }
        String address = properties.getProperty("world_address");
        String[] split = address.split(":");
        String host = split[0].trim();
        int port = Integer.valueOf(split[1]);
        if(host.isEmpty() || host.equals("127.0.0.1") || host.equals("localhost"))
            host = IPAddress.get();
        World.address = host + ":" + port;
        /**
         * Offline mode
         */
        if(OfflineMode.enabled = Boolean.valueOf(properties.getProperty("offline_mode"))) {
            OfflineMode.setPath();
            println("WARNING: Offline mode enabled!", true);
        }
        /**
         * Loading (Data from cache & databases)
         */
        println("Loading server data...", false);
        try {
            fileStore = new FileStore(properties.getProperty("cache_path"));
            dataFolder = FileUtils.get(properties.getProperty("data_path"));
            Varpbit.load();
            IdentityKit.load();
            AnimDef.load();
            GfxDef.load();
            ScriptDef.load();
            InterfaceDef.load();
            ItemDef.load();
            NPCDef.load();
            ObjectDef.load();
            DataFile.load();

            /**
             * The following must come after DataFile.load
             */
            login_set.setActive(null, properties.getProperty("login_set"));
        } catch(Throwable t) {
            logError(t);
            return;
        }

        /**
         * Database connections
         */
        if(!OfflineMode.enabled) {
            println("Connecting to SQL databases...", false);
            gameDb = new Database(World.gameDbHost, World.gameDb, World.gameDbUser, World.gameDbPassword);
            forumDb = new Database(World.webDbHost, World.webDB, World.webDbUser, World.webDbPassword);
            DatabaseUtils.connect(new Database[]{gameDb, forumDb}, errors -> {
                if(!errors.isEmpty()) {
                    for(Throwable t : errors)
                        println(t.getMessage(), true);
                    System.exit(1);
                }
            });
            Loggers.clearOnlinePlayers(World.id);
            LatestUpdate.fetch();
            Giveaway.updateTotalAmount();
        }

        /**
         * Loading (After data has been loaded!
         */
        for(Runnable r : afterData) {
            try {
                r.run();
            } catch(Throwable t) {
                logError(t);
                return;
            }
        }
        afterData.clear();
        afterData = null;
        /**
         * Loading (Scripts & handlers)
         */
        println("Loading server scripts & handlers...", false);
        try {
            Special.load();
            Incoming.load();
            PackageLoader.load("io.ruin"); //ensures all static blocks load
        } catch(Throwable t) {
            logError(t);
            return;
        }
        /**
         * Processing
         */
        println("Starting server workers...", false);
        worker.queue(() -> {
            CoreWorker.process();
            EventWorker.process();
            return false;
        });
        LoginWorker.start();
        LiveData.start();
        /**
         * Network
         */
        NettyServer nettyServer = NettyServer.start(World.type.getWorldName() + " World (" + World.id + ") Server", port, LoginDecoder.class, 5);
        /**
         * Central server
         */
        if(!OfflineMode.enabled)
            CentralClient.start();

        PlayersOnline.initialize();

        /**
         * Shutdown hook
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println();
            System.out.println("Gracefully shutting down world server...");
            /**
             * Shutdown network
             */
            nettyServer.shutdown();
            /**
             * Remove players
             */
            int fails = 0;
            while(true) {
                try {
                    if(Server.worker.getExecutor().submit(World::removePlayers).get())
                        break;
                    ThreadUtils.sleep(10L); //^ that will already be a big enough delay
                } catch(Throwable t) {
                    if(++fails >= 5 && World.removePlayers())
                        break;
                    ThreadUtils.sleep(1000L);
                }
            }
            /**
             * Exit (Just to be safe lol)
             */
            System.exit(1);
        }));
    }

    private static void println(String message, boolean error) {
        PrintStream out = error ? System.err : System.out;
        out.println(TimeUtils.addTimestamp(message));
        out.flush();
        ThreadUtils.sleep(100L);
    }

    /**
     * Timing
     */

    public static long currentTick() {
        return worker.getExecutions();
    }

    public static long getEnd(long ticks) {
        return currentTick() + ticks;
    }

    public static boolean isPast(long end) {
        return currentTick() >= end;
    }

    public static int tickMs() {
        return (int) Server.worker.getPeriod();
    }

    public static int toTicks(int seconds) {
        return (seconds * 1000) / tickMs();
    }

}