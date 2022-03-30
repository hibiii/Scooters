package hibi.scooters;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class ScooterScreenHandler
extends ScreenHandler {
	public final int scooterId;
	public final ScooterEntity scooter;
	public ItemStack frontTire, rearTire;

	// Client Init
	public ScooterScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
		super(Common.SCOOTER_SCREEN_HANDLER, syncId);
		this.scooterId = buf.readInt();
		MinecraftClient inst = MinecraftClient.getInstance();
		this.scooter = (ScooterEntity) inst.world.getEntityById(scooterId);
		this.addScooterSlots(this.scooter.items);
		this.addPlayerInventory(playerInventory);
	}

	// Server Init
	public ScooterScreenHandler(int syncId, PlayerInventory playerInventory, ScooterEntity entity) {
		super(Common.SCOOTER_SCREEN_HANDLER, syncId);
		this.scooter = entity;
		this.scooterId = entity.getId();
		this.addScooterSlots(this.scooter.items);
		this.addPlayerInventory(playerInventory);
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}

	private void addScooterSlots(Inventory inventory) {
		this.addSlot(new Slot(inventory, 0, 62, 36){
			public boolean canInsert(ItemStack stack) {
				return stack.isOf(Common.TIRE_ITEM);
			}
		});
		this.addSlot(new Slot(inventory, 1, 62, 54){
			public boolean canInsert(ItemStack stack) {
				return stack.isOf(Common.TIRE_ITEM);
			}
		});
	}

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
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.getSlot(index);
		if (slot != null && slot.hasStack()) {
			ItemStack itemStack2 = slot.getStack();
			itemStack = itemStack2.copy();
			int size = this.scooter.items.size();
			if (index < size) {
				if(!this.insertItem(itemStack2, size, this.slots.size(), true))
					return ItemStack.EMPTY;
			}
			else {
				if(this.getSlot(0).canInsert(itemStack2) && !this.getSlot(0).hasStack() && !this.insertItem(itemStack2, 0, 1, false))
					return ItemStack.EMPTY;
				else if(this.getSlot(1).canInsert(itemStack2) && !this.getSlot(1).hasStack() && !this.insertItem(itemStack2, 1, 2, false))
					return ItemStack.EMPTY;
				else
					return ItemStack.EMPTY;
			}
			if (itemStack2.isEmpty()) {
				slot.setStack(ItemStack.EMPTY);
			} else {
				slot.markDirty();
			}
		}
		return itemStack;
	}
}
