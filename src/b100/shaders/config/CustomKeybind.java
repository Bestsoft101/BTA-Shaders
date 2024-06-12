package b100.shaders.config;

import net.minecraft.client.option.KeyBinding;

public class CustomKeybind implements ConfigEntry {

	public final KeyBinding keyBinding;
	
	public CustomKeybind(KeyBinding keyBinding) {
		this.keyBinding = keyBinding;
	}
	
	@Override
	public String getValue() {
		return keyBinding.toOptionsString();
	}

	@Override
	public void read(String value) {
		keyBinding.fromOptionsString(value);
	}

	@Override
	public String getId() {
		return keyBinding.getId();
	}

}
