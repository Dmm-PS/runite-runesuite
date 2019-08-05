package io.ruin.data.impl;

import io.ruin.Server;
import io.ruin.api.database.DatabaseUtils;
import io.ruin.api.utils.FileUtils;
import io.ruin.api.utils.JsonUtils;
import io.ruin.data.DataFile;
import io.ruin.model.World;
import io.ruin.model.map.MultiZone;
import io.ruin.model.map.Region;
import io.ruin.model.map.dynamic.DynamicMap;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class region_keys implements DataFile {

    @Override
    public String path() {
        return "region_keys.json";
    }

    @Override
    public int priority() {
        return 2;
    }

    @Override
    public Object fromJson(String fileName, String json) {
        Map<Integer, int[]> keys = JsonUtils.fromJson(json, Map.class, Integer.class, int[].class);
        for(int regionId = 0; regionId < Region.LOADED.length; regionId++) {
            Region region = new Region(regionId);
            if((region.keys = keys.get(regionId)) != null && !isValid(region.id, region.keys) && World.debug) {
                System.err.println("Invalid Keys for Region (" + regionId + "): base=(" + region.baseX + ", " + region.baseY + ") keys=" + Arrays.toString(region.keys));
                region.keys = null;
            }
            Region.LOADED[regionId] = region;
        }
        for(Region region : Region.LOADED)
            region.init();
        MultiZone.load();
        DynamicMap.load();
//        export(true);
        return keys;
    }

    private static void export(boolean useDump) {
        Map<Integer, int[]> keys = new HashMap<>();
        for(Region region : Region.LOADED) {
            if(region != null && region.keys != null && isValid(region.id, region.keys))
                keys.put(region.id, region.keys);
        }
        if(useDump) {
            Server.dumpsDb.executeAwait(con -> {
                Statement statement = null;
                ResultSet resultSet = null;
                try {
                    statement = con.createStatement();
                    resultSet = statement.executeQuery("SELECT * FROM region_dumps");
                    while(resultSet.next()) {
                        int regionId = resultSet.getInt("id");
                        int[] regionKeys = new int[4];
                        for(int i = 0; i < regionKeys.length; i++)
                            regionKeys[i] = resultSet.getInt("k" + (i + 1));
                        if(isValid(regionId, regionKeys))
                            keys.put(regionId, regionKeys);
                    }
                } finally {
                    DatabaseUtils.close(statement, resultSet);
                }
            });
        }
        int[] noKeys = new int[4];
        for(Region region : Region.LOADED) {
            if(region != null && !keys.containsKey(region.id) && isValid(region.id, noKeys))
                keys.put(region.id, noKeys);
        }
        File file = FileUtils.get("%HOME%/Desktop/region_keys.json");
        System.out.println("Exporting map keys to \"" + file.getAbsolutePath() + "\"...");
        try {
            JsonUtils.toFile(file, JsonUtils.toPrettyJson(keys));
        } catch(IOException e) {
            e.printStackTrace();
        }
        System.out.println("DONE!");
    }

    private static boolean isValid(int id, int[] keys) {
        Region r = new Region(id);
        r.keys = keys;
        try {
            r.getLandscapeData();
            return true;
        } catch(Throwable t) {
            return false;
        }
    }

}