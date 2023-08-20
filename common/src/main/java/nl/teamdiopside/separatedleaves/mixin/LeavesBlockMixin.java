package nl.teamdiopside.separatedleaves.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.level.block.LeavesBlock.DISTANCE;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin {
    @Inject(method = "updateDistance", at = @At("HEAD"), cancellable = true)
    private static void updateDistance(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, CallbackInfoReturnable<BlockState> cir) {
        int i = 7;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            mutableBlockPos.setWithOffset(blockPos, direction);
            i = Math.min(i, separatedLeaves$getDistance(blockState, levelAccessor.getBlockState(mutableBlockPos)) + 1);
            if (i == 1) break;
        }
        cir.setReturnValue(blockState.setValue(DISTANCE, i));
    }

    @Unique
    private static int separatedLeaves$getDistance(BlockState thisState, BlockState targetState) {
        String thisLeaves = Registry.BLOCK.getKey(thisState.getBlock()).getPath();
        String target = Registry.BLOCK.getKey(targetState.getBlock()).getPath();
        String thisWoodType = separatedLeaves$getWoodType(thisLeaves);
        String targetWoodType = separatedLeaves$getWoodType(target);

        if (targetState.is(BlockTags.LOGS) && separatedLeaves$isCertainLog(thisWoodType, target)) {
                return 0;
        }
        if (targetState.getBlock() instanceof LeavesBlock && thisWoodType.equals(targetWoodType)) {
            return targetState.getValue(DISTANCE);
        }
        return 7;
    }

    @Unique
    private static String separatedLeaves$getWoodType(String string) {
        if (string.equals("alazea_leaves") || string.equals("flowering_azalea_leaves")) {
            return "oak";
        } else {
            return string.replace("_leaves", "");
        }
    }

    @Unique
    private static boolean separatedLeaves$isCertainLog(String wood, String log) {
        return log.equals(wood + "_log") ||
                log.equals(wood + "_wood") ||
                log.equals("stripped_" + wood + "_log") ||
                log.equals("stripped_" + wood + "_wood");
    }
}