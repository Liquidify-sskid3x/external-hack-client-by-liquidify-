package com.liquidify.modules;

import com.liquidify.sdk.MinecraftSDK;
import com.liquidify.modules.Setting.*;
import java.lang.reflect.Method;
import java.util.stream.StreamSupport;

public class KillAura extends Module {
    private FloatSetting range = new FloatSetting("Range", 3.0f, 6.0f, 3.8f);
    private FloatSetting delay = new FloatSetting("Attack Delay (ms)", 0f, 1000f, 0f);
    
    private Object currentTarget = null;
    private long lastTargetTime = 0;

    public KillAura() {
        super("KillAura", "Combat");
        addSetting(range);
        addSetting(delay);
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
            if (bestTarget != currentTarget) {
                currentTarget = bestTarget;
                lastTargetTime = System.currentTimeMillis();
            }

            if (System.currentTimeMillis() - lastTargetTime >= delay.getValue()) {
                try {
                    float cooldown = (float) MinecraftSDK.getAttackStrengthScaleMethod.invoke(player, 0.5f);
                    if (cooldown >= 0.92f) {
                        MinecraftSDK.attack(bestTarget);
                        if (MinecraftSDK.swingMethod != null && MinecraftSDK.mainHandEnum != null) {
                            MinecraftSDK.swingMethod.invoke(player, MinecraftSDK.mainHandEnum);
                        }
                    }
                } catch (Exception e) {}
            }
        } else {
            currentTarget = null;
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

            if (entity == MinecraftSDK.getPlayer()) return false;
            
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
