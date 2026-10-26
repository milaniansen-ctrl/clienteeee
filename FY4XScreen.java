package com.alienmc.fy4x;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import com.alienmc.fy4x.modules.Module;
import com.alienmc.fy4x.modules.ModuleManager;

import java.util.*;

/** FY4X client dashboard. Module switches are UI presets until a module has a real implementation. */
public final class FY4XScreen extends Screen {
    private static final int BG = 0xF009111D, PANEL = 0xF51A2636, CARD = 0xFF202E40;
    private static final int EDGE = 0xFF354B63, BLUE = 0xFF38A0FF, TEXT = 0xFFF1F6FF, MUTED = 0xFF9BAEC3;
    private final Map<String, List<Entry>> categories = new LinkedHashMap<>();
    private String activeCategory = "COMBAT", contextModule, selectedRegion = "E4";
    private int scroll = 0;
    private int mapX, mapY, mapW, mapH, tileW, tileH;

    public FY4XScreen() {
        super(Text.literal("FY4X Client"));
        categories.put("COMBAT", modules("Anchor Macro", "Auto Crystal", "Auto Totem", "Auto Trap", "Crystal Optimizer", "Double Anchor", "Hit Crystal", "Inventory Totem", "Jump Reset", "Shield Breaker", "Totem Manager", "Trigger Bot"));
        categories.put("MOVEMENT", modules("Auto Sprint", "Elytra Helper", "Flight HUD", "Inventory Move", "Jump Assist", "No Slow", "Parkour", "Safe Walk", "Speedometer", "Step Assist", "Toggle Sneak", "Velocity Display"));
        categories.put("PLAYER", modules("Armor Status", "Auto Eat", "Auto Respawn", "Auto Tool", "Item Alerts", "Mouse Tweaks", "Potion HUD", "Quick Use", "Totem Counter", "Waypoint Manager"));
        categories.put("WORLD", modules("Block Overlay", "Chest Finder", "Durability Alert", "Entity Counter", "Light Level", "Mob Radar", "Ore Highlights", "Reach Display", "Storage ESP", "Time HUD", "Weather HUD"));
        categories.put("DONUTSMP", modules("Auction Tracker", "Auto Sell Helper", "Base Waypoints", "Item Price Notes", "Netherite Finder", "RTP Notes", "Spawner Tracker", "Trade Helper"));
        categories.put("RENDER", modules("Clean HUD", "Coordinates", "Crosshair Editor", "FPS Graph", "Health Indicators", "Motion Blur", "Player Highlights", "Scoreboard Tweaks", "Zoom"));
        categories.put("SOCIAL", modules("Friend List", "Name Tags", "Ping Display", "Quick Messages", "Session Stats", "Staff List", "Staff Notifications"));
        categories.put("REGION MAP", List.of());
    }

    private static List<Entry> modules(String... names) {
        List<Entry> out = new ArrayList<>(); for (String name : names) out.add(new Entry(name)); return out;
    }

    @Override protected void init() {
        clearChildren();
        int left = Math.max(10, (width - Math.min(900, width - 20)) / 2);
        int top = Math.max(10, (height - Math.min(540, height - 20)) / 2);
        int w = Math.min(900, width - 20), h = Math.min(540, height - 20);
        int navY = top + 66, navW = Math.max(65, (w - 32) / categories.size()); int i = 0;
        for (String category : categories.keySet()) {
            final String cat = category;
            addDrawableChild(ButtonWidget.builder(Text.literal(category), b -> { activeCategory = cat; contextModule = null; scroll = 0; clearAndInit(); })
                .dimensions(left + 16 + i++ * navW, navY, navW - 4, 24).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Close  ×"), b -> close()).dimensions(left + w - 86, top + 14, 70, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset toggles"), b -> { ModuleManager.disableAll(); clearAndInit(); })
            .dimensions(left + w - 126, top + h - 32, 110, 20).build());

