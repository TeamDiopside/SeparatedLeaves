package nl.teamdiopside.separatedleaves.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import nl.teamdiopside.separatedleaves.Reload;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin {
    @Shadow @Final public static IntegerProperty DISTANCE;

    @Inject(method = "updateDistance", at = @At("HEAD"), cancellable = true)
    private static void updateDistance(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, CallbackInfoReturnable<BlockState> cir) {
        boolean hasFile = false;
        int i = 7;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        Block thisBlock = levelAccessor.getBlockState(blockPos).getBlock();
        for (Direction direction : Direction.values()) {
            mutableBlockPos.setWithOffset(blockPos, direction);
            BlockState targetState = levelAccessor.getBlockState(mutableBlockPos);
            Block targetBlock = targetState.getBlock();
            for (Reload.LeavesRule rule : Reload.LEAVES_RULES) {
                if (!rule.leaves().contains(thisBlock)) {
                    continue;
                }
                hasFile = true;
                if (rule.logs().contains(targetBlock)) {
                    i = 1;
                    break;
                } else if (targetBlock instanceof LeavesBlock && rule.leaves().contains(targetBlock)) {
                    i = Math.min(i, targetState.getValue(DISTANCE) + 1);
                }
            }
            if (i == 1) break;
        }
        if (hasFile) {
            cir.setReturnValue(blockState.setValue(DISTANCE, i));
        }
    }
}