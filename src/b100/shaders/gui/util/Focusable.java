package b100.shaders.gui.util;

public interface Focusable {
	
	public void setFocused(boolean focused);
	
	public boolean isFocused();
	
	public void addFocusListener(FocusListener focusListener);
	
	public void removeFocusListener(FocusListener focusListener);

}
