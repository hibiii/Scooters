package hibi.scooters.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin {

    @Shadow public @Final ModelPart rightLeg;
    @Shadow public @Final ModelPart leftLeg;
}
