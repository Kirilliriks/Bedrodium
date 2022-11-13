package me.kirillirik.bedrodium;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class Bedrodium implements ModInitializer {

    public static boolean passSide = true;
    private static KeyBinding keyBinding;
    private static MinecraftClient client;

    @Override
    public void onInitialize() {
        client = MinecraftClient.getInstance();
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Switch Bedrodium work",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "Bedrodium"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!keyBinding.wasPressed()) return;
            passSide = !passSide;
        });
    }

    public static boolean validY(int y) {
        return client.world != null && y != client.world.getBottomY();
    }
}
