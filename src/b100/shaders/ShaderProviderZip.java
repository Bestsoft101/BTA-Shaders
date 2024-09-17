package b100.shaders;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import b100.utils.StringUtils;

public class ShaderProviderZip implements ShaderProvider {
	
	public File file;
	public ZipFile zipFile;
	
	public ShaderProviderZip(File file) {
		this.file = file;
	}
	
	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public String getFileContentAsString(String path) {
		try {
			ZipEntry entry = zipFile.getEntry(path);
			if(entry == null) {
				return null;
			}
			return StringUtils.readInputString(zipFile.getInputStream(entry));
		}catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public BufferedImage getImage(String path) {
		try {
			ZipEntry entry = zipFile.getEntry(path);
			if(entry == null) {
				return null;
			}
			return ImageIO.read(zipFile.getInputStream(entry));
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void open() {
		if(zipFile == null) {
			try {
				zipFile = new ZipFile(file);
			}catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void close() {
		if(zipFile != null) {
			try {
				zipFile.close();
			}catch (Exception e) {}
			zipFile = null;
		}
	}

	@Override
	public boolean equals(ShaderProvider shaderProvider) {
		if(shaderProvider instanceof ShaderProviderZip) {
			ShaderProviderZip spz = (ShaderProviderZip) shaderProvider;
			return spz.file.equals(file);
		}
		return false;
	}

}
