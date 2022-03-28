package hibi.scooters;

import java.util.List;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ScooterEntity extends Entity
implements ExtendedScreenHandlerFactory {

	protected boolean keyW = false, keyA = false, keyS = false, keyD = false;
	protected float yawVelocity;
	protected int interpTicks;
	protected double x, y, z;
	protected float yaw;

	protected double maxSpeed;
	protected double acceleration;
	protected double brakeForce;
	protected double baseInertia;
	protected Item item;

	public ScooterEntity(EntityType<? extends ScooterEntity> type, World world) {
		super(type, world);
		this.stepHeight = 0.6f;
		this.maxSpeed = 0.4d;
		this.acceleration = 0.02d;
		this.brakeForce = 0.93d;
		this.baseInertia = 0.98d;
		this.item = Common.SCOOTER_ITEM;
	}

	public static ScooterEntity create(EntityType<? extends ScooterEntity> type, World world, Vec3d pos) {
		ScooterEntity out = type.create(world);
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
		this.yawVelocity *= 0.8f;
		super.tick();
		this.interp();
		if(this.isLogicalSideForUpdatingMovement()) {
			if(this.world.isClient) {
				this.drive();
			}
			if(!this.hasNoGravity()) {
				this.setVelocity(this.getVelocity().add(0, -0.04, 0));
			}
			if(!this.hasPassengers()){
				this.setVelocity(this.getVelocity().multiply(0.7, 1, 0.7));
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
		List<Entity> others = this.world.getOtherEntities(this, this.getBoundingBox(), EntityPredicates.canBePushedBy(this));
		if(!others.isEmpty()) {
			for (Entity e : others) {
				this.pushAwayFrom(e);
			}
		}
	}

	protected void drive() {
		double speed = this.getVelocity().multiply(1, 0, 1).length();
		double inertia = this.baseInertia;
		if(this.keyW && speed < this.maxSpeed) {
			speed += this.acceleration;
		}
		if(this.keyS) {
			inertia *= this.brakeForce;
		}
		if(this.keyA) {
			this.yawVelocity -= 1.2f;
		}
		if(this.keyD) {
			this.yawVelocity += 1.2f;
		}
		this.setYaw(this.getYaw() + this.yawVelocity);
		this.setVelocity(
			MathHelper.sin(-this.getYaw() * 0.017453293f) * speed * inertia,
			this.getVelocity().y,
			MathHelper.cos(this.getYaw() * 0.017453293f) * speed * inertia);
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

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if(this.hasPassengers()) return ActionResult.PASS;
		if(this.isTouchingWater()) return ActionResult.PASS;
		if(player.shouldCancelInteraction()) {
			if(!this.world.isClient)
				((ServerPlayerEntity)player).openHandledScreen(this);
			return ActionResult.success(this.world.isClient);
		}
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
			this.dropItem(this.item);
		this.removeAllPassengers();
		this.discard();
		return true;
	}

	@Override
	public double getMountedHeightOffset() {
		return 0.45d;
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
	public ItemStack getPickBlockStack() {
		return this.item.getDefaultStack();
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

	@Override
	public ScreenHandler createMenu(int sid, PlayerInventory pi, PlayerEntity pe) {
		return new ScooterScreenHandler(sid, pi, this);
	}

	@Override
	public Text getDisplayName() {
		return this.hasCustomName()? this.getCustomName() : this.getDefaultName();
	}

	@Override
	public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
		buf.writeInt(this.getId());
	}
}
