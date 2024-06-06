package b100.shaders;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.core.Global;

public class ShaderMod {
	
	private static File minecraftDirectory;
	private static File shaderDirectory;
	private static File shaderPackDirectory;
	
	public static OptionsPage optionsPage;
	
	public static final Minecraft mc = Minecraft.getMinecraft(Minecraft.class);
	
	static {
		grabInstanceAndDirectory();
	}
	
	private static void grabInstanceAndDirectory() {
		minecraftDirectory = Global.accessor.getMinecraftDir();
		
		shaderPackDirectory = new File(minecraftDirectory, "shaderpacks");
		shaderPackDirectory.mkdirs();
	}
	
	public static File getShaderDirectory() {
		return shaderDirectory;
	}
	
	public static File getCurrentShaderPackDirectory() {
		return shaderDirectory;
	}
	
	public static File getShaderPackDirectory() {
		return shaderPackDirectory;
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
	
	public static void setShaderpack(File file) {
		shaderDirectory = file;
		
		ShaderRenderer shaderRenderer = (ShaderRenderer) mc.renderer;
		shaderRenderer.shaderPackChanged = true;
	}

}
