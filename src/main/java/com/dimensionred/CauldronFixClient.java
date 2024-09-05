package com.dimensionred;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class CauldronFixClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(CFRegistry.CRUCIBLE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(CFRegistry.WATER_CRUCIBLE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(CFRegistry.LAVA_CRUCIBLE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(CFRegistry.POWDER_SNOW_CRUCIBLE, RenderLayer.getCutout());

    }
}
