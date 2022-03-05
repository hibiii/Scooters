package hibi.scooters.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.Entity;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin {
    @Shadow public @Final ModelPart head;
    @Shadow public @Final ModelPart hat;
    @Shadow public @Final ModelPart body;
    @Shadow public @Final ModelPart rightArm;
    @Shadow public @Final ModelPart leftArm;
    @Shadow public @Final ModelPart rightLeg;
    @Shadow public @Final ModelPart leftLeg;

	@Inject(
		method = "setAngles(Lnet/minecraft/entity/Entity;FFFFF)V",
		at = @At("TAIL"))
	protected void setAngleInject(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo info) {
	}
}
