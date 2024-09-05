package com.dimensionred;

import com.dimensionred.crucible.*;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class CFRegistry {


    public static final Block CRUCIBLE = regBlock("crucible", new Crucible(FabricBlockSettings.of(Material.METAL).strength(2.0f).requiresTool().nonOpaque()));
    public static final Block WATER_CRUCIBLE = regBlock("water_crucible", new CFLeveledCauldron(FabricBlockSettings.of(Material.METAL).strength(2.0f).requiresTool().nonOpaque(),  CFLeveledCauldron.RAIN_PREDICATE, CrucibleBehavior.WATER_CRUCIBLE_BEHAVIOR));

    public static final Block LAVA_CRUCIBLE = regBlock("lava_crucible", new LavaCrucible(FabricBlockSettings.of(Material.METAL).strength(2.0f).requiresTool().nonOpaque().luminance((state) -> {return 15;}), CrucibleBehavior.LAVA_CRUCIBLE_BEHAVIOR));
    public static final Block POWDER_SNOW_CRUCIBLE = regBlock("powder_snow_crucible", new PowderSnowCrucible(FabricBlockSettings.of(Material.METAL).requiresTool().strength(2.0f).nonOpaque(),  CFLeveledCauldron.SNOW_PREDICATE, CrucibleBehavior.POWDER_SNOW_CRUCIBLE_BEHAVIOR));

    private static Block regBlock(String id, Block block){
        regBlockItem(id, block);
        return Registry.register(Registry.BLOCK, new Identifier(CauldronFix.MOD_ID, id), block);
    }

    private static Item regBlockItem(String id, Block block) {
        Item blockItem = Registry.register(Registry.ITEM, new Identifier(CauldronFix.MOD_ID, id), new BlockItem(block, new FabricItemSettings()));
        return blockItem;
    }


    public static void setup(){
        CauldronFix.LOGGER.info("Registering Blocks for " + CauldronFix.MOD_ID);
    }


}
