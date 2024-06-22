package b100.shaders;

import java.io.File;

import b100.shaders.config.ShaderModConfig;
import b100.shaders.gui.GuiShaderMenu;
import b100.shaders.gui.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.input.InputDevice;

public class ShaderMod {
	
	public static Minecraft mc;
	
	public static ShaderModConfig config;
	
	private static File minecraftDirectory;
	private static File shaderDirectory;
	private static File shaderPackDirectory;
	private static File configFile;
	
	public static OptionsPage optionsPage;
	
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
		config.load();
	}
	
	public static File getShaderDirectory() {
		return shaderDirectory;
	}
	
	public static File getCurrentShaderPackFile() {
		return shaderDirectory;
	}
	
	public static File getShaderPackDirectory() {
		return shaderPackDirectory;
	}
	
	public static File getConfigFile() {
		return configFile;
	}
	
	public static boolean isShaderPack(File file) {
		if(file.isDirectory()) {
			File shaderJson = new File(file, "shader.json");
			return shaderJson.exists();
		}
//		if(file.isFile() && file.getName().toLowerCase().endsWith(".zip")) {
//			ZipFile zipFile = null;
//			try {
//				zipFile = new ZipFile(file);
//				
//				return zipFile.getEntry("/shader.json") != null;
//			}catch (Exception e) {
//				e.printStackTrace();
//			}finally {
//				try {
//					zipFile.close();
//				}catch (Exception e) {}
//			}
//		}
		return false;
	}
	
	public static boolean isShaderPackSelected(File file) {
		if(file == null && shaderDirectory == null) return true;
		if(file == null || shaderDirectory == null) return false;
		return file.equals(shaderDirectory);
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
	
	public static void setShaderpack(File file) {
		log("Set Shaderpack: " + (file != null ? file.getName() : "(None)"));
		shaderDirectory = file;
		
		if(mc.renderer != null) {
			ShaderRenderer shaderRenderer = (ShaderRenderer) mc.renderer;
			shaderRenderer.shaderPackChanged = true;
		}
	}
	
	public static boolean handleGlobalInput(InputDevice device) {
		if(config.keyReloadShaders.keyBinding.isPressEvent(device)) {
			ShaderRenderer shaderRenderer = (ShaderRenderer) mc.renderer;
			shaderRenderer.shaderPackChanged = true;
			logAndDisplay("Reloading shaders...");
			return true;
		}
		if(config.keyToggleShaders.keyBinding.isPressEvent(device)) {
			ShaderRenderer shaderRenderer = (ShaderRenderer) mc.renderer;
			shaderRenderer.enableShaders = !shaderRenderer.enableShaders;
			logAndDisplay("Shaders: " + (shaderRenderer.enableShaders ? "On" : "Off"));
			return true;
		}
		if(config.keyShowTextures.keyBinding.isPressEvent(device)) {
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

}
