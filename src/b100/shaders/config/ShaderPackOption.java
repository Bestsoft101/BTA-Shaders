package b100.shaders.config;

import java.io.File;

import b100.shaders.ShaderMod;
import b100.shaders.ShaderProvider;

public class ShaderPackOption implements ConfigEntry {

	@Override
	public String getValue() {
		ShaderProvider shaderProvider = ShaderMod.getCurrentShaderPack();
		if(shaderProvider == null) {
			return "";
		}
		return shaderProvider.getName();
	}

	@Override
	public void read(String value) {
		File shaderPackFile = new File(ShaderMod.getShaderPackDirectory(), value);
		ShaderProvider shaderProvider = ShaderProvider.getShaderProvider(shaderPackFile);
		if(shaderPackFile != null) {
			ShaderMod.setShaderpack(shaderProvider);	
		}
	}

	@Override
	public String getId() {
		return "shaderpack";
	}

}
