package hibi.scooters;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ElectricScooterEntity
extends ScooterEntity {

	private static final TrackedData<BlockPos> CHARGER = DataTracker.registerData(ElectricScooterEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
	private boolean charging = false;

	public ElectricScooterEntity(EntityType<? extends ScooterEntity> type, World world) {
		super(type, world);
		this.maxSpeed = 0.66d;
		this.acceleration = 0.015d;
		this.brakeForce = 0.88d;
		this.baseInertia = 0.995d;
		this.item = Common.ELECTRIC_SCOOTER_ITEM;
	}
	
	@Override
	public void tick() {
		super.tick();
		if(!this.world.isClient) {
			if(this.submergedInWater)
				this.damage(DamageSource.DROWN, Float.MAX_VALUE);
			if(this.charging && this.world.getTime() % 20 == 0) {
				if(!this.checkCharger()) this.detachFromCharger();
				BlockPos charger = this.dataTracker.get(CHARGER);
				if(charger.getSquaredDistanceFromCenter(this.getX(), this.getY(), this.getZ()) > 8)
					DockBlockEntity.detachScooter(this.world.getBlockState(charger), this.world, charger, (DockBlockEntity)this.world.getBlockEntity(charger));
			}
		}
	}

	@Override
	protected void initDataTracker() {
		this.dataTracker.startTracking(CHARGER, null);
		super.initDataTracker();
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if(this.hasPassengers()) return ActionResult.PASS;
		if(this.charging) {
			BlockPos charger = this.dataTracker.get(CHARGER);
			BlockState cached = this.world.getBlockState(charger);
			if(cached.getBlock() == Common.DOCK_BLOCK && cached.get(DockBlock.CHARGING)) {
				DockBlockEntity.detachScooter(cached, this.world, charger, (DockBlockEntity)this.world.getBlockEntity(charger));
				return ActionResult.success(this.world.isClient);
			}
		}
		return super.interact(player, hand);
	}

	public boolean chargingAt(BlockPos pos) {
		return this.dataTracker.get(CHARGER) == pos;
	}

	public void attachToCharher(BlockPos pos) {
		BlockState state = this.world.getBlockState(pos);
		if(state.getBlock() == Common.DOCK_BLOCK && !state.get(DockBlock.CHARGING)) {
			this.removeAllPassengers();
			this.charging = true;
			this.dataTracker.set(CHARGER, pos);
			this.playSound(SoundEvents.BLOCK_TRIPWIRE_ATTACH, 1.0f, 1.0f);
		}
	}

	public void detachFromCharger() {
		this.charging = false;
		this.dataTracker.set(CHARGER, null);
		this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
	}

	public boolean isCharging() {
		return this.charging;
	}

	public boolean checkCharger() {
		BlockPos charger = this.dataTracker.get(CHARGER);
		BlockState cached = this.world.getBlockState(charger);
		return cached.getBlock() == Common.DOCK_BLOCK && cached.get(DockBlock.CHARGING);
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
	}
}
