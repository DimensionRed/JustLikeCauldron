package com.dimensionred;

import com.dimensionred.crucible.CFCauldron;
import com.dimensionred.crucible.CFLeveledCauldron;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;


public class CFEvents {


    public static void registerEvents() {
        //CHANGE CRUCIBLE DATA
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack itemStack = player.getStackInHand(hand);
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);


            if (itemStack.getItem() == Items.HONEYCOMB) {
                Block curBlock = state.getBlock();
                if (curBlock == CFRegistry.CRUCIBLE ||
                curBlock == CFRegistry.LAVA_CRUCIBLE ||
                curBlock == CFRegistry.WATER_CRUCIBLE ||
                curBlock == CFRegistry.POWDER_SNOW_CRUCIBLE) {
                    Integer data = (Integer)state.get(CFLeveledCauldron.CUSTOM_DATA);
                    int new_data = data + 1;
                    Direction facing = (Direction)state.get(CFLeveledCauldron.FACING);

                    if (new_data < 8) {

                        if (curBlock == CFRegistry.CRUCIBLE) {
                            BlockState newState = CFRegistry.CRUCIBLE.getDefaultState();
                            world.setBlockState(pos, newState.with(CFLeveledCauldron.FACING, facing).with(CFLeveledCauldron.CUSTOM_DATA, new_data));

                        } else if (curBlock == CFRegistry.LAVA_CRUCIBLE) {
                            BlockState newState = CFRegistry.LAVA_CRUCIBLE.getDefaultState();
                            world.setBlockState(pos, newState.with(CFLeveledCauldron.FACING, facing).with(CFLeveledCauldron.CUSTOM_DATA, new_data));

                        } else if (curBlock == CFRegistry.WATER_CRUCIBLE) {
                            int level = (Integer)state.get(CFLeveledCauldron.LEVEL);
                            BlockState newState = CFRegistry.WATER_CRUCIBLE.getDefaultState();
                            world.setBlockState(pos, newState.with(CFLeveledCauldron.FACING, facing).with(CFLeveledCauldron.CUSTOM_DATA, new_data).with(CFLeveledCauldron.LEVEL, level));

                        } else {
                            int level = (Integer)state.get(CFLeveledCauldron.LEVEL);
                            BlockState newState = CFRegistry.POWDER_SNOW_CRUCIBLE.getDefaultState();
                            world.setBlockState(pos, newState.with(CFLeveledCauldron.FACING, facing).with(CFLeveledCauldron.CUSTOM_DATA, new_data).with(CFLeveledCauldron.LEVEL, level));
                        }

                        if (world instanceof ServerWorld) {
                            ((ServerWorld) world).spawnParticles(ParticleTypes.WAX_ON, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0.1);
                        }


                        world.playSound(null, pos, SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        return ActionResult.SUCCESS;

                    }
                }
            }
            return ActionResult.PASS;
        });
        //WAX OFF CRUCIBLE
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack itemStack = player.getStackInHand(hand);
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);


            if (itemStack.getItem() instanceof AxeItem) {
                Block curBlock = state.getBlock();
                if (curBlock == CFRegistry.CRUCIBLE ||
                        curBlock == CFRegistry.LAVA_CRUCIBLE ||
                        curBlock == CFRegistry.WATER_CRUCIBLE ||
                        curBlock == CFRegistry.POWDER_SNOW_CRUCIBLE) {

                        if (curBlock == CFRegistry.CRUCIBLE) {
                            BlockState newState = Blocks.CAULDRON.getDefaultState();
                            world.setBlockState(pos, newState);

                        } else if (curBlock == CFRegistry.LAVA_CRUCIBLE) {
                            BlockState newState = Blocks.LAVA_CAULDRON.getDefaultState();
                            world.setBlockState(pos, newState);

                        } else if (curBlock == CFRegistry.WATER_CRUCIBLE) {
                            int level = (Integer)state.get(CFLeveledCauldron.LEVEL);
                            BlockState newState = Blocks.WATER_CAULDRON.getDefaultState();
                            world.setBlockState(pos, newState.with(LeveledCauldronBlock.LEVEL, level));

                        } else {
                            int level = (Integer)state.get(CFLeveledCauldron.LEVEL);
                            BlockState newState = Blocks.POWDER_SNOW_CAULDRON.getDefaultState();
                            world.setBlockState(pos, newState.with(LeveledCauldronBlock.LEVEL, level));
                        }

                        if (world instanceof ServerWorld) {
                            ((ServerWorld) world).spawnParticles(ParticleTypes.WAX_OFF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0.1);
                        }

                    if (!player.isCreative()) {
                        itemStack.damage(1, player, (p) -> p.sendToolBreakStatus(hand));
                    }

                        world.playSound(null, pos, SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        return ActionResult.SUCCESS;


                }
            }
            return ActionResult.PASS;
        });
        //WAX CAULDRON
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack itemStack = player.getStackInHand(hand);
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);


            if (itemStack.getItem() == Items.HONEYCOMB) {
                Block curBlock = state.getBlock();
                if (curBlock == Blocks.CAULDRON ||
                        curBlock == Blocks.LAVA_CAULDRON ||
                        curBlock == Blocks.WATER_CAULDRON ||
                        curBlock == Blocks.POWDER_SNOW_CAULDRON) {
                        Direction oppositeFacing = getOppositeDirection(player);

                        if (curBlock == Blocks.CAULDRON) {
                            BlockState newState = CFRegistry.CRUCIBLE.getDefaultState();
                            world.setBlockState(pos, newState.with(CFLeveledCauldron.FACING, oppositeFacing));

                        } else if (curBlock == Blocks.LAVA_CAULDRON) {
                            BlockState newState = CFRegistry.LAVA_CRUCIBLE.getDefaultState();
                            world.setBlockState(pos, newState.with(CFLeveledCauldron.FACING, oppositeFacing));

                        } else if (curBlock == Blocks.WATER_CAULDRON) {
                            int level = (Integer)state.get(LeveledCauldronBlock.LEVEL);
                            BlockState newState = CFRegistry.WATER_CRUCIBLE.getDefaultState();
                            world.setBlockState(pos, newState.with(CFLeveledCauldron.FACING, oppositeFacing).with(CFLeveledCauldron.LEVEL, level));

                        } else {
                            int level = (Integer)state.get(LeveledCauldronBlock.LEVEL);
                            BlockState newState = CFRegistry.POWDER_SNOW_CRUCIBLE.getDefaultState();
                            world.setBlockState(pos, newState.with(CFLeveledCauldron.FACING, oppositeFacing).with(CFLeveledCauldron.LEVEL, level));
                        }

                        if (world instanceof ServerWorld) {
                            ((ServerWorld) world).spawnParticles(ParticleTypes.WAX_ON, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0.1);
                        }

                         if (!player.isCreative()) {
                            itemStack.decrement(1);
                         }

                        world.playSound(null, pos, SoundEvents.ITEM_HONEYCOMB_WAX_ON, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        return ActionResult.SUCCESS;

                }
            }
            return ActionResult.PASS;
        });

    }

    private static Direction getOppositeDirection(PlayerEntity player) {
        return player.getHorizontalFacing().getOpposite();
    }



}
