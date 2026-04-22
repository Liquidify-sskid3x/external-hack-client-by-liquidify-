package com.liquidify.sdk;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

public class MinecraftSDK {
    public static ClassLoader loader;
    public static Class<?> mcClass;
    public static Object mcInstance;

    public static Field playerField;
    public static Field optionsField;
    public static Field levelField;
    public static Field screenField;
    public static Method executeMethod;
    public static Field mouseHandlerField;
    public static Method grabMouseMethod;
    public static Method releaseMouseMethod;

    public static Field onGroundField;
    public static Class<?> localPlayerClass;
    public static Method setSprintingMethod;
    
    public static Class<?> playerClass;
    public static Field abilitiesField;

    public static Class<?> abilitiesClass;
    public static Field flyingField;
    public static Field mayflyField;

    public static Class<?> optionsClass;
    public static Field gammaField;
    
    public static Class<?> optionInstanceClass;
    public static Field valueField;

    public static Method getWindowMethod;
    public static Class<?> windowClass;
    public static Method getXMethod;
    public static Method getYMethod;
    public static Method getWidthMethod;
    public static Method getHeightMethod;
    public static Method setTitleMethod;
    public static Method isWindowActiveMethod;

    public static Class<?> inputConstantsClass;
    public static Method isKeyDownMethod;

    public static Field gameModeField;
    public static Method attackMethod;
    public static Method getAttackStrengthScaleMethod;
    
    public static Method entitiesForRenderingMethod;
    
    public static Class<?> blockPosClass;
    public static Class<?> blockStateClass;
    public static Method getBlockStateMethod;
    public static Method isAirMethod;

    public static Class<?> entityClass;
    public static Method getXMethodEntity;
    public static Method getYMethodEntity;
    public static Method getZMethodEntity;
    public static Method getIdMethod;
    public static Method setSharedFlagMethod;
    public static Method setGlowingTagMethod;
    public static Method getYRotMethod;
    public static Method getXRotMethod;
    public static Method absSnapRotationToMethod;
    public static Method getMainCameraMethod;
    public static Method cameraPositionMethod;
    public static Method cameraXRotMethod;
    public static Method cameraYRotMethod;
    public static Method cameraRotationMethod;
    public static Method swingMethod;
    public static Object mainHandEnum;
    public static Field fovField;

    public static Method setShiftKeyDownMethod;
    public static Method getNameMethod;
    public static Method getDisplayNameMethod;
    public static Method getTypeMethod;

    public static Field inputField;
    public static Field keyPressesField;
    public static Constructor<?> inputConstructor;
    public static Method forwardMethodRecord, backwardMethodRecord, leftMethodRecord, rightMethodRecord, jumpMethodRecord, shiftMethodRecord, sprintMethodRecord;

    public static boolean initialized = false;

    private static void log(String msg) { System.out.println("[MinecraftSDK] " + msg); }

