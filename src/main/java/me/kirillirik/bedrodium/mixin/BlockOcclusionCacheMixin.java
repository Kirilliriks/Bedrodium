package me.kirillirik.bedrodium.mixin;

import me.jellysquid.mods.sodium.client.render.occlusion.BlockOcclusionCache;
import me.kirillirik.bedrodium.Bedrodium;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockOcclusionCache.class, remap = false)
public final class BlockOcclusionCacheMixin {

    @Inject(method = "shouldDrawSide", at = @At("RETURN"), cancellable = true)
    public void shouldDrawSide(BlockState selfState, BlockView view, BlockPos pos, Direction facing, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (!Bedrodium.passSide) return;

        if (facing != Direction.DOWN || Bedrodium.validY(pos.getY())) return;
        callbackInfo.setReturnValue(false);
    }
}
