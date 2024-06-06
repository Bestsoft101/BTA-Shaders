package b100.shaders.gui;

import static org.lwjgl.opengl.GL11.*;

import b100.shaders.gui.util.ScrollPaneListener;

public class GuiScrollBar extends GuiElement implements ScrollPaneListener {

	public final GuiScrollPane scrollPane;
	
	public boolean enabled;
	
	private int scrollerHeight;
	private int scrollerPosition;
	
	private boolean dragging = false;
	private int dragStartPosY;
	private int prevScrollAmount;
	
	public GuiScrollBar(GuiScrollPane scrollPane) {
		this.scrollPane = scrollPane;
		this.scrollPane.addListener(this);
	}
	
	@Override
	public void draw(float partialTicks) {
		if(dragging) {
			int contentHeight = scrollPane.layout.getContentHeight();
			
			float scrollFactor = contentHeight / (float) scrollPane.height;
			
			int offset = (int) ((dragStartPosY - scrollPane.screen.cursorY) * scrollFactor);
			
			scrollPane.setScrollAmount(prevScrollAmount - offset);
		}
		
		if(!enabled) {
			return;
		}
		
		int y = posY + scrollerPosition;
		
		glDisable(GL_TEXTURE_2D);
		utils.drawRectangle(posX, posY, width, height, 0xFF000000);
		utils.drawRectangle(posX, y, width, scrollerHeight, 0xFF808080);
		utils.drawRectangle(posX, y, width - 1, scrollerHeight - 1, 0xFFC0C0C0);
		glEnable(GL_TEXTURE_2D);
	}
	
	public void update() {
		int contentHeight = scrollPane.layout.getContentHeight(); 
		
		this.enabled = contentHeight > scrollPane.height;
		
		if(enabled) {
			float heightFactor = scrollPane.height / (float) contentHeight;
			scrollerHeight = (int) (heightFactor * height);
			scrollerHeight = Math.max(scrollerHeight, 16);
			scrollerHeight = Math.min(scrollerHeight, height / 2);
			
			int maxScrollAmount = contentHeight - scrollPane.height;
			
			float scrollFactor = scrollPane.scrollAmount / (float) maxScrollAmount;
			scrollerPosition = (int) ((height - scrollerHeight) * scrollFactor);
		}else {
			this.scrollerHeight = 0;
			this.scrollerPosition = 0;
		}
	}
	
	@Override
	public void mouseEvent(int button, boolean pressed, int mouseX, int mouseY) {
		if(button == 0 && !pressed) {
			dragging = false;
		}
		
		super.mouseEvent(button, pressed, mouseX, mouseY);
		
		if(button == 0 && pressed && scrollPane.screen.getClickElementAt(mouseX, mouseY) == this) {
			dragging = true;
			dragStartPosY = mouseY;
			prevScrollAmount = scrollPane.scrollAmount;
		}
	}
	
	@Override
	public void onResize() {
		update();
	}
	
	@Override
	public void elementsUpdated(GuiScrollPane scrollPane) {
		update();
	}
	
	@Override
	public boolean isSolid() {
		return enabled;
	}
}
