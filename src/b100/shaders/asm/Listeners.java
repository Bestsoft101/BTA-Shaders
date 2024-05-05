package b100.shaders.asm;

import static org.lwjgl.opengl.GL11.*;

import b100.natrium.CustomTessellator;
import b100.shaders.CustomRenderer;
import b100.shaders.ShaderRenderer;
import b100.shaders.asm.utils.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.ChunkRenderer;
import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.Renderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.camera.EntityCamera;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.client.render.culling.CameraFrustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;

public class Listeners {
	
	public static final Minecraft mc = Minecraft.getMinecraft(Minecraft.class);
	
	public static void onSetRenderer(Minecraft minecraft, Renderer renderer, CallbackInfo ci) {
		ci.setCancelled(true);
		
		if(minecraft.renderer instanceof ShaderRenderer) {
			return;
		}
		
		if(minecraft.renderer != null) {
			minecraft.renderer.delete();
		}
		minecraft.renderer = new ShaderRenderer(minecraft);
	}
	
	public static void beforeSetupCameraTransform(WorldRenderer worldRenderer, float partialTicks, CallbackInfo ci) {
		if(mc.renderer instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.renderer;
			if(customRenderer.beforeSetupCameraTransform(partialTicks)) {
				ci.setCancelled(true);
			}
		}
	}
	
	public static void afterSetupCameraTransform(WorldRenderer worldRenderer, float partialTicks, CallbackInfo ci) {
		if(mc.renderer instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.renderer;
			customRenderer.afterSetupCameraTransform(partialTicks);
		}
	}
	
	public static void showPlayerOverride(EntityCamera entityCamera, CallbackInfo ci) {
		if(mc.renderer instanceof ShaderRenderer) {
			ShaderRenderer shadersRenderer = (ShaderRenderer) mc.renderer;
			if(shadersRenderer.isRenderingShadowmap) {
				ci.setCancelled(true);
				ci.setReturnValue(true);
			}
		}
	}
	
	public static void updateRenderersCancel(RenderGlobal renderGlobal, ICamera camera, CallbackInfo ci) {
		if(mc.renderer instanceof ShaderRenderer) {
			ShaderRenderer shadersRenderer = (ShaderRenderer) mc.renderer;
			if(shadersRenderer.isRenderingShadowmap) {
				ci.setCancelled(true);
				ci.setReturnValue(true);
			}
		}
	}
	
	public static void renderRainSnowCancel(WorldRenderer worldRenderer, float partialTicks, CallbackInfo ci) {
		if(mc.renderer instanceof ShaderRenderer) {
			ShaderRenderer shadersRenderer = (ShaderRenderer) mc.renderer;
			if(shadersRenderer.isRenderingShadowmap) {
				ci.setCancelled(true);
				ci.setReturnValue(true);
				return;
			}
		}
		if(mc.renderer instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.renderer;
			customRenderer.beginRenderWeather();
		}
	}
	
	public static void setSunPathRotation() {
		if(mc.renderer instanceof ShaderRenderer) {
			ShaderRenderer shadersRenderer = (ShaderRenderer) mc.renderer;
			shadersRenderer.setSunPathRotation();
		}
	}
	
	public static void onClearWorldBuffer() {
		if(mc.renderer instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.renderer;
			customRenderer.onClearWorldBuffer();
		}
	}
	
	public static void beginRenderBasic() {
		if(mc.renderer instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.renderer;
			customRenderer.beginRenderBasic();
		}
	}
	
	public static void beginRenderTextured() {
		if(mc.renderer instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.renderer;
			customRenderer.beginRenderTextured();
		}
	}
	
	public static void beginRenderTerrain() {
		if(mc.renderer instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.renderer;
			customRenderer.beginRenderTerrain();
		}
	}
	
	public static void beginRenderSkyBasic() {
		if(mc.renderer instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.renderer;
			customRenderer.beginRenderSkyBasic();
		}
	}
	
	public static void beginRenderSkyTextured() {
		if(mc.renderer instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.renderer;
			customRenderer.beginRenderSkyTextured();
		}
	}
	
	public static void beginRenderTranslucent() {
		if(mc.renderer instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.renderer;
			customRenderer.beginRenderTranslucent();
		}
	}
	
	public static void beginRenderClouds() {
		if(mc.renderer instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.renderer;
			customRenderer.beginRenderClouds();
		}
	}
	
	public static void beginRenderEntities() {
		if(mc.renderer instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.renderer;
			customRenderer.beginRenderEntities();
		}
	}
	
	public static void beginRenderHand() {
		if(mc.renderer instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.renderer;
			customRenderer.beginRenderHand();
		}
	}
	
	public static boolean beginRenderAurora(RenderGlobal renderGlobal, ICamera camera, float partialTicks) {
		if(mc.renderer instanceof ShaderRenderer) {
			ShaderRenderer shadersRenderer = (ShaderRenderer) mc.renderer;
			if(shadersRenderer.isRenderingShadowmap) {
				return true;
			}
		}
		beginRenderSkyTextured();
		return false;
	}
	
	public static void setFogMode(int pname, int param) {
		glFogi(pname, param);
		if(pname == GL_FOG_MODE) {
			if(mc.renderer instanceof ShaderRenderer) {
				ShaderRenderer shadersRenderer = (ShaderRenderer) mc.renderer;
				shadersRenderer.fogMode = param;
			}
		}
	}
	
	public static void updateCelestialPosition() {
		if(mc.renderer instanceof ShaderRenderer) {
			ShaderRenderer shaderRenderer = (ShaderRenderer) mc.renderer;
			shaderRenderer.uniforms.updateCelestialPosition();
		}
	}
	
	public static boolean beforeRenderShadow(EntityRenderer<?> entityRenderer, Tessellator tessellator, Entity entity, double posX, double posY, double posZ, float opacity, float partialTicks) {
		if(mc.renderer instanceof ShaderRenderer) {
			ShaderRenderer shadersRenderer = (ShaderRenderer) mc.renderer;
			if(shadersRenderer.enableShadowmap) {
				return true;
			}
		}
		return false;
	}
	
	public static void onChunkRenderStart(CustomTessellator customTessellator) {
		if(mc.renderer instanceof ShaderRenderer) {
			ShaderRenderer shaderRenderer = (ShaderRenderer) mc.renderer;
			customTessellator.addVertexAttrib(shaderRenderer.attributeID);
			customTessellator.addVertexAttrib(shaderRenderer.attributeTopVertex);
			shaderRenderer.attributeID.value = 0.0f;
			shaderRenderer.attributeTopVertex.value = 0.0f;
			customTessellator.enableAutoNormal();
		}
	}
	
	public static void setBlockID(Block block) {
		if(mc.renderer instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.renderer;
			customRenderer.setID(block.id);
		}
	}
	
	public static void setIsTopVertex(float topVertex) {
		if(mc.renderer instanceof CustomRenderer) {
			CustomRenderer customRenderer = (CustomRenderer) mc.renderer;
			customRenderer.setIsTopVertex(topVertex);
		}
	}
	
	public static void beforeRenderBlock(ChunkRenderer chunkRenderer, Block block, int x, int y, int z) {
		setBlockID(block);
	}
	
	public static boolean cancelFrustumCulling(RenderGlobal renderGlobal, CameraFrustum frustum, float partialTicks) {
		if(mc.renderer instanceof ShaderRenderer) {
			ShaderRenderer shaderRenderer = (ShaderRenderer) mc.renderer;
			return shaderRenderer.isRenderingShadowmap;
		}
		return false;
	}

}
