package b100.shaders;

import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiPhotoMode;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.render.FogManager;
import net.minecraft.client.render.PostProcessingManager;
import net.minecraft.client.render.camera.CameraUtil;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.enums.LightLayer;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.Vec3d;
import net.minecraft.core.world.World;
import net.minecraft.core.world.season.Season;
import net.minecraft.core.world.season.Seasons;
import net.minecraft.core.world.type.WorldType;
import net.minecraft.core.world.weather.Weather;
import net.minecraft.core.world.wind.WindManager;

public class Uniforms {
	
	public ShaderRenderer renderer;
	public Minecraft mc;
	
	public long startTime = System.currentTimeMillis();
	public float frameTimeCounter = 0;
	public int isEyeInLiquid;
	public float biomeTemperature;
	public float biomeHumidity;
	public float sunAngle;
	
	public int weather;
	public float weatherIntensity;
	public float weatherPower;
	public float weatherFogDistance;
	public int weatherSubtractLightLevel;
	public int weatherPrecipitationType;
	
	public float windIntensity;
	public float windDirection;
	public int isGuiOpened;
	public int dimension;
	public int dimensionShadow;
	public int heldItemID;
	public float heldItemLightValue;
	
	public final Matrix4f shadowProjectionMatrix = new Matrix4f();
	public final Matrix4f shadowModelViewMatrix = new Matrix4f();

	public final Matrix4f projectionMatrix = new Matrix4f();
	public final Matrix4f modelViewMatrix = new Matrix4f();

	public final Matrix4f projectionInverseMatrix = new Matrix4f();
	public final Matrix4f modelViewInverseMatrix = new Matrix4f();
	
	public final Matrix4f previousProjectionMatrix = new Matrix4f();
	public final Matrix4f previousModelViewMatrix = new Matrix4f();
	
	public int eyeBrightnessSky;
	public int eyeBrightnessBlock;

	public boolean previousCameraPositionSet = false;
	public Vector3f cameraPos = new Vector3f();
	public Vector3f prevCameraPos = new Vector3f();
	public Vector4f shadowLightPosition = new Vector4f();
	public Vector4f moonPosition = new Vector4f();
	public Vector4f sunPosition = new Vector4f();
	public Vector3f fogColor = new Vector3f();
	public Vector3f skyColor = new Vector3f();
	
	public int worldTypeMinY;
	public int worldTypeMaxY;
	
	private Matrix4f matrixBuffer = new Matrix4f();
	
	public Uniforms(ShaderRenderer shaderRenderer) {
		this.renderer = shaderRenderer;
		this.mc = shaderRenderer.mc;
	}
	
	public void updateCelestialPosition() {
		MatrixHelper.getMatrix(GL11.GL_MODELVIEW_MATRIX, matrixBuffer);
		
		sunPosition.set(0.0f, 100.0f, 0.0f, 0.0f);
		moonPosition.set(0.0f, -100.0f, 0.0f, 0.0f);

		Matrix4f.transform(matrixBuffer, sunPosition, sunPosition);
		Matrix4f.transform(matrixBuffer, moonPosition, moonPosition);
		
		if(sunAngle > 0.25f && sunAngle < 0.75f) {
			shadowLightPosition.set(moonPosition.x, moonPosition.y, moonPosition.z, sunPosition.w);
		}else {
			shadowLightPosition.set(sunPosition.x, sunPosition.y, sunPosition.z, sunPosition.w);
		}
	}
	
