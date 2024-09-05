package com.dimensionred.crucible;

import com.dimensionred.CFRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Map;
import java.util.function.Predicate;

public class PowderSnowCrucible extends CFLeveledCauldron {


    private final Map<Item, CrucibleBehavior> map;

    public PowderSnowCrucible(FabricBlockSettings settings, Predicate<Biome.Precipitation> precipitationPredicate, Map<Item, CrucibleBehavior> behaviorMap) {
        super(settings, precipitationPredicate, behaviorMap);
        this.map = behaviorMap;
    }

    protected void onFireCollision(BlockState state, World world, BlockPos pos) {
        decrementFluidLevel((BlockState) CFRegistry.WATER_CRUCIBLE.getDefaultState().with(LEVEL, (Integer)state.get(LEVEL)).with(CUSTOM_DATA, (Integer)state.get(CUSTOM_DATA)).with(FACING, (Direction)state.get(FACING)), world, pos);
    }

}
