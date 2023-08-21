package nl.teamdiopside.separatedleaves.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
        String namespace = blockState.getBlock().arch$registryName().getNamespace();
        if (!(namespace.equals("minecraft") || namespace.equals("biomesoplenty") || namespace.equals("autumnity") || namespace.equals("quark") || namespace.equals("windswept") || namespace.equals("ecologist"))) {
            return;
        }

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
        String thisLeaves = thisState.getBlock().arch$registryName().getPath();
        String target = targetState.getBlock().arch$registryName().getPath();
        String thisWoodType = separatedLeaves$getWoodType(thisLeaves);
        String targetWoodType = separatedLeaves$getWoodType(target);

        String namespace = thisState.getBlock().arch$registryName().getNamespace();
        if (targetState.is(BlockTags.LOGS) && separatedLeaves$isCertainLog(thisWoodType, target, namespace)) {
                return 0;
        }
        if (targetState.getBlock() instanceof LeavesBlock && thisWoodType.equals(targetWoodType)) {
            return targetState.getValue(DISTANCE);
        }
        return 7;
    }

    @Unique
    private static String separatedLeaves$getWoodType(String string) {
        if (string.contains("flowering_")) {
            string = string.replace("flowering_", "");
        }
        return string.replace("_leaves", "");
    }

    @Unique
    private static boolean separatedLeaves$isCertainLog(String wood, String log, String namespace) {
        switch (wood) {
            case "azalea", "origin" -> wood = "oak";
            case "white_cherry", "pink_cherry", "snowblossom" -> wood = "cherry";
            case "maple" -> wood = namespace.equals("biomesoplenty") ? "oak" : "maple";
            case "orange_maple", "red_maple", "yellow_maple" -> wood = "maple";
            case "yellow_autumn", "rainbow_birch" -> wood = "birch";
            case "orange_autumn" -> wood = "dark_oak";
            case "red_blossom", "orange_blossom", "yellow_blossom", "blue_blossom", "lavender_blossom", "pink_blossom" -> wood = "blossom";
        }

        return log.equals(wood + "_log") ||
                log.equals(wood + "_wood") ||
                log.equals("stripped_" + wood + "_log") ||
                log.equals("stripped_" + wood + "_wood");
    }
}