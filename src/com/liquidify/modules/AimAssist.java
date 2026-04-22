package com.liquidify.modules;

import com.liquidify.sdk.MinecraftSDK;
import com.liquidify.modules.Setting.*;
import java.util.stream.StreamSupport;
import java.lang.reflect.Method;

public class AimAssist extends Module {
    private FloatSetting range = new FloatSetting("Range", 3.0f, 6.0f, 4.0f);
    private FloatSetting speed = new FloatSetting("Speed", 0.1f, 1.0f, 0.5f);

    public AimAssist() {
        super("AimAssist", "Combat");
        addSetting(range);
        addSetting(speed);
    }

    @Override
    public void onUpdate() {
        Object player = MinecraftSDK.getPlayer();
        if (player == null) return;

        Iterable<?> entities = MinecraftSDK.getEntities();
        if (entities == null) return;

        Object bestTarget = StreamSupport.stream(entities.spliterator(), false)
            .filter(e -> e != player)
            .filter(this::isValid)
            .min((e1, e2) -> Double.compare(getDist(e1), getDist(e2)))
            .orElse(null);

        if (bestTarget != null && getDist(bestTarget) <= range.getValue()) {
            aimAt(bestTarget);
        }
    }

    private boolean isValid(Object entity) {
        if (entity == null) return false;
        String simpleName = entity.getClass().getSimpleName();
        
        if (simpleName.contains("ItemEntity") || simpleName.contains("Projectile") || 
            simpleName.contains("ArmorStand") || simpleName.contains("Villager") ||
            simpleName.contains("ExperienceOrb") || simpleName.contains("AreaEffectCloud")) return false;

        try {
            Method isRemoved = entity.getClass().getMethod("isRemoved");
            if ((boolean) isRemoved.invoke(entity)) return false;

            try {
                Method getHealth = entity.getClass().getMethod("getHealth");
                float health = (float) getHealth.invoke(entity);
                if (health <= 0) return false;
            } catch (Exception e) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void aimAt(Object entity) {
        try {
            Object player = MinecraftSDK.getPlayer();
            double px = (double) MinecraftSDK.getXMethodEntity.invoke(player);
            double py = (double) MinecraftSDK.getYMethodEntity.invoke(player);
            double pz = (double) MinecraftSDK.getZMethodEntity.invoke(player);
            
            double ex = (double) MinecraftSDK.getXMethodEntity.invoke(entity);
            double ey = (double) MinecraftSDK.getYMethodEntity.invoke(entity);
            double ez = (double) MinecraftSDK.getZMethodEntity.invoke(entity);
            
            double dx = ex - px;
            double dy = (ey + 1.0) - (py + 1.62);
            double dz = ez - pz;
            double dist = Math.sqrt(dx*dx + dz*dz);
            
            float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
            
            float currentYaw = (float) MinecraftSDK.getYRotMethod.invoke(player, 1.0f);
            float currentPitch = (float) MinecraftSDK.getXRotMethod.invoke(player, 1.0f);
            
            float diffYaw = yaw - currentYaw;
            while (diffYaw > 180) diffYaw -= 360;
            while (diffYaw < -180) diffYaw += 360;
            
            float diffPitch = pitch - currentPitch;
            
            float s = speed.getValue();
            MinecraftSDK.absSnapRotationToMethod.invoke(player, currentYaw + diffYaw * s, currentPitch + diffPitch * s);
        } catch (Exception e) {}
    }

    private double getDist(Object entity) {
        try {
            Object player = MinecraftSDK.getPlayer();
            double px = (double) MinecraftSDK.getXMethodEntity.invoke(player);
            double py = (double) MinecraftSDK.getYMethodEntity.invoke(player);
            double pz = (double) MinecraftSDK.getZMethodEntity.invoke(player);
            
            double ex = (double) MinecraftSDK.getXMethodEntity.invoke(entity);
            double ey = (double) MinecraftSDK.getYMethodEntity.invoke(entity);
            double ez = (double) MinecraftSDK.getZMethodEntity.invoke(entity);
            
            double dx = px - ex;
            double dy = (py + 1.62) - (ey + 1.0);
            double dz = pz - ez;
            return Math.sqrt(dx*dx + dy*dy + dz*dz);
        } catch (Exception e) { return 999; }
    }
}
