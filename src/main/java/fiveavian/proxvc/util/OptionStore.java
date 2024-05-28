package fiveavian.proxvc.util;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class OptionStore {
    public static void loadOptions(File file, Option<?>[] options, KeyBinding[] keys) {
        try {
            if (!file.exists())
                return;
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            for (Option<?> option : options)
                if (properties.containsKey(option.name))
                    option.parse(properties.getProperty(option.name));
            for (KeyBinding key : keys)
                if (properties.containsKey(key.getId()))
                    key.fromOptionsString(properties.getProperty(key.getId()));
        } catch (IOException ex) {
            System.out.println("Failed to load options.");
            ex.printStackTrace();
        }
    }

    public static void saveOptions(File file, Option<?>[] options, KeyBinding[] keys) {
        try {
            if (!file.exists())
                file.createNewFile();
            Properties properties = new Properties();
            for (Option<?> option : options)
                properties.setProperty(option.name, option.getValueString());
            for (KeyBinding key : keys)
                properties.setProperty(key.getId(), key.toOptionsString());
            properties.store(new FileOutputStream(file), null);
        } catch (IOException ex) {
            System.out.println("Failed to save options.");
            ex.printStackTrace();
        }
    }
}
