package hibi.scooters;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

public class ScooterScreen extends HandledScreen<ScooterScreenHandler> {
	private static final Identifier TEXTURE = new Identifier(Common.MODID,"textures/gui/scooter.png");
	private final ScooterEntity entity;
	private final boolean electric;

	public ScooterScreen(ScooterScreenHandler handler, PlayerInventory inventory, Text text) {
		super(handler, inventory, text);
		MinecraftClient inst = MinecraftClient.getInstance();
		this.entity = (ScooterEntity) inst.world.getEntityById(handler.scooterId);
		this.electric = handler.electric;
	}

	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, TEXTURE);
        int lmost = (this.width - this.backgroundWidth) / 2;
        int tmost = (this.height - this.backgroundHeight) / 2;

        this.drawTexture(matrices, lmost, tmost, 0, 0, this.backgroundWidth, this.backgroundHeight);
		DefaultedList<Slot> slots = this.getScreenHandler().slots;

		// Tire outlines
		if(!slots.get(0).hasStack())
			this.drawTexture(matrices, lmost + 61, tmost + 35, 0, 166, 18, 18);
		if(!slots.get(1).hasStack())
			this.drawTexture(matrices, lmost + 61, tmost + 53, 0, 166, 18, 18);

		if(this.electric) {

			// Charging slots
			this.drawTexture(matrices, lmost + 97, tmost + 17, 18, 166, 18, 54);

			// Potato outlines
			if(!slots.get(2).hasStack())
				this.drawTexture(matrices, lmost + 97, tmost + 17, 0, 184, 18, 18);
			if(!slots.get(3).hasStack())
				this.drawTexture(matrices, lmost + 97, tmost + 53, 0, 202, 18, 18);
			
			// Spark
			this.drawTexture(matrices, lmost + 138, tmost + 37, 56, 166, 12, 14);
			// Charger
			this.drawTexture(matrices, lmost + 115, tmost + 53, 56, 184, 35, 18);

			ElectricScooterEntity e = (ElectricScooterEntity)this.entity;
			if (e.isCharging()) {
				if(e.getCanCharge()) {
					int y = (int) (this.entity.world.getTime() % 15);
					if(y != 0) {
						// Spark foreground
						this.drawTexture(matrices, lmost + 138, tmost + 51 - y, 68, 180 - y, 12, y);
					}
				}
				// Cord from charger
				this.drawTexture(matrices, lmost + 115, tmost + 53, 56, 202, 35, 18);
			}

			// Charge bar
			this.drawTexture(matrices, lmost + 115, tmost + 26, 36, 175, 10, 28);
			// Charge filled in
			int p = (int) (((ElectricScooterEntity)this.entity).getChargeProgress() * 29);
			this.drawTexture(matrices, lmost + 115, tmost + 54 - p, 46, 203 - p, 10, p);
		}
		ScooterScreen.drawEntity(lmost + 36, tmost + 60, 22, 110f, -24f, this.entity);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(matrices, mouseX, mouseY);
	}

	// Ultra hacky solution to get around InventoryScreen.drawEntity being made for LivingEntitys
	public static void drawEntity(int x, int y, int size, float yaw, float pitch, Entity entity) {
		MatrixStack matrixStack = RenderSystem.getModelViewStack();
		matrixStack.push();
		matrixStack.translate(x, y, 1050.0);
		matrixStack.scale(1.0f, 1.0f, -1.0f);
		RenderSystem.applyModelViewMatrix();
		MatrixStack matrixStack2 = new MatrixStack();
		matrixStack2.translate(0.0, 0.0, 1000.0);
		matrixStack2.scale(size, size, size);
		Quaternion quaternion = Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0f);
		Quaternion quaternion2 = Vec3f.POSITIVE_X.getDegreesQuaternion(pitch);
		quaternion.hamiltonProduct(quaternion2);
		matrixStack2.multiply(quaternion);
		DiffuseLighting.method_34742();
		EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
		quaternion2.conjugate();
		entityRenderDispatcher.setRotation(quaternion2);
		entityRenderDispatcher.setRenderShadows(false);
		VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
		// VANILLA DOES IT SO LET ME DO IT FS
		RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, yaw, 1.0f, matrixStack2, immediate, 0xF000F0));
		immediate.draw();
		entityRenderDispatcher.setRenderShadows(true);
		matrixStack.pop();
		RenderSystem.applyModelViewMatrix();
		DiffuseLighting.enableGuiDepthLighting();
	}
}
