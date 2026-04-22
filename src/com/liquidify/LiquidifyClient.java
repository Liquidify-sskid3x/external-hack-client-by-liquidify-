package com.liquidify;

import com.liquidify.sdk.MinecraftSDK;
import com.liquidify.modules.ModuleManager;
import com.liquidify.modules.Module;
import com.liquidify.modules.Setting;
import java.io.*;
import java.net.*;
import java.util.List;

public class LiquidifyClient implements Runnable {
    public static LiquidifyClient instance;
    private static final int PORT = 55555;
    private boolean toggleGUI = false;
    private boolean lastRShiftState = false;
    public LiquidifyClient() {
        instance = this;
    }

    @Override
    public void run() {
        System.out.println("[Liquidify] Client thread started. Waiting for SDK init...");

        try {
            Thread.sleep(1000);

            System.out.println("[Liquidify] Initializing SDK...");
            MinecraftSDK.init();
            
            System.out.println("[Liquidify] Initializing Module Manager...");
            ModuleManager.init();
            
            if (!MinecraftSDK.initialized) {
                System.out.println("[Liquidify] WARNING: SDK initialized with errors. Some features may be disabled.");
            }

            Object window = MinecraftSDK.getWindow();
            if (window != null) {
                try {
                    MinecraftSDK.setTitleMethod.invoke(window, "Liquidify's external client");
                } catch (Exception e) {}
            }

            System.out.println("[Liquidify] Starting Command Server on port " + PORT + "...");
            Thread serverThread = new Thread(this::startCommandServer, "LiquidifyCommandServer");
            serverThread.setDaemon(true);
            serverThread.start();

            while (true) {
                try {
                    // IMPORTANT: all Minecraft interactions must run on the game thread.
                    // Calling into MC from this injected thread can corrupt game state and crash (fastutil/attributes etc).
                    MinecraftSDK.runOnMainThread(() -> {
                        try {
                            boolean inGui = MinecraftSDK.getScreen() != null;
                            boolean focused = MinecraftSDK.isWindowActive();

                            for (Module m : ModuleManager.getModules()) {
                                if (m == null) continue;
                                int key = m.getKeybind();
                                if (key != -1) {
                                    boolean isDown = MinecraftSDK.isKeyDown(key);
                                    if (isDown && !m.isLastKeyState()) {
                                        if (!focused) {
                                            System.out.println("[Liquidify] Ignored " + m.getName() + " toggle (Window not active)");
                                        } else if (inGui) {
                                            System.out.println("[Liquidify] Ignored " + m.getName() + " toggle (In Container/GUI)");
                                        } else {
                                            m.toggle();
                                            System.out.println("[Liquidify] Toggled " + m.getName() + " -> " + m.isEnabled());
                                        }
                                    }
                                    m.setLastKeyState(isDown);
                                }
                            }

                            boolean rShiftDown = MinecraftSDK.isKeyDown(344);
                            if (rShiftDown && !lastRShiftState) {
                                if (focused && !inGui) {
                                    toggleGUI = true;
                                    System.out.println("[Liquidify] Toggled GUI via Right Shift");
                                }
                            }
                            lastRShiftState = rShiftDown;

                            for (Module m : ModuleManager.getModules()) {
                                if (m != null && m.isEnabled()) m.onUpdate();
                            }
                        } catch (Exception e) {
                            System.err.println("[Liquidify] Error in main-thread loop: " + e.getMessage());
                        }
                    });

                    Thread.sleep(1);
                } catch (Exception e) {
                    System.err.println("[Liquidify] Error in main loop: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[Liquidify] FATAL error in Agent thread:");
            e.printStackTrace();
        }
    }

    private void startCommandServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[Liquidify] Command Server listening on port " + PORT);
            while (true) {
                try (Socket client = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                     PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {

                    client.setSoTimeout(500);
                    String line = in.readLine();
                    if (line == null) continue;

                    if (line.equals("STATUS")) {
                        handleStatusRequest(out);
                    } else if (line.startsWith("TOGGLE ")) {
                        String name = line.substring(7);
                        MinecraftSDK.runOnMainThread(() -> {
                            for (Module m : ModuleManager.getModules()) {
                                if (m != null && m.getName().equalsIgnoreCase(name)) {
                                    m.toggle();
                                    break;
                                }
                            }
                        });
                        out.println("END");
                    } else if (line.startsWith("BIND ")) {
                        String[] p = line.split(" ");
                        if (p.length == 3) {
                            for (Module m : ModuleManager.getModules()) {
                                if (m != null && m.getName().equalsIgnoreCase(p[1])) {
                                    m.setKeybind(parseKey(p[2]));
                                    ModuleManager.saveConfig();
                                }
                            }
                        }
                        out.println("END");
                    } else if (line.startsWith("SET|")) {
                        String[] parts = line.split("\\|");
                        if (parts.length == 4) {
                            for (Module m : ModuleManager.getModules()) {
                                if (m != null && m.getName().equalsIgnoreCase(parts[1])) {
                                    for (Setting<?> s : m.getSettings()) {
                                        if (s != null && s.getName().equalsIgnoreCase(parts[2])) {
                                            s.setValueFromString(parts[3]);
                                            ModuleManager.saveConfig();
                                        }
                                    }
                                }
                            }
                        }
                        out.println("END");
                    } else if (line.startsWith("GUI|")) {
                        // GUI|OPEN or GUI|CLOSE - used by Swing overlay to release/grab mouse.
                        String state = line.substring(4).trim().toUpperCase();
                        MinecraftSDK.runOnMainThread(() -> {
                            try {
                                if (state.equals("OPEN")) MinecraftSDK.releaseMouse();
                                else if (state.equals("CLOSE")) MinecraftSDK.grabMouse();
                            } catch (Exception ignored) {}
                        });
                        out.println("END");
                    } else if (line.startsWith("PROFILE|")) {
                        String[] p = line.split("\\|", 3);
                        if (p.length >= 2) {
                            String sub = p[1].toUpperCase();
                            if (sub.equals("LIST")) {
                                for (String profile : ModuleManager.listProfiles()) {
                                    out.println("PROFILE|" + profile);
                                }
                            } else if (sub.equals("SAVE") && p.length == 3) {
                                out.println("PROFILE_SAVE|" + ModuleManager.saveProfile(p[2]));
                            } else if (sub.equals("LOAD") && p.length == 3) {
                                out.println("PROFILE_LOAD|" + ModuleManager.loadProfile(p[2]));
                            }
                        }
                        out.println("END");
                    }
                } catch (Exception e) {
                    // System.err.println("[Liquidify] Error handling client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[Liquidify] CRITICAL: Could not start ServerSocket on port " + PORT + "!");
            System.err.println("[Liquidify] Reason: " + e.getMessage());
            System.err.println("[Liquidify] Port might be in use by another Minecraft instance.");
        }
    }

    private void handleStatusRequest(PrintWriter out) {
        try {
            // Camera + Quaternion
            Object camera = MinecraftSDK.getCamera();
            if (camera != null) {
                try {
                    Object pos = MinecraftSDK.cameraPositionMethod.invoke(camera);
                    double cx = (double) pos.getClass().getField("x").get(pos);
                    double cy = (double) pos.getClass().getField("y").get(pos);
                    double cz = (double) pos.getClass().getField("z").get(pos);
                    
                    Object quat = MinecraftSDK.cameraRotationMethod.invoke(camera);
                    float qx = (float) quat.getClass().getField("x").get(quat);
                    float qy = (float) quat.getClass().getField("y").get(quat);
                    float qz = (float) quat.getClass().getField("z").get(quat);
                    float qw = (float) quat.getClass().getField("w").get(quat);

                    int fov = MinecraftSDK.getFOV();
                    out.println("CAMERA|" + cx + "|" + cy + "|" + cz + "|" + qx + "|" + qy + "|" + qz + "|" + qw + "|" + fov);
                } catch (Exception e) { out.println("CAMERA|0|0|0|0|0|0|1|70"); }
            } else {
                out.println("CAMERA|0|0|0|0|0|0|1|70");
            }

            // Window & Toggle Signal
            boolean guiAllowed = MinecraftSDK.isWindowActive() && MinecraftSDK.getScreen() == null;
            out.println("GUI_ALLOWED|" + guiAllowed);
            out.println("GUI_TOGGLE|" + toggleGUI);
            toggleGUI = false;

            Object win = MinecraftSDK.getWindow();
            if (win != null) {
                try {
                    out.println("WINDOW|" + MinecraftSDK.getXMethod.invoke(win) + "|" + MinecraftSDK.getYMethod.invoke(win) + "|" + MinecraftSDK.getWidthMethod.invoke(win) + "|" + MinecraftSDK.getHeightMethod.invoke(win));
                } catch (Exception e) {}
            }

            // Modules
            List<Module> mods = ModuleManager.getModules();
            for (Module m : mods) {
                if (m == null) continue;
                out.println("MOD|" + m.getName() + "|" + (char) m.getKeybind() + "|" + m.isEnabled());
                for (Setting<?> s : m.getSettings()) {
                    if (s == null) continue;
                    String type = (s instanceof Setting.FloatSetting) ? "FLOAT" : (s instanceof Setting.BooleanSetting) ? "BOOL" : "STRING";
                    out.println("SETTING|" + m.getName() + "|" + s.getName() + "|" + type + "|" + s.getValueAsString() + "|");
                }
            }

            for (String profile : ModuleManager.listProfiles()) {
                out.println("PROFILE|" + profile);
            }

            // ESP Entities
            boolean espOn = false;
            for (Module m : mods) if (m != null && m.getName().equals("ESP") && m.isEnabled()) espOn = true;
            
            if (espOn) {
                Iterable<?> entities = MinecraftSDK.getEntities();
                if (entities != null) {
                    Object player = MinecraftSDK.getPlayer();
                    int pid = -1;
                    try { if (player != null) pid = (int) MinecraftSDK.getIdMethod.invoke(player); } catch (Exception e) {}
                    
                    for (Object e : entities) {
                        try {
                            if (e == null) continue;
                            int id = (int) MinecraftSDK.getIdMethod.invoke(e);
                            if (id == pid) continue;
                            
                            String simpleName = e.getClass().getSimpleName();
                            if (simpleName.contains("ItemEntity") || simpleName.contains("Projectile") || 
                                simpleName.contains("ArmorStand") || simpleName.contains("ExperienceOrb") ||
                                simpleName.contains("AreaEffectCloud")) continue;

                            Object nameComp = MinecraftSDK.getDisplayNameMethod.invoke(e);
                            String name = (String) nameComp.getClass().getMethod("getString").invoke(nameComp);

                            double ex = (double) MinecraftSDK.getXMethodEntity.invoke(e);
                            double ey = (double) MinecraftSDK.getYMethodEntity.invoke(e);
                            double ez = (double) MinecraftSDK.getZMethodEntity.invoke(e);
                            Object bb = e.getClass().getMethod("getBoundingBox").invoke(e);
                            double minX = (double) bb.getClass().getField("minX").get(bb);
                            double maxX = (double) bb.getClass().getField("maxX").get(bb);
                            double minY = (double) bb.getClass().getField("minY").get(bb);
                            double maxY = (double) bb.getClass().getField("maxY").get(bb);
                            double minZ = (double) bb.getClass().getField("minZ").get(bb);
                            double maxZ = (double) bb.getClass().getField("maxZ").get(bb);
                            
                            out.println("ENT|" + id + "|" + minX + "|" + minY + "|" + minZ + "|" + maxX + "|" + maxY + "|" + maxZ + "|" + name);
                        } catch (Exception ex) {}
                    }
                }
            }
        } catch (Exception e) {}
        out.println("END");
    }

    private int parseKey(String name) {
        if (name == null || name.isEmpty() || name.equalsIgnoreCase("NONE")) return -1;
        if (name.length() == 1) return name.toUpperCase().charAt(0);
        
        name = name.toUpperCase();
        switch (name) {
            case "SPACE": return 32;
            case "LEFT SHIFT":
            case "LSHIFT":
            case "SHIFT": return 340;
            case "RIGHT SHIFT":
            case "RSHIFT": return 344;
            case "LEFT CONTROL":
            case "LCONTROL":
            case "LCTRL":
            case "CTRL": return 341;
            case "LEFT ALT":
            case "LALT":
            case "ALT": return 342;
            case "TAB": return 258;
            case "CAPS LOCK": return 280;
            case "BACKSPACE": return 259;
            case "ENTER": return 257;
            default: return -1;
        }
    }
}
