package hibi.scooters.mixin;

import hibi.scooters.Common;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.InputSlotFiller;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InputSlotFiller.class)
public class InputSlotFillerMixin {
    @Shadow protected PlayerInventory inventory;

    private ItemStack scooters$storedStack;

    @Inject(method = "fillInputSlot", at = @At("HEAD"))
    private void storeStack(Slot slot, ItemStack stack, CallbackInfo ci) {
        scooters$storedStack = stack;
    }

    @Inject(method = "fillInputSlot", at = @At(value = "JUMP", ordinal = 0))
    private void releaseStack(Slot slot, ItemStack stack, CallbackInfo ci) {
        scooters$storedStack = null;
    }

    // I don't know why MCDev complains, but it works, so
    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "fillInputSlot", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/player/PlayerInventory;indexOf(Lnet/minecraft/item/ItemStack;)I", shift = At.Shift.AFTER))
    private int fillInScooters(int slotId) {
        if (slotId != -1) return slotId;

        var stack = scooters$storedStack;

        if (!stack.isOf(Common.KICK_SCOOTER_ITEM) && !stack.isOf(Common.TIRE_ITEM)) return slotId;

        for(int i = 0; i < this.inventory.main.size(); ++i) {
            ItemStack otherStack = this.inventory.main.get(i);
            if (!otherStack.isEmpty() && ItemStack.areItemsEqual(stack, otherStack) && !otherStack.isDamaged() && !otherStack.hasEnchantments() && !otherStack.hasCustomName()) {
                return i;
            }
        }
        return -1;
    }
}
