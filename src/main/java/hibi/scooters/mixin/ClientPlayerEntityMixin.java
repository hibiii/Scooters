package hibi.scooters.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import hibi.scooters.ScooterEntity;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends Entity {
	
	public ClientPlayerEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Shadow
	private boolean riding;

	@Shadow
	private Input input;

	@Inject(
		method = "tickRiding()V",
		at = @At("TAIL")
	)
	private void tickRiding(CallbackInfo info) {
		Entity vehicle = this.getVehicle();
		if(vehicle instanceof ScooterEntity) {
			((ScooterEntity)vehicle).setInputs(this.input.pressingForward, this.input.pressingBack, this.input.pressingLeft, this.input.pressingRight);
			this.riding = vehicle.getVelocity().length() > 0.02d;
		}
	}
}
