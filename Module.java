package com.alienmc.fy4x.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

/** Base class for a real FY4X module. */
public abstract class Module {
    public final String name, category, description;
    private boolean enabled;

    protected Module(String name, String category, String description) {
        this.name = name; this.category = category; this.description = description;
    }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean on) {
        if (on == enabled) return;
        enabled = on;
        if (on) onEnable(); else onDisable();
    }
    public void toggle() { setEnabled(!enabled); }

    public void onEnable() {}
    public void onDisable() {}
    /** Called every client tick while enabled and in a world. */
    public void onTick(MinecraftClient mc) {}
    /** Text lines stacked in the top-left HUD. */
    public List<String> hudLines(MinecraftClient mc) { return List.of(); }
    /** Custom HUD drawing (for non-text modules). */
    public void onHud(DrawContext ctx, MinecraftClient mc) {}
}
