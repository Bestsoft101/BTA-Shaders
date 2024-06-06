package b100.shaders.gui;

public abstract class GuiElement extends Gui {
	
	public int posX;
	public int posY;
	public int width;
	public int height;
	
	public GuiElement() {
		
	}
	
	public abstract void draw(float partialTicks);
	
	public void keyEvent(int key, char c, boolean pressed, boolean repeat, int mouseX, int mouseY) {
		
	}
	
	public void mouseEvent(int button, boolean pressed, int mouseX, int mouseY) {
		
	}
	
	public void scrollEvent(int dir, int mouseX, int mouseY) {
		
	}

	public void onResize() {
		
	}
	
	public void onAddToContainer(GuiContainer container) {
		
	}
	
	public boolean isSolid() {
		return true;
	}
	
	public GuiElement setPositionAndSize(int x, int y, int w, int h) {
		setPosition(x, y);
		setSize(w, h);
		return this;
	}
	
	public GuiElement setPosition(int x, int y) {
		this.posX = x;
		this.posY = y;
		return this;
	}
	
	public GuiElement setSize(int w, int h) {
		this.width = w;
		this.height = h;
		return this;
	}
	
	public GuiElement setPositionAndSize(GuiElement element) {
		this.posX = element.posX;
		this.posY = element.posY;
		this.width = element.width;
		this.height = element.height;
		return this;
	}
	
	public boolean isInside(int x, int y) {
		return x >= posX && y >= posY && x < posX + width && y < posY + height;
	}
	
}
