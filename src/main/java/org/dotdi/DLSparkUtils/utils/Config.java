package org.dotdi.DLSparkUtils.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class Config {

    Map<String, String> data = new HashMap<String, String>();

    public Config() {
        
    }
    
    public Config(File file) throws IOException {
        List<String> lines = FileUtils.readLines(file);
        process(lines);
    }
    
    public Config(String txt) {
        process(txt);
    }

    private void process(String txt) {
        String[] lines = txt.split("\n");
        process(lines);
    }

    private void process(String[] lines) {
        Iterable<String> iterable = Arrays.asList(lines);
        process(iterable);
    }

    private void process(Iterable<String> lines) {
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("#"))
                continue;
            String[] parts = line.split("=");
            if (parts.length != 2)
                throw new IllegalArgumentException();
            data.put(parts[0], parts[1]);
        }
    }

    public String getString(String key) {
        if (data.containsKey(key))
            return data.get(key);
        throw new IllegalArgumentException();
    }

    public String getString(String key, String defaultValue) {
        return data.containsKey(key) ? data.get(key) : defaultValue;
    }

    public int getInt(String key) {
        if (data.containsKey(key)) {
            int value = Integer.parseInt(data.get(key));
            return value;
        }
        throw new IllegalArgumentException();
    }
    
    public int getInt(String key, int defaultValue) {
        if (data.containsKey(key)) {
            int value = Integer.parseInt(data.get(key));
            return value;
        }
        return defaultValue;
    }

    public long getLong(String key) {
        if (data.containsKey(key)) {
            long value = Long.parseLong(data.get(key));
            return value;
        }
        throw new IllegalArgumentException();
    }
    
    public long getLong(String key, long defaultValue) {
        if (data.containsKey(key)) {
            long value = Long.parseLong(data.get(key));
            return value;
        }
        return defaultValue;
    }
    
    public double getDouble(String key) {
        if (data.containsKey(key)) {
            double value = Double.parseDouble(data.get(key));
            return value;
        }
        throw new IllegalArgumentException();
    }
    
    public double getDouble(String key, double defaultValue) {
        if (data.containsKey(key)) {
            double value = Double.parseDouble(data.get(key));
            return value;
        }
        return defaultValue;
    }
}
