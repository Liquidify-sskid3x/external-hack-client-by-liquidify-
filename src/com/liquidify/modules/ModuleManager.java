package com.liquidify.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModuleManager {
    private static List<Module> modules = new ArrayList<>();
    private static final String CONFIG_PATH = "C:\\liquid\\config.json";
    private static final File CONFIG_DIR = new File("C:\\liquid");

    public static void init() {
        modules.clear();
        modules.add(new KillAura());
        modules.add(new ESP());
        modules.add(new AimAssist());
        modules.add(new Eagle());
        modules.add(new Fly());
        modules.add(new Fullbright());
        modules.add(new Sprint());
        
        System.out.println("[ModuleManager] Loaded " + modules.size() + " modules.");
        loadConfig();
    }

    public static List<Module> getModules() {
        return modules;
    }

    public static Module getModule(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

    public static void saveConfig() {
        File dir = new File("C:\\liquid");
        if (!dir.exists()) dir.mkdirs();

        saveConfigToFile(new File(CONFIG_PATH));
    }

    public static void loadConfig() {
        File file = new File(CONFIG_PATH);
        if (!file.exists()) {
            System.out.println("[ModuleManager] No config found. Creating default...");
            saveConfig();
            return;
        }

        loadConfigFromFile(file);
    }

    public static boolean saveProfile(String profileName) {
        // Ignore custom names here; profiles are versioned as config.json, config_1.json, ...
        File file = getNextConfigProfileFile();
        return saveConfigToFile(file);
    }

    public static boolean loadProfile(String profileName) {
        String safeName = sanitizeProfileName(profileName);
        if (safeName.isEmpty()) return false;
        File file = new File(CONFIG_DIR, safeName + ".json");
        if (!file.exists()) return false;
        boolean loaded = loadConfigFromFile(file);
        if (loaded) saveConfig(); // Keep default config in sync.
        return loaded;
    }

    public static List<String> listProfiles() {
        List<String> names = new ArrayList<>();
        if (!CONFIG_DIR.exists() || !CONFIG_DIR.isDirectory()) return names;
        File[] files = CONFIG_DIR.listFiles((dir, name) -> {
            String n = name.toLowerCase();
            if (!n.endsWith(".json")) return false;
            return n.equals("config.json") || n.matches("config_\\d+\\.json");
        });
        if (files == null) return names;
        for (File f : files) {
            String n = f.getName();
            names.add(n.substring(0, n.length() - 5));
        }
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    private static File getNextConfigProfileFile() {
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        File base = new File(CONFIG_DIR, "config.json");
        if (!base.exists()) return base;
        int i = 1;
        while (true) {
            File f = new File(CONFIG_DIR, "config_" + i + ".json");
            if (!f.exists()) return f;
            i++;
        }
    }

    private static String sanitizeProfileName(String name) {
        if (name == null) return "";
        return name.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private static boolean saveConfigToFile(File file) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("{");
            for (int i = 0; i < modules.size(); i++) {
                Module m = modules.get(i);
                writer.println("  \"" + m.getName() + "\": {");
                writer.println("    \"key\": " + m.getKeybind() + ",");
                writer.println("    \"enabled\": " + m.isEnabled() + ",");
                writer.println("    \"settings\": {");
                List<Setting<?>> settings = m.getSettings();
                for (int j = 0; j < settings.size(); j++) {
                    Setting<?> s = settings.get(j);
                    writer.print("      \"" + s.getName() + "\": \"" + s.getValueAsString() + "\"");
                    if (j < settings.size() - 1) writer.println(",");
                    else writer.println();
                }
                writer.println("    }");
                writer.print("  }");
                if (i < modules.size() - 1) writer.println(",");
                else writer.println();
            }
            writer.println("}");
            System.out.println("[ModuleManager] Config saved to " + file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.out.println("[ModuleManager] Error saving config: " + e.getMessage());
            return false;
        }
    }

    private static boolean loadConfigFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            
            String json = sb.toString();
            for (Module m : modules) {
                String modJson = extractObject(json, m.getName());
                if (modJson == null) continue;

                Integer key = extractInt(modJson, "key");
                if (key != null) m.setKeybind(key);

                Boolean enabled = extractBool(modJson, "enabled");
                if (enabled != null) m.setEnabled(enabled);

                String settingsJson = extractObject(modJson, "settings");
                if (settingsJson != null) {
                    for (Setting<?> s : m.getSettings()) {
                        String val = extractString(settingsJson, s.getName());
                        if (val != null) s.setValueFromString(val);
                    }
                }
            }
            System.out.println("[ModuleManager] Config loaded from " + file.getAbsolutePath());
            return true;
        } catch (Exception e) {
            System.out.println("[ModuleManager] Error loading config: " + e.getMessage());
            return false;
        }
    }

    private static String extractObject(String json, String key) {
        int keyIndex = json.indexOf("\"" + key + "\"");
        if (keyIndex < 0) return null;
        int open = json.indexOf("{", keyIndex);
        if (open < 0) return null;
        int depth = 0;
        for (int i = open; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return json.substring(open, i + 1);
            }
        }
        return null;
    }

    private static Integer extractInt(String json, String key) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        if (m.find()) return Integer.parseInt(m.group(1));
        return null;
    }

    private static Boolean extractBool(String json, String key) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE).matcher(json);
        if (m.find()) return Boolean.parseBoolean(m.group(1));
        return null;
    }

    private static String extractString(String json, String key) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        if (m.find()) return m.group(1);
        return null;
    }
}