    public static void init() {
        log("Initializing...");
        try {
            loader = Thread.currentThread().getContextClassLoader();
            log("Using ClassLoader: " + loader);
            mcClass = loader.loadClass("net.minecraft.client.Minecraft");
            try {
                mcInstance = mcClass.getMethod("getInstance").invoke(null);
            } catch (Exception e) {
                Field f = mcClass.getDeclaredField("instance");
                f.setAccessible(true);
                mcInstance = f.get(null);
            }
            
            if (mcInstance == null) {
                log("FATAL: mcInstance is null!");
                return;
            }

            // Core Fields
            try { playerField = mcClass.getDeclaredField("player"); playerField.setAccessible(true); } catch(Exception e){}
            try { optionsField = mcClass.getDeclaredField("options"); optionsField.setAccessible(true); } catch(Exception e){}
            try { levelField = mcClass.getDeclaredField("level"); levelField.setAccessible(true); } catch(Exception e){}
            try { screenField = mcClass.getDeclaredField("screen"); screenField.setAccessible(true); } catch(Exception e){}
            try { onGroundField = loader.loadClass("net.minecraft.world.entity.Entity").getDeclaredField("onGround"); onGroundField.setAccessible(true); } catch(Exception e){}
            try { executeMethod = mcClass.getMethod("execute", Runnable.class); } catch (Exception e) {}
            try { mouseHandlerField = mcClass.getDeclaredField("mouseHandler"); mouseHandlerField.setAccessible(true); } catch (Exception e) {}
            try {
                Class<?> mhClass = loader.loadClass("net.minecraft.client.MouseHandler");
                try { grabMouseMethod = mhClass.getMethod("grabMouse"); } catch (Exception e) {}
                try { releaseMouseMethod = mhClass.getMethod("releaseMouse"); } catch (Exception e) {}
            } catch (Exception e) {}

            // Player Methods
            try {
                localPlayerClass = loader.loadClass("net.minecraft.client.player.LocalPlayer");
                setSprintingMethod = localPlayerClass.getMethod("setSprinting", boolean.class);
                inputField = localPlayerClass.getDeclaredField("input");
                inputField.setAccessible(true);
                
                Class<?> clientInputClass = loader.loadClass("net.minecraft.client.player.ClientInput");
                keyPressesField = clientInputClass.getDeclaredField("keyPresses");
                keyPressesField.setAccessible(true);
                
                Class<?> inputClass = loader.loadClass("net.minecraft.world.entity.player.Input");
                inputConstructor = inputClass.getConstructor(boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class);
                
                forwardMethodRecord = inputClass.getMethod("forward");
                backwardMethodRecord = inputClass.getMethod("backward");
                leftMethodRecord = inputClass.getMethod("left");
                rightMethodRecord = inputClass.getMethod("right");
                jumpMethodRecord = inputClass.getMethod("jump");
                shiftMethodRecord = inputClass.getMethod("shift");
                sprintMethodRecord = inputClass.getMethod("sprint");
            } catch(Exception e){}

            try {
                playerClass = loader.loadClass("net.minecraft.world.entity.player.Player");
                abilitiesField = playerClass.getDeclaredField("abilities");
                abilitiesField.setAccessible(true);
                abilitiesClass = loader.loadClass("net.minecraft.world.entity.player.Abilities");
                flyingField = abilitiesClass.getDeclaredField("flying");
                flyingField.setAccessible(true);
                mayflyField = abilitiesClass.getDeclaredField("mayfly");
                mayflyField.setAccessible(true);
            } catch(Exception e){}

            // Options & Window
            try {
                optionsClass = loader.loadClass("net.minecraft.client.Options");
                gammaField = optionsClass.getDeclaredField("gamma");
                gammaField.setAccessible(true);
                optionInstanceClass = loader.loadClass("net.minecraft.client.OptionInstance");
                valueField = optionInstanceClass.getDeclaredField("value");
                valueField.setAccessible(true);
                fovField = optionsClass.getDeclaredField("fov");
                fovField.setAccessible(true);
            } catch(Exception e){}

            try {
                getWindowMethod = mcClass.getMethod("getWindow");
                windowClass = loader.loadClass("com.mojang.blaze3d.platform.Window");
                getXMethod = windowClass.getMethod("getX");
                getYMethod = windowClass.getMethod("getY");
                getWidthMethod = windowClass.getMethod("getWidth");
                getHeightMethod = windowClass.getMethod("getHeight");
                setTitleMethod = windowClass.getMethod("setTitle", String.class);
                isWindowActiveMethod = mcClass.getMethod("isWindowActive");
                inputConstantsClass = loader.loadClass("com.mojang.blaze3d.platform.InputConstants");
                isKeyDownMethod = inputConstantsClass.getMethod("isKeyDown", windowClass, int.class);
            } catch(Exception e){}

            // Rendering & Camera
            try {
                Class<?> gameRendererClass = loader.loadClass("net.minecraft.client.renderer.GameRenderer");
                getMainCameraMethod = gameRendererClass.getMethod("getMainCamera");
                Class<?> cameraClass = loader.loadClass("net.minecraft.client.Camera");
                cameraPositionMethod = cameraClass.getMethod("position");
                cameraXRotMethod = cameraClass.getMethod("xRot");
                cameraYRotMethod = cameraClass.getMethod("yRot");
                cameraRotationMethod = cameraClass.getMethod("rotation");
            } catch(Exception e){}

            // Entity Core
            try {
                entityClass = loader.loadClass("net.minecraft.world.entity.Entity");
                getXMethodEntity = entityClass.getMethod("getX");
                getYMethodEntity = entityClass.getMethod("getY");
                getZMethodEntity = entityClass.getMethod("getZ");
                getIdMethod = entityClass.getMethod("getId");
                getYRotMethod = entityClass.getMethod("getYRot", float.class);
                getXRotMethod = entityClass.getMethod("getXRot", float.class);
                absSnapRotationToMethod = entityClass.getMethod("absSnapRotationTo", float.class, float.class);
                setSharedFlagMethod = entityClass.getDeclaredMethod("setSharedFlag", int.class, boolean.class);
                setSharedFlagMethod.setAccessible(true);
                setGlowingTagMethod = entityClass.getMethod("setGlowingTag", boolean.class);
                setShiftKeyDownMethod = entityClass.getMethod("setShiftKeyDown", boolean.class);
                
                getNameMethod = entityClass.getMethod("getName");
                getDisplayNameMethod = entityClass.getMethod("getDisplayName");
                getTypeMethod = entityClass.getMethod("getType");
            } catch(Exception e){}

            // Living Entity & Combat
            try {
                Class<?> livingEntityClass = loader.loadClass("net.minecraft.world.entity.LivingEntity");
                Class<?> interactionHandClass = loader.loadClass("net.minecraft.world.InteractionHand");
                mainHandEnum = interactionHandClass.getField("MAIN_HAND").get(null);
                swingMethod = livingEntityClass.getMethod("swing", interactionHandClass);
            } catch(Exception e){}

            try {
                gameModeField = mcClass.getDeclaredField("gameMode");
                gameModeField.setAccessible(true);
                Class<?> gameModeClass = loader.loadClass("net.minecraft.client.multiplayer.MultiPlayerGameMode");
                attackMethod = gameModeClass.getMethod("attack", playerClass, entityClass);
                getAttackStrengthScaleMethod = playerClass.getMethod("getAttackStrengthScale", float.class);
                entitiesForRenderingMethod = loader.loadClass("net.minecraft.client.multiplayer.ClientLevel").getMethod("entitiesForRendering");
            
                blockPosClass = loader.loadClass("net.minecraft.core.BlockPos");
                blockStateClass = loader.loadClass("net.minecraft.world.level.block.state.BlockState");
                getBlockStateMethod = loader.loadClass("net.minecraft.world.level.Level").getMethod("getBlockState", blockPosClass);
                isAirMethod = blockStateClass.getMethod("isAir");
            } catch(Exception e){}

            initialized = true;
            log("Initialization complete (with some optional failures handled).");
        } catch (Exception e) {
            log("CRITICAL Initialization Failed!");
            e.printStackTrace();
        }
    }

