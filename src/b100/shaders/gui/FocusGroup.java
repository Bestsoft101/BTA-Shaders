package b100.shaders.gui;

import java.util.ArrayList;
import java.util.List;

import b100.shaders.gui.util.FocusListener;
import b100.shaders.gui.util.Focusable;

public class FocusGroup implements FocusListener {

	private List<Focusable> elements = new ArrayList<>();
	
	public void add(Focusable focusable) {
		elements.add(focusable);
		focusable.addFocusListener(this);
	}
	
	public void remove(Focusable focusable) {
		elements.remove(focusable);
		focusable.removeFocusListener(this);
	}
	
	public void clear() {
		for(int i=0; i < elements.size(); i++) {
			elements.get(i).removeFocusListener(this);
		}
		elements.clear();
	}
	
	@Override
	public void focusChanged(Focusable element) {
		if(element.isFocused()) {
			for(int i=0; i < elements.size(); i++) {
				Focusable e = elements.get(i);
				if(e != element) {
					e.setFocused(false);
				}
			}
		}
	}

}
