package hibi.scooters;

import java.util.List;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ScooterEntity extends Entity
implements ExtendedScreenHandlerFactory,
InventoryChangedListener {

	protected boolean keyW = false, keyA = false, keyS = false, keyD = false;
	protected float yawVelocity, yawAccel;
	protected int interpTicks;
	protected double x, y, z;
	protected double oldx, oldz;
	protected float yaw;

	protected double maxSpeed;
	protected double acceleration;
	protected double brakeForce;
	protected double baseInertia;
	protected double tireMult;

	protected Item item;

	public boolean frontTire = true;
	public boolean rearTire = true;

	/**
	 * <ul>
	 * <li>Slot 0 - Front Tyre</li>
	 * <li>Slot 1 - Rear Tyre</li>
	 * <li>Slot 2 - Charged Battery</li>
	 * <li>Slot 3 - Discharged Battery</li>
	 * </ul>
	 */
	public SimpleInventory items;

	public ScooterEntity(EntityType<? extends ScooterEntity> type, World world) {
		super(type, world);
		this.stepHeight = 0.6f;
		this.maxSpeed = 0.4d;
		this.acceleration = 0.02d;
		this.brakeForce = 0.93d;
		this.baseInertia = 0.98d;
		this.yawAccel = 1.2f;
		this.item = Common.SCOOTER_ITEM;
		this.items = new SimpleInventory(2);
		this.items.addListener(this);
		this.oldx = this.getX();
		this.oldz = this.getZ();
	}

	public static ScooterEntity create(EntityType<? extends ScooterEntity> type, ItemUsageContext context) {
		Vec3d pos = context.getHitPos();
		ScooterEntity out = type.create(context.getWorld());
		out.setPosition(pos);
		out.prevX = pos.x;
		out.prevY = pos.y;
		out.prevZ = pos.z;
		ItemStack stack = context.getStack();
		if(!stack.hasNbt()) {
			out.items.setStack(0, Common.TIRE_ITEM.getDefaultStack());
			out.items.setStack(1, Common.TIRE_ITEM.getDefaultStack());
		}
		else {
			out.readCustomDataFromNbt(stack.getNbt());
		}
		return out;
	}

	@Override
	public Packet<?> createSpawnPacket() {
		int tires = 0;
		if(!this.items.getStack(0).isEmpty()) tires |= 1;
		if(!this.items.getStack(1).isEmpty()) tires |= 2;
		return new EntitySpawnS2CPacket(this, tires);
	}

	@Override
	public void onSpawnPacket(EntitySpawnS2CPacket packet) {
		super.onSpawnPacket(packet);
		int tires = packet.getEntityData();
		this.frontTire = (tires & 1) == 1;
		this.rearTire = (tires & 2) == 2;
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
			if(!this.world.isClient && this.hasPassengers()) {
				Entity e = this.getPrimaryPassenger();
				if(e instanceof PlayerEntity && !((ServerPlayerEntity)e).isCreative() && this.world.getTime() % 20 == 0) {
					boolean abrasive = this.isOnAbrasive();
					if(!abrasive && this.world.getTime() % 40 == 0) return;
					double displx = this.oldx - this.getX();
					double displz = this.oldz - this.getZ();
					double displ = displx * displx + displz * displz;
					if(displ > 0.001225 && displ < 25) // 0.7 m/s, 500 m/s
						this.damageTires(abrasive);
				}
				this.oldx = this.getX();
				this.oldz = this.getZ();
			}
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
		double inertia = this.baseInertia * this.tireMult;
		if(this.keyW && speed < this.maxSpeed) {
			speed += this.acceleration * this.tireMult;
		}
		if(this.keyS) {
			inertia *= this.brakeForce;
		}
		if(this.keyA) {
			this.yawVelocity -= this.yawAccel;
		}
		if(this.keyD) {
			this.yawVelocity += this.yawAccel;
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
		if(!this.world.isClient) {
			if(this.items.getStack(0).isEmpty() || this.items.getStack(1).isEmpty())
				return ActionResult.PASS;
			if(player.startRiding(this)) {
				this.updateClientScootersInventory((ServerPlayerEntity)player);
				return ActionResult.CONSUME;
			}
			return ActionResult.PASS;
		}
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
			this.dropStack(this.asItemStack());
		this.removeAllPassengers();
		this.discard();
		return true;
	}

	protected ItemStack asItemStack() {
		NbtCompound nbt = new NbtCompound();
		ItemStack out = this.item.getDefaultStack();
		this.writeCustomDataToNbt(nbt);
		out.setNbt(nbt);
		return out;
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
		return this.asItemStack();
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
		if(nbt.contains("Tires", NbtElement.LIST_TYPE)) {
			NbtList list = nbt.getList("Tires", NbtElement.COMPOUND_TYPE);
			this.items.setStack(0, ItemStack.fromNbt(list.getCompound(0)));
			this.items.setStack(1, ItemStack.fromNbt(list.getCompound(1)));
		}
		this.onInventoryChanged(this.items);
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		NbtList tiresNbt = new NbtList();
		// unrolled loop
		NbtCompound compound = new NbtCompound();
		ItemStack is = this.items.getStack(0);
		if(!is.isEmpty())
			is.writeNbt(compound);
		tiresNbt.add(compound);
		compound = new NbtCompound();
		is = this.items.getStack(1);
		if(!is.isEmpty())
			is.writeNbt(compound);
		tiresNbt.add(compound);
		nbt.put("Tires", tiresNbt);
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

	@Override
	public void onInventoryChanged(Inventory inv) {
		if(this.world.isClient) {
			this.items = (SimpleInventory) inv;
			this.tireMult = 1.0;
			ItemStack is = inv.getStack(0);
			this.frontTire = !is.isEmpty();
			if(!this.frontTire || is.getDamage() == is.getMaxDamage())
				this.tireMult *= 0.85;
			is = inv.getStack(1);
			this.rearTire = !is.isEmpty();
			if(!this.rearTire || is.getDamage() == is.getMaxDamage())
				this.tireMult *= 0.85;
		}
	}

	protected void damageTires(boolean abrasive) {
		ItemStack stack = this.items.getStack(0);
		int damage = abrasive? 2 : 1;
		boolean popped = stack.getDamage() == stack.getMaxDamage();
		boolean markDirty = false;
		if(this.random.nextDouble() < 0.8d && stack.isOf(Common.TIRE_ITEM) && stack.getDamage() < stack.getMaxDamage())
			stack.damage(damage, this.random, null);
		if(stack.getDamage() == stack.getMaxDamage() && !popped) {
			markDirty = true;
			this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.0f, 0.7f);
		}
		stack = this.items.getStack(1);
		popped = stack.getDamage() == stack.getMaxDamage();
		if(this.random.nextDouble() < 0.8d && stack.isOf(Common.TIRE_ITEM) && stack.getDamage() < stack.getMaxDamage())
			stack.damage(damage, this.random, null);
		if(stack.getDamage() == stack.getMaxDamage() && !popped) {
			markDirty = true;
			this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.0f, 0.7f);
		}
		if(markDirty)
			this.updateClientScootersInventory();
	}

	protected boolean isOnAbrasive() {
		return !this.getLandingBlockState().isAir() && this.world.getBlockState(this.getVelocityAffectingPos()).isIn(Common.ABRASIVE_BLOCKS);
	}

	protected void updateClientScootersInventory() {
		PacketByteBuf buf = this.inventoryChangedPacket();
		for(ServerPlayerEntity player : PlayerLookup.tracking(this)) {
			ServerPlayNetworking.send(player, Common.PACKET_INVENTORY_CHANGED, buf);
		}
	}

	protected void updateClientScootersInventory(ServerPlayerEntity player) {
		PacketByteBuf buf = this.inventoryChangedPacket();
		ServerPlayNetworking.send(player, Common.PACKET_INVENTORY_CHANGED, buf);
	}

	protected PacketByteBuf inventoryChangedPacket() {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeInt(this.getId());
		List<ItemStack> contents = DefaultedList.ofSize(this.items.size(), ItemStack.EMPTY);
		for (int i = 0; i < contents.size(); ++i) {
			contents.set(i, this.items.getStack(i));
		}
		buf.writeCollection(contents, PacketByteBuf::writeItemStack);
		return buf;
	}
}
