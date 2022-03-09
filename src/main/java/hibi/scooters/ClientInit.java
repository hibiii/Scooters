package hibi.scooters;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ClientInit implements ClientModInitializer {
	
	public static final EntityModelLayer SCOOTER_MODEL_LAYER = new EntityModelLayer(new Identifier("scooters", "scooter"), "main");

	@Override
	public void onInitializeClient() {
		EntityModelLayerRegistry.registerModelLayer(SCOOTER_MODEL_LAYER, ScooterEntityModel::model);
		EntityRendererRegistry.register(Common.SCOOTER_ENTITY, ScooterEntityRenderer::new);
		EntityRendererRegistry.register(Common.ELECTRIC_SCOOTER_ENTITY, ScooterEntityRenderer::new);
	}
}
