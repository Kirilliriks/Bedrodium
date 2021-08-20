package me.kirillirik.bedrodium;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class Bedrodium implements ModInitializer {

    public static boolean passSide = true;
    private static KeyBinding keyBinding;

    @Override
    public void onInitialize() {
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
}
