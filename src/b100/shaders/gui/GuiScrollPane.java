package b100.shaders.gui;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

import b100.shaders.gui.util.ScrollPaneListener;
import net.minecraft.core.util.helper.MathHelper;

public class GuiScrollPane extends GuiContainer {
	
	public GuiScreen screen;
	public Layout layout;
	
	public int scrollAmount;
	public boolean scrollAmountUpdated = false;
	
	public int paddingLeft = 0;
	public int paddingRight = 0;
	
	private boolean dragging = false;
	private int dragStartPosY;
	private int prevScrollAmount;
	
	public boolean enableBackground = true;
	
	private List<ScrollPaneListener> scrollPaneListeners = new ArrayList<>();
	
	public GuiScrollPane(GuiScreen screen) {
		this.screen = screen;
		this.layout = new ListLayout(this);
	}
	
	public GuiScrollPane(GuiScreen screen, Layout layout) {
		this.screen = screen;
		this.layout = layout;
	}
	
	@Override
	public void draw(float partialTicks) {
		if(dragging) {
			setScrollAmount(prevScrollAmount - (screen.cursorY - dragStartPosY));
			prevScrollAmount = scrollAmount;
			dragStartPosY = screen.cursorY;
		}
		
		if(scrollAmountUpdated) {
			scrollAmountUpdated = false;
			updateElements();
		}
		
		if(enableBackground) {
			if(mc.theWorld != null) {
				glDisable(GL_TEXTURE_2D);
				utils.drawRectangle(posX, posY, width, height, 0x80000000);
				glEnable(GL_TEXTURE_2D);
			}else {
				int bgScrollAmount = scrollAmount / 2;
				
				utils.bindTexture(BACKGROUND_TEX);
				utils.drawTexturedRectangle(posX, posY, width, height, 0, bgScrollAmount / 32.0f, width / 32.0f, (height + bgScrollAmount) / 32.0f, 0xFF202020);
			}
		}
		
		super.draw(partialTicks);
	}
	
	@Override
	public void onResize() {
		super.onResize();
		
		updateElements();
	}
	
	@Override
	public void mouseEvent(int button, boolean pressed, int mouseX, int mouseY) {
		if(button == 0 && !pressed) {
			dragging = false;
		}
		
		super.mouseEvent(button, pressed, mouseX, mouseY);
		
		if(button == 0 && pressed && screen.getClickElementAt(mouseX, mouseY) == this) {
			dragging = true;
			dragStartPosY = mouseY;
			prevScrollAmount = scrollAmount;
		}
	}
	
	@Override
	public void scrollEvent(int dir, int mouseX, int mouseY) {
		super.scrollEvent(dir, mouseX, mouseY);
		
		setScrollAmount(this.scrollAmount - (dir / 4));
	}
	
	public void setScrollAmount(int newScrollAmount) {
		int min = 0;
		int max = Math.max(0, layout.getContentHeight() - this.height);
		newScrollAmount = MathHelper.clamp(newScrollAmount, min, max);
		
		if(newScrollAmount != scrollAmount) {
			scrollAmount = newScrollAmount;
			scrollAmountUpdated = true;
		}
	}
	
	public void updateElements() {
		layout.updateElements();
		
		for(int i=0; i < scrollPaneListeners.size(); i++) {
			scrollPaneListeners.get(i).elementsUpdated(this);
		}
	}
	
	public void addListener(ScrollPaneListener listener) {
		this.scrollPaneListeners.add(listener);
	}
	
	public void removeListener(ScrollPaneListener listener) {
		this.scrollPaneListeners.remove(listener);
	}
	
	@Override
	public boolean isSolid() {
		return true;
	}
	
	public static interface Layout {
		
		public void updateElements();
		
		public int getContentHeight();
		
	}
	
	public static class ListLayout implements Layout {
		
		public GuiScrollPane scrollPane;
		public int padding = 3;
		
		public ListLayout(GuiScrollPane scrollPane) {
			this.scrollPane = scrollPane;
		}
		
		@Override
		public void updateElements() {
			int y = scrollPane.posY - scrollPane.scrollAmount + padding;
			for(int i=0; i < scrollPane.elements.size(); i++) {
				GuiElement e = scrollPane.elements.get(i);
				
				e.posX = scrollPane.posX + scrollPane.paddingLeft + padding;
				e.posY = y;
				e.width = scrollPane.width - scrollPane.paddingLeft - scrollPane.paddingRight - 2 * padding;
				
				y += e.height;
			}
		}

		@Override
		public int getContentHeight() {
			int h = 2 * padding;
			for(int i=0; i < scrollPane.elements.size(); i++) {
				GuiElement e = scrollPane.elements.get(i);
				
				h += e.height;
			}
			return h;
		}
	}

}
