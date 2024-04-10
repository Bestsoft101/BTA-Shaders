package b100.shaders.asm;

import static org.lwjgl.opengl.GL11.*;

import b100.shaders.CustomRenderer;
import b100.shaders.ShaderRenderer;
import b100.shaders.asm.utils.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.Renderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.camera.EntityCamera;
import net.minecraft.client.render.camera.ICamera;

public class Listeners {
	
	public static Minecraft mc;
	
	public static void onSetRenderer(Minecraft minecraft, Renderer renderer, CallbackInfo ci) {
		mc = minecraft;
		
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
		if(mc.render instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.render;
			if(customRenderer.beforeSetupCameraTransform(partialTicks)) {
				ci.setCancelled(true);
			}
		}
	}
	
	public static void afterSetupCameraTransform(WorldRenderer worldRenderer, float partialTicks, CallbackInfo ci) {
		if(mc.render instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.render;
			customRenderer.afterSetupCameraTransform(partialTicks);
		}
	}
	
	public static void showPlayerOverride(EntityCamera entityCamera, CallbackInfo ci) {
		if(mc.render instanceof ShaderRenderer) {
			ShaderRenderer shadersRenderer = (ShaderRenderer) mc.render;
			if(shadersRenderer.isRenderingShadowmap) {
				ci.setCancelled(true);
				ci.setReturnValue(true);
			}
		}
	}
	
	public static void updateRenderersCancel(RenderGlobal renderGlobal, ICamera camera, CallbackInfo ci) {
		if(mc.render instanceof ShaderRenderer) {
			ShaderRenderer shadersRenderer = (ShaderRenderer) mc.render;
			if(shadersRenderer.isRenderingShadowmap) {
				ci.setCancelled(true);
				ci.setReturnValue(true);
			}
		}
	}
	
	public static void renderRainSnowCancel(WorldRenderer worldRenderer, float partialTicks, CallbackInfo ci) {
		if(mc.render instanceof ShaderRenderer) {
			ShaderRenderer shadersRenderer = (ShaderRenderer) mc.render;
			if(shadersRenderer.isRenderingShadowmap) {
				ci.setCancelled(true);
				ci.setReturnValue(true);
			}
		}
	}
	
	public static void onClearWorldBuffer() {
		if(mc.render instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.render;
			customRenderer.onClearWorldBuffer();
		}
	}
	
	public static void beginRenderTerrain() {
		if(mc.render instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.render;
			customRenderer.beginRenderTerrain();
		}
	}
	
	public static void beginRenderSkyBasic() {
		if(mc.render instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.render;
			customRenderer.beginRenderSkyBasic();
		}
	}
	
	public static void beginRenderSkyTextured() {
		if(mc.render instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.render;
			customRenderer.beginRenderSkyTextured();
		}
	}
	
	public static void setFogMode(int pname, int param) {
		glFogi(pname, param);
		if(pname == GL_FOG_MODE) {
			if(mc.render instanceof ShaderRenderer) {
				ShaderRenderer shadersRenderer = (ShaderRenderer) mc.render;
				shadersRenderer.fogMode = param;
			}
		}
	}

}
