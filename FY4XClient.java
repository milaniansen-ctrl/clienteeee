package com.alienmc.fy4x;

import com.alienmc.fy4x.modules.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class FY4XClient implements ClientModInitializer {
    private static KeyBinding openGui;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.fy4x.open_gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
            KeyBinding.Category.MISC
        ));
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> ModuleManager.renderHud(ctx, MinecraftClient.getInstance()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModuleManager.tick(client);
            while (openGui.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new FY4XScreen());
            }
        });
    }
}