	public void update(float partialTicks) {
		long now = System.currentTimeMillis();
		int passedTime = (int) (now - startTime);
		frameTimeCounter = passedTime / 1000.0f;
		
		if(mc.thePlayer != null && mc.theWorld != null) {
			EntityPlayer player = mc.thePlayer;
			World world = mc.theWorld;
			
			if(mc.activeCamera != null) {
				if(CameraUtil.isUnderLiquid(mc.activeCamera, mc.theWorld, Material.lava, partialTicks)) {
					isEyeInLiquid = 2;
				}else if(CameraUtil.isUnderLiquid(mc.activeCamera, mc.theWorld, Material.water, partialTicks)) {
					isEyeInLiquid = 1;
				}else {
					isEyeInLiquid = 0;
				}
			}else {
				isEyeInLiquid = 0;
			}

			int playerX = MathHelper.floor_double(player.x);
			int playerY = MathHelper.floor_double(player.y);
			int playerZ = MathHelper.floor_double(player.z);
			
			biomeTemperature = (float) world.getBlockTemperature(playerX, playerZ);
			biomeHumidity = (float) world.getBlockHumidity(playerX, playerZ);
			sunAngle = world.getCelestialAngle(partialTicks);
			
			eyeBrightnessBlock = world.getSavedLightValue(LightLayer.Block, playerX, playerY, playerZ);
			eyeBrightnessSky = world.getSavedLightValue(LightLayer.Sky, playerX, playerY, playerZ);
			
			Weather currentWeather = world.weatherManager.getCurrentWeather();
			if(currentWeather != null) {
				weather = currentWeather.weatherId;
				weatherFogDistance = currentWeather.fogDistance;
				weatherSubtractLightLevel = currentWeather.subtractLightLevel;
				weatherPrecipitationType = currentWeather.precipitationType;
			}else {
				weather = 0;
				weatherFogDistance = 0.0f;
				weatherSubtractLightLevel = 0;
				weatherPrecipitationType = 0;
			}
			
			weatherIntensity = world.weatherManager.getWeatherIntensity();
			weatherPower = world.weatherManager.getWeatherPower();
			
			WindManager windManager = world.getWorldType().getWindManager();
			if(windManager != null) {
				windDirection = windManager.getWindDirection(world, 0.0f, 500.0f, 0.0f);
				windIntensity = windManager.getWindIntensity(world, 0.0f, 500.0f, 0.0f);
			}else {
				windDirection = 0.0f;
				windIntensity = 0.0f;
			}
			
			dimension = world.dimension.id;
			dimensionShadow = world.worldType.hasCeiling() ? 0 : 1;
			
			ICamera camera = mc.activeCamera;
			double renderPosX = camera.getX(partialTicks);
			double renderPosY = camera.getY(partialTicks);
			double renderPosZ = camera.getZ(partialTicks);
			
			if(!previousCameraPositionSet) {
				previousCameraPositionSet = true;
				prevCameraPos.set((float) renderPosX, (float) renderPosY, (float) renderPosZ);
			}else {
				prevCameraPos.set(cameraPos);
			}
			
			cameraPos.set((float) renderPosX, (float) renderPosY, (float) renderPosZ);
			
			FogManager fogManager = mc.worldRenderer.fogManager;
			fogColor.set(fogManager.fogRed, fogManager.fogGreen, fogManager.fogBlue);
			
			heldItemID = 0;
			heldItemLightValue = 0.0f;
			
			if(player != null) {
				ItemStack heldItem = player.getHeldItem();
				if(heldItem != null) {
					heldItemID = heldItem.itemID;
					if(heldItemID < Block.lightEmission.length) {
						heldItemLightValue = Block.lightEmission[heldItemID] / 15.0f;
					}
				}
			}
			
			WorldType worldType = world.worldType;
			worldTypeMinY = worldType.getMinY();
			worldTypeMaxY = worldType.getMaxY();
			
			Vec3d skyColor = world.getSkyColor(mc.activeCamera, partialTicks);
			this.skyColor.x = (float) skyColor.xCoord;
			this.skyColor.y = (float) skyColor.yCoord;
			this.skyColor.z = (float) skyColor.zCoord;
		}else {
			biomeTemperature = 0.7f;
			biomeHumidity = 0.5f;
			isEyeInLiquid = 0;

			weather = 0;
			weatherFogDistance = 0.0f;
			weatherSubtractLightLevel = 0;
			weatherPrecipitationType = 0;
			weatherIntensity = 0.0f;
			weatherPower = 0.0f;
			
			windDirection = 0.0f;
			windIntensity = 0.0f;
			
			dimension = 0;
			dimensionShadow = 0;
			
			this.fogColor.set(0.0f, 0.0f, 0.0f);
			this.cameraPos.set(0.0f, 0.0f, 0.0f);
			this.prevCameraPos.set(0.0f, 0.0f, 0.0f);
			this.moonPosition.set(0.0f, 0.0f, 0.0f);
			this.sunPosition.set(0.0f, 0.0f, 0.0f);
			this.shadowLightPosition.set(0.0f, 0.0f, 0.0f);
			
			heldItemID = 0;
			heldItemLightValue = 0.0f;
			
			eyeBrightnessBlock = 0;
			eyeBrightnessSky = 0;
		}
		
		GuiScreen currentScreen = mc.currentScreen;
		if(currentScreen instanceof GuiChat) {
			isGuiOpened = 3;
		}else if(currentScreen instanceof GuiPhotoMode) {
			isGuiOpened = 2;
		}else if(currentScreen != null) {
			isGuiOpened = 1;
		}else {
			isGuiOpened = 0;
		}
	}
	