    // Helper methods (kept same as before)
    public static Object getPlayer() { if (!initialized || mcInstance == null) return null; try { return playerField.get(mcInstance); } catch (Exception e) { return null; } }
    public static Object getScreen() { if (!initialized || mcInstance == null) return null; try { return screenField.get(mcInstance); } catch (Exception e) { return null; } }
    public static Object getOptions() { if (!initialized || mcInstance == null) return null; try { return optionsField.get(mcInstance); } catch (Exception e) { return null; } }
    public static Object getWindow() { if (!initialized || mcInstance == null) return null; try { return getWindowMethod.invoke(mcInstance); } catch (Exception e) { return null; } }
    public static boolean isKeyDown(int key) { if (!initialized || mcInstance == null) return false; try { Object window = getWindow(); if (window != null) return (boolean) isKeyDownMethod.invoke(null, window, key); } catch (Exception e) {} return false; }
    public static boolean isWindowActive() { if (!initialized || mcInstance == null || isWindowActiveMethod == null) return false; try { return (boolean) isWindowActiveMethod.invoke(mcInstance); } catch (Exception e) { return false; } }
    public static Object getGameMode() { if (!initialized || mcInstance == null) return null; try { return gameModeField.get(mcInstance); } catch (Exception e) { return null; } }
    public static Object getLevel() { if (!initialized || mcInstance == null) return null; try { return levelField.get(mcInstance); } catch (Exception e) { return null; } }
    public static void attack(Object target) { if (!initialized || mcInstance == null || attackMethod == null) return; try { Object player = getPlayer(); Object gameMode = getGameMode(); if (player != null && gameMode != null && target != null) attackMethod.invoke(gameMode, player, target); } catch (Exception e) {} }
    public static Object getCamera() { if (!initialized || mcInstance == null || getMainCameraMethod == null) return null; try { Field grField = mcClass.getDeclaredField("gameRenderer"); grField.setAccessible(true); Object gr = grField.get(mcInstance); if (gr != null) return getMainCameraMethod.invoke(gr); } catch (Exception e) {} return null; }
    public static int getFOV() { if (!initialized || mcInstance == null || fovField == null) return 70; try { Object options = getOptions(); Object fovInstance = fovField.get(options); return (int) valueField.get(fovInstance); } catch (Exception e) { return 70; } }
    public static Iterable<?> getEntities() { if (!initialized || mcInstance == null || entitiesForRenderingMethod == null) return null; try { Object level = getLevel(); if (level != null) return (Iterable<?>) entitiesForRenderingMethod.invoke(level); } catch (Exception e) {} return null; }
    public static void runOnMainThread(Runnable r) {
        if (!initialized || mcInstance == null || r == null) return;
        try {
            if (executeMethod != null) executeMethod.invoke(mcInstance, r);
            else r.run();
        } catch (Exception e) {
            try { r.run(); } catch (Exception ignored) {}
        }
    }
    public static void releaseMouse() {
        if (!initialized || mcInstance == null || mouseHandlerField == null || releaseMouseMethod == null) return;
        try {
            Object mh = mouseHandlerField.get(mcInstance);
            if (mh != null) releaseMouseMethod.invoke(mh);
        } catch (Exception ignored) {}
    }
    public static void grabMouse() {
        if (!initialized || mcInstance == null || mouseHandlerField == null || grabMouseMethod == null) return;
        try {
            Object mh = mouseHandlerField.get(mcInstance);
            if (mh != null) grabMouseMethod.invoke(mh);
        } catch (Exception ignored) {}
    }

