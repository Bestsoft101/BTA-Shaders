package b100.shaders;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import java.util.HashMap;
import java.util.Map;

import b100.json.element.JsonElement;
import b100.json.element.JsonObject;

public class TextureConfig {
	
	public boolean enableMipmap = false;
	public int internalformat = GL_RGBA;
	public int clearType = 0;
	public float clearR;
	public float clearG;
	public float clearB;
	public float clearA;
	
	public void setup(JsonObject obj) {
		if(obj.has("mipmap")) {
			enableMipmap = obj.getBoolean("mipmap");
		}
		
		JsonElement format = obj.get("format");
		if(format != null) {
			if(format.isNumber()) {
				this.internalformat = format.getAsNumber().getInteger();
			}else {
				this.internalformat = getGlValue(format.getAsString().value);
			}
		}
		
		JsonElement clear = obj.get("clear");
		if(clear != null) {
			String clearStr = clear.getAsString().value;
			if(clearStr.equals("fog")) {
				clearType = 1;
			}else if(clearStr.equals("none")) {
				clearType = -1;
			}else if(clearStr.startsWith("#")) {
				clearType = 2;
				int color = parseHexColor(clearStr);
				System.out.println(clearStr + " : " + colorToString(color));
				setClearColor(color);
				System.out.println("R: " + clearR + " G: " + clearG + " B: " + clearB + " A: " + clearA);
			}else {
				throw new RuntimeException("Unknown clear mode: '" + clearStr + "'!");
			}
		}
	}
	
	public void setClearColor(int rgba) {
		int r = (rgba >> 24) & 0xFF;
		int g = (rgba >> 16) & 0xFF;
		int b = (rgba >> 8) & 0xFF;
		int a = (rgba >> 0) & 0xFF;
		
		this.clearA = a / 255.0f;
		this.clearR = r / 255.0f;
		this.clearG = g / 255.0f;
		this.clearB = b / 255.0f;
	}
	
	public static int getGlValue(String name) {
		Integer value = constants.get(name);
		if(value == null) {
			value = constants.get("GL_" + name);
			if(value == null) {
				throw new RuntimeException("Unknown format: '" + name + "'!");
			}
		}
		return value;
	}
	
	public static int parseHexColor(String string) {
		if(string.startsWith("#")) {
			string = string.substring(1);
		}
		int val = 0;
		
		for(int i=0; i < string.length(); i++) {
			char c = string.charAt(i);
			
			int charVal;
			if(c >= '0' && c <= '9') {
				charVal = c - '0';
			}else if(c >= 'A' && c <= 'F') {
				charVal = (c - 'A') + 10;
			}else if(c >= 'a' && c <= 'f') {
				charVal = (c - 'a') + 10;
			}else {
				throw new RuntimeException("Invalid character '" + c + "' at index " + i);
			}
			
			val <<= 4;
			val += charVal;
		}
		
		return val;
	}
	
	public static String colorToString(int color) {
		int r = (color >> 24) & 0xFF;
		int g = (color >> 16) & 0xFF;
		int b = (color >>  8) & 0xFF;
		int a = (color >>  0) & 0xFF;
		
		return "R: " + r + " G: " + g + " B: " + b + " A: " + a;
	}
	
	public static void main(String[] args) {
		System.out.println("#" + Integer.toHexString(parseHexColor("#FF0000")).toUpperCase());
		System.out.println("#" + Integer.toHexString(parseHexColor("#00FF00")).toUpperCase());
		System.out.println("#" + Integer.toHexString(parseHexColor("#0000FF")).toUpperCase());
		System.out.println("#" + Integer.toHexString(parseHexColor("#abcdef")).toUpperCase());
	}
	
	private static final Map<String, Integer> constants = new HashMap<>();
	
	static {
		constants.put("GL_RGB", GL_RGB);
		constants.put("GL_RGBA", GL_RGBA);
		constants.put("GL_RGB16F", GL_RGB16F);
		constants.put("GL_RGB32F", GL_RGB32F);
		constants.put("GL_RGBA16F", GL_RGBA16F);
		constants.put("GL_RGBA32F", GL_RGBA32F);
	}

}
