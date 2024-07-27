package b100.shaders;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.minecraft.core.util.helper.Buffer;

public abstract class TextureHelper {
	
	public static BufferedImage getTextureIfExists(String path) {
		InputStream in = ShaderMod.mc.texturePackList.getResourceAsStream(path);
		if(in == null) {
			return null;
		}
		try {
			return ImageIO.read(in);
		}catch (Exception e) {
			throw new RuntimeException("Reading texture: '" + path + "'");
		}finally {
			try {
				in.close();
			}catch (Exception e) {}
		}
	}
	
	public static void drawImage(BufferedImage from, BufferedImage to, int toX, int toY) {
		drawImage(from, 0, 0, from.getWidth(), from.getHeight(), to, toX, toY);
	}
	
	public static void drawImage(BufferedImage from, int fromX, int fromY, int w, int h, BufferedImage to, int toX, int toY) {
		for(int i=0; i < w; i++) {
			for(int j=0; j < h; j++) {
				int color = from.getRGB(fromX + i, fromY + j);
				to.setRGB(toX + i, toY + j, color);
			}
		}
	}
	
	public static void fillColor(BufferedImage image, int color) {
		for(int i=0; i < image.getWidth(); i++) {
			for(int j=0; j < image.getHeight(); j++) {
				image.setRGB(i, j, color);
			}	
		}
	}
	
	public static void setTextureImage(int tex, BufferedImage image) {
		if(tex == 0) {
			throw new NullPointerException("No texture!");
		}
		
		Buffer.put(image);
		
		glBindTexture(GL_TEXTURE_2D, tex);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, Buffer.buffer);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	}

}
