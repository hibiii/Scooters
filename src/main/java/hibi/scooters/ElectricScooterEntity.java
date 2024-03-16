package hibi.scooters;

import java.util.Optional;

import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// TODO Optimize charge progress accesses
public class ElectricScooterEntity
extends ScooterEntity {

	public static final TrackedData<Optional<BlockPos>> CHARGER = DataTracker.registerData(ElectricScooterEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
	public static final TrackedData<Float> CHARGE_PROGRESS = DataTracker.registerData(ElectricScooterEntity.class, TrackedDataHandlerRegistry.FLOAT);
	public static final TrackedData<Boolean> CHARGER_IS_POWERED = DataTracker.registerData(ElectricScooterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	public static final int SLOT_CHARGED = 2;
	public static final int SLOT_DISCHARGED = 3;
	public static final String NBT_KEY_BATTERIES = "Batteries";
	public static final String NBT_KEY_CHARGE_PROGRESS = "ChargeProgress";
	public static final String NBT_KEY_CHARGER_X = "ChargerX";
	public static final String NBT_KEY_CHARGER_Y = "ChargerY";
	public static final String NBT_KEY_CHARGER_Z = "ChargerZ";

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
		if(!this.getWorld().isClient()) {
			// Short circuit the batteries and discharge them isntantaneously
			if(this.submergedInWater) {
				this.dischargeItem(64);
				this.dataTracker.set(CHARGE_PROGRESS, 0f);
				this.damage(this.getWorld().getDamageSources().drown(), Float.MAX_VALUE);
				return;
			}
			if(this.isConnectedToCharger()) {
				if(!this.hasValidCharger()) {
					this.detachFromCharger();
					return;
				}
				this.trackChargerPower();
				if(this.dataTracker.get(CHARGER_IS_POWERED) && !this.items.getStack(SLOT_DISCHARGED).isEmpty()) {
					float chargeProgress = this.dataTracker.get(CHARGE_PROGRESS);
					chargeProgress += 1f/120f; // 6 seconds per item, 6"24' per stack
					if(chargeProgress > 1f) {
						chargeProgress = 0f;
						this.chargeItem(1);
					}
					this.dataTracker.set(CHARGE_PROGRESS, chargeProgress);
				}
				if((this.getWorld().getTime() + this.getId()) % 20 != 0) {
					return;
				}
			}
			if(this.items.getStack(SLOT_DISCHARGED).isEmpty() && this.dataTracker.get(CHARGE_PROGRESS) != 0f) {
				this.dataTracker.set(CHARGE_PROGRESS, 0f);
			}
		}
	}

	@Override
	protected void initDataTracker() {
		this.dataTracker.startTracking(CHARGER, Optional.empty());
		this.dataTracker.startTracking(CHARGE_PROGRESS, 0f);
		this.dataTracker.startTracking(CHARGER_IS_POWERED, false);
		super.initDataTracker();
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if(this.hasPassengers()) return ActionResult.PASS;

		Optional<BlockPos> optChargerPos = this.dataTracker.get(CHARGER);
		// If the player right clicks a charging e-scooter then detach it from a charger.
		if(optChargerPos.isPresent() && !player.shouldCancelInteraction()) {
			BlockPos charger = optChargerPos.get();
			World world = this.getWorld();
			BlockState cached = world.getBlockState(charger);
			if(cached.getBlock() == Common.CHARGING_STATION_BLOCK && cached.get(ChargingStationBlock.CHARGING)) {
				ChargingStationBlockEntity.detachScooter(cached, world, charger, (ChargingStationBlockEntity)world.getBlockEntity(charger));
				this.detachFromCharger();
				return ActionResult.success(world.isClient);
			}
		}
		return super.interact(player, hand);
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		this.playSound(Common.SOUND_SCOOTER_ROLLING, 0.75f, 0.75f);
	}

	public boolean chargingAt(BlockPos pos) {
		var optionalCharger = this.dataTracker.get(CHARGER);
		if (optionalCharger.isEmpty())
			return false;
		return optionalCharger.get() == pos;
	}

	/**
	 * Called from the dock block entity.
	 * Sets up internal values related to charging.
	 * Charging <b>must</b> be initialized from {@link ChargingStationBlockEntity}.attachScooter, as the charger is required to actually exist.
	 * @param pos The position of the charger.
	 */
	public void attachToCharher(BlockPos pos) {
		BlockState blockState = this.getWorld().getBlockState(pos);
		if(blockState.getBlock() != Common.CHARGING_STATION_BLOCK) {
			return;
		}
		this.removeAllPassengers();
		this.dataTracker.set(CHARGER, Optional.of(pos));
		this.dataTracker.set(CHARGER_IS_POWERED, blockState.get(ChargingStationBlock.POWERED));
		this.playSound(Common.SOUND_CHARGER_CONNECT, 1.0f, 1.0f);
	}

	/**
	 * Detaches this e-scooter from any chargers.
	 * If there is a charger, then detaching <b>must</b> be initialized from that {@link ChargingStationBlockEntity}.detachScooter.
	 * This method should only be called if there's no charger available (e.g. it is destroyed).
	 */
	public void detachFromCharger() {
		Optional<BlockPos> optChargerPos = this.dataTracker.get(CHARGER);
		if (optChargerPos.isEmpty()) {
			return;
		}
		BlockPos blockPos = optChargerPos.get();
		World world = this.getWorld();
		BlockEntity blockEntity = world.getBlockEntity(blockPos);
		if (!(blockEntity instanceof ChargingStationBlockEntity)) {
			return;
		}
		ChargingStationBlockEntity.detachScooter(world.getBlockState(blockPos), world, blockPos, (ChargingStationBlockEntity) blockEntity);
		this.dataTracker.set(CHARGER, Optional.empty());
		this.playSound(Common.SOUND_CHARGER_DISCONNECT, 1.0f, 1.0f);
	}

	public boolean isConnectedToCharger() {
		return this.dataTracker.get(CHARGER).isPresent();
	}

	/**
	 * Checks if the attached charger is actually valid for charging.
	 * {@code this.canCharge} is set if the charger is valid to stay connected, and also powered.
	 * @return {@code true} if the charger is valid to stay connected, {@code false} otherwise.
	 */
	public boolean hasValidCharger() {
		Optional<BlockPos> optionalCharger = this.dataTracker.get(CHARGER);
		if(optionalCharger.isEmpty()) {
			return false;
		}
		BlockPos charger = optionalCharger.get();
		BlockState blockState = this.getWorld().getBlockState(charger);
		if(blockState.getBlock() != Common.CHARGING_STATION_BLOCK) {
			return false;
		}
		if (charger.getSquaredDistanceToCenter(this.getPos()) > 9) {
			return false;
		}
		return blockState.get(ChargingStationBlock.CHARGING);
	}

	public float getChargeProgress() {
		return this.dataTracker.get(CHARGE_PROGRESS);
	}

	public boolean getChargerIsPowered() {
		return this.dataTracker.get(CHARGER_IS_POWERED);
	}

	/**
	 * <p>
	 *   The NBT for a normal, electric scooter is the following:
	 * </p>
	 * <ul>
	 *   <li>{@code Tires}, {@link ItemStack}[2]</li>
	 *   <li>{@code Batteries}, {@link ItemStack}[2]</li>
	 *   <li>{@code ChargeProgress}, float</li>
	 *   <li>{@code ChargerX}, int</li>
	 *   <li>{@code ChargerY}, int</li>
	 *   <li>{@code ChargerZ}, int</li>
	 * </ul>
	 * <hr>
	 * @param nbt The NBT data to write into.
	 */
	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		NbtList batteriesNbt = new NbtList();
		// Unrolled loop
			NbtCompound compound = new NbtCompound();
			ItemStack is = this.items.getStack(SLOT_CHARGED);
			if(!is.isEmpty())
				is.writeNbt(compound);
			batteriesNbt.add(compound);
		// --- //
			compound = new NbtCompound();
			is = this.items.getStack(SLOT_DISCHARGED);
			if(!is.isEmpty())
				is.writeNbt(compound);
			batteriesNbt.add(compound);
		nbt.put(NBT_KEY_BATTERIES, batteriesNbt);

		NbtFloat prog = NbtFloat.of(this.dataTracker.get(CHARGE_PROGRESS));
		nbt.put(NBT_KEY_CHARGE_PROGRESS, prog);

		var optChargerPos = this.dataTracker.get(CHARGER);
		BlockPos chargerPos = optChargerPos.isPresent()? optChargerPos.get() : null;
		if(chargerPos != null) {
			nbt.putInt(NBT_KEY_CHARGER_X, chargerPos.getX());
			nbt.putInt(NBT_KEY_CHARGER_Y, chargerPos.getY());
			nbt.putInt(NBT_KEY_CHARGER_Z, chargerPos.getZ());
		}
		super.writeCustomDataToNbt(nbt);
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		if(nbt.contains(NBT_KEY_BATTERIES, NbtElement.LIST_TYPE)) {
			NbtList list = nbt.getList(NBT_KEY_BATTERIES, NbtElement.COMPOUND_TYPE);
			this.items.setStack(SLOT_CHARGED, ItemStack.fromNbt(list.getCompound(0)));
			this.items.setStack(SLOT_DISCHARGED, ItemStack.fromNbt(list.getCompound(1)));
		}
		if(nbt.contains(NBT_KEY_CHARGE_PROGRESS, NbtElement.FLOAT_TYPE)) {
			this.dataTracker.set(CHARGE_PROGRESS, nbt.getFloat(NBT_KEY_CHARGE_PROGRESS));
		}
		if(nbt.contains(NBT_KEY_CHARGER_X) && nbt.contains(NBT_KEY_CHARGER_Y) && nbt.contains(NBT_KEY_CHARGER_Z)) {
			BlockPos charger = new BlockPos(nbt.getInt(NBT_KEY_CHARGER_Z), nbt.getInt(NBT_KEY_CHARGER_Y), nbt.getInt(NBT_KEY_CHARGER_Z));
			World world = this.getWorld();
			BlockEntity be = world.getBlockEntity(charger);
			if(be instanceof ChargingStationBlockEntity) {
				ChargingStationBlockEntity.attachScooter(world.getBlockState(charger), world, charger, (ChargingStationBlockEntity)be, this);
			}
		}
		super.readCustomDataFromNbt(nbt);
	}

	@Override
	public void onInventoryChanged(Inventory inv) {
		super.onInventoryChanged(inv);
		// Make it so we can't accelerate if the battery is dead.
		// Manipulating the acceleration gets around an issue where you can continue to accelerate if
		//   the battery depletes while you're still riding the e-scooter.
		if(this.items.getStack(SLOT_CHARGED).isEmpty() && this.dataTracker.get(CHARGE_PROGRESS) <= 0f) {
			this.acceleration = 0d;
		}
		else {
			this.acceleration = 0.022d;
		}
	}

	/**
	 * Charges an item.
	 * Take an {@code amount} of discharged items from slot 3 and transmute them into the charged one in slot 2.
	 * Charge progress isn't taken into account and is left untouched.
	 * @param amount How many items to charge.
	 * @return {@code true} if any amount of items were charged, {@code false} otherwise.
	 */
	protected boolean chargeItem(int amount) {
		ItemStack discharged = this.items.getStack(SLOT_DISCHARGED);
		if(discharged.isEmpty()) return false;

		ItemStack charged = this.items.getStack(SLOT_CHARGED);
		if(charged.isEmpty()) {
			charged = Common.POTATO_BATTERY_ITEM.getDefaultStack();
			charged.setCount(Math.min(amount, discharged.getCount()));
		}
		else {
			charged.increment(Math.min(amount, discharged.getCount()));
		}
		discharged.decrement(amount);
		this.items.setStack(SLOT_CHARGED, charged);
		return true;
	}

	/**
	 * Discharges an item.
	 * Take an {@code amount} of charged items from slot 2 and transmute them into the discharged one in slot 3.
	 * Charge progress isn't taken into account and is left untouched.
	 * @param amount How many items to discharge.
	 * @return {@code true} if any amount of items were discharged, {@code false} otherwise.
	 */
	protected boolean dischargeItem(int amount) {
		ItemStack charged = this.items.getStack(SLOT_CHARGED);
		if(charged.isEmpty()) return false;

		ItemStack discharged = this.items.getStack(SLOT_DISCHARGED);
		if(discharged.isEmpty()) {
			discharged = Common.SPENT_POTATO_BATTERY_ITEM.getDefaultStack();
			discharged.setCount(Math.min(amount, charged.getCount()));
		}
		else {
			discharged.increment(Math.min(amount, charged.getCount()));
		}
		charged.decrement(amount);
		this.items.setStack(SLOT_DISCHARGED, discharged);
		return true;
	}

	@Override
	public void setInputs(boolean forward, boolean back, boolean left, boolean right) {
		if(forward != this.keyW) {
			this.sendThrottlePacket(forward);
		}
		super.setInputs(forward, back, left, right);
	}

	// This is only called in the server
	@Override
	protected void wearTear(double displ) {
		this.damageTires(displ);
		
		// Discharge the battery when the throttle is being held down
		if(this.keyW) {
			float charge = this.dataTracker.get(CHARGE_PROGRESS);
			charge -= 1f/90f;
			if(charge <= 0f) {
				this.updateClientScootersInventory();
				if(this.dischargeItem(1)) {
					charge = 1f;
				}
			}
			this.dataTracker.set(CHARGE_PROGRESS, charge);
		}
	}
	
	/**
	 * Throttle packets are used so the server is aware of battery consumption and actually consumes battery while the player is using the throttle.
	 * There is no other way around this.
	 * @param throttle The state of the "forward" key (usually W)
	 */
	protected void sendThrottlePacket(boolean throttle) {
		if(!this.getWorld().isClient()) return;
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeInt(this.getId());
		buf.writeBoolean(throttle);
		ClientPlayNetworking.send(Common.PACKET_THROTTLE_ID, buf);
	}

	public static void updateThrottle(ServerWorld world, PacketByteBuf buf) {
		int id = buf.readInt();
		Entity e = world.getEntityById(id);
		if(e instanceof ElectricScooterEntity) {
			((ElectricScooterEntity)e).keyW = buf.readBoolean();
		}
	}

	// Remove position from the NBT if it's destroyed or picked when it's charging.
	// Otherwise, the scooter would connect to the charger when it's placed down and immediately disconnect (distance)
	@Override
	protected ItemStack asItemStack() {
		ItemStack out = super.asItemStack();
		NbtCompound nbt = out.getNbt();
		nbt.remove(NBT_KEY_CHARGER_X);
		nbt.remove(NBT_KEY_CHARGER_X);
		nbt.remove(NBT_KEY_CHARGER_Z);
		return out;
	}

	protected void trackChargerPower() {
		Optional<BlockPos> optChargerPos = this.dataTracker.get(CHARGER);
		if(optChargerPos.isEmpty()) {
			return;
		}
		boolean isRememberedPowered = this.dataTracker.get(CHARGER_IS_POWERED);
		boolean isChargerPowered = this.getWorld().getBlockState(optChargerPos.get()).get(ChargingStationBlock.POWERED);
		if (isChargerPowered != isRememberedPowered) {
			this.dataTracker.set(CHARGER_IS_POWERED, isChargerPowered);
		}
	}
}
