package com.liquidify.modules;

import com.liquidify.sdk.MinecraftSDK;

public class Fly extends Module {
    public Fly() {
        super("Fly", "Movement");
        setKeybind(70); // GLFW_KEY_F
    }

    @Override
    public void onUpdate() {
        if (!isEnabled()) return;
        
        Object player = MinecraftSDK.getPlayer();
        if (player != null) {
            try {
                Object abilities = MinecraftSDK.abilitiesField.get(player);
                if (abilities != null) {
                    MinecraftSDK.flyingField.set(abilities, true);
                    MinecraftSDK.mayflyField.set(abilities, true);
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }
    
    @Override
    public void onDisable() {
        Object player = MinecraftSDK.getPlayer();
        if (player != null) {
            try {
                Object abilities = MinecraftSDK.abilitiesField.get(player);
                if (abilities != null) {
                    MinecraftSDK.flyingField.set(abilities, false);
                    MinecraftSDK.mayflyField.set(abilities, false);
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
