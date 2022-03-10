package hibi.scooters;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ElectricScooterEntity
extends ScooterEntity {

	private static final TrackedData<BlockPos> CHARGER = DataTracker.registerData(ElectricScooterEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);

	public ElectricScooterEntity(EntityType<? extends ScooterEntity> type, World world) {
		super(type, world);
		this.maxSpeed = 0.8d;
		this.acceleration = 0.015d;
		this.brakeForce = 0.88d;
		this.baseInertia = 0.995d;
		this.stepHeight = 0.7f;
		this.item = Common.ELECTRIC_SCOOTER_ITEM;
	}
	
	@Override
	public void tick() {
		super.tick();
		if(!this.world.isClient && this.submergedInWater) {
			this.damage(DamageSource.DROWN, Float.MAX_VALUE);
		}
	}

	@Override
	protected void initDataTracker() {
		this.dataTracker.startTracking(CHARGER, null);
		super.initDataTracker();
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if(player.shouldCancelInteraction() || this.hasPassengers()) return ActionResult.PASS;
		return super.interact(player, hand);
	}
}
