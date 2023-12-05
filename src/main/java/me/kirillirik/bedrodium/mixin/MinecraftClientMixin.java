package me.kirillirik.bedrodium.mixin;

import me.kirillirik.bedrodium.Bedrodium;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin that changes metadata on world reload.
 *
 * @author VidTu
 */
@Mixin(MinecraftClient.class)
@Environment(EnvType.CLIENT)
public abstract class MinecraftClientMixin {
    /**
     * An instance of this class should not be created.
     *
     * @throws AssertionError Always
     */
    @Contract(value = "-> fail", pure = true)
    private MinecraftClientMixin() {
        throw new AssertionError("No instances.");
    }

    // Instructs Bedrodium to update info on world switch.
    @Inject(method = "setWorld", at = @At("RETURN"))
    public void bedrodium$setWorld$return(ClientWorld world, CallbackInfo ci) {
        // Skip world unloads.
        if (world == null) return;

        // Get the dimension
        Identifier dimension = world.getDimension().effects();

        // Hide floor in overworld and nether.
        Bedrodium.floorY = dimension.equals(DimensionTypes.OVERWORLD_ID) || dimension.equals(DimensionTypes.THE_NETHER_ID) ? world.getDimension().minY() : -1;

        // Hide ceiling in nether.
        Bedrodium.ceilingY = dimension.equals(DimensionTypes.THE_NETHER_ID) ? world.getDimension().logicalHeight() - 1 : -1;
    }
}
