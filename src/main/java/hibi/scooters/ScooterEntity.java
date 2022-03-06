package hibi.scooters;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ScooterEntity extends Entity {

	protected boolean keyW = false, keyA = false, keyS = false, keyD = false;
	protected float yawVelocity;
	protected double speed;
	protected double inertia;

	public ScooterEntity(EntityType<?> type, World world) {
		super(type, world);
		this.stepHeight = 0.5f;
	}

	@Override
	public Packet<?> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}

	@Override
	public void tick() {
		super.tick();
		this.speed = this.getVelocity().length();
		this.inertia = 0.98f;
		this.yawVelocity *= 0.8f;
		if(this.isLogicalSideForUpdatingMovement()) {
			if(this.world.isClient) {
				if(this.keyW && this.speed < 0.35) {
					this.speed += 0.02f;
				}
				if(this.keyS) {
					this.inertia = 0.92f;
				}
				if(this.keyA) {
					this.yawVelocity -= 1.2f;
				}
				if(this.keyD) {
					this.yawVelocity += 1.2f;
				}
				this.setYaw(this.getYaw() + this.yawVelocity);
			}
			if(!this.hasNoGravity()) {
				this.setVelocity(this.getVelocity().add(0, -0.04, 0));
			}
			if(!this.hasPassengers()){
				this.setVelocity(this.getVelocity().multiply(0.6, 1, 0.6));
			} else {
				this.setVelocity(
					MathHelper.sin(-this.getYaw() * 0.017453293f) * speed * this.inertia,
					this.getVelocity().y,
					MathHelper.cos(this.getYaw() * 0.017453293f) * speed * this.inertia);
			}
			this.move(MovementType.SELF, this.getVelocity());
		}
		else {
			this.setVelocity(Vec3d.ZERO);
		}
		this.checkBlockCollision();
	}

	// ---- Interaction ---- //
		@Override
		public ActionResult interact(PlayerEntity player, Hand hand) {
			if(player.shouldCancelInteraction() || this.hasPassengers()) return ActionResult.PASS;
			if(!this.world.isClient)
				return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
			return ActionResult.SUCCESS;
		}

		@Override
		public boolean isPushable() {
			return true;
		}
		@Override
		public boolean isCollidable() {
			return false;
		}

		@Override
		public boolean collides() {
			return !this.isRemoved();
		}

		@Override
		public Box getVisibilityBoundingBox() {
			return this.getBoundingBox();
		}

		@Override
		public boolean damage(DamageSource source, float amount) {
			if(this.isInvulnerableTo(source)) return false;
			if(this.world.isClient || this.isRemoved()) return true;
			this.emitGameEvent(GameEvent.ENTITY_DAMAGED, source.getAttacker());
			this.discard();
			return true;
		}

	@Override
	public double getMountedHeightOffset() {
		return 0.5d;
	}

	@Override
	public void updatePassengerPosition(Entity passenger) {
		if(!this.hasPassenger(passenger)) return;
		passenger.setYaw(passenger.getYaw() + this.yawVelocity);
		passenger.setBodyYaw(this.getYaw());
		float f = MathHelper.wrapDegrees(passenger.getYaw() - this.getYaw());
        float g = MathHelper.clamp(f, -105.0f, 105.0f);
        passenger.prevYaw += g - f;
        passenger.setYaw(passenger.getYaw() + g - f);
        passenger.setHeadYaw(passenger.getYaw());
		super.updatePassengerPosition(passenger);
	}

	@Override
	protected MoveEffect getMoveEffect() {
		return MoveEffect.NONE;
	}
	
	@Override
	public Entity getPrimaryPassenger() {
		List<Entity> list = this.getPassengerList();
		return list.isEmpty() ? null : list.get(0);
	}

	public void setInputs(boolean forward, boolean back, boolean left, boolean right) {
		this.keyW = forward;
		this.keyA = left;
		this.keyS = back;
		this.keyD = right;
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
