package com.dimensionred.crucible;

import com.dimensionred.CFRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.Map;
import java.util.function.Predicate;


public interface CrucibleBehavior{



    Map<Item, CrucibleBehavior> EMPTY_CRUCIBLE_BEHAVIOR = createMap();
    Map<Item, CrucibleBehavior> WATER_CRUCIBLE_BEHAVIOR = createMap();
    Map<Item, CrucibleBehavior> LAVA_CRUCIBLE_BEHAVIOR = createMap();
    Map<Item, CrucibleBehavior> POWDER_SNOW_CRUCIBLE_BEHAVIOR = createMap();
    CrucibleBehavior FILL_WITH_WATER = (state, world, pos, player, hand, stack) -> {
        Integer data = (Integer)state.get(CFLeveledCauldron.CUSTOM_DATA);
        Direction facing = (Direction)state.get(CFLeveledCauldron.FACING);
        return fillCrucible(world, pos, player, hand, stack, (BlockState) CFRegistry.WATER_CRUCIBLE.getDefaultState().with(CFLeveledCauldron.LEVEL, 3).with(CFLeveledCauldron.FACING, facing).with(CFLeveledCauldron.CUSTOM_DATA, data), SoundEvents.ITEM_BUCKET_EMPTY);
    };
    CrucibleBehavior FILL_WITH_LAVA = (state, world, pos, player, hand, stack) -> {
        Integer data = (Integer)state.get(LavaCrucible.CUSTOM_DATA);
        Direction facing = (Direction)state.get(LavaCrucible.FACING);
        return fillCrucible(world, pos, player, hand, stack, CFRegistry.LAVA_CRUCIBLE.getDefaultState().with(LavaCrucible.FACING, facing).with(LavaCrucible.CUSTOM_DATA, data), SoundEvents.ITEM_BUCKET_EMPTY_LAVA);
    };
    CrucibleBehavior FILL_WITH_POWDER_SNOW = (state, world, pos, player, hand, stack) -> {
        Integer data = (Integer)state.get(CFLeveledCauldron.CUSTOM_DATA);
        Direction facing = (Direction)state.get(CFLeveledCauldron.FACING);
        return fillCrucible(world, pos, player, hand, stack, (BlockState)CFRegistry.POWDER_SNOW_CRUCIBLE.getDefaultState().with(CFLeveledCauldron.LEVEL, 3).with(CFLeveledCauldron.FACING, facing).with(CFLeveledCauldron.CUSTOM_DATA, data), SoundEvents.ITEM_BUCKET_EMPTY_POWDER_SNOW);
    };

