package b100.shaders.gui;

import b100.shaders.ShaderMod;
import net.minecraft.client.Minecraft;

public abstract class Gui {

	public static final String BACKGROUND_TEX = "/assets/minecraft/textures/gui/background.png";
	public static final String GUI_TEX = "/assets/minecraft/textures/gui/gui.png";

	public static final int V_BUTTON_DISABLED = 46;
	public static final int V_BUTTON_NORMAL = 66;
	public static final int V_BUTTON_HOVER = 86;
	
	public GuiUtils utils = GuiUtils.instance;
	public Minecraft mc = ShaderMod.mc;
	
}
