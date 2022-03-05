package hibi.scooters.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import hibi.scooters.ScooterEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;

@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelMixin
extends BipedEntityModelMixin {

	@Inject(
		method="setAngles",
		at = @At("TAIL")
	)
	protected void setAngleInject(LivingEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo info) {
		if(((PlayerEntityModel<?>)(Object)this).riding && entity.getVehicle() instanceof ScooterEntity) {
			this.leftLeg.pitch = 0.261799388f; // 15 deg
			this.leftLeg.yaw = 0f;
			this.leftLeg.roll = 0.130899694f; // 7.5 deg
			this.rightLeg.pitch = -0.261799388f; // 15 deg
			this.rightLeg.yaw = 0f;
			this.rightLeg.roll = -0.130899694f; // 7.5 deg
		}
	}
}
