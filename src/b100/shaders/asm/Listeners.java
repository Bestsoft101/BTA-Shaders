package b100.shaders.asm;

import static org.lwjgl.opengl.GL11.*;

import b100.natrium.ChunkRendererMultiDraw;
import b100.natrium.CustomTessellator;
import b100.shaders.CustomRenderer;
import b100.shaders.ShaderRenderer;
import b100.shaders.asm.utils.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.Renderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.camera.EntityCamera;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;

public class Listeners {
	
	public static final Minecraft mc = Minecraft.getMinecraft(Minecraft.class);
	
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
	
	public static void beginRenderBasic() {
		if(mc.render instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.render;
			customRenderer.beginRenderBasic();
		}
	}
	
	public static void beginRenderTextured() {
		if(mc.render instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.render;
			customRenderer.beginRenderTextured();
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
	
	public static void beginRenderTranslucent() {
		if(mc.render instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.render;
			customRenderer.beginRenderTranslucent();
		}
	}
	
	public static void beginRenderClouds() {
		if(mc.render instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.render;
			customRenderer.beginRenderClouds();
		}
	}
	
	public static void beginRenderEntities() {
		if(mc.render instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.render;
			customRenderer.beginRenderEntities();
		}
	}
	
	public static void beginRenderHand() {
		if(mc.render instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.render;
			customRenderer.beginRenderHand();
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
	public static boolean beforeRenderShadow(EntityRenderer<?> entityRenderer, Entity entity, double posX, double posY, double posZ, float opacity, float partialTicks) {
		if(mc.render instanceof ShaderRenderer) {
			ShaderRenderer shadersRenderer = (ShaderRenderer) mc.render;
			if(shadersRenderer.enableShadowmap) {
				return true;
			}
		}
		return false;
	}
	
	public static void onChunkRenderStart(ChunkRendererMultiDraw chunkRendererMultiDraw, CustomTessellator customTessellator) {
		if(mc.render instanceof ShaderRenderer) {
			ShaderRenderer shaderRenderer = (ShaderRenderer) mc.render;
			customTessellator.addVertexAttrib(shaderRenderer.attributeID);
			customTessellator.addVertexAttrib(shaderRenderer.attributeTopVertex);
			shaderRenderer.attributeID.value = 0.0f;
			shaderRenderer.attributeTopVertex.value = 0.0f;
		}
	}
	
	public static void setBlockID(Block block) {
		if(mc.render instanceof ShaderRenderer) {
			ShaderRenderer shaderRenderer = (ShaderRenderer) mc.render;
			shaderRenderer.attributeID.value = block.id;
		}
	}
	
	public static void setIsTopVertex(float topVertex) {
		if(mc.render instanceof ShaderRenderer) {
			ShaderRenderer shaderRenderer = (ShaderRenderer) mc.render;
			shaderRenderer.attributeTopVertex.value = topVertex;
		}
	}

}
