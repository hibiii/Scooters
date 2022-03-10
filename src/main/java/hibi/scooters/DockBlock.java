package hibi.scooters;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class DockBlock
extends Block {

	private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0f, 0f, 11f, 16f, 16f, 16f);
	private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0f, 0f, 0f, 5f, 16f, 16f);
	private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0f, 0f, 0f, 16f, 16f, 5f);
	private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(11f, 0f, 0f, 16f, 16f, 16f);
	public static final BooleanProperty POWERED = Properties.POWERED;
	public static final BooleanProperty CHARGING = BooleanProperty.of("charging");
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

	public DockBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
			.with(POWERED, false)
			.with(CHARGING, false));
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		switch(state.get(FACING)) {
			case EAST:
				return EAST_SHAPE;
			case NORTH:
				return NORTH_SHAPE;
			case WEST:
				return WEST_SHAPE;
			default:
			case SOUTH:
				return SOUTH_SHAPE;
		}
	}

	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		if(world.isClient) return;
		boolean hasPower = state.get(POWERED);
		if(hasPower != world.isReceivingRedstonePower(pos)) {
			if(hasPower)
				world.createAndScheduleBlockTick(pos, this, 4);
			else
				world.setBlockState(pos, state.cycle(POWERED), 2);
		}
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if(state.get(POWERED) && !world.isReceivingRedstonePower(pos))
			world.setBlockState(pos, state.cycle(POWERED), 2);
	}

	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		builder.add(POWERED).add(CHARGING).add(FACING);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return (BlockState)this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite()).with(POWERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

}
