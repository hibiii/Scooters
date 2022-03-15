package hibi.scooters.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import hibi.scooters.Common;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;

@Mixin(GrindstoneScreenHandler.class)
public class GrindstoneScreenHandlerMixin {

	@ModifyArgs(
		method = "updateResult",
		at = @At(
			value = "INVOKE",
			target = "grind(Lnet/minecraft/item/ItemStack;II)Lnet/minecraft/item/ItemStack;"
		)
	)
	private void buffGrind(Args args) {
		ItemStack item = args.get(0);
		if(!item.isOf(Common.TIRE_ITEM)) return;
		int damage = args.get(1);
		damage -= 37;
		if(damage < 0) damage = 0;
		args.set(1, damage);
	}
}
