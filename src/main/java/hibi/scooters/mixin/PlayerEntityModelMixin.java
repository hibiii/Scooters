package hibi.scooters.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import hibi.scooters.ScooterEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;

@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelMixin
extends BipedEntityModelMixin {

	@Shadow public @Final ModelPart leftPants;
	@Shadow public @Final ModelPart rightPants;

	@Inject(
		method="setAngles",
		at = @At("TAIL")
	)
	protected void setAngleInject(LivingEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo info) {
		if(((PlayerEntityModel<?>)(Object)this).riding && entity.getVehicle() instanceof ScooterEntity) {
			this.leftLeg.roll = 0.130899694f; // 7.5 deg
			this.leftLeg.yaw = 0f;
			this.leftPants.roll = 0.130899694f;
			this.leftPants.yaw = 0f;
			this.rightLeg.roll = -0.130899694f;
			this.rightLeg.yaw = 0f;
			this.rightPants.roll = -0.130899694f;
			this.rightPants.yaw = 0f;
			// Simulate leg dominance relationship with hand dominance
			if(((ClientPlayerEntity)entity).getMainArm() == Arm.RIGHT) {
				this.leftLeg.pitch = 0.261799388f; // 15 deg
				this.leftPants.pitch = 0.261799388f;
				this.rightLeg.pitch = -0.261799388f;
				this.rightPants.pitch = -0.261799388f;
			}
			else {
				this.leftLeg.pitch = -0.261799388f;
				this.leftPants.pitch = -0.261799388f;
				this.rightLeg.pitch = 0.261799388f;
				this.rightPants.pitch = 0.261799388f;
			}
		}
	}
}
