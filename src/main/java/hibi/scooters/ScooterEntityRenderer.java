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
	protected final Identifier texture = new Identifier("scooters", "textures/entity/kick_scooter.png");

	protected ScooterEntityRenderer(Context ctx) {
		super(ctx);
		this.model = new ScooterEntityModel(ctx.getPart(ClientInit.SCOOTER_MODEL_LAYER));
	}

	@Override
	public Identifier getTexture(ScooterEntity entity) {
		return this.texture;
	}

	@Override
	public void render(ScooterEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		matrices.push();
		matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0f));
		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f - yaw));
		matrices.translate(0d, -1.5d, 0d);
		VertexConsumer vertices = vertexConsumers.getBuffer(this.model.getLayer(this.texture));
		this.model.render(matrices, vertices, light, OverlayTexture.DEFAULT_UV, 0, 0, 0, 1);
		matrices.pop();
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
	}
	
}
