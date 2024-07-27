package b100.shaders;

import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.GLAllocation;
import net.minecraft.core.util.helper.Buffer;

public class TextureCache {
	
	private final Map<String, Integer> textureMap = new HashMap<>();
	
	public int getTexture(String name) {
		Integer id = textureMap.get(name);
		if(id == null) {
			return 0;
		}
		return id;
	}
	
	public void setupTexture(String name, BufferedImage image) {
		Integer id = textureMap.get(name);
		if(id == null) {
			id = GLAllocation.generateTexture();
			textureMap.put(name, id);
		}
		
		glBindTexture(GL11.GL_TEXTURE_2D, id);
		
		Buffer.put(image);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, Buffer.buffer);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	}
	
	public void deleteTexture(String name) {
		Integer id = textureMap.get(name);
		if(id != null) {
			GLAllocation.deleteTexture(id);
			textureMap.remove(name);
		}
	}
	
	public void deleteAll() {
		for(String key : textureMap.keySet()) {
			GLAllocation.deleteTexture(textureMap.get(key));
		}
		textureMap.clear();
	}

}
