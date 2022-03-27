package hibi.scooters;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class ScooterScreenHandler extends ScreenHandler {

	// Client Init
	public ScooterScreenHandler(int syncId, PlayerInventory playerInventory) {
		super(Common.SCOOTER_SCREEN_HANDLER, syncId);
		this.addSlots(playerInventory);
	}

	// Server Init
	public ScooterScreenHandler(int syncId, PlayerInventory playerInventory, ScooterEntity entity) {
		super(Common.SCOOTER_SCREEN_HANDLER, syncId);
		this.addSlots(playerInventory);
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}

	private void addSlots(PlayerInventory pi) {
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
	
}
