package b100.shaders;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.zip.ZipFile;

public interface ShaderProvider {
	
	public String getName();
	
	public String getFileContentAsString(String path);
	
	public BufferedImage getImage(String path);
	
	public void open();
	
	public void close();
	
	public boolean equals(ShaderProvider shaderProvider);
	
	public static ShaderProvider getShaderProvider(File file) {
		if(file.isDirectory() && new File(file, "shader.json").exists()) {
			return new ShaderProviderFolder(file);
		}
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
			zipFile.close();
			return new ShaderProviderZip(file);
		}catch (Exception e) {}
		return null;
	}

}
