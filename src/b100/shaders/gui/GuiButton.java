package b100.shaders.gui;

import java.util.ArrayList;
import java.util.List;

import b100.shaders.gui.util.ActionListener;

public class GuiButton extends GuiElement {

	public GuiScreen screen;
	
	public String text;
	
	private List<ActionListener> actionListeners = new ArrayList<>();
	
	public boolean enabled = true;
	
	{
		this.width = 200;
		this.height = 20;
	}
	
	public GuiButton(GuiScreen screen, String text) {
		this.screen = screen;
		this.text = text;
	}
	
	public GuiButton(GuiScreen screen, String text, ActionListener actionListener) {
		this.screen = screen;
		this.text = text;
		
		addActionListener(actionListener);
	}
	
	@Override
	public void draw(float partialTicks) {
		int v;
		if(enabled) {
			if(screen.getClickElementAt(screen.cursorX, screen.cursorY) == this) {
				v = V_BUTTON_HOVER;
			}else {
				v = V_BUTTON_NORMAL;
			}
		}else {
			v = V_BUTTON_DISABLED;
		}
		
		int w0 = width / 2;
		int w1 = width - w0;
		
		utils.bindTexture(GUI_TEX);
		utils.drawTexturedRectangle(posX, posY, w0, height, 0, v);
		utils.drawTexturedRectangle(posX + w0, posY, w1, height, 200 - w0, v);
		
		int textWidth = utils.getStringWidth(text);
		utils.drawString(text, posX + (width - textWidth) / 2, posY + height / 2 - 4, 0xFFFFFFFF);
	}
	
	@Override
	public void mouseEvent(int button, boolean pressed, int mouseX, int mouseY) {
		if(!enabled) {
			return;
		}
		if(button == 0 && pressed && screen.getClickElementAt(mouseX, mouseY) == this) {
			utils.playButtonSound();
			onPress();
		}
	}
	
	public void onPress() {
		for(int i=0; i < actionListeners.size(); i++) {
			actionListeners.get(i).actionPerformed(this);
		}
	}
	
	public void addActionListener(ActionListener actionListener) {
		this.actionListeners.add(actionListener);
	}
	
	public boolean removeActionListener(ActionListener actionListener) {
		return actionListeners.remove(actionListener);
	}

}
