package me.kirillirik.bedrodium;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

/**
 * Main Bedrodium class.
 *
 * @author kirillirik
 * @author VidTu
 */
@Environment(EnvType.CLIENT)
public final class Bedrodium implements ClientModInitializer {

    /**
     * Camera position controller
     */
    public static final CameraController cameraController = new CameraController();

    /**
     * Bedrodium toggle key.
     */
    private static KeyBinding KEY = new KeyBinding(
            "bedrodium.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "bedrodium.category"
    );

    /**
     * Whether the mod is enabled.
     */
    public static boolean enabled = true;

    /**
     * Current dimension floor Y, {@link Integer#MIN_VALUE} if none.
     */
    public static int floorY = Integer.MIN_VALUE;

    /**
     * Current dimension ceiling Y, {@link Integer#MAX_VALUE} if none.
     */
    public static int ceilingY = Integer.MAX_VALUE;

    @Override
    public void onInitializeClient() {
        // Register the key.
        KEY = KeyBindingHelper.registerKeyBinding(KEY);

        // Handle the key.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Key wasn't pressed.
            if (!KEY.wasPressed()) return;

            // Toggle the mod.
            enabled = !enabled;

            // Rerender the world.
            client.worldRenderer.reload();

            // Display the info.
            client.inGameHud.setOverlayMessage(
                    Text.translatable(enabled ? "bedrodium.toggle.on" : "bedrodium.toggle.off")
                            .formatted(enabled ? Formatting.GREEN : Formatting.RED, Formatting.BOLD),
                    false
            );
        });

        // Follow camera.
        ClientTickEvents.END_WORLD_TICK.register(world -> cameraController.handleEndTick());
    }

    /**
     * Checks if the block face should be rendered.
     *
     * @param pos    Block position
     * @param facing Rendered face
     * @return Whether the block face should be rendered
     */
    public static boolean shouldRender(@NotNull BlockPos pos, @NotNull Direction facing) {
        // Render if not enabled.
        if (!Bedrodium.enabled) return true;

        // Check the face.
        return switch (facing) {
            case DOWN -> cameraController.belowFloor || pos.getY() != floorY;
            case UP -> cameraController.aboveCeiling || pos.getY() != ceilingY;
            default -> true;
        };
    }
}
