package hibi.scooters;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ClientInit implements ClientModInitializer {
	
	public static final EntityModelLayer SCOOTER_MODEL_LAYER = new EntityModelLayer(new Identifier("scooters", "scooter"), "main");

	@Override
	public void onInitializeClient() {
		EntityModelLayerRegistry.registerModelLayer(SCOOTER_MODEL_LAYER, ScooterEntityModel::model);
		EntityRendererRegistry.register(Common.SCOOTER_ENTITY, ScooterEntityRenderer::new);
		EntityRendererRegistry.register(Common.ELECTRIC_SCOOTER_ENTITY, ScooterEntityRenderer::new);
		ScreenRegistry.register(Common.SCOOTER_SCREEN_HANDLER, ScooterScreen::new);
		ClientPlayNetworking.registerGlobalReceiver(Common.PACKET_INVENTORY_CHANGED, (client, handler, buf, responseSender) -> {
			int id = buf.readInt();
			ScooterEntity scoot = (ScooterEntity)client.world.getEntityById(id);
			if(scoot == null) return;
			List<ItemStack> contents = buf.readCollection(s -> new ArrayList<ItemStack>(s), PacketByteBuf::readItemStack);
			client.execute(()->{
				for (int i = 0; i < scoot.items.size(); i++) {
					scoot.items.setStack(i, contents.get(i));
				}
				scoot.onInventoryChanged(scoot.items);
			});
		});
	}
}
