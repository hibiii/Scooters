package hibi.scooters;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;

public class ElectricScooterEntity
extends ScooterEntity {

	public ElectricScooterEntity(EntityType<? extends ScooterEntity> type, World world) {
		super(type, world);
		this.maxSpeed = 0.7d;
		this.acceleration = 0.015d;
		this.brakeForce = 0.93d;
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
}
