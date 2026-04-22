package com.liquidify.modules;

import java.util.ArrayList;
import java.util.List;

public abstract class Setting<T> {
    private String name;
    protected T value;

    public Setting(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public abstract String getValueAsString();

    public abstract void setValueFromString(String val);

    public static class FloatSetting extends Setting<Float> {
        private float min, max;

        public FloatSetting(String name, float def, float min, float max) {
            super(name, def);
            this.min = min;
            this.max = max;
        }

        @Override
        public String getValueAsString() {
            return String.valueOf(value);
        }

        @Override
        public void setValueFromString(String val) {
            this.value = Float.parseFloat(val);
        }
    }

    public static class BooleanSetting extends Setting<Boolean> {
        public BooleanSetting(String name, boolean def) {
            super(name, def);
        }

        @Override
        public String getValueAsString() {
            return String.valueOf(value);
        }

        @Override
        public void setValueFromString(String val) {
            this.value = Boolean.parseBoolean(val);
        }
    }

    public static class ModeSetting extends Setting<String> {
        private String[] modes;

        public ModeSetting(String name, String def, String... modes) {
            super(name, def);
            this.modes = modes;
        }

        public String[] getModes() {
            return modes;
        }

        @Override
        public String getValueAsString() {
            return value;
        }

        @Override
        public void setValueFromString(String val) {
            this.value = val;
        }
    }
}
