package b100.shaders.gui;

import java.util.ArrayList;
import java.util.List;

public class GuiContainer extends GuiElement {
	
	public List<GuiElement> elements = new ArrayList<>();
	
	public GuiContainer() {
		
	}
	
	public <E extends GuiElement> E add(E element) {
		elements.add(element);
		element.onAddToContainer(this);
		return element;
	}
	
	@Override
	public void draw(float partialTicks) {
		for(int i=0; i < elements.size(); i++) {
			elements.get(i).draw(partialTicks);
		}
	}
	
	@Override
	public void keyEvent(int key, char c, boolean pressed, boolean repeat, int mouseX, int mouseY) {
		for(int i=0; i < elements.size(); i++) {
			elements.get(i).keyEvent(key, c, pressed, repeat, mouseX, mouseY);
		}
	}
	
	@Override
	public void mouseEvent(int button, boolean pressed, int mouseX, int mouseY) {
		for(int i=0; i < elements.size(); i++) {
			elements.get(i).mouseEvent(button, pressed, mouseX, mouseY);
		}
	}
	
	@Override
	public void scrollEvent(int dir, int mouseX, int mouseY) {
		for(int i=0; i < elements.size(); i++) {
			elements.get(i).scrollEvent(dir, mouseX, mouseY);
		}
	}

	@Override
	public void onResize() {
		for(int i=0; i < elements.size(); i++) {
			elements.get(i).onResize();
		}
	}
	
	public GuiElement getClickElementAt(int x, int y) {
		for(int i = elements.size() - 1; i >= 0; i--) {
			GuiElement element = elements.get(i);
			if(element instanceof GuiContainer) {
				GuiContainer container = (GuiContainer) element;
				GuiElement element1 = container.getClickElementAt(x, y);
				if(element1 != null) {
					return element1;
				}
			}
			if(element.isSolid() && element.isInside(x, y)) {
				return element;
			}
		}
		return null;
	}
	
	public boolean containsElement(GuiElement element) {
		for(int i=0; i < elements.size(); i++) {
			GuiElement e = elements.get(i);
			if(e == element) {
				return true;
			}
			if(e instanceof GuiContainer) {
				GuiContainer container = (GuiContainer) e;
				if(container.containsElement(element)) {
					return true;
				}
			}
		}
		return false;
	}
	
}
