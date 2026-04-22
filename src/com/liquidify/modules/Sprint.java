package com.liquidify.modules;

import com.liquidify.sdk.MinecraftSDK;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint", "Movement");
        setKeybind(82); // GLFW_KEY_R
    }

    @Override
    public void onUpdate() {
        if (!isEnabled()) return;
        
        Object player = MinecraftSDK.getPlayer();
        if (player != null) {
            try {
                MinecraftSDK.setSprintingMethod.invoke(player, true);
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
