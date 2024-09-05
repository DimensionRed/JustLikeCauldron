package com.dimensionred.crucible;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Map;

public class LavaCrucible extends CFAbstractCauldron {

    public static final DirectionProperty FACING = DirectionProperty.of("facing", Direction.Type.HORIZONTAL);
    public static  final IntProperty CUSTOM_DATA = IntProperty.of("data", 0, 7);
    private final Map<Item, CrucibleBehavior> map;
    public LavaCrucible(FabricBlockSettings settings, Map<Item, CrucibleBehavior> behaviorMap) {
        super(settings, CrucibleBehavior.LAVA_CRUCIBLE_BEHAVIOR);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(CUSTOM_DATA, 0));
        this.map = behaviorMap;
    }

    protected double getFluidHeight(BlockState state) {
        return 0.9375;
    }

    public boolean isFull(BlockState state) {
        return true;
    }

    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (this.isEntityTouchingFluid(state, pos, entity)) {
            entity.setOnFireFromLava();
        }

    }

    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return 3;
    }


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING).add(CUSTOM_DATA);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
    }
    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
}
