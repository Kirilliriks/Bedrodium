package me.kirillirik.bedrodium;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;

public final class CameraController {

    /**
     * Whether the camera is below floor.
     */
    public boolean belowFloor = false;

    /**
     * Whether the camera is above ceiling.
     */
    public boolean aboveCeiling = false;

    /**
     * Tracking camera position at the end of a tick
     */
    public void handleEndTick() {
        // Render bedrock if camera is below or above it.
        final double cameraY = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().y;

        // Check various configurations.
        if (cameraY < Bedrodium.floorY) {
            // Below floor.

            // Re-render the floor. (if wasn't below it)
            if (!belowFloor) {
                belowFloor = true;
                renderLayers(false);
            }

            // Re-render the ceiling. (if was above it)
            if (aboveCeiling) {
                aboveCeiling = false;
                renderLayers(true);
            }
        } else if (cameraY > Bedrodium.ceilingY) {
            // Above ceiling.

            // Re-render the floor. (if was below it)
            if (belowFloor) {
                belowFloor = false;
                renderLayers(false);
            }

            // Re-render the ceiling. (if wasn't above it)
            if (!aboveCeiling) {
                aboveCeiling = true;
                renderLayers(true);
            }
        } else {
            // In the world.

            // Re-render the floor. (if was below it)
            if (belowFloor) {
                belowFloor = false;
                renderLayers(false);
            }

            // Re-render the ceiling. (if was above it)
            if (aboveCeiling) {
                aboveCeiling = false;
                renderLayers(true);
            }
        }
    }

    /**
     * Re-renders layers at the floor or at the ceiling.
     *
     * @param ceiling {@code true} to re-render layers at the ceiling, {@code false} to re-render at the floor
     */
    private void renderLayers(boolean ceiling) {
        // Get the world.
        final MinecraftClient client = MinecraftClient.getInstance();
        final ClientWorld world = client.world;

        // Skip if null.
        if (world == null) return;

        // Calculate the positions.
        final Vec3d camera = client.gameRenderer.getCamera().getPos();
        final int sx = ChunkSectionPos.getSectionCoord(camera.x);
        final int sy = ChunkSectionPos.getSectionCoord(ceiling ? Bedrodium.ceilingY : Bedrodium.floorY);
        final int sz = ChunkSectionPos.getSectionCoord(camera.z);

        // Get the distance.
        final WorldRenderer worldRenderer = client.worldRenderer;
        final int dist = (int) (worldRenderer.getViewDistance() + 1);

        // Schedule re-render for every block section.
        final int x1 = sx - dist;
        final int z1 = sz - dist;
        final int x2 = sx + dist;
        final int z2 = sz + dist;
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                worldRenderer.scheduleBlockRender(x, sy, z);
            }
        }
    }
}
