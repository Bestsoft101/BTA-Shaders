package b100.shaders.gui;

import static org.lwjgl.opengl.GL11.*;

public class GuiShadow extends GuiElement {

	public GuiElement element;
	public boolean inside;
	public boolean bottom;
	
	public int shadowColor = 0xDD000000;
	
	public GuiShadow(GuiElement element, boolean inside, boolean bottom) {
		this.element = element;
		this.inside = inside;
		this.bottom = bottom;
	}
	
	@Override
	public void draw(float partialTicks) {
		glDisable(GL_TEXTURE_2D);
		
		int transparentColor = shadowColor & 0x00FFFFFF;
		
		int topColor = transparentColor;
		int bottomColor = transparentColor;
		
		if(inside) {
			if(bottom) {
				bottomColor = shadowColor;
			}else {
				topColor = shadowColor;
			}
		}else {
			if(bottom) {
				bottomColor = shadowColor;
			}else {
				topColor = shadowColor;
			}
		}
		
		utils.drawGradientRectangle(posX, posY, width, height, topColor, bottomColor);
		
		glEnable(GL_TEXTURE_2D);
	}
	
	@Override
	public boolean isSolid() {
		return false;
	}
	
	@Override
	public void onResize() {
		posX = element.posX;
		width = element.width;
		height = 4;
		
		if(inside) {
			if(bottom) {
				posY = element.posY + element.height - height;
			}else {
				posY = element.posY;
			}
		}else {
			if(bottom) {
				posY = element.posY + element.height;
			}else {
				posY = element.posY - height;
			}
		}
	}

}
