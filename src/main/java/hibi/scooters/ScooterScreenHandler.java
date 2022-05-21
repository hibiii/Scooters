package hibi.scooters;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class ScooterScreenHandler
extends ScreenHandler {
	public final int scooterId;
	public final ScooterEntity scooter;
	public final boolean electric;
	

	// Client Init
	public ScooterScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
		super(Common.SCOOTER_SCREEN_HANDLER, syncId);
		this.scooterId = buf.readInt();
		MinecraftClient inst = MinecraftClient.getInstance();
		this.scooter = (ScooterEntity) inst.world.getEntityById(scooterId);
		this.electric = this.scooter instanceof ElectricScooterEntity;
		this.addScooterSlots(this.scooter.items);
		this.addPlayerInventory(playerInventory);
	}

	// Server Init
	public ScooterScreenHandler(int syncId, PlayerInventory playerInventory, ScooterEntity entity) {
		super(Common.SCOOTER_SCREEN_HANDLER, syncId);
		this.scooter = entity;
		this.scooterId = entity.getId();
		this.electric = this.scooter instanceof ElectricScooterEntity;
		this.addScooterSlots(this.scooter.items);
		this.addPlayerInventory(playerInventory);
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		// Prevent multiplayer shenanigans
		if(this.scooter.hasPassengers())
			return false;
		return this.scooter.squaredDistanceTo(player) < 64d;
	}

	private void addScooterSlots(Inventory inventory) {
		// Front Tire
		this.addSlot(new Slot(inventory, 0, 62, 36){
			public boolean canInsert(ItemStack stack) {
				return stack.isOf(Common.TIRE_ITEM);
			}
		});
		// Rear Tire
		this.addSlot(new Slot(inventory, 1, 62, 54){
			public boolean canInsert(ItemStack stack) {
				return stack.isOf(Common.TIRE_ITEM);
			}
		});
		if(this.electric) {
			// Charged Batteries
			LinkedSlot a = new LinkedSlot(inventory, 2, 98, 18, Items.POTATO);
			// Discharged Batteries
			LinkedSlot b = new LinkedSlot(inventory, 3, 98, 54, Items.POISONOUS_POTATO);
			a.linkWith(b);
			b.linkWith(a);
			this.addSlot(a);
			this.addSlot(b);
		}
	}

	// Standard method of adding the player inventory
	private void addPlayerInventory(PlayerInventory pi) {
		int k = 0;
		for (k = 0; k < 3; ++k) {
			for (int l = 0; l < 9; ++l) {
				this.addSlot(new Slot(pi, l + k * 9 + 9, 8 + l * 18, 102 + k * 18 + -18));
			}
		}
		for (k = 0; k < 9; ++k) {
			this.addSlot(new Slot(pi, k, 8 + k * 18, 142));
		}
	}

	@Override
	public ItemStack transferSlot(PlayerEntity player, int index) {
		Slot slot = this.getSlot(index);
		if (slot != null && slot.hasStack()) {
			ItemStack itemStack2 = slot.getStack();
			int size = this.scooter.items.size();

			// Scooter Inventory -> Player Inventory
			if (index < size) {
				if(!this.insertItem(itemStack2, size, this.slots.size(), true))
					return ItemStack.EMPTY;
			}
			// Player Inventory -> Scooter Inventory
			else {
				// Front Tire
				if(this.getSlot(ScooterEntity.SLOT_FRONT_TIRE).canInsert(itemStack2) && !this.getSlot(0).hasStack() && !this.insertItem(itemStack2, 0, 1, false))
					return ItemStack.EMPTY;
				// Rear Tire
				else if(this.getSlot(ScooterEntity.SLOT_REAR_TIRE).canInsert(itemStack2) && !this.getSlot(1).hasStack() && !this.insertItem(itemStack2, 1, 2, false))
					return ItemStack.EMPTY;
				else if (this.electric) {
					// Charged Batteries
					if(this.getSlot(ElectricScooterEntity.SLOT_CHARGED).canInsert(itemStack2) && !this.insertItem(itemStack2, 2, 3, false))
						return ItemStack.EMPTY;
					// Discharged Batteries
					else if(this.getSlot(ElectricScooterEntity.SLOT_DISCHARGED).canInsert(itemStack2) && !this.insertItem(itemStack2, 3, 4, false))
						return ItemStack.EMPTY;
				}
				else
					return ItemStack.EMPTY;
			}
			if (itemStack2.isEmpty()) {
				slot.setStack(ItemStack.EMPTY);
			} else {
				slot.markDirty();
			}
		}
		// NEVER EVER return the item stack unmodified or you'll break people's worlds
		// While returned itemstack not empty and stacks are not equal
		return ItemStack.EMPTY;
	}

	/**
	 * Specialized slot class for the unique case that is the battery slots in the scooter.
	 * Linked slots only work in pairs, and they must be linked with {@code linkWith()} after they're created.
	 */
	private class LinkedSlot
	extends Slot {
		private LinkedSlot other;
		private final Item input;
		public LinkedSlot(Inventory inventory, int index, int x, int y, Item input) {
			super(inventory, index, x, y);
			this.input = input;
		}
		public void linkWith(Slot slot) {
			this.other = (LinkedSlot)slot;
		}
		@Override
		public boolean canInsert(ItemStack stack) {
			return stack.isOf(this.input);
		}
		@Override
		public int getMaxItemCount() {
			return 64 - this.other.getStack().getCount();
		}
	}
}
