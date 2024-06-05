package fiveavian.proxvc.util;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Option;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class OptionStore {
    public static void loadOptions(Path path, Option<?>[] options, KeyBinding[] keys) {
        try {
            if (!Files.exists(path)) {
                return;
            }
            Properties properties = new Properties();
            properties.load(Files.newInputStream(path));
            for (Option<?> option : options) {
                if (properties.containsKey(option.name)) {
                    option.parse(properties.getProperty(option.name));
                }
            }
            for (KeyBinding key : keys) {
                if (properties.containsKey(key.getId())) {
                    key.fromOptionsString(properties.getProperty(key.getId()));
                }
            }
        } catch (IOException ex) {
            System.out.println("Failed to load options.");
            ex.printStackTrace();
        }
    }

    public static void saveOptions(Path path, Option<?>[] options, KeyBinding[] keys) {
        try {
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            Properties properties = new Properties();
            for (Option<?> option : options) {
                if (option.value != null) {
                    properties.setProperty(option.name, option.getValueString());
                }
            }
            for (KeyBinding key : keys) {
                properties.setProperty(key.getId(), key.toOptionsString());
            }
            properties.store(Files.newOutputStream(path), null);
        } catch (IOException ex) {
            System.out.println("Failed to save options.");
            ex.printStackTrace();
        }
    }
}
