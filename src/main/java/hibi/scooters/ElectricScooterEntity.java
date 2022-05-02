package hibi.scooters;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ElectricScooterEntity
extends ScooterEntity {

	public static final TrackedData<BlockPos> CHARGER = DataTracker.registerData(ElectricScooterEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
	public static final TrackedData<Float> CHARGE_PROGRESS = DataTracker.registerData(ElectricScooterEntity.class, TrackedDataHandlerRegistry.FLOAT);
	public static final TrackedData<Boolean> CAN_CHARGE = DataTracker.registerData(ElectricScooterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private boolean charging = false;
	private boolean canCharge = false;

	public ElectricScooterEntity(EntityType<? extends ScooterEntity> type, World world) {
		super(type, world);
		this.maxSpeed = 0.7d;
		this.acceleration = 0.022d;
		this.brakeForce = 0.88d;
		this.baseInertia = 0.995d;
		this.coastInertia = 0.9991d;
		this.yawAccel = 1.33f;
		this.item = Common.ELECTRIC_SCOOTER_ITEM;
		this.items = new SimpleInventory(4);
		this.items.addListener(this);
	}
	
	@Override
	public void tick() {
		super.tick();
		if(!this.world.isClient) {
			if(this.submergedInWater) {
				this.dischageItem(64);
				this.damage(DamageSource.DROWN, Float.MAX_VALUE);
			}
			if(this.charging) {
				if(!this.items.getStack(3).isEmpty() && this.canCharge) {
					float chargeProgress = this.dataTracker.get(CHARGE_PROGRESS);
					chargeProgress += 1f/120f; // 6 seconds per item, 6"24' per stack
					if(chargeProgress > 1f) {
						chargeProgress = 0f;
						this.chargeItem(1);
					}
					this.dataTracker.set(CHARGE_PROGRESS, chargeProgress);
				}
				if(this.world.getTime() % 20 == 0) {
					if(this.checkCharger()) {
						BlockPos charger = this.dataTracker.get(CHARGER);
						if(charger.getSquaredDistanceFromCenter(this.getX(), this.getY(), this.getZ()) > 8)
							DockBlockEntity.detachScooter(this.world.getBlockState(charger), this.world, charger, (DockBlockEntity)this.world.getBlockEntity(charger));
					}
					else
						this.detachFromCharger();
				}
			}
			if(this.items.getStack(3).isEmpty() && this.dataTracker.get(CHARGE_PROGRESS) != 0f) {
				this.dataTracker.set(CHARGE_PROGRESS, 0f);
			}
		}
	}

	@Override
	protected void initDataTracker() {
		this.dataTracker.startTracking(CHARGER, null);
		this.dataTracker.startTracking(CHARGE_PROGRESS, 0f);
		this.dataTracker.startTracking(CAN_CHARGE, false);
		super.initDataTracker();
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if(this.hasPassengers()) return ActionResult.PASS;
		if(this.charging && !player.shouldCancelInteraction()) {
			BlockPos charger = this.dataTracker.get(CHARGER);
			BlockState cached = this.world.getBlockState(charger);
			if(cached.getBlock() == Common.DOCK_BLOCK && cached.get(DockBlock.CHARGING)) {
				DockBlockEntity.detachScooter(cached, this.world, charger, (DockBlockEntity)this.world.getBlockEntity(charger));
				return ActionResult.success(this.world.isClient);
			}
		}
		return super.interact(player, hand);
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		this.playSound(Common.SCOOTER_ROLLING, 0.75f, 0.75f);
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
			boolean b = state.get(DockBlock.POWERED);
			if(b != this.dataTracker.get(CAN_CHARGE))
				this.dataTracker.set(CAN_CHARGE, b);
			this.playSound(Common.CHARGER_CONNECT, 1.0f, 1.0f);
		}
	}

	public void detachFromCharger() {
		this.charging = false;
		this.dataTracker.set(CHARGER, null);
		this.playSound(Common.CHARGER_DISCONNECT, 1.0f, 1.0f);
	}

	public boolean isCharging() {
		return this.charging || this.dataTracker.get(CHARGER) != null;
	}

	public boolean checkCharger() {
		BlockPos charger = this.dataTracker.get(CHARGER);
		BlockState cached = this.world.getBlockState(charger);
		boolean b = cached.get(DockBlock.POWERED);
		if(b != this.canCharge) {
			this.canCharge = b;
			this.dataTracker.set(CAN_CHARGE, b);
		}
		return cached.getBlock() == Common.DOCK_BLOCK && cached.get(DockBlock.CHARGING);
	}

	public float getChargeProgress() {
		return this.dataTracker.get(CHARGE_PROGRESS);
	}

	public boolean getCanCharge() {
		return this.dataTracker.get(CAN_CHARGE);
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		NbtList batteriesNbt = new NbtList();
		// unrolled loop
		NbtCompound compound = new NbtCompound();
		ItemStack is = this.items.getStack(2);
		if(!is.isEmpty())
			is.writeNbt(compound);
		batteriesNbt.add(compound);
		compound = new NbtCompound();
		is = this.items.getStack(3);
		if(!is.isEmpty())
			is.writeNbt(compound);
		batteriesNbt.add(compound);
		nbt.put("Batteries", batteriesNbt);
		super.writeCustomDataToNbt(nbt);
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		if(nbt.contains("Batteries", NbtElement.LIST_TYPE)) {
			NbtList list = nbt.getList("Batteries", NbtElement.COMPOUND_TYPE);
			this.items.setStack(2, ItemStack.fromNbt(list.getCompound(0)));
			this.items.setStack(3, ItemStack.fromNbt(list.getCompound(1)));
		}
		super.readCustomDataFromNbt(nbt);
	}

	@Override
	public void onInventoryChanged(Inventory inv) {
		super.onInventoryChanged(inv);
		if(this.items.getStack(2).isEmpty() && this.dataTracker.get(CHARGE_PROGRESS) < 0f) {
			this.acceleration = 0d;
		}
		else {
			this.acceleration = 0.022d;
		}
	}

	private void chargeItem(int amount) {
		ItemStack discharged = this.items.getStack(3);
		ItemStack charged = this.items.getStack(2);
		if(charged.isEmpty()) {
			charged = Items.POTATO.getDefaultStack();
			charged.setCount(Math.min(amount, discharged.getCount()));
		}
		else {
			charged.increment(Math.min(amount, discharged.getCount()));
		}
		discharged.decrement(amount);
		this.items.setStack(2, charged);
	}

	private void dischageItem(int amount) {
		ItemStack charged = this.items.getStack(2);
		ItemStack discharged = this.items.getStack(3);
		if(discharged.isEmpty()) {
			discharged = Items.POISONOUS_POTATO.getDefaultStack();
			discharged.setCount(Math.min(amount, charged.getCount()));
		}
		else {
			discharged.increment(Math.min(amount, charged.getCount()));
		}
		charged.decrement(amount);
		this.items.setStack(3, discharged);
	}

	@Override
	public void setInputs(boolean forward, boolean back, boolean left, boolean right) {
		if(forward != this.keyW) {
			this.sendThrottlePacket(forward);
		}
		super.setInputs(forward, back, left, right);
	}

	@Override
	protected void wearTear(double displ) {
		this.damageTires(displ);
		if(this.keyW) {
			float charge = this.dataTracker.get(CHARGE_PROGRESS);
			charge -= 1f/90f;
			if(charge <= 0f) {
				if(!this.items.getStack(2).isEmpty()) {
					charge = 1f;
					this.dischageItem(1);
				}
				else {
					this.updateClientScootersInventory();
				}
			}
			this.dataTracker.set(CHARGE_PROGRESS, charge);
		}
	}
	
	protected void sendThrottlePacket(boolean throttle) {
		if(!this.world.isClient) return;
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeInt(this.getId());
		buf.writeBoolean(throttle);
		ClientPlayNetworking.send(new Identifier("scooters", "esctup"), buf);
	}

	public static void updateThrottle(ServerWorld world, PacketByteBuf buf) {
		int id = buf.readInt();
		Entity e = world.getEntityById(id);
		if(e instanceof ElectricScooterEntity) {
			((ElectricScooterEntity)e).keyW = buf.readBoolean();
		}
	}
}
