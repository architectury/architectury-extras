/*
 * This file is part of architectury, licensed under the MIT License.
 * Copyright (c) 2017 - Original Author is MrRiegel as seen here https://github.com/MrRiegel
 * - Secondary author  Sam Bassett aka Lothrazar https://minecraft.curseforge.com/members/Lothrazar
 * Copyright (c) 2020, 2021, 2022 architectury
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.lothrazar.storagenetwork.block.cable;

import com.google.common.collect.Maps;
import com.lothrazar.storagenetwork.api.IConnectable;
import com.lothrazar.storagenetwork.api.IConnectableItemAutoIO;
import com.lothrazar.storagenetwork.block.BaseBlock;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.capability.CapabilityConnectableAutoIO;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.registry.StorageNetworkCapabilities;
import dev.architectury.transfer.TransferHandler;
import dev.architectury.transfer.item.ItemTransfer;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class BlockCable extends BaseBlock implements SimpleWaterloggedBlock {
    
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    
    public BlockCable(String registryName) {
        super(Block.Properties.of(Material.STONE).strength(0.2F), registryName);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, EnumConnectType.NONE).setValue(EAST, EnumConnectType.NONE)
                .setValue(SOUTH, EnumConnectType.NONE).setValue(WEST, EnumConnectType.NONE)
                .setValue(UP, EnumConnectType.NONE).setValue(DOWN, EnumConnectType.NONE).setValue(WATERLOGGED, false));
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;
        return super.getStateForPlacement(context).setValue(WATERLOGGED, Boolean.valueOf(flag));
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
    
    @Deprecated
    @Override
    public boolean isPathfindable(BlockState bs, BlockGetter bg, BlockPos pos, PathComputationType path) {
        return false;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity != null) {
                TransferHandler<ItemStack> items = ItemTransfer.BLOCK.get(tileentity, null);
                if (items != null) {
                    items.forEachContent(view -> {
                        Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), view.getResource().copy());
                    });
                    worldIn.updateNeighbourForOutputSignal(pos, this);
                }
                IConnectableItemAutoIO connectable = StorageNetworkCapabilities.CONNECTABLE_AUTO_IO.get(tileentity, null);
                if (connectable instanceof CapabilityConnectableAutoIO) {
                    CapabilityConnectableAutoIO filterCable = (CapabilityConnectableAutoIO) connectable;
                    filterCable.upgrades.forEachContent(view -> {
                        Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), view.getResource().copy());
                    });
                    worldIn.updateNeighbourForOutputSignal(pos, this);
                }
            }
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }
    
    public static BlockState cleanBlockState(BlockState state) {
        for (Direction d : Direction.values()) {
            EnumProperty<EnumConnectType> prop = FACING_TO_PROPERTY_MAP.get(d);
            if (state.getValue(prop) == EnumConnectType.INVENTORY) {
                //dont replace cable types only inv types
                state = state.setValue(prop, EnumConnectType.NONE);
            }
        }
        return state;
    }
    
    private static final EnumProperty<EnumConnectType> DOWN = EnumProperty.create("down", EnumConnectType.class);
    private static final EnumProperty<EnumConnectType> UP = EnumProperty.create("up", EnumConnectType.class);
    private static final EnumProperty<EnumConnectType> NORTH = EnumProperty.create("north", EnumConnectType.class);
    private static final EnumProperty<EnumConnectType> SOUTH = EnumProperty.create("south", EnumConnectType.class);
    private static final EnumProperty<EnumConnectType> WEST = EnumProperty.create("west", EnumConnectType.class);
    private static final EnumProperty<EnumConnectType> EAST = EnumProperty.create("east", EnumConnectType.class);
    public static final Map<Direction, EnumProperty<EnumConnectType>> FACING_TO_PROPERTY_MAP = Util.make(Maps.newEnumMap(Direction.class), (p) -> {
        p.put(Direction.NORTH, NORTH);
        p.put(Direction.EAST, EAST);
        p.put(Direction.SOUTH, SOUTH);
        p.put(Direction.WEST, WEST);
        p.put(Direction.UP, UP);
        p.put(Direction.DOWN, DOWN);
    });
    private static final double top = 16;
    private static final double bot = 0;
    private static final double C = 8;
    private static final double w = 2;
    private static final double sm = C - w;
    private static final double lg = C + w;
    //(double x1, double y1, double z1, double x2, double y2, double z2)
    private static final VoxelShape AABB = Block.box(sm, sm, sm, lg, lg, lg);
    //Y for updown
    private static final VoxelShape AABB_UP = Block.box(sm, sm, sm, lg, top, lg);
    private static final VoxelShape AABB_DOWN = Block.box(sm, bot, sm, lg, lg, lg);
    //Z for n-s
    private static final VoxelShape AABB_NORTH = Block.box(sm, sm, bot, lg, lg, lg);
    private static final VoxelShape AABB_SOUTH = Block.box(sm, sm, sm, lg, lg, top);
    //X for e-w
    private static final VoxelShape AABB_WEST = Block.box(bot, sm, sm, lg, lg, lg);
    private static final VoxelShape AABB_EAST = Block.box(sm, sm, sm, top, lg, lg);
    
    private boolean shapeConnects(BlockState state, EnumProperty<EnumConnectType> dirctionProperty) {
        return state.getValue(dirctionProperty).equals(EnumConnectType.CABLE)
                || state.getValue(dirctionProperty).equals(EnumConnectType.INVENTORY);
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return ShapeCache.getOrCreate(state, this::createShape);
    }
    
    private VoxelShape createShape(BlockState state) {
        VoxelShape shape = AABB;
        if (shapeConnects(state, UP)) {
            shape = Shapes.joinUnoptimized(shape, AABB_UP, BooleanOp.OR);
        }
        if (shapeConnects(state, DOWN)) {
            shape = Shapes.joinUnoptimized(shape, AABB_DOWN, BooleanOp.OR);
        }
        if (state.getValue(WEST).equals(EnumConnectType.CABLE)) {
            shape = Shapes.joinUnoptimized(shape, AABB_WEST, BooleanOp.OR);
        }
        if (state.getValue(EAST).equals(EnumConnectType.CABLE)) {
            shape = Shapes.joinUnoptimized(shape, AABB_EAST, BooleanOp.OR);
        }
        if (state.getValue(NORTH).equals(EnumConnectType.CABLE)) {
            shape = Shapes.joinUnoptimized(shape, AABB_NORTH, BooleanOp.OR);
        }
        if (state.getValue(SOUTH).equals(EnumConnectType.CABLE)) {
            shape = Shapes.joinUnoptimized(shape, AABB_SOUTH, BooleanOp.OR);
        }
        return shape;
    }
    
    @Override
    public RenderShape getRenderShape(BlockState bs) {
        return RenderShape.MODEL;
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileCable(pos, state);
    }
    
    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState stateIn, LivingEntity placer, ItemStack stack) {
        BlockState facingState;
        for (Direction d : Direction.values()) {
            BlockPos posoff = pos.relative(d);
            facingState = worldIn.getBlockState(posoff);
            BlockEntity tileOffset = worldIn.getBlockEntity(posoff);
            IConnectable cap = null;
            if (tileOffset != null) {
                cap = StorageNetworkCapabilities.CONNECTABLE_CAPABILITY.get(tileOffset, null);
            }
            if (cap != null
                    || facingState.getBlock() == SsnRegistry.MAIN) {
                stateIn = stateIn.setValue(FACING_TO_PROPERTY_MAP.get(d), EnumConnectType.CABLE);
                worldIn.setBlockAndUpdate(pos, stateIn);
            }
        }
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(UP, DOWN, NORTH, EAST, SOUTH, WEST, WATERLOGGED);
    }
    
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
//    if (stateIn.getValue(WATERLOGGED)) {
//      world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
//    }
        EnumProperty<EnumConnectType> property = FACING_TO_PROPERTY_MAP.get(facing);
        if (facingState.getBlock() == SsnRegistry.MAIN
                || facingState.getBlock() instanceof BlockCable) {
            //      StorageNetwork.log("plain cable" + facingState.getBlock());
            return stateIn.setValue(property, EnumConnectType.CABLE);
        } //
        //based on capability you have, edit connection type
        BlockEntity tileOffset = world.getBlockEntity(facingPos);
        IConnectable cap = null;
        if (tileOffset != null) {
            cap = StorageNetworkCapabilities.CONNECTABLE_CAPABILITY.get(tileOffset, null);
        }
        if (cap != null) {
            //      StorageNetwork.log("EARLY EXIT block" + facingState.getBlock() + " has cap " + cap);
            if (cap.getMainPos() != null) {
                //its a network bock of some type, knows where network is but not exactly inventory
                return stateIn.setValue(property, EnumConnectType.INVENTORY);
            }
            return stateIn.setValue(property, EnumConnectType.CABLE);
        }
        //if i have zero other inventories, and this is one now, ok go invo
        if (!this.hasInventoryAlready(stateIn)
                && isInventory(stateIn, facing, facingState, world, currentPos, facingPos)) {
            return stateIn.setValue(property, EnumConnectType.INVENTORY);
        }
        return stateIn.setValue(property, EnumConnectType.NONE);
    }
    
    //only one inventory allowed per link cable eh
    private boolean hasInventoryAlready(BlockState stateIn) {
        for (Direction d : Direction.values()) {
            if (stateIn.getValue(FACING_TO_PROPERTY_MAP.get(d)).isInventory()) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isInventory(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        if (facing == null || world.isClientSide()) {
            return false;
        }
        if (!TileMain.isTargetAllowed(facingState)) {
            return false;
        }
        BlockEntity neighbor = world.getBlockEntity(facingPos);
        return neighbor != null && ItemTransfer.BLOCK.get(neighbor, facing.getOpposite()) != null;
    }
}
