package com.dimensionred.crucible;

import com.dimensionred.CFRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;

import java.util.Map;
import java.util.function.Predicate;

public class CFLeveledCauldron extends CFAbstractCauldron {


    public static final IntProperty LEVEL;
    public static final DirectionProperty FACING = DirectionProperty.of("facing", Direction.Type.HORIZONTAL);
    public static  final IntProperty CUSTOM_DATA = IntProperty.of("data", 0, 7);

    public static final Predicate<Biome.Precipitation> RAIN_PREDICATE;
    public static final Predicate<Biome.Precipitation> SNOW_PREDICATE;
    private final Predicate<Biome.Precipitation> precipitationPredicate;
    public CFLeveledCauldron(FabricBlockSettings settings, Predicate<Biome.Precipitation> precipitationPredicate, Map<Item, CrucibleBehavior> behaviorMap) {
        super(settings, behaviorMap);
        this.precipitationPredicate = precipitationPredicate;
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(LEVEL, 1).with(CUSTOM_DATA, 0).with(FACING, Direction.NORTH));
    }
    public boolean isFull(BlockState state) {
        return (Integer)state.get(LEVEL) == 3;
    }

    protected boolean canBeFilledByDripstone(Fluid fluid) {
        return fluid == Fluids.WATER && this.precipitationPredicate == RAIN_PREDICATE;
    }
    protected double getFluidHeight(BlockState state) {
        return (6.0 + (double)(Integer)state.get(LEVEL) * 3.0) / 16.0;
    }


    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && entity.isOnFire() && this.isEntityTouchingFluid(state, pos, entity)) {
            entity.extinguish();
            if (entity.canModifyAt(world, pos)) {
                this.onFireCollision(state, world, pos);
            }
        }

    }
    public static void decrementFluidLevel(BlockState state, World world, BlockPos pos) {
        int i = (Integer)state.get(LEVEL) - 1;
        Integer data = (Integer)state.get(CFLeveledCauldron.CUSTOM_DATA);
        Direction facing = (Direction)state.get(CFLeveledCauldron.FACING);
        BlockState blockState = i == 0 ? CFRegistry.CRUCIBLE.getDefaultState() : (BlockState)state.with(LEVEL, i);
        world.setBlockState(pos, blockState.with(CFLeveledCauldron.FACING, facing).with(CFLeveledCauldron.CUSTOM_DATA, data));
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(blockState));
    }

    protected void onFireCollision(BlockState state, World world, BlockPos pos) {
        decrementFluidLevel(state, world, pos);
    }

    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return (Integer)state.get(LEVEL);
    }

    public void precipitationTick(BlockState state, World world, BlockPos pos, Biome.Precipitation precipitation) {
        if (CFCauldron.canFillWithPrecipitation(world, precipitation) && (Integer)state.get(LEVEL) != 3 && this.precipitationPredicate.test(precipitation)) {
            BlockState blockState = (BlockState)state.cycle(LEVEL);
            world.setBlockState(pos, blockState);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(blockState));
        }
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{LEVEL}).add(FACING).add(CUSTOM_DATA);
    }

    protected void fillFromDripstone(BlockState state, World world, BlockPos pos, Fluid fluid) {
        if (!this.isFull(state)) {
            Integer data = (Integer)state.get(CFLeveledCauldron.CUSTOM_DATA);
            Direction facing = (Direction)state.get(CFLeveledCauldron.FACING);
            BlockState blockState = (BlockState)state.with(LEVEL, (Integer)state.get(LEVEL) + 1).with(FACING, facing).with(CUSTOM_DATA, data);
            world.setBlockState(pos, blockState);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(blockState));
            world.syncWorldEvent(1047, pos, 0);
        }
    }

    static {
        LEVEL = Properties.LEVEL_3;
        RAIN_PREDICATE = (precipitation) -> {
            return precipitation == Biome.Precipitation.RAIN;
        };
        SNOW_PREDICATE = (precipitation) -> {
            return precipitation == Biome.Precipitation.SNOW;
        };
    }
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite()).with(CUSTOM_DATA, 0).with(LEVEL, 1);
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
            if (fluid == Fluids.WATER && this.canBeFilledByDripstone(fluid)) {
                // 5.9% chance to fill with lava
                if (random.nextFloat() < (0.000837)) { //15.0 / 256.0
                this.fillFromDripstone(state, world, pos, fluid);
                }
            }
        }
        world.createAndScheduleBlockTick(pos, this, 20); // Schedule the next tick
    }
}
