package b100.shaders.gui;

public class GuiBackgroundBox extends GuiElement {

	public int color = 0xFF404040;
	
	@Override
	public void draw(float partialTicks) {
		utils.bindTexture(BACKGROUND_TEX);
		utils.drawTexturedRectangle(posX, posY, width, height, 0, 0, width / 32.0f, height / 32.0f, 0xFF404040);
	}

}
