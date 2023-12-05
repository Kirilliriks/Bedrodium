package me.kirillirik.bedrodium.mixin;

import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache;
import me.kirillirik.bedrodium.Bedrodium;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin that prevents drawing bedrock layers.
 *
 * @author kirillirik
 * @author VidTu
 */
@Mixin(value = BlockOcclusionCache.class, remap = false)
@Environment(EnvType.CLIENT)
public abstract class BlockOcclusionCacheMixin {
    /**
     * An instance of this class should not be created.
     *
     * @throws AssertionError Always
     */
    @Contract(value = "-> fail", pure = true)
    private BlockOcclusionCacheMixin() {
        throw new AssertionError("No instances.");
    }

    // Injects into head of Sodium occlusion cache check to prevent rendering.
    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
    public void bedrodium$shouldDrawSide$head(BlockState selfState, BlockView view, BlockPos pos, Direction facing, CallbackInfoReturnable<Boolean> cir) {
        // Skip if should be rendered.
        if (Bedrodium.shouldRender(pos, facing)) return;

        // Skip rendering.
        cir.setReturnValue(false);
    }
}
