package hibi.scooters;

import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ScooterItem
extends Item
implements DyeableItem {

	protected final int defaultColor;

	public ScooterItem(Settings settings, int defaultColor) {
		super(settings);
		this.defaultColor = defaultColor;
	}
	
	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if(context.getSide() == Direction.DOWN)
			return ActionResult.FAIL;
		
		// Attempt to create a scooter entity
		World world = context.getWorld();
		ScooterEntity scooter = ScooterEntity.create(this.asItem() == Common.ELECTRIC_SCOOTER_ITEM? Common.ELECTRIC_SCOOTER_ENTITY: Common.KICK_SCOOTER_ENTITY, context);
		if(!world.isSpaceEmpty(scooter, scooter.getBoundingBox()) || !world.getOtherEntities(scooter, scooter.getBoundingBox()).isEmpty())
			return ActionResult.FAIL;

		// Actually spawn the scooter if it's successful
		if(world instanceof ServerWorld) {
			world.spawnEntity(scooter);
			world.emitGameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, scooter.getPos());
		}

		context.getStack().decrement(1);
		return ActionResult.success(world.isClient);
	}

	@Override
	public ItemStack getDefaultStack() {
		ItemStack out = new ItemStack(this);
		NbtCompound stackNbt = out.getOrCreateNbt();
		NbtList list = new NbtList();
		NbtCompound tireCompound = new NbtCompound();
		new ItemStack(Common.TIRE_ITEM).writeNbt(tireCompound);
		list.add(tireCompound);
		list.add(tireCompound);
		stackNbt.put(ScooterEntity.NBT_KEY_TIRES, list);
		if (this == Common.ELECTRIC_SCOOTER_ITEM) {
			list = new NbtList();
			NbtCompound batteryCompound = new NbtCompound();
			new ItemStack(Common.POTATO_BATTERY_ITEM, Common.POTATO_BATTERY_ITEM.getMaxCount()).writeNbt(batteryCompound);
			list.add(batteryCompound);
			stackNbt.put(ElectricScooterEntity.NBT_KEY_BATTERIES, list);
		}
		return out;
	}

	@Override
	public boolean hasColor(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		return nbt != null && nbt.contains(ScooterEntity.NBT_KEY_BODY_COLOR, NbtElement.NUMBER_TYPE);
	}

	@Override
	public int getColor(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		if (nbt != null && nbt.contains(ScooterEntity.NBT_KEY_BODY_COLOR, NbtElement.NUMBER_TYPE)) {
			return nbt.getInt(ScooterEntity.NBT_KEY_BODY_COLOR);
		} else {
			return this.defaultColor;
		}
	}

	@Override
	public void removeColor(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		if (nbt == null) {
			return;
		}
		// Skipping existence check because NbtCompound.remove calls Map.remove,
		// and the latter does not complain if removing nonexistant
		nbt.remove(ScooterEntity.NBT_KEY_BODY_COLOR);
	}

	@Override
	public void setColor(ItemStack stack, int color) {
		stack.getOrCreateNbt().putInt(ScooterEntity.NBT_KEY_BODY_COLOR, color);
	}

	public static int colorItem(ItemStack stack, int index) {
		if (index > 0) {
			return ((DyeableItem)stack.getItem()).getColor(stack);
		} else {
			return -1;
		}
	}
}
