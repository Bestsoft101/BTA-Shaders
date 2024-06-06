package b100.shaders.gui;

public class GuiTextElement extends GuiElement {

	public static final int ALIGN_START = 0;
	public static final int ALIGN_CENTER = 1;
	public static final int ALIGN_END = 2;
	
	public int horizontalAlign = ALIGN_CENTER;
	public int verticalAlign = ALIGN_CENTER;
	
	public String text;
	public int color = 0xFFFFFFFF;
	
	public GuiTextElement() {
		
	}
	
	public GuiTextElement(String text) {
		this.text = text;
	}
	
	public GuiTextElement(String text, int color) {
		this.text = text;
		this.color = color;
	}
	
	@Override
	public void draw(float partialTicks) {
		if(text == null) {
			return;
		}
				
		int stringWidth = utils.getStringWidth(text);
		
		int x, y;
		
		if(horizontalAlign == ALIGN_END) {
			x = posX + width - stringWidth;
		}else if(horizontalAlign == ALIGN_CENTER) {
			x = posX + (width - stringWidth) / 2;
		}else {
			x = posX;
		}
		
		if(verticalAlign == ALIGN_END) {
			y = posY + height - 8;
		}else if(horizontalAlign == ALIGN_CENTER) {
			y = posY + (height - 8) / 2;
		}else {
			y = posY;
		}
		
		utils.drawString(text, x, y, color);
	}
	
	@Override
	public boolean isSolid() {
		return false;
	}

}
