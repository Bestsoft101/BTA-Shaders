package b100.shaders.asm;

import b100.shaders.CustomRenderer;
import b100.shaders.ShaderRenderer;
import b100.shaders.asm.utils.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Renderer;
import net.minecraft.client.render.WorldRenderer;

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
	
	public static void beforeSetupCameraTransform(WorldRenderer worldRenderer, float partialTicks, CallbackInfo ci) {
		if(worldRenderer.mc.render instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) worldRenderer.mc.render;
			if(customRenderer.beforeSetupCameraTransform(partialTicks)) {
				ci.setCancelled(true);
			}
		}
	}
	
	public static void afterSetupCameraTransform(WorldRenderer worldRenderer, float partialTicks, CallbackInfo ci) {
		if(worldRenderer.mc.render instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) worldRenderer.mc.render;
			customRenderer.afterSetupCameraTransform(partialTicks);
		}
	}

}