    CrucibleBehavior CLEAN_SHULKER_BOX = (state, world, pos, player, hand, stack) -> {
        Block block = Block.getBlockFromItem(stack.getItem());
        if (!(block instanceof ShulkerBoxBlock)) {
            return ActionResult.PASS;
        } else {
            if (!world.isClient) {
                ItemStack itemStack = new ItemStack(Blocks.SHULKER_BOX);
                if (stack.hasNbt()) {
                    itemStack.setNbt(stack.getNbt().copy());
                }

                player.setStackInHand(hand, itemStack);
                player.incrementStat(Stats.CLEAN_SHULKER_BOX);
                CFLeveledCauldron.decrementFluidLevel(state, world, pos);
            }

            return ActionResult.success(world.isClient);
        }
    };
    CrucibleBehavior CLEAN_BANNER = (state, world, pos, player, hand, stack) -> {
        if (BannerBlockEntity.getPatternCount(stack) <= 0) {
            return ActionResult.PASS;
        } else {
            if (!world.isClient) {
                ItemStack itemStack = stack.copy();
                itemStack.setCount(1);
                BannerBlockEntity.loadFromItemStack(itemStack);
                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }

                if (stack.isEmpty()) {
                    player.setStackInHand(hand, itemStack);
                } else if (player.getInventory().insertStack(itemStack)) {
                    player.playerScreenHandler.syncState();
                } else {
                    player.dropItem(itemStack, false);
                }

                player.incrementStat(Stats.CLEAN_BANNER);
                CFLeveledCauldron.decrementFluidLevel(state, world, pos);
            }

            return ActionResult.success(world.isClient);
        }
    };
    CrucibleBehavior CLEAN_DYEABLE_ITEM = (state, world, pos, player, hand, stack) -> {
        Item item = stack.getItem();
        if (!(item instanceof DyeableItem dyeableItem)) {
            return ActionResult.PASS;
        } else if (!dyeableItem.hasColor(stack)) {
            return ActionResult.PASS;
        } else {
            if (!world.isClient) {
                dyeableItem.removeColor(stack);
                player.incrementStat(Stats.CLEAN_ARMOR);
                CFLeveledCauldron.decrementFluidLevel(state, world, pos);
            }

            return ActionResult.success(world.isClient);
        }
    };


    static Object2ObjectOpenHashMap<Item, CrucibleBehavior> createMap() {
        Object2ObjectOpenHashMap<Item, CrucibleBehavior> map = new Object2ObjectOpenHashMap<>();
        map.defaultReturnValue((state, world, pos, player, hand, stack) -> ActionResult.PASS);
        return map;
    }



    ActionResult interact(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack);

    static void registerBehavior() {
        CrucibleBehavior.registerBucketBehavior(EMPTY_CRUCIBLE_BEHAVIOR);
        EMPTY_CRUCIBLE_BEHAVIOR.put(Items.POTION, (state, world, pos, player, hand, stack) -> {
            if (PotionUtil.getPotion(stack) != Potions.WATER) {
                return ActionResult.PASS;
            } else {
                if (!world.isClient) {
                    Item item = stack.getItem();
                    Integer data = (Integer)state.get(CFLeveledCauldron.CUSTOM_DATA);
                    Direction facing = (Direction)state.get(CFLeveledCauldron.FACING);
                    player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                    player.incrementStat(Stats.USE_CAULDRON);
                    player.incrementStat(Stats.USED.getOrCreateStat(item));
                    world.setBlockState(pos, CFRegistry.WATER_CRUCIBLE.getDefaultState().with(CFLeveledCauldron.FACING, facing).with(CFLeveledCauldron.CUSTOM_DATA, data));
                    world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.emitGameEvent((Entity)null, GameEvent.FLUID_PLACE, pos);
                }

                return ActionResult.success(world.isClient);
            }
        });

        CrucibleBehavior.registerBucketBehavior(WATER_CRUCIBLE_BEHAVIOR);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.BUCKET, (state, world, pos, player, hand, stack) -> {
            return emptyCrucible(state, world, pos, player, hand, stack, new ItemStack(Items.WATER_BUCKET), (statex) -> {
                return (Integer)statex.get(CFLeveledCauldron.LEVEL) == 3;
            }, SoundEvents.ITEM_BUCKET_FILL);
        });
        WATER_CRUCIBLE_BEHAVIOR.put(Items.GLASS_BOTTLE, (state, world, pos, player, hand, stack) -> {
            if (!world.isClient) {
                Item item = stack.getItem();
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER)));
                player.incrementStat(Stats.USE_CAULDRON);
                player.incrementStat(Stats.USED.getOrCreateStat(item));
                CFLeveledCauldron.decrementFluidLevel(state, world, pos);
                world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.emitGameEvent((Entity)null, GameEvent.FLUID_PICKUP, pos);
            }

            return ActionResult.success(world.isClient);
        });
        WATER_CRUCIBLE_BEHAVIOR.put(Items.POTION, (state, world, pos, player, hand, stack) -> {
            if ((Integer)state.get(CFLeveledCauldron.LEVEL) != 3 && PotionUtil.getPotion(stack) == Potions.WATER) {
                if (!world.isClient) {
                    player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                    player.incrementStat(Stats.USE_CAULDRON);
                    player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
                    world.setBlockState(pos, (BlockState)state.cycle(CFLeveledCauldron.LEVEL));
                    world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.emitGameEvent((Entity)null, GameEvent.FLUID_PLACE, pos);
                }

                return ActionResult.success(world.isClient);
            } else {
                return ActionResult.PASS;
            }
        });



        WATER_CRUCIBLE_BEHAVIOR.put(Items.LEATHER_BOOTS, CLEAN_DYEABLE_ITEM);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.LEATHER_LEGGINGS, CLEAN_DYEABLE_ITEM);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.LEATHER_CHESTPLATE, CLEAN_DYEABLE_ITEM);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.LEATHER_HELMET, CLEAN_DYEABLE_ITEM);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.LEATHER_HORSE_ARMOR, CLEAN_DYEABLE_ITEM);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.WHITE_BANNER, CLEAN_BANNER);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.GRAY_BANNER, CLEAN_BANNER);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.BLACK_BANNER, CLEAN_BANNER);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.BLUE_BANNER, CLEAN_BANNER);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.BROWN_BANNER, CLEAN_BANNER);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.CYAN_BANNER, CLEAN_BANNER);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.GREEN_BANNER, CLEAN_BANNER);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.LIGHT_BLUE_BANNER, CLEAN_BANNER);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.LIGHT_GRAY_BANNER, CLEAN_BANNER);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.LIME_BANNER, CLEAN_BANNER);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.MAGENTA_BANNER, CLEAN_BANNER);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.ORANGE_BANNER, CLEAN_BANNER);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.PINK_BANNER, CLEAN_BANNER);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.PURPLE_BANNER, CLEAN_BANNER);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.RED_BANNER, CLEAN_BANNER);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.YELLOW_BANNER, CLEAN_BANNER);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.WHITE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.GRAY_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.BLACK_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.BLUE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.BROWN_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.CYAN_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.GREEN_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.LIGHT_BLUE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.LIGHT_GRAY_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.LIME_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.MAGENTA_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.ORANGE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.PINK_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.PURPLE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.RED_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CRUCIBLE_BEHAVIOR.put(Items.YELLOW_SHULKER_BOX, CLEAN_SHULKER_BOX);

        LAVA_CRUCIBLE_BEHAVIOR.put(Items.BUCKET, (state, world, pos, player, hand, stack) -> {
            return emptyCrucible(state, world, pos, player, hand, stack, new ItemStack(Items.LAVA_BUCKET), (statex) -> {
                return true;
            }, SoundEvents.ITEM_BUCKET_FILL_LAVA);
        });
        registerBucketBehavior(LAVA_CRUCIBLE_BEHAVIOR);
        POWDER_SNOW_CRUCIBLE_BEHAVIOR.put(Items.BUCKET, (state, world, pos, player, hand, stack) -> {
            return emptyCrucible(state, world, pos, player, hand, stack, new ItemStack(Items.POWDER_SNOW_BUCKET), (statex) -> {
                return (Integer)statex.get(LeveledCauldronBlock.LEVEL) == 3;
            }, SoundEvents.ITEM_BUCKET_FILL_POWDER_SNOW);
        });
        registerBucketBehavior(POWDER_SNOW_CRUCIBLE_BEHAVIOR);
    }


    static void registerBucketBehavior(Map<Item, CrucibleBehavior> behavior) {
        behavior.put(Items.LAVA_BUCKET, FILL_WITH_LAVA);
        behavior.put(Items.WATER_BUCKET, FILL_WITH_WATER);
        behavior.put(Items.POWDER_SNOW_BUCKET, FILL_WITH_POWDER_SNOW);
    }


    static ActionResult emptyCrucible(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack, ItemStack output, Predicate<BlockState> fullPredicate, SoundEvent soundEvent) {
        if (!fullPredicate.test(state)) {
            return ActionResult.PASS;
        } else {
            if (!world.isClient) {
                Item item = stack.getItem();
                Integer data = (Integer)state.get(CFLeveledCauldron.CUSTOM_DATA);
                Direction facing = (Direction)state.get(CFLeveledCauldron.FACING);
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, output));
                player.incrementStat(Stats.USE_CAULDRON);
                player.incrementStat(Stats.USED.getOrCreateStat(item));
                world.setBlockState(pos, CFRegistry.CRUCIBLE.getDefaultState().with(CFLeveledCauldron.FACING, facing).with(CFLeveledCauldron.CUSTOM_DATA, data));
                world.playSound((PlayerEntity)null, pos, soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.emitGameEvent((Entity)null, GameEvent.FLUID_PICKUP, pos);
            }

            return ActionResult.success(world.isClient);
        }
    }



    static ActionResult fillCrucible(World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack, BlockState state, SoundEvent soundEvent) {
        if (!world.isClient) {
            Item item = stack.getItem();
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.BUCKET)));
            player.incrementStat(Stats.FILL_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(item));
            world.setBlockState(pos, state);
            world.playSound((PlayerEntity)null, pos, soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.emitGameEvent((Entity)null, GameEvent.FLUID_PLACE, pos);
        }

        return ActionResult.success(world.isClient);
    }

}
