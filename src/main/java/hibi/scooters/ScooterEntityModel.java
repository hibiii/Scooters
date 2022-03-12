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
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();

		modelPartData.addChild("Back", ModelPartBuilder.create()
			.uv(0, 0).cuboid(-2.0F, -1.9F, -7.0F, 4.0F, 1.0F, 14.0F, Dilation.NONE) // Platform
			.uv(0, 17).cuboid(-0.5F, -3.0F, 8.0F, 1.0F, 3.0F, 3.0F, Dilation.NONE)  // Back tire
			.uv(4, 0).cuboid(-1.0F, -4.0F, 7.0F, 2.0F, 3.0F, 3.0F, Dilation.NONE),  // Tire cover
		ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData Steering = modelPartData.addChild("Steering", ModelPartBuilder.create()
			.uv(0, 15).cuboid(-6.0F, -16.75F, 1.25F, 12.0F, 1.0F, 1.0F, Dilation.NONE) // Handle bar
			.uv(8, 17).cuboid(-0.5F, -3.0F, -4.0F, 1.0F, 3.0F, 3.0F, Dilation.NONE)    // Front tire
			.uv(4, 6).cuboid(-1.0F, -4.0F, -3.0F, 2.0F, 3.0F, 3.0F, Dilation.NONE),    // Tire cover
		ModelTransform.pivot(0.0F, 24.0F, -7.0F));

		Steering.addChild("Bar", ModelPartBuilder.create()
			.uv(0, 0).cuboid(-0.5F, -12.5F, -0.5F, 1.0F, 13.0F, 1.0F, Dilation.NONE), // Steering Column
		ModelTransform.of(0.0F, -4.0F, -1.5F, -0.2443F, 0.0F, 0.0F));

		Steering.addChild("Brake", ModelPartBuilder.create()
			.uv(13, 17).cuboid(0.0F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F, Dilation.NONE), // Brake lever
		ModelTransform.of(1.0F, -15.75F, 1.25F, 0.0F, 0.2182F, 0.1309F));

		Steering.addChild("Throttle", ModelPartBuilder.create()
			.uv(26,15).cuboid(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, Dilation.NONE), // Throttle lever
		ModelTransform.of(-1.5F, -16.25F, 2.0F, 0.3927F, 0.0F, 0.0F));

		return TexturedModelData.of(modelData, 64, 64);
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