	public void apply(Shader shader, int stage) {
		float width = renderer.currentWidth;
		float height = renderer.currentHeight;
		float aspectRatio = width / height;
		
		glUniform1f(shader.getUniform("viewWidth"), width);
		glUniform1f(shader.getUniform("viewHeight"), height);
		
		glUniform1f(shader.getUniform("aspectRatio"), aspectRatio);
		
		glUniform1f(shader.getUniform("displayWidth"), Display.getWidth());
		glUniform1f(shader.getUniform("displayHeight"), Display.getHeight());
		
		if(stage == 0) {
			if(renderer.enableShadowmap) {
				MatrixHelper.uniformMatrix(shader.getUniform("shadowProjection"), shadowProjectionMatrix);
				MatrixHelper.uniformMatrix(shader.getUniform("shadowModelView"), shadowModelViewMatrix);	
			}

			MatrixHelper.uniformMatrix(shader.getUniform("gbufferProjectionInverse"), projectionInverseMatrix);
			MatrixHelper.uniformMatrix(shader.getUniform("gbufferModelViewInverse"), modelViewInverseMatrix);

			MatrixHelper.uniformMatrix(shader.getUniform("gbufferProjection"), projectionMatrix);
			MatrixHelper.uniformMatrix(shader.getUniform("gbufferModelView"), modelViewMatrix);
			
			MatrixHelper.uniformMatrix(shader.getUniform("gbufferPreviousProjection"), previousProjectionMatrix);
			MatrixHelper.uniformMatrix(shader.getUniform("gbufferPreviousModelView"), previousModelViewMatrix);
		}
		
		boolean spring = false;
		boolean summer = false;
		boolean autumn = false;
		boolean winter = false;
		
		if(mc.theWorld != null && mc.thePlayer != null) {
			World world = mc.theWorld;
			
			Season currentSeason = world.getSeasonManager().getCurrentSeason(); 
			
			spring = currentSeason == Seasons.OVERWORLD_SPRING;
			summer = currentSeason == Seasons.OVERWORLD_SUMMER;
			autumn = currentSeason == Seasons.OVERWORLD_FALL;
			winter = currentSeason == Seasons.OVERWORLD_WINTER || currentSeason == Seasons.OVERWORLD_WINTER_ENDLESS;
		}

		glUniform1f(shader.getUniform("spring"), spring ? 1.0f : 0.0f);
		glUniform1f(shader.getUniform("summer"), summer ? 1.0f : 0.0f);
		glUniform1f(shader.getUniform("autumn"), autumn ? 1.0f : 0.0f);
		glUniform1f(shader.getUniform("winter"), winter ? 1.0f : 0.0f);

		glUniform1f(shader.getUniform("biomeTemperature"), biomeTemperature);
		glUniform1f(shader.getUniform("biomeHumidity"), biomeHumidity);
		glUniform1f(shader.getUniform("sunAngle"), sunAngle);

		glUniform1i(shader.getUniform("weather"), weather);
		glUniform1f(shader.getUniform("weatherIntensity"), weatherIntensity);
		glUniform1f(shader.getUniform("weatherPower"), weatherPower);
		glUniform1f(shader.getUniform("weatherFogDistance"), weatherFogDistance);
		glUniform1i(shader.getUniform("weatherSubtractLightLevel"), weatherSubtractLightLevel);
		glUniform1f(shader.getUniform("weatherPrecipitationType"), weatherPrecipitationType);
		
		glUniform1f(shader.getUniform("windDirection"), windDirection);
		glUniform1f(shader.getUniform("windIntensity"), windIntensity);
		
		glUniform1i(shader.getUniform("dimension"), dimension);
		glUniform1i(shader.getUniform("dimensionShadow"), dimensionShadow);
		
		glUniform1f(shader.getUniform("frameTimeCounter"), frameTimeCounter);

		glUniform1f(shader.getUniform("gamma"), mc.gameSettings.gamma.value);
		glUniform1f(shader.getUniform("brightness"), mc.gameSettings.brightness.value);
		glUniform1f(shader.getUniform("colorCorrection"), mc.gameSettings.colorCorrection.value);
		glUniform1f(shader.getUniform("fxaa"), mc.gameSettings.fxaa.value);
		glUniform1i(shader.getUniform("bloom"), mc.gameSettings.bloom.value);
		glUniform1i(shader.getUniform("heatHaze"), mc.gameSettings.heatHaze.value ? 1 : 0);
		glUniform1i(shader.getUniform("cbCorrectionMode"), mc.gameSettings.colorblindnessFix.value.ordinal());
		glUniform1i(shader.getUniform("fullbright"), mc.fullbright ? 1 : 0);
		
		PostProcessingManager ppm = mc.ppm;
		glUniform1f(shader.getUniform("cc_brightness"), ppm.brightness);
		glUniform1f(shader.getUniform("cc_contrast"), ppm.contrast);
		glUniform1f(shader.getUniform("cc_exposure"), ppm.exposure);
		glUniform1f(shader.getUniform("cc_saturation"), ppm.saturation);
		glUniform1f(shader.getUniform("cc_rMod"), ppm.rMod);
		glUniform1f(shader.getUniform("cc_gMod"), ppm.gMod);
		glUniform1f(shader.getUniform("cc_bMod"), ppm.bMod);

		glUniform1i(shader.getUniform("isEyeInLiquid"), isEyeInLiquid);
		glUniform1i(shader.getUniform("isGuiOpened"), isGuiOpened);
		glUniform1i(shader.getUniform("isWorldOpened"), mc.thePlayer != null && mc.theWorld != null ? 1 : 0);
		glUniform2i(shader.getUniform("eyeBrightness"), eyeBrightnessSky, eyeBrightnessBlock);

		glUniform1i(shader.getUniform("heldItemID"), heldItemID);
		glUniform1f(shader.getUniform("heldItemLightValue"), heldItemLightValue);
		
		uniform3f(shader.getUniform("cameraPosition"), cameraPos);
		uniform3f(shader.getUniform("previousCameraPosition"), prevCameraPos);
		uniform3f(shader.getUniform("shadowLightPosition"), shadowLightPosition);
		uniform3f(shader.getUniform("sunPosition"), sunPosition);
		uniform3f(shader.getUniform("moonPosition"), moonPosition);
		uniform3f(shader.getUniform("fogColor"), fogColor);
		uniform3f(shader.getUniform("skyColor"), skyColor);
		
		glUniform1i(shader.getUniform("worldTypeMinY"), worldTypeMinY);
		glUniform1i(shader.getUniform("worldTypeMaxY"), worldTypeMaxY);
	}
	
	public static void uniform3f(int location, Vector3f vec) {
		glUniform3f(location, vec.x, vec.y, vec.z);
	}
	
	public static void uniform3f(int location, Vector4f vec) {
		glUniform3f(location, vec.x, vec.y, vec.z);
	}

}
