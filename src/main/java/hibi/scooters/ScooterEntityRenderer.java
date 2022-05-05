package hibi.scooters;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
		if(entity instanceof ElectricScooterEntity) {
			ElectricScooterEntity e = (ElectricScooterEntity)entity;
				if(e.isCharging()) {
				BlockPos charger = e.getDataTracker().get(ElectricScooterEntity.CHARGER);
				if(e != null) {
					double r = MathHelper.lerp((double)tickDelta, e.prevX, e.getX()) - 0.5;
					double s = MathHelper.lerp((double)tickDelta, e.prevY, e.getY()) - 0.8;
					double t = MathHelper.lerp((double)tickDelta, e.prevZ, e.getZ()) - 0.5;
					float u = (float)(charger.getX() - r);
					float v = (float)(charger.getY() - s);
					float w = (float)(charger.getZ() - t);
					VertexConsumer vertexConsumer2 = vertexConsumers.getBuffer(RenderLayer.getLineStrip());
					MatrixStack.Entry entry2 = matrices.peek();
					// unrolled loop
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.0f, 0.1f, light);
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.1f, 0.2f, light);
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.2f, 0.3f, light);
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.3f, 0.4f, light);
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.4f, 0.5f, light);
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.5f, 0.6f, light);
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.6f, 0.7f, light);
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.7f, 0.8f, light);
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.8f, 0.9f, light);
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.9f, 1.0f, light);
				}
			}
		}
	}

	private static void renderChagingCable(float x, float y, float z, VertexConsumer buffer, MatrixStack.Entry matrices, float segmentStart, float segmentEnd, int q) {
		float f = x * segmentStart;
		float g = y * (segmentStart * segmentStart + segmentStart) * 0.5f + 0.1f;
		float h = z * segmentStart;
		float i = x * segmentEnd - f;
		float j = y * (segmentEnd * segmentEnd + segmentEnd) * 0.5f + 0.1f - g;
		float k = z * segmentEnd - h;
		float l = MathHelper.sqrt(i * i + j * j + k * k);
		buffer.vertex(matrices.getPositionMatrix(), f , g, h).color(20, 20, 20, 255).normal(matrices.getNormalMatrix(), i /= l, j /= l, k /= l).light(q).next();
	}
}
