package b100.shaders.gui;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

import b100.shaders.gui.util.FocusListener;
import b100.shaders.gui.util.Focusable;

public class GuiFocusButton extends GuiElement implements Focusable {
	
	public GuiScreen screen;
	
	public String text;
	
	private List<FocusListener> focusListeners = new ArrayList<>();
	private boolean focused = false;
	
	public GuiFocusButton(GuiScreen screen) {
		this.screen = screen;
		this.width = 200;
		this.height = 20;
	}
	
	public GuiFocusButton(GuiScreen screen, String text) {
		this(screen);
		this.text = text;
	}
	
	@Override
	public void draw(float partialTicks) {
		if(focused) {
			glDisable(GL_TEXTURE_2D);
			utils.drawRectangle(posX, posY, width, height, 0xFF808080);
			utils.drawRectangle(posX + 1, posY + 1, width - 2, height - 2, 0xFF000000);
			glEnable(GL_TEXTURE_2D);
		}
		
		if(text != null) {
			utils.drawString(text, posX + 4, posY + height / 2 - 4, 0xFFFFFF);
		}
	}

	@Override
	public void setFocused(boolean focused) {
		this.focused = focused;
		onFocusChanged();
	}
	
	public void onFocusChanged() {
		for(int i=0; i < focusListeners.size(); i++) {
			focusListeners.get(i).focusChanged(this);
		}
	}
	
	@Override
	public void mouseEvent(int button, boolean pressed, int mouseX, int mouseY) {
		if(button == 0 && pressed && screen.getClickElementAt(mouseX, mouseY) == this) {
			setFocused(true);
		}
	}

	@Override
	public boolean isFocused() {
		return focused;
	}

	@Override
	public void addFocusListener(FocusListener focusListener) {
		focusListeners.add(focusListener);
	}

	@Override
	public void removeFocusListener(FocusListener focusListener) {
		focusListeners.remove(focusListener);
	}

}
