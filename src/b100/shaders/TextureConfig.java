package b100.shaders;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL33.*;

import java.util.HashMap;
import java.util.Map;

public class TextureConfig {
	
	public boolean enableMipmap = false;
	public int type = GL_RGBA;
	
	public static int getType(String string) {
		Integer id = textureTypes.get(string);
		if(id == null) {
			throw new NullPointerException("Unknown Type: '" + string + "'!");
		}
		return id;
	}
	
	private static Map<String, Integer> textureTypes = new HashMap<>();
	
	static {
		textureTypes.put("GL_RGBA", GL_RGBA);
		textureTypes.put("GL_R8", GL_R8);
		textureTypes.put("GL_R8_SNORM", GL_R8_SNORM);
		textureTypes.put("GL_R16", GL_R16);
		textureTypes.put("GL_R16_SNORM", GL_R16_SNORM);
		textureTypes.put("GL_RG8", GL_RG8);
		textureTypes.put("GL_RG8_SNORM", GL_RG8_SNORM);
		textureTypes.put("GL_RG16", GL_RG16);
		textureTypes.put("GL_RG16_SNORM", GL_RG16_SNORM);
		textureTypes.put("GL_R3_G3_B2", GL_R3_G3_B2);
		textureTypes.put("GL_RGB4", GL_RGB4);
		textureTypes.put("GL_RGB5", GL_RGB5);
		textureTypes.put("GL_RGB8", GL_RGB8);
		textureTypes.put("GL_RGB8_SNORM", GL_RGB8_SNORM);
		textureTypes.put("GL_RGB10", GL_RGB10);
		textureTypes.put("GL_RGB12", GL_RGB12);
		textureTypes.put("GL_RGB16_SNORM", GL_RGB16_SNORM);
		textureTypes.put("GL_RGBA2", GL_RGBA2);
		textureTypes.put("GL_RGBA4", GL_RGBA4);
		textureTypes.put("GL_RGB5_A1", GL_RGB5_A1);
		textureTypes.put("GL_RGBA8", GL_RGBA8);
		textureTypes.put("GL_RGBA8_SNORM", GL_RGBA8_SNORM);
		textureTypes.put("GL_RGB10_A2", GL_RGB10_A2);
		textureTypes.put("GL_RGB10_A2UI", GL_RGB10_A2UI);
		textureTypes.put("GL_RGBA12", GL_RGBA12);
		textureTypes.put("GL_RGBA16", GL_RGBA16);
		textureTypes.put("GL_SRGB8", GL_SRGB8);
		textureTypes.put("GL_SRGB8_ALPHA8", GL_SRGB8_ALPHA8);
		textureTypes.put("GL_R16F", GL_R16F);
		textureTypes.put("GL_RG16F", GL_RG16F);
		textureTypes.put("GL_RGB16F", GL_RGB16F);
		textureTypes.put("GL_RGBA16F", GL_RGBA16F);
		textureTypes.put("GL_R32F", GL_R32F);
		textureTypes.put("GL_RG32F", GL_RG32F);
		textureTypes.put("GL_RGB32F", GL_RGB32F);
		textureTypes.put("GL_RGBA32F", GL_RGBA32F);
		textureTypes.put("GL_R11F_G11F_B10F", GL_R11F_G11F_B10F);
		textureTypes.put("GL_RGB9_E5", GL_RGB9_E5);
		textureTypes.put("GL_R8I", GL_R8I);
		textureTypes.put("GL_R8UI", GL_R8UI);
		textureTypes.put("GL_R16I", GL_R16I);
		textureTypes.put("GL_R16UI", GL_R16UI);
		textureTypes.put("GL_R32I", GL_R32I);
		textureTypes.put("GL_R32UI", GL_R32UI);
		textureTypes.put("GL_RG8I", GL_RG8I);
		textureTypes.put("GL_RG8UI", GL_RG8UI);
		textureTypes.put("GL_RG16I", GL_RG16I);
		textureTypes.put("GL_RG16UI", GL_RG16UI);
		textureTypes.put("GL_RG32I", GL_RG32I);
		textureTypes.put("GL_RG32UI", GL_RG32UI);
		textureTypes.put("GL_RGB8I", GL_RGB8I);
		textureTypes.put("GL_RGB8UI", GL_RGB8UI);
		textureTypes.put("GL_RGB16I", GL_RGBA);
		textureTypes.put("GL_RGB16UI", GL_RGB16UI);
		textureTypes.put("GL_RGB32I", GL_RGB32I);
		textureTypes.put("GL_RGB32UI", GL_RGB32UI);
		textureTypes.put("GL_RGBA8I", GL_RGBA8I);
		textureTypes.put("GL_RGBA8UI", GL_RGBA8UI);
		textureTypes.put("GL_RGBA16I", GL_RGBA16I);
		textureTypes.put("GL_RGBA16UI", GL_RGBA16UI);
		textureTypes.put("GL_RGBA32I", GL_RGBA32I);
		textureTypes.put("GL_RGBA32UI", GL_RGBA32UI);
	}

}
