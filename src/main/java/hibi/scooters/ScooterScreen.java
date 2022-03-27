package hibi.scooters;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ScooterScreen extends HandledScreen<ScooterScreenHandler> {
	private static final Identifier TEXTURE = new Identifier("scooters","textures/gui/scooter.png");
	private final ScooterEntity entity;
	private float mouseX, mouseY;

	public ScooterScreen(ScooterScreenHandler handler, PlayerInventory inventory, ScooterEntity entity) {
		super(handler, inventory, entity.getDisplayName());
		this.entity = entity;
	}

	public ScooterScreen(ScooterScreenHandler handler, PlayerInventory inventory, Text text) {
		super(handler, inventory, text);
		this.entity = null;
	}

	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, TEXTURE);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		super.render(matrices, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(matrices, mouseX, mouseY);
	}
	
}
