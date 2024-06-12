package b100.shaders.config;

import java.io.File;

import b100.shaders.ShaderMod;

public class ShaderPackOption implements ConfigEntry {

	@Override
	public String getValue() {
		File shaderpack = ShaderMod.getCurrentShaderPackFile();
		if(shaderpack == null) {
			return "";
		}
		return shaderpack.getName();
	}

	@Override
	public void read(String value) {
		ShaderMod.setShaderpack(new File(ShaderMod.getShaderPackDirectory(), value));
	}

	@Override
	public String getId() {
		return "shaderpack";
	}

}
