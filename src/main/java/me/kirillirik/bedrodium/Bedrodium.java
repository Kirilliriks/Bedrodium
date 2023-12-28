package me.kirillirik.bedrodium;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
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
     * Bedrodium toggle key.
     */
    private static KeyBinding KEY = new KeyBinding("bedrodium.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "bedrodium.category");

    /**
     * Bedrodium channel.
     */
    private static final Identifier CHANNEL = new Identifier("bedrodium", "v1");

    /**
     * Whether the mod is enabled.
     */
    public static boolean enabled = true;

    /**
     * Whether the mod is disabled by the server.
     */
    public static boolean serverDisabled = false;

    /**
     * Current dimension floor Y, {@link Integer#MIN_VALUE} if none.
     */
    public static int floorY = Integer.MIN_VALUE;

    /**
     * Current dimension ceiling Y, {@link Integer#MAX_VALUE} if none.
     */
    public static int ceilingY = Integer.MAX_VALUE;

    /**
     * Whether the camera is below floor.
     */
    public static boolean cameraBelowFloor = false;

    /**
     * Whether the camera is above ceiling.
     */
    public static boolean cameraAboveCeiling = false;

    @Override
    public void onInitializeClient() {
        // Register the key.
        KEY = KeyBindingHelper.registerKeyBinding(KEY);

        // Handle the key.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Key wasn't pressed.
            if (!KEY.wasPressed()) return;

            // Mod disabled by server.
            if (serverDisabled) {
                client.inGameHud.setOverlayMessage(Text.translatable("bedrodium.toggle.server")
                        .formatted(Formatting.DARK_RED, Formatting.BOLD), false);
                return;
            }

            // Toggle the mod.
            enabled = !enabled;

            // Rerender the world.
            client.worldRenderer.reload();

            // Display the info.
            client.inGameHud.setOverlayMessage(Text.translatable(enabled ? "bedrodium.toggle.on" : "bedrodium.toggle.off")
                    .formatted(enabled ? Formatting.GREEN : Formatting.RED, Formatting.BOLD), false);
        });

        // Follow camera.
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            // Render bedrock if camera is below or above it.
            Vec3d camera = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

            // Check various configurations.
            if (camera.y < floorY) {
                // Below floor.

                // Re-render the floor. (if wasn't below it)
                if (!cameraBelowFloor) {
                    cameraBelowFloor = true;
                    renderLayers(false);
                }

                // Re-render the ceiling. (if was above it)
                if (cameraAboveCeiling) {
                    cameraAboveCeiling = false;
                    renderLayers(true);
                }
            } else if (camera.y > ceilingY) {
                // Above ceiling.

                // Re-render the floor. (if was below it)
                if (cameraBelowFloor) {
                    cameraBelowFloor = false;
                    renderLayers(false);
                }

                // Re-render the ceiling. (if wasn't above it)
                if (!cameraAboveCeiling) {
                    cameraAboveCeiling = true;
                    renderLayers(true);
                }
            } else {
                // In the world.

                // Re-render the floor. (if was below it)
                if (cameraBelowFloor) {
                    cameraBelowFloor = false;
                    renderLayers(false);
                }

                // Re-render the ceiling. (if was above it)
                if (cameraAboveCeiling) {
                    cameraAboveCeiling = false;
                    renderLayers(true);
                }
            }
        });

        // Handle networking.
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL, (client, handler, buf, sender) -> {
            try {
                // Listen to server.
                serverDisabled = buf.readBoolean();
            } catch (Throwable ignored) {
                // Disable if unknown data. (for clarity)
                serverDisabled = true;
            }

            // Schedule to main thread.
            client.execute(() -> {
                // Rerender the world.
                client.worldRenderer.reload();

                // Display the info.
                client.inGameHud.setOverlayMessage(Text.translatable(serverDisabled ? "bedrodium.toggle.server" : (enabled ? "bedrodium.toggle.on" : "bedrodium.toggle.off"))
                        .formatted(serverDisabled ? Formatting.DARK_RED : (enabled ? Formatting.GREEN : Formatting.RED), Formatting.BOLD), false);
            });
        });

        // Enable on join.
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> serverDisabled = false);

        // Enable on quit.
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> serverDisabled = false);
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
        if (!Bedrodium.enabled || Bedrodium.serverDisabled) return true;

        // Check the face.
        return switch (facing) {
            case DOWN -> cameraBelowFloor || pos.getY() != floorY;
            case UP -> cameraAboveCeiling || pos.getY() != ceilingY;
            default -> true;
        };
    }

    /**
     * Re-renders layers at the floor or at the ceiling.
     *
     * @param ceiling {@code true} to re-render layers at the ceiling, {@code false} to re-render at the floor
     */
    private static void renderLayers(boolean ceiling) {
        // Get the world.
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;

        // Skip if null.
        if (world == null) return;

        // Calculate the positions.
        Vec3d camera = client.gameRenderer.getCamera().getPos();
        int sx = ChunkSectionPos.getSectionCoord(camera.x);
        int sy = ChunkSectionPos.getSectionCoord(ceiling ? ceilingY : floorY);
        int sz = ChunkSectionPos.getSectionCoord(camera.z);

        // Get the distance.
        WorldRenderer worldRenderer = client.worldRenderer;
        int dist = (int) (worldRenderer.getViewDistance() + 1);

        // Schedule re-render for every block section.
        int x1 = sx - dist;
        int z1 = sz - dist;
        int x2 = sx + dist;
        int z2 = sz + dist;
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                 worldRenderer.scheduleBlockRender(x, sy, z);
            }
        }
    }
}
