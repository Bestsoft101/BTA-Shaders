package b100.shaders;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import b100.utils.ReflectUtils;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.lang.Language;

public class LanguageHelper {
	
	public static void onReloadLanguages(I18n stringTranslate, String languageName) {
		Language currentLanguage = (Language) ReflectUtils.getValue(ReflectUtils.getField(I18n.class, "currentLanguage"), stringTranslate);
		Language defaultLanguage = Language.Default.INSTANCE;
		
		if(currentLanguage != defaultLanguage) {
			loadLanguage(defaultLanguage);
		}
		
		loadLanguage(currentLanguage);
	}
	private static void loadLanguage(Language language) {
		Properties properties = (Properties) ReflectUtils.getValue(ReflectUtils.getField(Language.class, "entries"), language);
		
		int oldSize = properties.size();

		String path = "/" + language.getId() + ".lang";
		URL url = ShaderMod.class.getResource(path);
		InputStream stream = null;
		try {
			stream = url.openStream();
			if(stream == null) {
				ShaderMod.log("Missing language file: '" + path + "'!");
				return;
			}
			properties.load(stream);	
		}catch (Exception e) {
			throw new RuntimeException("Error loading language file '" + path + "'", e);
		}finally {
			try {
				stream.close();
			}catch (Exception e) {}
		}
		
		int newSize = properties.size();
		int loaded = newSize - oldSize;
		ShaderMod.log("Loaded " + loaded + " Language Keys");
	}
}
