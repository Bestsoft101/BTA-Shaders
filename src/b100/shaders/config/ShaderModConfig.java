package b100.shaders.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import b100.shaders.ShaderMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.option.KeyBinding;

public class ShaderModConfig {
	
	public Minecraft mc;
	
	public File configFile;
	
	public CustomKeybind keyOpenShaderMenu = new CustomKeybind(new KeyBinding("key.openShaderMenu").bindKeyboard(Keyboard.KEY_O));
	public CustomKeybind keyReloadShaders = new CustomKeybind(new KeyBinding("key.reloadShaders").bindKeyboard(Keyboard.KEY_R));
	public CustomKeybind keyToggleShaders = new CustomKeybind(new KeyBinding("key.toggleShaders").bindKeyboard(Keyboard.KEY_K));
	public CustomKeybind keyShowTextures = new CustomKeybind(new KeyBinding("key.showShaderTextures").bindKeyboard(Keyboard.KEY_NONE));
	
	private Map<String, ConfigEntry> configEntries = new HashMap<>();
	
	public ShaderModConfig(Minecraft minecraft) {
		mc = minecraft;
		
		configFile = ShaderMod.getConfigFile();
		
		add(new ShaderPackOption());
		add(keyOpenShaderMenu);
		add(keyReloadShaders);
		add(keyToggleShaders);
		add(keyShowTextures);
		
		load();
	}
	
	public void add(ConfigEntry entry) {
		configEntries.put(entry.getId(), entry);
	}
	
	public void load() {
		ShaderMod.log("Loading config!");
		if(!configFile.exists()) {
			ShaderMod.log("Config file does not exist!");
			return;
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(configFile)));
			
			while(true) {
				String line = br.readLine();
				if(line == null) {
					break;
				}
				line = line.trim();
				if(line.length() == 0 || line.startsWith("#")) {
					continue;
				}
				int i = line.indexOf(':');
				if(i == -1) {
					continue;
				}
				String key = line.substring(0, i).trim();
				String value = line.substring(i + 1).trim();
				if(key.length() == 0 || value.length() == 0) {
					continue;
				}
				
				ConfigEntry entry = configEntries.get(key);
				if(entry == null) {
					continue;
				}
				
				try {
					entry.read(value);
				}catch (Exception e) {
					System.err.println("Could not read config line \"" + line + "\"!");
					e.printStackTrace();
					continue;
				}
			}
		}catch (Exception e) {
			throw new RuntimeException("Reading config", e);
		}finally {
			try {
				br.close();
			}catch (Exception e) {}
		}
		ShaderMod.log("Loaded config!");
	}
	
	public void save() {
		ShaderMod.log("Saving config!");
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile)));
			
			List<String> keys = new ArrayList<>(configEntries.keySet());
			keys.sort(String.CASE_INSENSITIVE_ORDER);
			
			for(int i=0; i < keys.size(); i++) {
				String key = keys.get(i);
				ConfigEntry entry = configEntries.get(key);
				String value = entry.getValue();
				if(value == null) {
					continue;
				}
				value = value.trim();
				if(value.length() == 0) {
					continue;
				}
				value = value.replace("\n", "");
				bw.write(key);
				bw.write(':');
				bw.write(value);
				bw.write('\n');
			}
		}catch (Exception e) {
			throw new RuntimeException("Saving config", e);
		}finally {
			try {
				bw.close();
			}catch (Exception e) {}
		}
		ShaderMod.log("Saved!");
	}

}
