package b100.shaders;

import static b100.shaders.OpenGLHelper.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import b100.json.JsonParser;
import b100.json.element.JsonArray;
import b100.json.element.JsonEntry;
import b100.json.element.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.PostProcessingManager;
import net.minecraft.client.render.Renderer;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.season.Season;
import net.minecraft.core.world.season.Seasons;
import net.minecraft.core.world.type.WorldTypes;

public class ShaderRenderer extends Renderer {
	
	private final List<RenderPass> postRenderPasses = new ArrayList<>();
	private final List<RenderPass> baseRenderPasses = new ArrayList<>();
	
	// Don't build strings every frame
	private static final String[] colortexStrings = new String[] {"colortex0", "colortex1", "colortex2", "colortex3", "colortex4", "colortex5", "colortex6", "colortex7"};
	
	private boolean isSetup = false;
	
	private final Framebuffer baseFramebuffer = new Framebuffer();
	private final Framebuffer postFramebuffer = new Framebuffer();
	
	private int fullscreenRectList = 0;
	
	private int framebufferWidth = -1;
	private int framebufferHeight = -1;
	
	private IntBuffer intBuffer = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asIntBuffer();

	private boolean enableShaders = true;
	private boolean showTextures = false;
	
	private boolean pressedReloadLast = false;
	private boolean pressedToggleLast = false;
	private boolean pressedShowTexturesLast = false;
	
	private long startTime = System.currentTimeMillis();
	private float frameTimeCounter = 0;
	
	public ShaderRenderer(Minecraft mc) {
		super(mc);
		
		ShaderMod.log("Shader Directory: " + ShaderMod.getShaderDirectory());
	}
	
	private void setup() {
		isSetup = true;
		
		checkError("pre setup");

		delete();
		
		checkError("delete");
		
		ShaderMod.log("Shader setup!");
		
		loadRenderPassConfig();
		
		setupFramebuffers();
		
		checkError("setup");
	}
	
	public void loadRenderPassConfig() {
		try {
			File shaderJson = new File(ShaderMod.getCurrentShaderPackDirectory(), "shader.json");
			JsonObject root = JsonParser.instance.parseFileContent(shaderJson);

			if(root == null) {
				ShaderMod.log("Missing shader.json!");
				
				// TODO Load internal default config
				
				return;
			}else {
				ShaderMod.log("Loading shader.json");
				
				JsonObject base = root.getObject("base");
				JsonObject post = root.getObject("post");
				
				if(base != null) {
					parseRenderConfig(base, baseRenderPasses, baseFramebuffer);
				}
				if(post != null) {
					parseRenderConfig(post, postRenderPasses, postFramebuffer);
				}
			}
			
			ShaderMod.log("Render Passes: " + baseRenderPasses.size() + " Base, " + postRenderPasses.size() + " Post");
			ShaderMod.log("Color Textures: " + baseFramebuffer.colortex.length + " Base, " + postFramebuffer.colortex.length + " Post");
		}catch (Exception e) {
			System.err.println("Shader setup error!");
			e.printStackTrace();
			
			baseRenderPasses.clear();
			postRenderPasses.clear();
			
			baseFramebuffer.delete();
			postFramebuffer.delete();
			
			baseFramebuffer.create(1, new boolean[] { false });
			postFramebuffer.create(1, new boolean[] { false });
		}
	}
	
	public void parseRenderConfig(JsonObject object, List<RenderPass> renderPasses, Framebuffer framebuffer) {
		JsonObject render = object.getObject("render");
		
		if(render != null) {
			List<JsonEntry> entries = render.entryList();
			for(int i=0; i < entries.size(); i++) {
				JsonEntry entry = entries.get(i);
				
				RenderPass renderPass = new RenderPass();

				renderPass.shader = new Shader();
				renderPass.shader.setupShader(entry.name);
				
				JsonObject obj = entry.value.getAsObject();
				
				if(i > 0) {
					renderPass.in = parseIntArray(obj.getArray("in"));
				}else {
					renderPass.in = new int[] {0};
				}
				
				if(i < entries.size() - 1) {
					renderPass.out = parseIntArray(obj.getArray("out"));
				}else {
					renderPass.out = new int[] {0};
				}
				
				renderPasses.add(renderPass);
			}
		}
		
		int textureCount = getTextureCount(renderPasses);
		
		boolean[] mipmap = new boolean[textureCount];
		
		JsonObject textureConfig = object.getObject("textures");
		if(textureConfig != null) {
			List<JsonEntry> entries = textureConfig.entryList();
			for(int i=0; i < entries.size(); i++) {
				JsonEntry entry = entries.get(i);
				int textureID = Integer.parseInt(entry.name);
				
				JsonObject obj = entry.value.getAsObject();
				if(obj.has("mipmap")) {
					mipmap[textureID] = obj.getBoolean("mipmap");
				}
			}
		}
		
		framebuffer.create(textureCount, mipmap);
	}
	
