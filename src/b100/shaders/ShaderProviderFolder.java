package b100.shaders;

import java.awt.image.BufferedImage;
import java.io.File;

import b100.utils.ImageUtils;
import b100.utils.StringUtils;

public class ShaderProviderFolder implements ShaderProvider {

	public File directory;
	
	public ShaderProviderFolder(File directory) {
		if(!directory.isDirectory()) {
			throw new RuntimeException("Not a directory: '" + directory + "'!");
		}
		this.directory = directory;
	}
	
	@Override
	public String getName() {
		return directory.getName();
	}

	@Override
	public String getFileContentAsString(String path) {
		File file = new File(directory, path);
		if(!file.exists()) {
			return null;
		}
		return StringUtils.getFileContentAsString(file);
	}

	@Override
	public BufferedImage getImage(String path) {
		return ImageUtils.loadExternalImage(new File(directory, path));
	}

	@Override
	public void open() {
		
	}

	@Override
	public void close() {
		
	}

	@Override
	public boolean equals(ShaderProvider shaderProvider) {
		if(shaderProvider instanceof ShaderProviderFolder) {
			ShaderProviderFolder spf = (ShaderProviderFolder) shaderProvider;
			return spf.directory.equals(directory);
		}
		return false;
	}

}