    public static boolean isAirAt(double offsetX, double offsetZ) {
        if (!initialized || mcInstance == null) return false;
        try {
            Object player = getPlayer();
            Object level = getLevel();
            if (player != null && level != null) {

                double x = (double) getXMethodEntity.invoke(player);
                double y = (double) getYMethodEntity.invoke(player);
                double z = (double) getZMethodEntity.invoke(player);
                
                Object pos = blockPosClass.getConstructor(int.class, int.class, int.class).newInstance(
                    (int) Math.floor(x + offsetX),
                    (int) Math.floor(y - 0.01),
                    (int) Math.floor(z + offsetZ)
                );
                Object state = getBlockStateMethod.invoke(level, pos);
                return (boolean) isAirMethod.invoke(state);
            }
        } catch (Exception e) {}
        return false;
    }

    public static void setShiftKeyDown(boolean down) {
        if (!initialized || mcInstance == null) return;
        try {
            Object player = getPlayer();
            if (player == null) return;

            // Prefer native entity method when available.
            if (setShiftKeyDownMethod != null) {
                try {
                    setShiftKeyDownMethod.invoke(player, down);
                    return;
                } catch (Exception ignored) {}
            }

            if (inputField != null && keyPressesField != null && inputConstructor != null) {
                Object inputObj = inputField.get(player);
                if (inputObj == null) return;
                Object kp = keyPressesField.get(inputObj);
                if (kp == null) return;

                // Create new Input record (immutable override)
                Object newKp = inputConstructor.newInstance(
                    (boolean) forwardMethodRecord.invoke(kp),
                    (boolean) backwardMethodRecord.invoke(kp),
                    (boolean) leftMethodRecord.invoke(kp),
                    (boolean) rightMethodRecord.invoke(kp),
                    (boolean) jumpMethodRecord.invoke(kp),
                    down,
                    (boolean) sprintMethodRecord.invoke(kp)
                );

                keyPressesField.set(inputObj, newKp);
            }
        } catch (Exception e) {}
    }
}
