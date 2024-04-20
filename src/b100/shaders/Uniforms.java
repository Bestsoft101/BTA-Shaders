package b100.shaders;

import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.util.vector.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiPhotoMode;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.render.PostProcessingManager;
import net.minecraft.client.render.camera.CameraUtil;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.season.Season;
import net.minecraft.core.world.season.Seasons;
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
	public float windIntensity;
	public float windDirection;
	public int isGuiOpened;
	public int dimension;
	public int dimensionShadow;
	
	public final Matrix4f shadowProjectionMatrix = new Matrix4f();
	public final Matrix4f shadowModelViewMatrix = new Matrix4f();

	public final Matrix4f projectionMatrix = new Matrix4f();
	public final Matrix4f modelViewMatrix = new Matrix4f();

	public final Matrix4f projectionInverseMatrix = new Matrix4f();
	public final Matrix4f modelViewInverseMatrix = new Matrix4f();
	
	public final Matrix4f previousProjectionMatrix = new Matrix4f();
	public final Matrix4f previousModelViewMatrix = new Matrix4f();
	
	public float cameraPosX;
	public float cameraPosY;
	public float cameraPosZ;
	
	public boolean previousCameraPositionSet = false;
	public float previousCameraPosX;
	public float previousCameraPosY;
	public float previousCameraPosZ;
	
	public float fogColorR;
	public float fogColorG;
	public float fogColorB;
	
	public Uniforms(ShaderRenderer shaderRenderer) {
		this.renderer = shaderRenderer;
		this.mc = shaderRenderer.mc;
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
			int playerZ = MathHelper.floor_double(player.z);
			
			biomeTemperature = (float) world.getBlockTemperature(playerX, playerZ);
			biomeHumidity = (float) world.getBlockHumidity(playerX, playerZ);
			sunAngle = world.getCelestialAngle(partialTicks);
			
			Weather currentWeather = world.weatherManager.getCurrentWeather();
			if(currentWeather != null) {
				weather = currentWeather.weatherId;	
			}else {
				weather = 0;
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
			float renderPosX = (float) camera.getX(partialTicks);
			float renderPosY = (float) camera.getY(partialTicks);
			float renderPosZ = (float) camera.getZ(partialTicks);
			
			if(!previousCameraPositionSet) {
				previousCameraPositionSet = true;
				previousCameraPosX = renderPosX;
				previousCameraPosY = renderPosY;
				previousCameraPosZ = renderPosZ;
			}else {
				previousCameraPosX = cameraPosX;
				previousCameraPosY = cameraPosY;
				previousCameraPosZ = cameraPosZ;
			}
			
			cameraPosX = renderPosX;
			cameraPosY = renderPosY;
			cameraPosZ = renderPosZ;
			
			fogColorR = mc.worldRenderer.fogManager.fogRed;
			fogColorG = mc.worldRenderer.fogManager.fogGreen;
			fogColorB = mc.worldRenderer.fogManager.fogBlue;
		}else {
			biomeTemperature = 0.7f;
			biomeHumidity = 0.5f;
			isEyeInLiquid = 0;
			
			weather = 0;
			weatherIntensity = 0.0f;
			weatherPower = 0.0f;
			
			windDirection = 0.0f;
			windIntensity = 0.0f;
			
			dimension = 0;
			dimensionShadow = 0;
			
			fogColorR = 0.0f;
			fogColorG = 0.0f;
			fogColorB = 0.0f;
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
		glUniform1f(shader.getUniform("viewWidth"), renderer.currentWidth);
		glUniform1f(shader.getUniform("viewHeight"), renderer.currentHeight);

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
			
			Season currentSeason = world.seasonManager.getCurrentSeason(); 
			
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
		
		glUniform1f(shader.getUniform("windDirection"), windDirection);
		glUniform1f(shader.getUniform("windIntensity"), windIntensity);
		
		glUniform1i(shader.getUniform("dimension"), dimension);
		glUniform1i(shader.getUniform("dimensionShadow"), dimensionShadow);
		
		glUniform1f(shader.getUniform("frameTimeCounter"), frameTimeCounter);

		glUniform1f(shader.getUniform("gamma"), mc.gameSettings.gamma.value);
		glUniform1f(shader.getUniform("colorCorrection"), mc.gameSettings.colorCorrection.value);
		glUniform1f(shader.getUniform("fxaa"), mc.gameSettings.fxaa.value);
		glUniform1i(shader.getUniform("bloom"), mc.gameSettings.bloom.value);
		glUniform1i(shader.getUniform("heatHaze"), mc.gameSettings.heatHaze.value ? 1 : 0);
		glUniform1i(shader.getUniform("cbCorrectionMode"), mc.gameSettings.colorblindnessFix.value.ordinal());
		
		PostProcessingManager ppm = mc.ppm;
		glUniform1f(shader.getUniform("brightness"), ppm.brightness);
		glUniform1f(shader.getUniform("contrast"), ppm.contrast);
		glUniform1f(shader.getUniform("exposure"), ppm.exposure);
		glUniform1f(shader.getUniform("saturation"), ppm.saturation);
		glUniform1f(shader.getUniform("rMod"), ppm.rMod);
		glUniform1f(shader.getUniform("gMod"), ppm.gMod);
		glUniform1f(shader.getUniform("bMod"), ppm.bMod);

		glUniform1i(shader.getUniform("isEyeInLiquid"), isEyeInLiquid);
		glUniform1i(shader.getUniform("isGuiOpened"), isGuiOpened);
		glUniform1i(shader.getUniform("isWorldOpened"), mc.thePlayer != null && mc.theWorld != null ? 1 : 0);
		
		glUniform3f(shader.getUniform("cameraPosition"), cameraPosX, cameraPosY, cameraPosZ);
		glUniform3f(shader.getUniform("previousCameraPosition"), previousCameraPosX, previousCameraPosY, previousCameraPosZ);
		
		glUniform3f(shader.getUniform("fogColor"), fogColorR, fogColorG, fogColorB);
	}

}
