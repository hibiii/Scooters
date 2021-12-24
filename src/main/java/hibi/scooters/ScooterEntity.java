package hibi.scooters;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.world.World;

public class ScooterEntity extends Entity {

	public ScooterEntity(EntityType<?> type, World world) {
		super(type, world);
		this.inanimate = true;
	}

	@Override
	public Packet<?> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}

	@Override
	public boolean isCollidable() {
		return true;
	}

	@Override
	protected void initDataTracker() {
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {		
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
	}
	
}
