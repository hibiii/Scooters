package hibi.scooters;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ScooterEntity extends Entity {

	protected boolean keyW = false, keyA = false, keyS = false, keyD = false;
	protected float yawVelocity;
	protected double inertia;
	protected int interpTicks;
	protected double x, y, z;
	protected float yaw;

	protected final double maxSpeed;
	protected final double acceleration;
	protected final double brakeForce;
	protected final double baseInertia;

	public ScooterEntity(EntityType<? extends ScooterEntity> type, World world) {
		super(type, world);
		this.stepHeight = 0.5f;
		this.maxSpeed = 0.4d;
		this.acceleration = 0.02d;
		this.brakeForce = 0.93d;
		this.baseInertia = 0.98d;
	}

	public static ScooterEntity create(EntityType<? extends ScooterEntity> type, World world, Vec3d pos) {
		ScooterEntity out = new ScooterEntity(type, world);
		out.setPosition(pos);
		out.prevX = pos.x;
		out.prevY = pos.y;
		out.prevZ = pos.z;
		return out;
	}

	@Override
	public Packet<?> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}

	@Override
	public void tick() {
		double speed = this.getVelocity().multiply(1, 0, 1).length();
		this.yawVelocity *= 0.8f;
		this.inertia = this.baseInertia;
		super.tick();
		this.interp();
		if(this.isLogicalSideForUpdatingMovement()) {
			if(this.world.isClient) {
				if(this.keyW && speed < this.maxSpeed) {
					speed += this.acceleration;
				}
				if(this.keyS) {
					this.inertia *= this.brakeForce;
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
				this.setVelocity(this.getVelocity().multiply(0.7, 1, 0.7));
			}
			else {
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
		if(this.hasPassengers() && this.isSubmergedInWater()) {
			this.removeAllPassengers();
		}
		this.checkBlockCollision();
		List<Entity> others = this.world.getOtherEntities(this, this.getBoundingBox().expand(0.2f, 0f, 0.2f), EntityPredicates.canBePushedBy(this));
		if(!others.isEmpty()) {
			for (Entity e : others) {
				this.pushAwayFrom(e);
			}
		}
	}

	protected void interp() {
		if(this.isLogicalSideForUpdatingMovement()) {
			this.interpTicks = 0;
			this.updateTrackedPosition(this.getX(), this.getY(), this.getZ());
			return;
		}
		if(this.interpTicks <= 0) return;
		double xoff = this.getX() + (this.x - this.getX()) / (double)this.interpTicks;
        double yoff = this.getY() + (this.y - this.getY()) / (double)this.interpTicks;
        double zoff = this.getZ() + (this.z - this.getZ()) / (double)this.interpTicks;
        float yawoff = (float) MathHelper.wrapDegrees(this.yaw - (double)this.getYaw());
		this.setYaw(this.getYaw() + yawoff / (float)this.interpTicks);
		this.setPosition(xoff, yoff, zoff);
		// this.setRotation(this.getYaw(), this.getPitch());
		this.interpTicks--;
	}

	@Override
	public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.interpTicks = 10;
	}

	// ---- Interaction ---- //
		@Override
		public ActionResult interact(PlayerEntity player, Hand hand) {
			if(player.shouldCancelInteraction() || this.hasPassengers()) return ActionResult.PASS;
			if(this.isTouchingWater()) return ActionResult.PASS;
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
			this.emitGameEvent(GameEvent.ENTITY_KILLED, source.getAttacker());
			boolean drops = !(source.getAttacker() instanceof PlayerEntity && ((PlayerEntity)source.getAttacker()).getAbilities().creativeMode);
			if(drops && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS))
				this.dropItem(Common.SCOOTER_ITEM);
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
		return MoveEffect.EVENTS;
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
	public float getEyeHeight(EntityPose pose) {
		return 0.5f;
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
