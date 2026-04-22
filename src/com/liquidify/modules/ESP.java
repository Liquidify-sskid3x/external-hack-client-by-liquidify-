package com.liquidify.modules;

public class ESP extends Module {
    public final Setting.BooleanSetting showNames = addSetting(new Setting.BooleanSetting("Names", true));
    public final Setting.FloatSetting range = addSetting(new Setting.FloatSetting("Range", 64.0f, 10.0f, 128.0f));

    public ESP() {
        super("ESP", "Render");
        setKeybind(86); // GLFW_KEY_V
    }

    @Override
    public void onUpdate() {
        // Logic handled by external GUI
    }
}
