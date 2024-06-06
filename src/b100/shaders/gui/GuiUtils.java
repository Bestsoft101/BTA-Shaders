package b100.shaders.gui;

import static org.lwjgl.opengl.GL11.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.sound.SoundCategory;

public class GuiUtils {
	
	public static GuiUtils instance;
	
	public Minecraft mc;
	
	public GuiUtils(Minecraft mc) {
		this.mc = mc;
	}
	
	public void drawString(String string, int x, int y, int color) {
		mc.fontRenderer.drawStringWithShadow(string, x, y, color);
	}

	public void drawCenteredString(String string, int x, int y, int color) {
		mc.fontRenderer.drawCenteredString(string, x, y, color);
	}

	public int getStringWidth(String string) {
		return mc.fontRenderer.getStringWidth(string);
	}

	public void drawRectangle(int x, int y, int w, int h, int color) {
		Tessellator tessellator = Tessellator.instance;
		int a = (color >> 24) & 0xFF;
		int r = (color >> 16) & 0xFF;
		int g = (color >>  8) & 0xFF;
		int b = (color >>  0) & 0xFF;
		int x0 = x;
		int y0 = y;
		int x1 = x + w;
		int y1 = y + h;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA(r, g, b, a);
		tessellator.addVertex(x0, y0, 0);
		tessellator.addVertex(x0, y1, 0);
		tessellator.addVertex(x1, y1, 0);
		tessellator.addVertex(x1, y0, 0);
		tessellator.draw();
	}

	public void drawGradientRectangle(int x, int y, int w, int h, int topColor, int bottomColor) {
		Tessellator tessellator = Tessellator.instance;
		
		int a0 = (topColor >> 24) & 0xFF;
		int r0 = (topColor >> 16) & 0xFF;
		int g0 = (topColor >>  8) & 0xFF;
		int b0 = (topColor >>  0) & 0xFF;
		
		int a1 = (bottomColor >> 24) & 0xFF;
		int r1 = (bottomColor >> 16) & 0xFF;
		int g1 = (bottomColor >>  8) & 0xFF;
		int b1 = (bottomColor >>  0) & 0xFF;
		
		int x0 = x;
		int y0 = y;
		int x1 = x + w;
		int y1 = y + h;
		
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA(r0, g0, b0, a0);
		tessellator.addVertex(x0, y0, 0);
		tessellator.setColorRGBA(r1, g1, b1, a1);
		tessellator.addVertex(x0, y1, 0);
		tessellator.addVertex(x1, y1, 0);
		tessellator.setColorRGBA(r0, g0, b0, a0);
		tessellator.addVertex(x1, y0, 0);
		tessellator.draw();
	}

	public void playButtonSound() {
		mc.sndManager.playSound("random.click", SoundCategory.GUI_SOUNDS, 1.0f, 1.0f);
	}

	public boolean isGuiOpened() {
		return mc.currentScreen != null;
	}

	public boolean isMinimapGuiOpened() {
		return mc.currentScreen instanceof GuiWrapper;
	}

	public void displayGui(GuiScreen screen) {
		if(screen != null) {
			GuiWrapper wrapper = new GuiWrapper(screen);
			mc.displayGuiScreen(wrapper);
			wrapper.onOpened();
		}else {
			mc.displayGuiScreen(null);
		}
	}

	public GuiScreen getCurrentScreen() {
		if(mc.currentScreen instanceof GuiWrapper) {
			GuiWrapper guiWrapper = (GuiWrapper) mc.currentScreen;
			return guiWrapper.screen;
		}
		return null;
	}

	public void drawIcon(int icon, int x, int y, int color) {
		glBindTexture(GL_TEXTURE_2D, mc.renderEngine.getTexture("/minimap/gui.png"));
		
		int a = color >> 24 & 0xFF;
		int r = color >> 16 & 0xFF;
		int g = color >>  8 & 0xFF;
		int b = color >>  0 & 0xFF;
		
		if(a == 0) {
			a = 255;
		}
		
		int iconX = icon & 3;
		int iconY = icon >> 2;
		
		float u0 = iconX / 4.0f;
		float v0 = iconY / 4.0f;
		
		float u1 = (iconX + 1) / 4.0f;
		float v1 = (iconY + 1) / 4.0f;
		
		int x1 = x + 8;
		int y1 = y + 8;
		
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA(r, g, b, a);
		tessellator.addVertexWithUV(x, y, 0, u0, v0);
		tessellator.addVertexWithUV(x, y1, 0, u0, v1);
		tessellator.addVertexWithUV(x1, y1, 0, u1, v1);
		tessellator.addVertexWithUV(x1, y, 0, u1, v0);
		tessellator.draw();
	}

	public void drawIconWithShadow(int icon, int x, int y, int color) {
		int shadowColor = color;
        int alphaChannelOnly = shadowColor & 0xFF000000;
        shadowColor = (shadowColor & 0xFCFCFC) >> 2;
        shadowColor += alphaChannelOnly;
        
        drawIcon(icon, x + 1, y + 1, shadowColor);
        drawIcon(icon, x, y, color);
	}

	public void drawTexturedRectangle(int x, int y, int w, int h, float u0, float v0, float u1, float v1, int color) {
		Tessellator tessellator = Tessellator.instance;
		int a = (color >> 24) & 0xFF;
		int r = (color >> 16) & 0xFF;
		int g = (color >>  8) & 0xFF;
		int b = (color >>  0) & 0xFF;
		int x0 = x;
		int y0 = y;
		int x1 = x + w;
		int y1 = y + h;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA(r, g, b, a);
		tessellator.addVertexWithUV(x0, y0, 0, u0, v0);
		tessellator.addVertexWithUV(x0, y1, 0, u0, v1);
		tessellator.addVertexWithUV(x1, y1, 0, u1, v1);
		tessellator.addVertexWithUV(x1, y0, 0, u1, v0);
		tessellator.draw();
	}
	
	public void drawTexturedRectangle(int x, int y, int w, int h, int u, int v) {
		drawTexturedRectangle(x, y, w, h, u, v, 256);
	}
	
	public void drawTexturedRectangle(int x, int y, int w, int h, int u, int v, int textureResolution) {
		float u0 = u / (float) textureResolution;
		float v0 = v / (float) textureResolution;
		float u1 = (u + w) / (float) textureResolution;
		float v1 = (v + h) / (float) textureResolution;
		drawTexturedRectangle(x, y, w, h, u0, v0, u1, v1, 0xFFFFFFFF);
	}
	
	public void bindTexture(String tex) {
		glBindTexture(GL_TEXTURE_2D, mc.renderEngine.getTexture(tex));
	}

}
