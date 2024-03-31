package b100.shaders;

import java.io.File;

import net.minecraft.core.Global;

public class ShaderMod {
	
	private static File minecraftDirectory;
	private static File shaderDirectory;
	
	static {
		grabInstanceAndDirectory();
	}
	
	private static void grabInstanceAndDirectory() {
		minecraftDirectory = Global.accessor.getMinecraftDir();
		
		shaderDirectory = new File(minecraftDirectory, "shaders");
		shaderDirectory.mkdirs();
	}
	
	public static File getShaderDirectory() {
		return shaderDirectory;
	}
	
	public static File getCurrentShaderPackDirectory() {
		return shaderDirectory;
	}
	
	public static void log(String string) {
		System.out.print("[ShaderMod] " + string + "\n");
	}

}
