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
		super(Common.CHARGING_STATION_BLOCK_ENTITY, pos, state);
	}

	public static void tick(World world, BlockPos pos, BlockState state, DockBlockEntity that) {
		if(world.isClient) return;
		if(that.chargee != null && ((ServerWorld)world).getEntity(that.chargee) == null)
			detachScooter(state, world, pos, that);
	}

	public static ActionResult use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, DockBlockEntity that) {

		// Detach an e-scooter if there's one connected to it.
		if(state.get(DockBlock.CHARGING)) {
			detachScooter(state, world, pos, that);
			return ActionResult.success(world.isClient());
		}

		for (Entity entity : world.getOtherEntities(null, CHARGING_AREA.offset(pos))) {
			if(!(entity instanceof ElectricScooterEntity)) continue;
			ElectricScooterEntity scooter = (ElectricScooterEntity)entity;
			if(scooter.isCharging()) continue;
			attachScooter(state, world, pos, that, scooter);
			return ActionResult.success(world.isClient());
		}

		return ActionResult.PASS;
	}

	/**
	 * Checks if a charger is actually charging something.
	 * Detaching is also handled here in case of discontinuity.
	 */
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

	/**
	 * Detach the e-scooter connected to this charger.
	 * Detaching should be made here, but it's perfectly okay to do on the scooter, as there are provisions for discontinuity.
	 */
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

	/**
	 * Attach an e-scooter to a charger.
	 * Attaching <b>must<b> be done here.
	 */
	public static void attachScooter(BlockState state, World world, BlockPos pos, DockBlockEntity that, ElectricScooterEntity entity) {
		if(world.isClient) return;

		// Sanity checks
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
