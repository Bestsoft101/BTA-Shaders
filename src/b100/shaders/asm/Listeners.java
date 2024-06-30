package b100.shaders.asm;

import static b100.shaders.ShaderMod.*;
import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import b100.natrium.CustomTessellator;
import b100.natrium.NatriumMod;
import b100.shaders.CustomRenderer;
import b100.shaders.LanguageHelper;
import b100.shaders.ShaderMod;
import b100.shaders.ShaderRenderer;
import b100.shaders.TextureHelper;
import b100.shaders.asm.utils.CallbackInfo;
import b100.shaders.gui.GuiShaderMenu;
import b100.shaders.gui.GuiUtils;
import net.minecraft.client.GLAllocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.options.GuiOptions;
import net.minecraft.client.gui.options.components.KeyBindingComponent;
import net.minecraft.client.gui.options.components.OptionsCategory;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.client.input.InputDevice;
import net.minecraft.client.render.ChunkRenderer;
import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.Renderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.camera.EntityCamera;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.client.render.culling.CameraFrustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.stitcher.AtlasStitcher;
import net.minecraft.client.render.stitcher.IconCoordinate;
import net.minecraft.client.render.stitcher.TextureRegistry;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;

public class Listeners {
	
	public static void beforeGameStart() {
		NatriumMod.renderListCount = 4;
		
		GuiUtils.instance = new GuiUtils(mc);
	}
	
	public static void onInitGui() {
		ShaderMod.optionsPage = new OptionsPage("Shaders", new ItemStack(Item.wandMonsterSpawner));

		OptionsPages.register(ShaderMod.optionsPage);
		
		OptionsCategory shaderControls = new OptionsCategory("category.controls.shaders");
		shaderControls.withComponent(new KeyBindingComponent(ShaderMod.config.keyOpenShaderMenu.keyBinding));
		shaderControls.withComponent(new KeyBindingComponent(ShaderMod.config.keyReloadShaders.keyBinding));
		shaderControls.withComponent(new KeyBindingComponent(ShaderMod.config.keyToggleShaders.keyBinding));
		shaderControls.withComponent(new KeyBindingComponent(ShaderMod.config.keyShowTextures.keyBinding));
		OptionsPages.CONTROLS.withComponent(shaderControls);
	}
	
	public static void onClickOptionsPage(GuiOptions guiOptions) {
		if(guiOptions.selectedPage == ShaderMod.optionsPage) {
			guiOptions.selectedPage = OptionsPages.GENERAL;
			GuiUtils.instance.displayGui(new GuiShaderMenu(mc.currentScreen));
		}
	}
	
	public static void onRefreshTextures() {
		if(!ShaderMod.enableNormals && !ShaderMod.enableSpecular) {
			return;
		}
		
		ShaderMod.log("Setup Normal & Specular");
		
		AtlasStitcher blockAtlas = TextureRegistry.blockAtlas;
		
		BufferedImage normalImage = new BufferedImage(blockAtlas.getAtlasWidth(), blockAtlas.getAtlasHeight(), BufferedImage.TYPE_INT_ARGB);
		BufferedImage specularImage = new BufferedImage(blockAtlas.getAtlasWidth(), blockAtlas.getAtlasHeight(), BufferedImage.TYPE_INT_ARGB);
		
		TextureHelper.fillColor(normalImage, 0xFF7F7FFF);
		TextureHelper.fillColor(specularImage, 0xFF000000);
		
		List<String> blockTextureKeys = new ArrayList<>(blockAtlas.textureMap.keySet());
		blockTextureKeys.sort(String.CASE_INSENSITIVE_ORDER);
		
		int normalTextureCount = 0;
		int specularTextureCount = 0;
		
		for(String string : blockTextureKeys) {
			IconCoordinate coord = blockAtlas.textureMap.get(string);
			
			int x = coord.iconX;
			int y = coord.iconY;
			
			int i = string.indexOf(':');
			
			String namespace = string.substring(0, i);
			String name = string.substring(i + 1);
			
			if(ShaderMod.enableNormals) {
				String path = "/assets/" + namespace + "/textures/block/" + name + "_n.png";
				BufferedImage normal = TextureHelper.getTextureIfExists(path);
				if(normal != null) {
					TextureHelper.drawImage(normal, normalImage, x, y);
					normalTextureCount++;
				}	
			}
			
			if(ShaderMod.enableSpecular) {
				String path = "/assets/" + namespace + "/textures/block/" + name + "_s.png";
				BufferedImage specular = TextureHelper.getTextureIfExists(path);
				if(specular != null) {
					TextureHelper.drawImage(specular, specularImage, x, y);
					specularTextureCount++;
				}	
			}
		}
		
		if(ShaderMod.enableNormals) {
			ShaderMod.log(normalTextureCount + " Normal Textures");
			if(ShaderMod.normalTexture == 0) {
				ShaderMod.normalTexture = GLAllocation.generateTexture();
			}
			TextureHelper.setTextureImage(ShaderMod.normalTexture, normalImage);	
		}
		
		if(ShaderMod.enableNormals) {
			ShaderMod.log(specularTextureCount + " Specular Textures");
			if(ShaderMod.specularTexture == 0) {
				ShaderMod.specularTexture = GLAllocation.generateTexture();
			}
			TextureHelper.setTextureImage(ShaderMod.specularTexture, specularImage);	
		}
	}
	
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
	
	public static void onReloadLanguages(I18n stringTranslate, String languageName) {
		LanguageHelper.onReloadLanguages(stringTranslate, languageName);
	}
	
	public static boolean checkGlobalMouseInput() {
		if(ShaderMod.handleGlobalInput(InputDevice.mouse)) {
			return true;
		}
		return false;
	}
	
	public static boolean checkGlobalKeyboardInput() {
		if(ShaderMod.handleGlobalInput(InputDevice.keyboard)) {
			return true;
		}
		return false;
	}
	
	public static void checkBoundInputs(Minecraft minecraft, InputDevice inputDevice, CallbackInfo ci) {
		if(ShaderMod.handleIngameInput(inputDevice)) {
			ci.setCancelled(true);
			ci.setReturnValue(true);
		}
	}

}
