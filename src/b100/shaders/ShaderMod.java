package b100.shaders;

import java.io.File;

import org.lwjgl.input.Keyboard;

import b100.shaders.config.ShaderModConfig;
import b100.shaders.gui.GuiShaderMenu;
import b100.shaders.gui.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.input.InputDevice;
import net.minecraft.client.option.KeyBinding;

public class ShaderMod {
	
	public static Minecraft mc;
	
	public static ShaderModConfig config;
	
	private static File minecraftDirectory;
	private static File shaderPackDirectory;
	private static File configFile;
	
	public static OptionsPage optionsPage;
	
	public static int normalTexture;
	public static int specularTexture;
	
	public static boolean enableNormals = false;
	public static boolean enableSpecular = false;
	
	private static ShaderProvider currentShaderPack;
	
	static {
		grabInstanceAndDirectory();
	}
	
	private static void grabInstanceAndDirectory() {
		mc = Minecraft.getMinecraft(Minecraft.class);
		
		minecraftDirectory = mc.getMinecraftDir();
		
		shaderPackDirectory = new File(minecraftDirectory, "shaderpacks");
		shaderPackDirectory.mkdirs();

		File configDirectory = new File(minecraftDirectory, "config");
		configDirectory.mkdirs();

		configFile = new File(configDirectory, "shaders.cfg");
		
		config = new ShaderModConfig(mc);
	}
	
	public static ShaderProvider getCurrentShaderPack() {
		return currentShaderPack;
	}
	
	public static File getShaderPackDirectory() {
		return shaderPackDirectory;
	}
	
	public static File getConfigFile() {
		return configFile;
	}
	
	public static boolean isShaderPackSelected(ShaderProvider shaderProvider) {
		if(shaderProvider == null && currentShaderPack == null) return true;
		if(shaderProvider == null || currentShaderPack == null) return false;
		return shaderProvider.equals(currentShaderPack);
	}
	
	public static void log(String string) {
		System.out.print("[ShaderMod] " + string + "\n");
	}
	
	public static void logAndDisplay(String string) {
		log(string);
		if(mc.ingameGUI != null && mc.ingameGUI.guiHeldItemTooltip != null) {
			mc.ingameGUI.guiHeldItemTooltip.setString(string);	
		}
	}
	
	public static void setShaderpack(ShaderProvider shaderProvider) {
		Thread.dumpStack();
		
		if(currentShaderPack != null) {
			currentShaderPack.close();
		}
		
		log("Set Shaderpack: " + (shaderProvider != null ? shaderProvider.getName() : "(None)"));
		currentShaderPack = shaderProvider;
		
		if(mc.renderer != null) {
			ShaderRenderer shaderRenderer = (ShaderRenderer) mc.renderer;
			shaderRenderer.shaderPackChanged = true;
		}
	}
	
	public static boolean handleGlobalInput(InputDevice device) {
		if(isGlobalPressEvent(config.keyReloadShaders.keyBinding, device)) {
			ShaderRenderer shaderRenderer = (ShaderRenderer) mc.renderer;
			shaderRenderer.shaderPackChanged = true;
			logAndDisplay("Reloading shaders...");
			return true;
		}
		if(isGlobalPressEvent(config.keyToggleShaders.keyBinding, device)) {
			ShaderRenderer shaderRenderer = (ShaderRenderer) mc.renderer;
			shaderRenderer.enableShaders = !shaderRenderer.enableShaders;
			logAndDisplay("Shaders: " + (shaderRenderer.enableShaders ? "On" : "Off"));
			return true;
		}
		if(isGlobalPressEvent(config.keyShowTextures.keyBinding, device)) {
			ShaderRenderer shaderRenderer = (ShaderRenderer) mc.renderer;
			if(shaderRenderer.enableShaders) {
				shaderRenderer.showTextures = !shaderRenderer.showTextures;
				return true;
			}
		}
		return false;
	}
	
	public static boolean handleIngameInput(InputDevice device) {
		if(handleGlobalInput(device)) {
			return true;
		}
		if(config.keyOpenShaderMenu.keyBinding.isPressEvent(device)) {
			GuiUtils.instance.displayGui(new GuiShaderMenu(null));
			return true;
		}
		return false;
	}
	
	public static boolean isGlobalPressEvent(KeyBinding keyBinding, InputDevice device) {
		boolean allowGlobal = false;
		if(keyBinding.getInputDevice() == InputDevice.keyboard) {
			int keyCode = keyBinding.getKeyCode();
			if(keyCode < Keyboard.KEY_F1 || keyCode > Keyboard.KEY_F19) {
				allowGlobal = true;
			}
		}
		if(mc.currentScreen != null && !allowGlobal) {
			return false;
		}
		return keyBinding.isPressEvent(device);
	}

}
