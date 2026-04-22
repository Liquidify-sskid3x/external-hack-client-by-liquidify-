package com.liquidify;

import java.lang.instrument.Instrumentation;
import java.io.*;
import java.util.*;

public class Agent {
    public static void agentmain(String agentArgs, Instrumentation inst) {
        // Safe log path in user's home to avoid C:\ permission issues
        String logPath = System.getProperty("user.home") + File.separator + "liquidify_agent.log";
        File logFile = new File(logPath);

        try (PrintWriter out = new PrintWriter(new FileWriter(logFile, true))) {
            out.println("\n[Agent] " + new Date() + " - Starting Liquidify's external client...");

            try {
                ClassLoader mcLoader = null;
                for (Class<?> clazz : inst.getAllLoadedClasses()) {
                    if (clazz.getName().equals("net.minecraft.client.Minecraft")) {
                        mcLoader = clazz.getClassLoader();
                        break;
                    }
                }

                if (mcLoader == null) {
                    out.println("[Agent] ERROR: Minecraft ClassLoader not found.");
                    return;
                }

                out.println("[Agent] Found Minecraft ClassLoader: " + mcLoader);

                final ClassLoader finalLoader = mcLoader;
                Thread clientThread = new Thread(() -> {
                    try {
                        Thread.currentThread().setContextClassLoader(finalLoader);
                        new LiquidifyClient().run();
                    } catch (Exception e) {
                        try (PrintWriter errOut = new PrintWriter(new FileWriter(logFile, true))) {
                            errOut.println("[Agent] Client Thread Crash:");
                            e.printStackTrace(errOut);
                        } catch (IOException ignored) {
                        }
                    }
                }, "LiquidifyClientThread");

                clientThread.setDaemon(true);
                clientThread.start();
                out.println("[Agent] Client thread launched successfully.");

            } catch (Exception e) {
                out.println("[Agent] Internal Initialization Error:");
                e.printStackTrace(out);
            }
        } catch (Exception e) {
            // Last resort: standard error
            e.printStackTrace();
        }
    }
}
