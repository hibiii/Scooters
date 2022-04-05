package hibi.scooters;

import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class DockBlockEntity
extends BlockEntity {

	private static final Box CHARGING_AREA = Box.of(Vec3d.ofBottomCenter(Vec3i.ZERO), 2d, 0.5d, 2d);
	private UUID chargee = null;

	public DockBlockEntity(BlockPos pos, BlockState state) {
		super(Common.DOCK_BLOCK_ENTITY_TYPE, pos, state);
	}

	public static void tick(World world, BlockPos pos, BlockState state, DockBlockEntity that) {
		if(world.isClient) return;
		if(((ServerWorld)world).getEntity(that.chargee) == null)
			detachScooter(state, world, pos, that);
	}

	public static ActionResult use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, DockBlockEntity that) {
		if(world.isClient)
			return ActionResult.success(true);
		if(that.isCharging()) {
			detachScooter(state, world, pos, that);
			return ActionResult.CONSUME;
		}
		boolean succeeded = false;
		for (Entity entity : world.getOtherEntities(null, CHARGING_AREA.offset(pos))) {
			if(!(entity instanceof ElectricScooterEntity)) continue;
			ElectricScooterEntity scooter = (ElectricScooterEntity)entity;
			if(scooter.isCharging()) continue;
			attachScooter(state, world, pos, that, scooter);
			succeeded = true;
			break;
		}
		return succeeded ? ActionResult.CONSUME: ActionResult.PASS;
	}

	public static void validateCharging(BlockState state, World world, BlockPos pos, DockBlockEntity that) {
		if(world.isClient) return;
		Entity e = ((ServerWorld)world).getEntity(that.chargee);
		if(state.get(DockBlock.CHARGING) && (that.chargee == null || e == null)) {
			that.chargee = null;
			world.setBlockState(pos, state.with(DockBlock.CHARGING, false), Block.NOTIFY_ALL);
			if(e != null) {
				ElectricScooterEntity es = ((ElectricScooterEntity)e);
				if(es.chargingAt(pos)) {
					es.detachFromCharger();
				}
			}
		}
	}

	public static void detachScooter(BlockState state, World world, BlockPos pos, DockBlockEntity that) {
		if(world.isClient) return;
		validateCharging(state, world, pos, that);
		if(that.isCharging()) {
			ElectricScooterEntity es = ((ElectricScooterEntity)((ServerWorld)that.world).getEntity(that.chargee));
			if(es != null)
				es.detachFromCharger();
			that.world.setBlockState(pos, state.with(DockBlock.CHARGING, false));
			that.chargee = null;
		}
	}

	public static void attachScooter(BlockState state, World world, BlockPos pos, DockBlockEntity that, ElectricScooterEntity entity) {
		if(world.isClient) return;
		validateCharging(state, world, pos, that);
		if(that.isCharging()) return;
		if(entity.isCharging()) return;
		entity.attachToCharher(pos);
		that.chargee = entity.getUuid();
		world.setBlockState(pos, state.with(DockBlock.CHARGING, true));
	}

	private boolean isCharging() {
		return this.getCachedState().get(DockBlock.CHARGING) && this.chargee != null && ((ServerWorld)this.world).getEntity(this.chargee) != null;
	}
}
