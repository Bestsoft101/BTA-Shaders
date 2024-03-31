package b100.shaders.asm;

import b100.shaders.ShaderRenderer;
import b100.shaders.asm.utils.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Renderer;

public class Listener {
	
	public static void onSetRenderer(Minecraft minecraft, Renderer renderer, CallbackInfo ci) {
		ci.setCancelled(true);
		
		if(minecraft.render instanceof ShaderRenderer) {
			return;
		}
		
		if(minecraft.render != null) {
			minecraft.render.delete();
		}
		minecraft.render = new ShaderRenderer(minecraft);
	}

}
