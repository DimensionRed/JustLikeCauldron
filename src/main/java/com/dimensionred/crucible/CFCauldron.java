package com.dimensionred.crucible;

import com.dimensionred.*;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;

import java.util.stream.Stream;

public class CFCauldron extends CFAbstractCauldron {


    private static final float FILL_WITH_RAIN_CHANCE = 0.05F;
    private static final float FILL_WITH_SNOW_CHANCE = 0.1F;

    public static final DirectionProperty FACING = DirectionProperty.of("facing", Direction.Type.HORIZONTAL);
    public static  final IntProperty CUSTOM_DATA = IntProperty.of("data", 0, 7);
    public static VoxelShape SHAPE = Stream.of(
            Block.createCuboidShape(0, 3, 0, 2, 16, 16),
            Block.createCuboidShape(2, 3, 2, 14, 4, 14),
            Block.createCuboidShape(14, 3, 0, 16, 16, 16),
            Block.createCuboidShape(2, 3, 0, 14, 16, 2),
            Block.createCuboidShape(2, 3, 14, 14, 16, 16),
            Block.createCuboidShape(0, 0, 0, 4, 3, 2),
            Block.createCuboidShape(0, 0, 2, 2, 3, 4),
            Block.createCuboidShape(12, 0, 0, 16, 3, 2),
            Block.createCuboidShape(14, 0, 2, 16, 3, 4),
            Block.createCuboidShape(0, 0, 14, 4, 3, 16),
            Block.createCuboidShape(0, 0, 12, 2, 3, 14),
            Block.createCuboidShape(12, 0, 14, 16, 3, 16),
            Block.createCuboidShape(14, 0, 12, 16, 3, 14)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    public CFCauldron(FabricBlockSettings settings) {
        super(settings, CrucibleBehavior.EMPTY_CRUCIBLE_BEHAVIOR);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(CUSTOM_DATA, 0).with(FACING, Direction.NORTH));
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

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING).add(CUSTOM_DATA);
    }
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction facing = ctx.getPlayerFacing().getOpposite();
        return this.getDefaultState().with(FACING, facing).with(CUSTOM_DATA, 0);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        if (!world.isClient) {
            world.createAndScheduleBlockTick(pos, this, 20); // Schedules a tick
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockPos blockPos = PointedDripstoneBlock.getDripPos(world, pos);
        if (blockPos != null) {
            Fluid fluid = PointedDripstoneBlock.getDripFluid(world, blockPos);
            if (fluid == Fluids.LAVA && this.canBeFilledByDripstone(fluid)) {
                // 5.9% chance to fill with lava
                if (random.nextFloat() < (0.000837)) { //15.0 / 256.0
                    this.fillFromDripstone(state, world, pos, fluid);
                }
            } else if (fluid == Fluids.WATER && this.canBeFilledByDripstone(fluid)) {
                if (random.nextFloat() < (0.000837)) {
                    this.fillFromDripstone(state, world, pos, fluid);
                }


            }
        }
        world.createAndScheduleBlockTick(pos, this, 20); // Schedule the next tick
    }
}
