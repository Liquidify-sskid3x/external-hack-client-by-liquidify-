package com.liquidify.sdk;

import java.util.HashMap;
import java.util.Map;

public class Mappings {
    private static boolean isFabric = false;
    private static final Map<String, String> mappings = new HashMap<>();

    static {
        // Multi-stage detection
        boolean detected = false;
        try {
            Class.forName("net.fabricmc.loader.api.FabricLoader");
            detected = true;
        } catch (ClassNotFoundException e) {
            // Second try: check for a common intermediary class
            try {
                Class.forName("net.minecraft.class_310");
                detected = true;
            } catch (ClassNotFoundException e2) {}
        }
        
        isFabric = detected;
        System.out.println("[Mappings] Environment initialized. Fabric: " + isFabric);

        initMappings();
    }

    private static void initMappings() {
        if (isFabric) {
            // Class Mappings
            mappings.put("Minecraft", "net.minecraft.class_310");
            mappings.put("LocalPlayer", "net.minecraft.class_746");
            mappings.put("Player", "net.minecraft.class_1657");
            mappings.put("Entity", "net.minecraft.class_1297");
            mappings.put("Level", "net.minecraft.class_1937");
            mappings.put("Options", "net.minecraft.class_315");
            mappings.put("Screen", "net.minecraft.class_437");
            mappings.put("Window", "net.minecraft.class_1041");
            mappings.put("InputConstants", "com.mojang.blaze3d.platform.class_3675");
            mappings.put("Camera", "net.minecraft.class_4184");
            mappings.put("GameRenderer", "net.minecraft.class_757");
            mappings.put("MultiPlayerGameMode", "net.minecraft.class_636");
            mappings.put("ClientLevel", "net.minecraft.class_638");
            mappings.put("BlockPos", "net.minecraft.class_2338");
            mappings.put("BlockState", "net.minecraft.class_2680");
            mappings.put("Abilities", "net.minecraft.class_1656");
            mappings.put("ClientInput", "net.minecraft.class_1863");
            mappings.put("Input", "net.minecraft.class_10521"); // 1.21.1 new Input record
            mappings.put("OptionInstance", "net.minecraft.class_7172");

            // Field Mappings
            mappings.put("Minecraft.player", "field_1724");
            mappings.put("Minecraft.options", "field_1690");
            mappings.put("Minecraft.level", "field_1687");
            mappings.put("Minecraft.screen", "field_1755");
            mappings.put("Minecraft.gameMode", "field_1761");
            mappings.put("Minecraft.gameRenderer", "field_1719");
            mappings.put("Entity.onGround", "field_6017");
            mappings.put("Player.abilities", "field_7512");
            mappings.put("Abilities.flying", "field_7479");
            mappings.put("Abilities.mayfly", "field_7480");
            mappings.put("Options.gamma", "field_1840");
            mappings.put("Options.fov", "field_1826");
            mappings.put("OptionInstance.value", "field_37875");
            mappings.put("LocalPlayer.input", "field_3913");
            mappings.put("ClientInput.keyPresses", "field_52317");

            // Method Mappings
            mappings.put("Minecraft.getInstance", "method_1551");
            mappings.put("Minecraft.getWindow", "method_22683");
            mappings.put("Minecraft.isWindowActive", "method_1569");
            mappings.put("LocalPlayer.setSprinting", "method_5728");
            mappings.put("Window.getX", "method_4490");
            mappings.put("Window.getY", "method_4492");
            mappings.put("Window.getWidth", "method_4480");
            mappings.put("Window.getHeight", "method_4502");
            mappings.put("Window.setTitle", "method_4504");
            mappings.put("InputConstants.isKeyDown", "method_15987");
            mappings.put("GameRenderer.getMainCamera", "method_19418");
            mappings.put("Camera.position", "method_19326");
            mappings.put("Camera.rotation", "method_19329");
            mappings.put("Entity.getX", "method_23317");
            mappings.put("Entity.getY", "method_23318");
            mappings.put("Entity.getZ", "method_23321");
            mappings.put("Entity.getId", "method_5628");
            mappings.put("Entity.setSharedFlag", "method_5757");
            mappings.put("Entity.setGlowingTag", "method_5739");
            mappings.put("Entity.setShiftKeyDown", "method_5720");
            mappings.put("Entity.getName", "method_5477");
            mappings.put("Entity.getDisplayName", "method_5476");
            mappings.put("Entity.getBoundingBox", "method_5829");
            mappings.put("Entity.getDeltaMovement", "method_18798");
            mappings.put("Entity.setDeltaMovement", "method_18799");
            mappings.put("LivingEntity.swing", "method_6104");
            mappings.put("MultiPlayerGameMode.attack", "method_2918");
            mappings.put("Player.getAttackStrengthScale", "method_7261");
            mappings.put("ClientLevel.entitiesForRendering", "method_18291");
            mappings.put("Level.getBlockState", "method_8320");
            mappings.put("BlockState.isAir", "method_26233");

            // Input Record Mappings (1.21.11)
            mappings.put("Input.forward", "method_65971");
            mappings.put("Input.backward", "method_65972");
            mappings.put("Input.left", "method_65973");
            mappings.put("Input.right", "method_65974");
            mappings.put("Input.jump", "method_65975");
            mappings.put("Input.shift", "method_65976");
            mappings.put("Input.sprint", "method_65977");

            // AABB Field Mappings
            mappings.put("AABB.minX", "field_1323");
            mappings.put("AABB.minY", "field_1322");
            mappings.put("AABB.minZ", "field_1321");
            mappings.put("AABB.maxX", "field_1320");
            mappings.put("AABB.maxY", "field_1325");
            mappings.put("AABB.maxZ", "field_1324");

        } else {
            // Vanilla / Mojang Mappings (already in original code, but we'll centralize them)
            mappings.put("Minecraft", "net.minecraft.client.Minecraft");
            mappings.put("LocalPlayer", "net.minecraft.client.player.LocalPlayer");
            mappings.put("Player", "net.minecraft.world.entity.player.Player");
            mappings.put("Entity", "net.minecraft.world.entity.Entity");
            mappings.put("Level", "net.minecraft.world.level.Level");
            mappings.put("Options", "net.minecraft.client.Options");
            mappings.put("Screen", "net.minecraft.client.gui.screens.Screen");
            mappings.put("Window", "com.mojang.blaze3d.platform.Window");
            mappings.put("InputConstants", "com.mojang.blaze3d.platform.InputConstants");
            mappings.put("Camera", "net.minecraft.client.Camera");
            mappings.put("GameRenderer", "net.minecraft.client.renderer.GameRenderer");
            mappings.put("MultiPlayerGameMode", "net.minecraft.client.multiplayer.MultiPlayerGameMode");
            mappings.put("ClientLevel", "net.minecraft.client.multiplayer.ClientLevel");
            mappings.put("BlockPos", "net.minecraft.core.BlockPos");
            mappings.put("BlockState", "net.minecraft.world.level.block.state.BlockState");
            mappings.put("Abilities", "net.minecraft.world.entity.player.Abilities");
            mappings.put("ClientInput", "net.minecraft.client.player.ClientInput");
            mappings.put("Input", "net.minecraft.world.entity.player.Input");
            mappings.put("OptionInstance", "net.minecraft.client.OptionInstance");

            // Field Mappings (Mojang names)
            mappings.put("Minecraft.player", "player");
            mappings.put("Minecraft.options", "options");
            mappings.put("Minecraft.level", "level");
            mappings.put("Minecraft.screen", "screen");
            mappings.put("Minecraft.gameMode", "gameMode");
            mappings.put("Minecraft.gameRenderer", "gameRenderer");
            mappings.put("Entity.onGround", "onGround");
            mappings.put("Player.abilities", "abilities");
            mappings.put("Abilities.flying", "flying");
            mappings.put("Abilities.mayfly", "mayfly");
            mappings.put("Options.gamma", "gamma");
            mappings.put("Options.fov", "fov");
            mappings.put("OptionInstance.value", "value");
            mappings.put("LocalPlayer.input", "input");
            mappings.put("ClientInput.keyPresses", "keyPresses");

            // Method Mappings (Mojang names)
            mappings.put("Minecraft.getInstance", "getInstance");
            mappings.put("Minecraft.getWindow", "getWindow");
            mappings.put("Minecraft.isWindowActive", "isWindowActive");
            mappings.put("LocalPlayer.setSprinting", "setSprinting");
            mappings.put("Window.getX", "getX");
            mappings.put("Window.getY", "getY");
            mappings.put("Window.getWidth", "getWidth");
            mappings.put("Window.getHeight", "getHeight");
            mappings.put("Window.setTitle", "setTitle");
            mappings.put("InputConstants.isKeyDown", "isKeyDown");
            mappings.put("GameRenderer.getMainCamera", "getMainCamera");
            mappings.put("Camera.position", "position");
            mappings.put("Camera.rotation", "rotation");
            mappings.put("Entity.getX", "getX");
            mappings.put("Entity.getY", "getY");
            mappings.put("Entity.getZ", "getZ");
            mappings.put("Entity.getId", "getId");
            mappings.put("Entity.setSharedFlag", "setSharedFlag");
            mappings.put("Entity.setGlowingTag", "setGlowingTag");
            mappings.put("Entity.setShiftKeyDown", "setShiftKeyDown");
            mappings.put("Entity.getName", "getName");
            mappings.put("Entity.getDisplayName", "getDisplayName");
            mappings.put("Entity.getBoundingBox", "getBoundingBox");
            mappings.put("Entity.getDeltaMovement", "getDeltaMovement");
            mappings.put("Entity.setDeltaMovement", "setDeltaMovement");
            mappings.put("LivingEntity.swing", "swing");
            mappings.put("MultiPlayerGameMode.attack", "attack");
            mappings.put("Player.getAttackStrengthScale", "getAttackStrengthScale");
            mappings.put("ClientLevel.entitiesForRendering", "entitiesForRendering");
            mappings.put("Level.getBlockState", "getBlockState");
            mappings.put("BlockState.isAir", "isAir");

            // Input Record Mappings (Vanilla)
            mappings.put("Input.forward", "forward");
            mappings.put("Input.backward", "backward");
            mappings.put("Input.left", "left");
            mappings.put("Input.right", "right");
            mappings.put("Input.jump", "jump");
            mappings.put("Input.shift", "shift");
            mappings.put("Input.sprint", "sprint");

            // AABB Field Mappings
            mappings.put("AABB.minX", "minX");
            mappings.put("AABB.minY", "minY");
            mappings.put("AABB.minZ", "minZ");
            mappings.put("AABB.maxX", "maxX");
            mappings.put("AABB.maxY", "maxY");
            mappings.put("AABB.maxZ", "maxZ");
        }
    }

    public static String get(String key) {
        return mappings.getOrDefault(key, key);
    }
    
    public static boolean isFabric() {
        return isFabric;
    }
}
