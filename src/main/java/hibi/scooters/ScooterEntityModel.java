package hibi.scooters;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

public class ScooterEntityModel extends EntityModel<ScooterEntity> {
	private final ModelPart Back;
	private final ModelPart Steering;

	public ScooterEntityModel(ModelPart root) {
		this.Back = root.getChild("Back");
		this.Steering = root.getChild("Steering");
	}

	public static TexturedModelData model() {
		ModelData meshdefinition = new ModelData();
		ModelPartData partdefinition = meshdefinition.getRoot();

		partdefinition.addChild("Back", ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, -2.0F, -7.0F, 4.0F, 1.0F, 14.0F, Dilation.NONE)
		.uv(0, 17).cuboid(-0.5F, -3.0F, 8.0F, 1.0F, 3.0F, 3.0F, Dilation.NONE)
		.uv(4, 0).cuboid(-1.0F, -4.0F, 7.0F, 2.0F, 3.0F, 3.0F, Dilation.NONE), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData Steering = partdefinition.addChild("Steering", ModelPartBuilder.create().uv(0, 15).cuboid(-6.0F, -17.0F, 0.0F, 12.0F, 1.0F, 1.0F, Dilation.NONE)
		.uv(8, 17).cuboid(-0.5F, -3.0F, -4.0F, 1.0F, 3.0F, 3.0F, Dilation.NONE)
		.uv(4, 6).cuboid(-1.0F, -4.0F, -3.0F, 2.0F, 3.0F, 3.0F, Dilation.NONE), ModelTransform.pivot(0.0F, 24.0F, -7.0F));

		Steering.addChild("Bar_r1", ModelPartBuilder.create().uv(0, 0).cuboid(-0.5F, -7.0F, 2.25F, 1.0F, 13.0F, 1.0F, Dilation.NONE), ModelTransform.of(0.0F, -10.0F, -3.5F, -0.1745F, 0.0F, 0.0F));

		return TexturedModelData.of(meshdefinition, 64, 64);
	}

	@Override
	public void setAngles(ScooterEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		Back.render(matrices, vertices, light, overlay);
		Steering.render(matrices, vertices, light, overlay);
	}
}