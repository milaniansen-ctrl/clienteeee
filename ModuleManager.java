package com.alienmc.fy4x.modules;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("fy4x-client.properties");

    private ModuleManager() {}

    public static void init() {
        MODULES.clear();
        MODULES.add(new AutoSprint());
        MODULES.add(new Coordinates());
        MODULES.add(new FpsDisplay());
        MODULES.add(new PingDisplay());
        MODULES.add(new Speedometer());
        MODULES.add(new TotemCounter());
        MODULES.add(new ArmorStatus());
        load();
    }

    public static List<Module> all() { return MODULES; }
    public static Module get(String name) {
        for (Module m : MODULES) if (m.name.equals(name)) return m;
        return null;
    }
    public static boolean isOn(String name) { Module m = get(name); return m != null && m.isEnabled(); }
    public static void disableAll() { MODULES.forEach(m -> m.setEnabled(false)); save(); }
    public static int enabledCount() { int n = 0; for (Module m : MODULES) if (m.isEnabled()) n++; return n; }

    public static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        for (Module m : MODULES) if (m.isEnabled()) m.onTick(mc);
    }

    public static void renderHud(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null || mc.options.hudHidden) return;
        int y = 4;
        for (Module m : MODULES) {
            if (!m.isEnabled()) continue;
            for (String line : m.hudLines(mc)) {
                ctx.drawText(mc.textRenderer, line, 4, y, 0xFF38A0FF, true);
                y += 10;
            }
            m.onHud(ctx, mc);
        }
    }

    public static void save() {
        Properties p = new Properties();
        for (Module m : MODULES) p.setProperty(m.name, Boolean.toString(m.isEnabled()));
        try (Writer w = Files.newBufferedWriter(FILE)) { p.store(w, "FY4X Client modules"); } catch (IOException ignored) {}
    }

    private static void load() {
        if (!Files.exists(FILE)) return;
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(FILE)) { p.load(r); } catch (IOException e) { return; }
        for (Module m : MODULES) m.setEnabled(Boolean.parseBoolean(p.getProperty(m.name, "false")));
    }

    // ---------------- modules ----------------

    static final class AutoSprint extends Module {
        AutoSprint() { super("Auto Sprint", "MOVEMENT", "Sprints automatically while you hold forward."); }
        @Override public void onTick(MinecraftClient mc) {
            if (mc.player.isSneaking() || mc.player.isUsingItem() || mc.player.horizontalCollision) return;
            if (mc.options.forwardKey.isPressed() && mc.player.getHungerManager().getFoodLevel() > 6) mc.player.setSprinting(true);
        }
    }

    static final class Coordinates extends Module {
        Coordinates() { super("Coordinates", "RENDER", "Shows your XYZ position."); }
        @Override public List<String> hudLines(MinecraftClient mc) {
            return List.of(String.format("XYZ: %.1f / %.1f / %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        }
    }

    static final class FpsDisplay extends Module {
        FpsDisplay() { super("FPS Graph", "RENDER", "Shows your current FPS (text only, no graph yet)."); }
        @Override public List<String> hudLines(MinecraftClient mc) { return List.of("FPS: " + mc.getCurrentFps()); }
    }

    static final class PingDisplay extends Module {
        PingDisplay() { super("Ping Display", "SOCIAL", "Shows your latency to the server."); }
        @Override public List<String> hudLines(MinecraftClient mc) {
            if (mc.getNetworkHandler() == null) return List.of();
            PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            return List.of("Ping: " + (e == null ? "?" : e.getLatency() + " ms"));
        }
    }

    static final class Speedometer extends Module {
        private Vec3d last;
        private double bps;
        Speedometer() { super("Speedometer", "MOVEMENT", "Shows horizontal speed in blocks per second."); }
        @Override public void onEnable() { last = null; }
        @Override public void onTick(MinecraftClient mc) {
            Vec3d now = new Vec3d(mc.player.getX(), 0, mc.player.getZ());
            if (last != null) bps = now.distanceTo(last) * 20.0;
            last = now;
        }
        @Override public List<String> hudLines(MinecraftClient mc) { return List.of(String.format("Speed: %.2f b/s", bps)); }
    }

    static final class TotemCounter extends Module {
        TotemCounter() { super("Totem Counter", "PLAYER", "Counts totems of undying in your inventory."); }
        @Override public List<String> hudLines(MinecraftClient mc) {
            int n = 0;
            for (int i = 0; i < mc.player.getInventory().size(); i++) {
                ItemStack s = mc.player.getInventory().getStack(i);
                if (s.isOf(Items.TOTEM_OF_UNDYING)) n += s.getCount();
            }
            if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) n += mc.player.getOffHandStack().getCount();
            return List.of("Totems: " + n);
        }
    }

    static final class ArmorStatus extends Module {
        ArmorStatus() { super("Armor Status", "PLAYER", "Shows your armor pieces with durability on the left side."); }
        @Override public void onHud(DrawContext ctx, MinecraftClient mc) {
            EquipmentSlot[] slots = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
            int x = 4, y = ctx.getScaledWindowHeight() / 2 - 36;
            for (EquipmentSlot slot : slots) {
                ItemStack s = mc.player.getEquippedStack(slot);
                if (!s.isEmpty()) {
                    ctx.drawItem(s, x, y);
                    ctx.drawStackOverlay(mc.textRenderer, s, x, y);
                }
                y += 18;
            }
        }
    }
}
