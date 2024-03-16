package hibi.scooters;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class ScooterEntityRenderer extends EntityRenderer<ScooterEntity> {

	protected final ScooterEntityModel model;
	protected final Identifier kick_texture = new Identifier(Common.MODID, "textures/entity/kick_scooter.png");
	protected final Identifier electric_texture = new Identifier(Common.MODID, "textures/entity/electric_scooter.png");
	protected final Identifier color_overlay = new Identifier(Common.MODID, "textures/entity/scooter_color.png");

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
		matrices.multiply(Axis.Z_POSITIVE.rotationDegrees(180.0f));
		matrices.multiply(Axis.Y_POSITIVE.rotationDegrees(180.0f + yaw));
		// Lower the scooter if it doesn't have any tires attached (= resting on the platform)
		if(!(entity.frontTire || entity.rearTire))
			matrices.translate(0d, -1.445d, 0d);
		else
			matrices.translate(0d, -1.5d, 0d);

		// Render the scooter itself
		VertexConsumer vertices = vertexConsumers.getBuffer(this.model.getLayer(this.getTexture(entity)));
		this.model.setTires(entity.frontTire, entity.rearTire);
		this.model.render(matrices, vertices, light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
		float[] color = entity.getBodyColor();
		if (color != null) {
			VertexConsumer cutout = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(color_overlay));
			this.model.render(matrices, cutout, light, OverlayTexture.DEFAULT_UV, color[0], color[1], color[2], 1);
		}
		matrices.pop();
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);

		// Render the charging cable if the scooter is connected to a charging station
		// TODO: Declutter: Break subroutine to function
		if(entity instanceof ElectricScooterEntity) {
			ElectricScooterEntity e = (ElectricScooterEntity)entity;
			if(e.isConnectedToCharger()) {
				BlockPos charger = e.getDataTracker().get(ElectricScooterEntity.CHARGER).orElseGet(() -> null);
				if(e != null) {
					// Apparently 0-offset is at the +X-Y+Zmost corner
					double r = MathHelper.lerp((double)tickDelta, e.prevX, e.getX()) - 0.5;
					double s = MathHelper.lerp((double)tickDelta, e.prevY, e.getY()) - 0.8;
					double t = MathHelper.lerp((double)tickDelta, e.prevZ, e.getZ()) - 0.5;
					float u = (float)(charger.getX() - r);
					float v = (float)(charger.getY() - s);
					float w = (float)(charger.getZ() - t);
					VertexConsumer vertexConsumer2 = vertexConsumers.getBuffer(RenderLayer.getLineStrip());
					MatrixStack.Entry entry2 = matrices.peek();
					// unrolled loop
					// for(float i = 0.0f; i < 1.0f; i += 0.1f) SER.rCC(u, v, w, vC2, e2, i, i + 0.1f);
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.0f, 0.1f);
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.1f, 0.2f);
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.2f, 0.3f);
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.3f, 0.4f);
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.4f, 0.5f);
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.5f, 0.6f);
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.6f, 0.7f);
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.7f, 0.8f);
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.8f, 0.9f);
					ScooterEntityRenderer.renderChagingCable(u, v, w, vertexConsumer2, entry2, 0.9f, 1.0f);
				}
			}
		}
	}

	// Based on the vanilla fishing line render
	private static void renderChagingCable(float x, float y, float z, VertexConsumer buffer, MatrixStack.Entry matrices, float segmentStart, float segmentEnd) {
		float f = x * segmentStart;
		float g = y * (segmentStart * segmentStart + segmentStart) * 0.5f + 0.1f;
		float h = z * segmentStart;
		float i = x * segmentEnd - f;
		float j = y * (segmentEnd * segmentEnd + segmentEnd) * 0.5f + 0.1f - g;
		float k = z * segmentEnd - h;
		float l = MathHelper.sqrt(i * i + j * j + k * k);
		buffer.vertex(matrices.getModel(), f , g, h).color(0, 0, 0, 255).normal(matrices.getNormal(), i / l, j / l, k / l).next();
	}

	
}
