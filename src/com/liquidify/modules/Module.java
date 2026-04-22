package com.liquidify.modules;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    private String name;
    private String category;
    private boolean enabled;
    private int keybind = -1;
    private boolean lastKeyState = false;
    private List<Setting<?>> settings = new ArrayList<>();
    
    public Module(String name, String category) {
        this.name = name;
        this.category = category;
        this.enabled = false;
    }
    
    public String getName() { return name; }
    public String getCategory() { return category; }
    
    public int getKeybind() { return keybind; }
    public void setKeybind(int key) { this.keybind = key; }

    public boolean isLastKeyState() { return lastKeyState; }
    public void setLastKeyState(boolean state) { this.lastKeyState = state; }
    
    public boolean isEnabled() { return enabled; }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }
    
    public void toggle() {
        setEnabled(!enabled);
    }
    
    public void onEnable() {}
    public void onDisable() {}
    public void onUpdate() {}

    public List<Setting<?>> getSettings() { return settings; }
    protected <T extends Setting<?>> T addSetting(T s) { settings.add(s); return s; }
}