	public int getTextureCount(List<RenderPass> renderPasses) {
		int textureCount = 0;
		for(int i=0; i < renderPasses.size(); i++) {
			RenderPass rp = renderPasses.get(i);
			if(rp.in != null) {
				for(int j=0; j < rp.in.length; j++) {
					textureCount = Math.max(textureCount, rp.in[j]);
				}
			}
			if(rp.out != null) {
				for(int j=0; j < rp.out.length; j++) {
					textureCount = Math.max(textureCount, rp.out[j]);
				}
			}
		}
		return textureCount + 1;
	}
	
	public void setupFramebuffers() {
		int width = Display.getWidth();
		int height = Display.getHeight();
		
		ShaderMod.log("Framebuffer Size: " + width + " x " + height);
		
		this.framebufferWidth = width;
		this.framebufferHeight = height;
		
		baseFramebuffer.setup(width, height);
		checkFramebufferStatus();
		
		postFramebuffer.setup(width, height);
		checkFramebufferStatus();
		
		checkError("framebuffer setup");
		
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	private void checkFramebufferStatus() {
		int framebufferStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
		if(framebufferStatus != GL_FRAMEBUFFER_COMPLETE) {
			throw new RuntimeException("Framebuffer not complete! (Status " + framebufferStatus + ")");
		}
	}
	
	@Override
	public void beginRenderGame(float partialTicks) {
		checkError("pre begin render game");
		
		if(!isSetup) {
			setup();
		}
		
		if(Display.getWidth() != framebufferWidth || Display.getHeight() != framebufferHeight) {
			setupFramebuffers();
		}
		
		boolean pressedReload = Keyboard.isKeyDown(Keyboard.KEY_F7);
		if(pressedReload != pressedReloadLast) {
			pressedReloadLast = pressedReload;
			if(pressedReload) {
				ShaderMod.log("Reloading Shaders!");
				setup();
				enableShaders = true;
			}
		}
		
		boolean pressedToggle = Keyboard.isKeyDown(Keyboard.KEY_F6);
		if(pressedToggle != pressedToggleLast) {
			pressedToggleLast = pressedToggle;
			if(pressedToggle) {
				enableShaders = !enableShaders;
				ShaderMod.log("Enable Shaders: " + enableShaders);
			}
		}
		
		boolean pressedShowTextures = Keyboard.isKeyDown(Keyboard.KEY_F8);
		if(pressedShowTextures != pressedShowTexturesLast) {
			pressedShowTexturesLast = pressedShowTextures;
			if(pressedShowTextures) {
				showTextures = !showTextures;
				ShaderMod.log("Show Textures: " + showTextures);
			}
		}
		
		if(!enableShaders) {
			glBindFramebuffer(GL_FRAMEBUFFER, 0);
			return;
		}
		
		long now = System.currentTimeMillis();
		int passedTime = (int) (now - startTime);
		frameTimeCounter = passedTime / 1000.0f;
		
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		
		if(baseRenderPasses.size() > 0) {
			glBindFramebuffer(GL_FRAMEBUFFER, baseFramebuffer.id);
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, baseFramebuffer.colortex[0], 0);
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, 0, 0);
			glDrawBuffers(GL_COLOR_ATTACHMENT0);
		}

		glEnable(GL_DEPTH_TEST);
		
