package com.liquidify.modules;

import com.liquidify.sdk.MinecraftSDK;

public class Fullbright extends Module {
    private Object originalGamma = null;

    public Fullbright() {
        super("Fullbright", "Render");
        setKeybind(71); // GLFW_KEY_G
    }

    @Override
    public void onUpdate() {
        if (!isEnabled()) return;
        
        Object options = MinecraftSDK.getOptions();
        if (options != null) {
            try {
                Object gammaOption = MinecraftSDK.gammaField.get(options);
                if (gammaOption != null) {
                    if (originalGamma == null) {
                        originalGamma = MinecraftSDK.valueField.get(gammaOption);
                    }
                    MinecraftSDK.valueField.set(gammaOption, 15.0);
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    @Override
    public void onDisable() {
        Object options = MinecraftSDK.getOptions();
        if (options != null && originalGamma != null) {
            try {
                Object gammaOption = MinecraftSDK.gammaField.get(options);
                if (gammaOption != null) {
                    MinecraftSDK.valueField.set(gammaOption, originalGamma);
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