        if (!activeCategory.equals("REGION MAP")) {
            int panelTop = navY + 42, cols = w >= 650 ? 2 : 1, colW = (w - 48) / cols, row = 0;
            for (Entry module : categories.get(activeCategory)) {
                int col = row % cols, y = panelTop + (row / cols) * 38 - scroll; row++;
                if (y < panelTop - 35 || y > top + h - 55) continue;
                int x = left + 18 + col * (colW + 12);
                String label = (real(module.name) ? (ModuleManager.isOn(module.name) ? "●  " : "○  ") : "·  ") + module.name + (real(module.name) ? "" : "  (planned)");
                addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> { toggle(module.name); clearAndInit(); })
                    .dimensions(x, y, colW - 34, 30).build());
                addDrawableChild(ButtonWidget.builder(Text.literal("⚙"), b -> { contextModule = module.name; clearAndInit(); })
                    .dimensions(x + colW - 30, y, 30, 30).build());
            }
        }
        if (contextModule != null) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Toggle module"), b -> { toggle(contextModule); contextModule = null; clearAndInit(); })
                .dimensions(left + w / 2 - 95, top + h / 2 + 5, 190, 24).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> { contextModule = null; clearAndInit(); })
                .dimensions(left + w / 2 - 95, top + h / 2 + 33, 190, 22).build());
        }
    }

    @Override public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        int w = Math.min(900, width - 20), h = Math.min(540, height - 20);
        int left = Math.max(10, (width - w) / 2), top = Math.max(10, (height - h) / 2);
        ctx.fill(0, 0, width, height, 0xB8030710);
        ctx.fill(left, top, left + w, top + h, BG); ctx.fill(left, top, left + w, top + 3, BLUE);
        border(ctx, left, top, left + w, top + h, EDGE);
        ctx.drawText(textRenderer, "FY4X  /  CLIENT DASHBOARD", left + 18, top + 15, TEXT, false);
        ctx.drawText(textRenderer, "MODULAR INTERFACE   •   RIGHT SHIFT", left + 18, top + 34, MUTED, false);
        ctx.fill(left + 14, top + 96, left + w - 14, top + h - 44, PANEL);
        if (activeCategory.equals("REGION MAP")) renderRegionMap(ctx, left, top, w, h, mouseX, mouseY);
        else {
            ctx.drawText(textRenderer, activeCategory + "  /  " + categories.get(activeCategory).size() + " MODULES", left + 22, top + 108, BLUE, false);
            int enabled = ModuleManager.enabledCount();
            ctx.drawText(textRenderer, enabled + " enabled  •  click a module to toggle", left + 22, top + 122, MUTED, false);
        }
        if (contextModule != null) {
            ctx.fill(left + w/2 - 112, top + h/2 - 30, left + w/2 + 112, top + h/2 + 62, 0xFF101B2A);
            border(ctx, left + w/2 - 112, top + h/2 - 30, left + w/2 + 112, top + h/2 + 62, BLUE);
            ctx.drawCenteredTextWithShadow(textRenderer, "MODULE SETTINGS", left + w/2, top + h/2 - 20, TEXT);
            ctx.drawCenteredTextWithShadow(textRenderer, contextModule, left + w/2, top + h/2 - 5, MUTED);
            ctx.drawCenteredTextWithShadow(textRenderer, (real(contextModule) ? ModuleManager.get(contextModule).description : "Planned - not implemented yet"), left + w/2, top + h/2 - 1, MUTED);
        }
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawText(textRenderer, "FY4X  •  Entries marked (planned) are not implemented yet", left + 18, top + h - 21, MUTED, false);
    }

    private void renderRegionMap(DrawContext ctx, int left, int top, int w, int h, int mouseX, int mouseY) {
        ctx.drawText(textRenderer, "REGION MAP", left + 22, top + 108, BLUE, false);
        ctx.drawText(textRenderer, "Select a tile to inspect its grid coordinate", left + 22, top + 122, MUTED, false);
        int cols = 10, rows = 8, gap = 3;
        mapX = left + 24; mapY = top + 146; mapW = w - 48; mapH = h - 202;
        tileW = (mapW - (cols - 1) * gap) / cols; tileH = Math.max(16, (mapH - (rows - 1) * gap) / rows);
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++) {
            String id = "" + (char)('A' + c) + (r + 1);
            int x = mapX + c * (tileW + gap), y = mapY + r * (tileH + gap);
            int color = regionColor(c, r); if (id.equals(selectedRegion)) color = 0xFF168BFF;
            ctx.fill(x, y, x + tileW, y + tileH, color);
            border(ctx, x, y, x + tileW, y + tileH, id.equals(selectedRegion) ? 0xFFFFFFFF : 0xFF526779);
            ctx.drawCenteredTextWithShadow(textRenderer, id, x + tileW/2, y + Math.max(2, (tileH - 8)/2), TEXT);
        }
        ctx.drawText(textRenderer, "SELECTED: " + selectedRegion + "     GRID: " + selectedRegion.charAt(0) + ", " + selectedRegion.substring(1), left + 24, top + h - 61, TEXT, false);
    }

    private static int regionColor(int c, int r) {
        if (r < 2) return (c < 3) ? 0xFFB67B35 : (c < 7 ? 0xFF356D9D : 0xFF397C9A);
        if (r < 5) return (c < 2) ? 0xFFB7A14B : (c < 6 ? 0xFF438B79 : 0xFF3D729A);
        return (c < 3) ? 0xFF4C8A58 : (c < 7 ? 0xFF347C65 : 0xFF4D8B55);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 0 && "REGION MAP".equals(activeCategory) && tileW > 0) {
            int cols = 10, rows = 8, gap = 3;
            for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++) {
                int x = mapX + c * (tileW + gap), y = mapY + r * (tileH + gap);
                if (mouseX >= x && mouseX <= x + tileW && mouseY >= y && mouseY <= y + tileH) {
                    selectedRegion = "" + (char)('A' + c) + (r + 1); return true;
                }
            }
        }
        return false;
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!"REGION MAP".equals(activeCategory)) { scroll = Math.max(0, Math.min(400, scroll - (int)(verticalAmount * 38))); clearAndInit(); return true; }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    private static void border(DrawContext c, int x1, int y1, int x2, int y2, int color) {
        c.fill(x1,y1,x2,y1+1,color); c.fill(x1,y2-1,x2,y2,color); c.fill(x1,y1,x1+1,y2,color); c.fill(x2-1,y1,x2,y2,color);
    }
    private static boolean real(String name) { return ModuleManager.get(name) != null; }
    private static void toggle(String name) { Module m = ModuleManager.get(name); if (m != null) { m.toggle(); ModuleManager.save(); } }
    @Override public boolean shouldPause() { return false; }
    private record Entry(String name) {}
}