		checkError("begin render game");
	}
	
	@Override
	public void beginRenderWorld(float partialTicks) {
		checkError("pre begin render world");

		if(!enableShaders) {
			return;
		}
		
		if(postRenderPasses.size() > 0) {
			glBindFramebuffer(GL_FRAMEBUFFER, postFramebuffer.id);
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, postFramebuffer.colortex[0], 0);
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, 0, 0);
			glDrawBuffers(GL_COLOR_ATTACHMENT0);
		}
		
		checkError("begin render world");
	}
	
	@Override
	public void endRenderWorld(float partialTicks) {
		checkError("pre end render world");

		if(!enableShaders) {
			return;
		}

		if(postRenderPasses.size() > 0) {
			postProcessPipeline(postRenderPasses, postFramebuffer, baseRenderPasses.size() > 0 ? baseFramebuffer.id : 0, true);
		}
		
		glEnable(GL_ALPHA_TEST);
		
		checkError("end render world");
	}
	
	@Override
	public void endRenderGame(float partialTicks) {
		checkError("pre end render game");

		if(!enableShaders) {
			return;
		}
		
		if(baseRenderPasses.size() > 0) {
			postProcessPipeline(baseRenderPasses, baseFramebuffer, 0, false);
		}
		
		checkError("end render game");
		
		if(showTextures) {
			int pos = 0;
			if(baseRenderPasses.size() > 0) {
				showTextures(baseFramebuffer, pos++);
			}
			if(postRenderPasses.size() > 0) {
				showTextures(postFramebuffer, pos++);
			}
		}
	}
	
	public void postProcessPipeline(List<RenderPass> renderPasses, Framebuffer framebuffer, int endFramebuffer, boolean enableDepthTex) {
		glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.id);
		
		int count = renderPasses.size();
		
		for(int renderPassIndex = 0; renderPassIndex < count; renderPassIndex++) {
			RenderPass renderPass = renderPasses.get(renderPassIndex);
			boolean last = renderPassIndex == count - 1;
			
			if(renderPass.shader.bind()) {
				// Setup input textures
				setupCommonUniforms(renderPass.shader);
				
				for(int inIndex=0; inIndex < renderPass.in.length; inIndex++) {
					glActiveTexture(GL_TEXTURE0 + inIndex);
					glBindTexture(GL_TEXTURE_2D, framebuffer.colortex[renderPass.in[inIndex]]);
					glUniform1i(renderPass.shader.getUniform(colortexStrings[inIndex]), inIndex);
					
					if(framebuffer.mipmap[renderPass.in[inIndex]]) {
						glGenerateMipmap(GL_TEXTURE_2D);
					}
				}
				
				if(enableDepthTex) {
					int depthTex = renderPass.in.length;
					glActiveTexture(GL_TEXTURE0 + depthTex);
					glBindTexture(GL_TEXTURE_2D, framebuffer.depthtex);
					glUniform1i(renderPass.shader.getUniform("depthtex0"), depthTex);
				}
				
				glActiveTexture(GL_TEXTURE0);
			}else {
				// Shader not compiled,
				// just draw first input texture
				glBindTexture(GL_TEXTURE_2D, framebuffer.colortex[renderPass.in[0]]);
			}
			
			if(!last) {
				// Not last renderpass, set target buffers
				intBuffer.position(0).limit(renderPass.out.length);
				for(int outIndex=0; outIndex < renderPass.out.length; outIndex++) {
					glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + outIndex, GL_TEXTURE_2D, framebuffer.colortex[renderPass.out[outIndex]], 0);
					intBuffer.put(GL_COLOR_ATTACHMENT0 + outIndex);
				}
				intBuffer.position(0);
				glDrawBuffers(intBuffer);
			}else {
				glDrawBuffers(GL_COLOR_ATTACHMENT0);
			}
			
			if(last) {
				glBindFramebuffer(GL_FRAMEBUFFER, endFramebuffer);
			}
			
			drawFramebuffer();
		}
		
		glUseProgram(0);
	}
	
	public void showTextures(Framebuffer framebuffer, int pos) {
		// Debug
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_ALPHA_TEST);
		glPushMatrix();
		glScaled(0.14, 0.14, 1.0);
		glTranslated(0.1, 0.1, 0.0);
		if(pos == 1) {
			glTranslated(0.0, 1.1, 0.0);	
		}
		
		for(int i=0; i < framebuffer.colortex.length; i++) {
			glBindTexture(GL_TEXTURE_2D, framebuffer.colortex[i]);

			double pad = 0.02;
			glDisable(GL_TEXTURE_2D);
			glBegin(GL_QUADS);
			glVertex2d(-pad, -pad);
			glVertex2d(-pad, 1.0 + pad);
			glVertex2d(1.0 + pad, 1.0 + pad);
			glVertex2d(1.0 + pad, -pad);
			glEnd();

			glEnable(GL_TEXTURE_2D);
			glBegin(GL_QUADS);
			glTexCoord2d(0.0, 0.0);
			glVertex2d(0.0, 0.0);
			glTexCoord2d(0.0, 1.0);
			glVertex2d(0.0, 1.0);
			glTexCoord2d(1.0, 1.0);
			glVertex2d(1.0, 1.0);
			glTexCoord2d(1.0, 0.0);
			glVertex2d(1.0, 0.0);
			glEnd();
			
			glTranslated(1.1, 0.0, 0.0);
		}
		
		glPopMatrix();
	}
	
	private void setupCommonUniforms(Shader shader) {
		checkError("pre uniforms");
		
		glUniform1f(shader.getUniform("viewWidth"), Display.getWidth());
		glUniform1f(shader.getUniform("viewHeight"), Display.getHeight());

		boolean spring = false;
		boolean summer = false;
		boolean autumn = false;
		boolean winter = false;
		
		float biomeTemperature = 0.7f;
		
		float sunAngle = 0.0f;
		
		boolean nether = false;
		boolean paradise = false;
		
		if(mc.theWorld != null && mc.thePlayer != null) {
			World world = mc.theWorld;
			EntityPlayer player = mc.thePlayer;
			
			Season currentSeason = world.seasonManager.getCurrentSeason(); 
			
			spring = currentSeason == Seasons.OVERWORLD_SPRING;
			summer = currentSeason == Seasons.OVERWORLD_SUMMER;
			autumn = currentSeason == Seasons.OVERWORLD_FALL;
			winter = currentSeason == Seasons.OVERWORLD_WINTER || currentSeason == Seasons.OVERWORLD_WINTER_ENDLESS;
			
			int playerX = MathHelper.floor_double(player.x);
			int playerZ = MathHelper.floor_double(player.z);
			
			biomeTemperature = (float) world.getBlockTemperature(playerX, playerZ);
			
			sunAngle = world.getCelestialAngle(1.0f);
			
			nether = world.worldType == WorldTypes.NETHER_DEFAULT;
			paradise = world.worldType == WorldTypes.PARADISE_DEFAULT;
		}
		
		glUniform1f(shader.getUniform("spring"), spring ? 1.0f : 0.0f);
		glUniform1f(shader.getUniform("summer"), summer ? 1.0f : 0.0f);
		glUniform1f(shader.getUniform("autumn"), autumn ? 1.0f : 0.0f);
		glUniform1f(shader.getUniform("winter"), winter ? 1.0f : 0.0f);
		
		glUniform1f(shader.getUniform("biomeTemperature"), biomeTemperature);
		glUniform1f(shader.getUniform("sunAngle"), sunAngle);

		glUniform1f(shader.getUniform("nether"), nether ? 1.0f : 0.0f);
		glUniform1f(shader.getUniform("paradise"), paradise ? 1.0f : 0.0f);
		
		glUniform1f(shader.getUniform("frameTimeCounter"), frameTimeCounter);

		glUniform1f(shader.getUniform("gamma"), mc.gameSettings.gamma.value);
		glUniform1f(shader.getUniform("colorCorrection"), mc.gameSettings.colorCorrection.value);
		glUniform1f(shader.getUniform("fxaa"), mc.gameSettings.fxaa.value);
		glUniform1i(shader.getUniform("bloom"), mc.gameSettings.bloom.value);
		glUniform1i(shader.getUniform("heatHaze"), mc.gameSettings.heatHaze.value ? 1 : 0);
		
		PostProcessingManager ppm = mc.ppm;
		glUniform1f(shader.getUniform("brightness"), ppm.brightness);
		glUniform1f(shader.getUniform("contrast"), ppm.contrast);
		glUniform1f(shader.getUniform("exposure"), ppm.exposure);
		glUniform1f(shader.getUniform("saturation"), ppm.saturation);
		glUniform1f(shader.getUniform("rMod"), ppm.rMod);
		glUniform1f(shader.getUniform("gMod"), ppm.gMod);
		glUniform1f(shader.getUniform("bMod"), ppm.bMod);
		
		checkError("uniforms");
	}
	
	private void drawFramebuffer() {
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, 1, 0, 1, -1, 1);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		
		glDisable(GL_CULL_FACE);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_ALPHA_TEST);
		glDisable(GL_BLEND);
		glDisable(GL_LIGHTING);
		glDisable(GL_FOG);
		glEnable(GL_TEXTURE_2D);
		
		glColor3d(1.0, 1.0, 1.0);
		
		glEnableClientState(GL_VERTEX_ARRAY);
		glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		
		drawFullscreenRect();
		
		glDisableClientState(GL_VERTEX_ARRAY);
		glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		
		glEnable(GL_ALPHA_TEST);
	}
	
	private void drawFullscreenRect() {
		glBindVertexArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		if(fullscreenRectList == 0) {
			int[] data = new int[] { 0, 0, 0, 1, 1, 1, 1, 0 };
			ByteBuffer buffer = ByteBuffer.allocateDirect(data.length * 2).order(ByteOrder.nativeOrder());
			for(int i=0; i < data.length; i++) {
				buffer.putShort((short) data[i]);
			}
			
			glEnableClientState(GL_VERTEX_ARRAY);
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			
			buffer.position(0);
			glVertexPointer(2, GL_SHORT, 4, buffer);
			buffer.position(0);
			glTexCoordPointer(2, GL_SHORT, 4, buffer);
			
			fullscreenRectList = glGenLists(1);
			glNewList(fullscreenRectList, GL_COMPILE);
			glDrawArrays(GL_QUADS, 0, 4);
			glEndList();
			
			glDisableClientState(GL_VERTEX_ARRAY);
			glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		}
		
		glCallList(fullscreenRectList);
	}
	
	@Override
	public void delete() {
		baseFramebuffer.delete();
		postFramebuffer.delete();
		for(int i=0; i < baseRenderPasses.size(); i++) {
			baseRenderPasses.get(i).shader.delete();
		}
		for(int i=0; i < postRenderPasses.size(); i++) {
			postRenderPasses.get(i).shader.delete();
		}
		baseRenderPasses.clear();
		postRenderPasses.clear();
	}
	
	static class Framebuffer {
		
		int id;
		int[] colortex;
		int depthtex;
		boolean[] mipmap;
		
		void create(int colorTextures, boolean[] mipmap) {
			id = glGenFramebuffers();
			
			colortex = new int[colorTextures];
			for(int i=0; i < colortex.length; i++) {
				colortex[i] = glGenTextures();
			}
			
			depthtex = glGenTextures();
			this.mipmap = mipmap;
		}
		
		void setup(int width, int height) {
			glBindFramebuffer(GL_FRAMEBUFFER, id);
			
			for(int i=0; i < colortex.length; i++) {
				boolean mipmapTex = mipmap[i];
				
				glBindTexture(GL_TEXTURE_2D, colortex[i]);
				glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
				if(mipmapTex) glGenerateMipmap(GL_TEXTURE_2D);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, mipmapTex ? GL_LINEAR_MIPMAP_LINEAR : GL_LINEAR);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
				glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colortex[i], 0);
			}
			
			glBindTexture(GL_TEXTURE_2D, depthtex);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, width, height, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_BYTE, (ByteBuffer) null);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthtex, 0);
		}
		
		void setActiveTexture(int attachment, int texture) {
			glBindFramebuffer(GL_FRAMEBUFFER, id);
			if(texture >= 0) {
				glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + attachment, GL_TEXTURE_2D, colortex[texture], 0);	
			}else {
				glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + attachment, GL_TEXTURE_2D, 0, 0);
			}
		}
		
		void delete() {
			if(id != 0) {
				glDeleteFramebuffers(id);
				id = 0;
			}
			if(colortex != null) {
				for(int i=0; i < colortex.length; i++) {
					glDeleteTextures(colortex[i]);
				}
				colortex = null;
			}
			if(depthtex != 0) {
				glDeleteTextures(depthtex);
				depthtex = 0;
			}
		}
		
	}
	
	public static class RenderPass {
		
		public Shader shader;
		public int[] in;
		public int[] out;
		
	}
	
	public static boolean isNullOrZeroArray(int[] arr) {
		if(arr == null) {
			return true;
		}
		if(arr.length != 1) {
			return false;
		}
		return arr[0] == 0;
	}
	
	public static int[] parseIntArray(JsonArray jsonArray) {
		int[] intArray = new int[jsonArray.length()];
		
		for(int i=0; i < intArray.length; i++) {
			intArray[i] = jsonArray.get(i).getAsNumber().getInteger();
		}
		
		return intArray;
	}

}
