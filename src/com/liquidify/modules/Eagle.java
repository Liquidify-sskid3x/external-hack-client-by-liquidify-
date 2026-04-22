package com.liquidify.modules;

import com.liquidify.sdk.MinecraftSDK;
import com.liquidify.modules.Setting.*;

public class Eagle extends Module {
    private FloatSetting edgeDistance = new FloatSetting("Edge Distance", 0.4f, 0.0f, 0.5f);
    private BooleanSetting onlyOnGround = new BooleanSetting("Only On Ground", true);

    private boolean forcedSneak = false;

    public Eagle() {
        super("Eagle", "Player");
        addSetting(edgeDistance);
        addSetting(onlyOnGround);
    }

    @Override
    public void onUpdate() {
        // Respect manual crouch (left/right shift)
        if (MinecraftSDK.isKeyDown(340) || MinecraftSDK.isKeyDown(344)) {
            if (forcedSneak) {
                MinecraftSDK.setShiftKeyDown(false);
                forcedSneak = false;
            }
            return;
        }

        Object player = MinecraftSDK.getPlayer();
        if (player == null) return;

        // Never apply while flying.
        if (isPlayerFlying(player)) {
            releaseSneak();
            return;
        }

        // Require on ground if setting enabled.
        if (onlyOnGround.getValue() && !isPlayerOnGround(player)) {
            releaseSneak();
            return;
        }

        float dist = Math.max(0.05f, edgeDistance.getValue());
        boolean hasAir = false;
        boolean hasSolid = false;

        // Sample around feet: Eagle activates when some support is missing near an edge,
        // but not when fully over air or fully on solid.
        for (int i = 0; i < 360; i += 30) {
            double rad = Math.toRadians(i);
            double dx = -Math.sin(rad) * dist;
            double dz = Math.cos(rad) * dist;
            boolean air = MinecraftSDK.isAirAt(dx, dz);
            hasAir |= air;
            hasSolid |= !air;
            if (hasAir && hasSolid) break;
        }

        boolean shouldSneak = hasAir && hasSolid;
        if (shouldSneak) {
            MinecraftSDK.setShiftKeyDown(true);
            forcedSneak = true;
        } else {
            releaseSneak();
        }
    }
    
    @Override
    public void onDisable() {
        releaseSneak();
    }

    private void releaseSneak() {
        if (forcedSneak) {
            MinecraftSDK.setShiftKeyDown(false);
            forcedSneak = false;
        }
    }

    private boolean isPlayerOnGround(Object player) {
        try {
            if (MinecraftSDK.onGroundField != null) {
                return (boolean) MinecraftSDK.onGroundField.get(player);
            }
        } catch (Exception ignored) {}
        return false;
    }

    private boolean isPlayerFlying(Object player) {
        try {
            if (MinecraftSDK.abilitiesField != null && MinecraftSDK.flyingField != null) {
                Object abilities = MinecraftSDK.abilitiesField.get(player);
                if (abilities != null) {
                    return (boolean) MinecraftSDK.flyingField.get(abilities);
                }
            }
        } catch (Exception ignored) {}
        return false;
    }
}
