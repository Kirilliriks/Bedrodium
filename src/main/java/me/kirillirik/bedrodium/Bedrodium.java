package me.kirillirik.bedrodium;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class Bedrodium implements ModInitializer {

    public static boolean PASS_SIDE = true;
    private static KeyBinding KEY_BINDING;
    private static MinecraftClient CLIENT;

    @Override
    public void onInitialize() {
        CLIENT = MinecraftClient.getInstance();
        KEY_BINDING = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Switch Bedrodium work",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_R,
                        "Bedrodium"
                )
        );
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!KEY_BINDING.wasPressed()) {
                return;
            }

            PASS_SIDE = !PASS_SIDE;
        });
    }

    public static boolean isValidHeight(int height) {
        return CLIENT.world != null && height != CLIENT.world.getBottomY();
    }
}
