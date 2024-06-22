package b100.shaders.gui;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import b100.shaders.ShaderMod;
import net.minecraft.client.input.InputDevice;

public class GuiWrapper extends net.minecraft.client.gui.GuiScreen {
	
	public GuiScreen screen;
	
	/**
	 * Fixes a bug where a character is typed into the focused textbox immediately when opening the create waypoint GUI using the hotkey
	 */
	public boolean skipInput = true;
	
	public GuiWrapper(GuiScreen screen) {
		this.screen = screen;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		boolean resize = false;
		
		if(screen.width != this.width || screen.height != this.height) {
			resize = true;
			
			screen.width = this.width;
			screen.height = this.height;
		}
		
		
		screen.cursorX = mouseX;
		screen.cursorY = mouseY;
		
		if(!screen.isInitialized()) {
			screen.init();
		}
		
		if(resize) {
			screen.onResize();	
		}
		
		while(Keyboard.next()) {
			int key = Keyboard.getEventKey();
			boolean repeat = Keyboard.isRepeatEvent();
			boolean pressed = Keyboard.getEventKeyState();
			char c = Keyboard.getEventCharacter();
			handleKeyEvent(key, c, pressed, repeat);
		}
		while(Mouse.next()) {
			int button = Mouse.getEventButton();
			boolean pressed = Mouse.getEventButtonState();
			handleMouseEvent(button, pressed);
		}
		
		handleScrolling(Mouse.getDWheel());
		
		if(skipInput) {
			skipInput = false;
		}
		
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_ALPHA_TEST);
		glDisable(GL_CULL_FACE);
		glEnable(GL_BLEND);
		glBlendFunc(770, 771);
		glShadeModel(GL_SMOOTH);
		
		screen.draw(partialTicks);
	}
	
	public void handleKeyEvent(int key, char c, boolean pressed, boolean repeat) {
		if(skipInput) {
			return;
		}
		if(key == Keyboard.KEY_F11) {
			if(pressed) {
				mc.gameWindow.toggleFullscreen();
				return;
			}
		}
		if(ShaderMod.handleGlobalInput(InputDevice.keyboard)) {
			return;
		}
		try{
			screen.keyEvent(key, c, pressed, repeat, screen.cursorX, screen.cursorY);
		}catch (CancelEventException e) {}
	}
	
	public void handleMouseEvent(int button, boolean pressed) {
		if(skipInput) {
			return;
		}
		if(ShaderMod.handleGlobalInput(InputDevice.mouse)) {
			return;
		}
		if(button >= 0) {
			try{
				screen.mouseEvent(button, pressed, screen.cursorX, screen.cursorY);
			}catch (CancelEventException e) {}
		}
	}
	
	public void handleScrolling(int scrollAmount) {
		if(skipInput) {
			return;
		}
		if(scrollAmount != 0) {
			try{
				screen.scrollEvent(scrollAmount, screen.cursorX, screen.cursorY);
			}catch (CancelEventException e) {}
		}
	}
	
	@Override
	public void handleInput() {
	}
	
	@Override
	public void onClosed() {
		Keyboard.enableRepeatEvents(false);
		
		screen.onGuiClosed();
	}

	public void onOpened() {
		Keyboard.enableRepeatEvents(true);
		
		screen.onGuiOpened();
	}
	
}
