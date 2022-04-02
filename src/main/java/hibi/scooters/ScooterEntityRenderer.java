package hibi.scooters;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

public class ScooterEntityRenderer extends EntityRenderer<ScooterEntity> {

	protected final ScooterEntityModel model;
	protected final Identifier kick_texture = new Identifier("scooters", "textures/entity/kick_scooter.png");
	protected final Identifier electric_texture = new Identifier("scooters", "textures/entity/electric_scooter.png");

	protected ScooterEntityRenderer(Context ctx) {
		super(ctx);
		this.model = new ScooterEntityModel(ctx.getPart(ClientInit.SCOOTER_MODEL_LAYER));
	}

	@Override
	public Identifier getTexture(ScooterEntity entity) {
		if(entity instanceof ElectricScooterEntity)
			return this.electric_texture;
		return this.kick_texture;
	}

	@Override
	public void render(ScooterEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		matrices.push();
		matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0f));
		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f + yaw));
		if(!(entity.frontTire || entity.rearTire))
			matrices.translate(0d, -1.445d, 0d);
		else
			matrices.translate(0d, -1.5d, 0d);
		VertexConsumer vertices = vertexConsumers.getBuffer(this.model.getLayer(this.getTexture(entity)));
		this.model.setTires(entity.frontTire, entity.rearTire);
		this.model.render(matrices, vertices, light, OverlayTexture.DEFAULT_UV, 0, 0, 0, 1);
		matrices.pop();
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
	}
	
}
