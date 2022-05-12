package hibi.scooters;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class DockBlock
extends BlockWithEntity {

	// TODO Rename to north-south and east-west
	private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(2f, 0f, 5f, 14f, 16f, 11f);
	private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(5f, 0f, 2f, 11f, 16f, 14f);
	public static final BooleanProperty POWERED = Properties.POWERED;
	public static final BooleanProperty CHARGING = BooleanProperty.of("charging");
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

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
			case WEST:
				return EAST_SHAPE;
			case NORTH:
			case SOUTH:
			default:
				return NORTH_SHAPE;
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
				world.setBlockState(pos, state.cycle(POWERED));
		}
		
		DockBlockEntity.validateCharging(state, (ServerWorld)world, pos, (DockBlockEntity)world.getBlockEntity(pos));
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if(state.get(POWERED) && !world.isReceivingRedstonePower(pos))
			world.setBlockState(pos, state.cycle(POWERED));
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

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		return DockBlockEntity.use(state, world, pos, player, hand, hit, (DockBlockEntity)world.getBlockEntity(pos));
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new DockBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return BlockWithEntity.checkType(type, Common.DOCK_BLOCK_ENTITY_TYPE, DockBlockEntity::tick);
	}
}
