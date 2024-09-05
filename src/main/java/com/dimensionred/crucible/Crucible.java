package com.dimensionred.crucible;

import com.dimensionred.CFRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;

import java.util.stream.Stream;

public class Crucible extends CFCauldron {

    private static final float FILL_WITH_RAIN_CHANCE = 0.05F;
    private static final float FILL_WITH_SNOW_CHANCE = 0.1F;

    public Crucible(FabricBlockSettings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState());

    }
    public boolean isFull(BlockState state) {
        return false;
    }
    protected static boolean canFillWithPrecipitation(World world, Biome.Precipitation precipitation) {
        if (precipitation == Biome.Precipitation.RAIN) {
            return world.getRandom().nextFloat() < FILL_WITH_RAIN_CHANCE;
        } else if (precipitation == Biome.Precipitation.SNOW) {
            return world.getRandom().nextFloat() < FILL_WITH_SNOW_CHANCE;
        } else {
            return false;
        }
    }

    public void precipitationTick(BlockState state, World world, BlockPos pos, Biome.Precipitation precipitation) {
        if (canFillWithPrecipitation(world, precipitation)) {
            if (precipitation == Biome.Precipitation.RAIN) {
                Integer data = (Integer)state.get(CUSTOM_DATA);
                Direction facing = (Direction)state.get(FACING);
                world.setBlockState(pos, CFRegistry.WATER_CRUCIBLE.getDefaultState().with(CFLeveledCauldron.FACING, facing).with(CFLeveledCauldron.CUSTOM_DATA, data));
                world.emitGameEvent((Entity)null, GameEvent.BLOCK_CHANGE, pos);
            } else if (precipitation == Biome.Precipitation.SNOW) {
                Integer data = (Integer)state.get(CUSTOM_DATA);
                Direction facing = (Direction)state.get(FACING);
                world.setBlockState(pos, CFRegistry.POWDER_SNOW_CRUCIBLE.getDefaultState().with(CFLeveledCauldron.FACING, facing).with(CFLeveledCauldron.CUSTOM_DATA, data));
                world.emitGameEvent((Entity)null, GameEvent.BLOCK_CHANGE, pos);
            }

        }
    }
    protected boolean canBeFilledByDripstone(Fluid fluid) {
        return true;
    }

    protected void fillFromDripstone(BlockState state, World world, BlockPos pos, Fluid fluid) {
        BlockState blockState;
        if (fluid == Fluids.WATER) {
            Integer data = (Integer)state.get(CFLeveledCauldron.CUSTOM_DATA);
            Direction facing = (Direction)state.get(CFLeveledCauldron.FACING);
            blockState = CFRegistry.WATER_CRUCIBLE.getDefaultState();
            world.setBlockState(pos, blockState.with(CFLeveledCauldron.FACING, facing).with(CFLeveledCauldron.CUSTOM_DATA, data));
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(blockState));
            world.syncWorldEvent(1047, pos, 0);
        } else if (fluid == Fluids.LAVA) {
            Integer data = (Integer)state.get(CFLeveledCauldron.CUSTOM_DATA);
            Direction facing = (Direction)state.get(CFLeveledCauldron.FACING);
            blockState = CFRegistry.LAVA_CRUCIBLE.getDefaultState();
            world.setBlockState(pos, blockState.with(CFLeveledCauldron.FACING, facing).with(CFLeveledCauldron.CUSTOM_DATA, data));
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(blockState));
            world.syncWorldEvent(1046, pos, 0);
        }

    }

}
