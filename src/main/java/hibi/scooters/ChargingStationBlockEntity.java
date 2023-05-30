package hibi.scooters;

import java.util.UUID;

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

public class ChargingStationBlockEntity
extends BlockEntity {

	private static final Box CHARGING_AREA = Box.of(Vec3d.ofBottomCenter(Vec3i.ZERO), 2d, 0.5d, 2d);
	private UUID chargee = null;

	public ChargingStationBlockEntity(BlockPos pos, BlockState state) {
		super(Common.CHARGING_STATION_BLOCK_ENTITY, pos, state);
	}

	public static void tick(World world, BlockPos pos, BlockState state, ChargingStationBlockEntity that) {
		if(world.isClient) return;
		ChargingStationBlockEntity.detectBrokenInvariants(that);
		ServerWorld serverWorld = (ServerWorld)world;
		if(!state.get(ChargingStationBlock.CHARGING)) {
			return;
		}
		if(serverWorld.getEntity(that.chargee) == null) {
			ChargingStationBlockEntity.detachScooter(state, serverWorld, pos, that);
		}
	}

	public static ActionResult use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, ChargingStationBlockEntity that) {

		// Detach an e-scooter if there's one connected to it.
		if(state.get(ChargingStationBlock.CHARGING)) {
			if(!world.isClient && that.chargee != null) {
				((ElectricScooterEntity)((ServerWorld)world).getEntity(that.chargee)).detachFromCharger();
			}
			ChargingStationBlockEntity.detachScooter(state, world, pos, that);
			return ActionResult.success(world.isClient());
		}

		for (Entity entity : world.getOtherEntities(null, CHARGING_AREA.offset(pos))) {
			if(!(entity instanceof ElectricScooterEntity)) {
				continue;
			}
			ElectricScooterEntity escooter = (ElectricScooterEntity)entity;
			if(escooter.isConnectedToCharger()) {
				continue;
			}
			ChargingStationBlockEntity.attachScooter(state, world, pos, that, escooter);
			escooter.attachToCharher(pos);
			return ActionResult.success(world.isClient());
		}

		return ActionResult.PASS;
	}

	public static void detachScooter(BlockState state, World world, BlockPos pos, ChargingStationBlockEntity that) {
		if(world.isClient) return;
		that.world.setBlockState(pos, state.with(ChargingStationBlock.CHARGING, false));
		that.chargee = null;
	}

	public static void attachScooter(BlockState state, World world, BlockPos pos, ChargingStationBlockEntity that, ElectricScooterEntity escooter) {
		if(world.isClient) return;

		if(that.isCharging()) return;
		if(escooter.isConnectedToCharger()) return;

		world.setBlockState(pos, state.with(ChargingStationBlock.CHARGING, true));
		that.chargee = escooter.getUuid();
	}

	private boolean isCharging() {
		return this.getCachedState().get(ChargingStationBlock.CHARGING) && this.chargee != null && ((ServerWorld)this.world).getEntity(this.chargee) != null;
	}

	private static void detectBrokenInvariants(ChargingStationBlockEntity that) {
		// if ((that.getCachedState().get(DockBlock.CHARGING) && that.chargee == null)
		// || (that.chargee != null && ((ServerWorld)that.world).getEntity(that.chargee) == null)
		// ) throw new IllegalStateException();
	}
}
