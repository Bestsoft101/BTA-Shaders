package b100.shaders;

import static b100.shaders.OpenGLHelper.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;

import b100.json.JsonParser;
import b100.json.element.JsonArray;
import b100.json.element.JsonEntry;
import b100.json.element.JsonObject;
import b100.natrium.VertexAttribute;
import b100.natrium.VertexAttributeFloat;
import net.minecraft.client.GLAllocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.LightmapHelper;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Renderer;

public class ShaderRenderer extends Renderer implements CustomRenderer {
	
	private final List<RenderPass> postRenderPasses = new ArrayList<>();
	private final List<RenderPass> baseRenderPasses = new ArrayList<>();
	
	// Don't build strings every frame
	private static final String[] colortexStrings = new String[] {"colortex0", "colortex1", "colortex2", "colortex3", "colortex4", "colortex5", "colortex6", "colortex7"};
	
	private boolean isSetup = false;

	private final Framebuffer shadowFramebuffer = new Framebuffer();
	private final Framebuffer baseFramebuffer = new Framebuffer();
	private final Framebuffer postFramebuffer = new Framebuffer();
	
	private int displayWidth;
	private int displayHeight;
	
	private int renderWidth;
	private int renderHeight;
	
	public int currentWidth;
	public int currentHeight;
	
	private final Shader shadowShader = new Shader();
	public boolean enableShadowmap = false;
	public boolean isRenderingShadowmap;
	public int shadowMapResolution = 1024;
	public float shadowDistance = 64.0f;

	private final Shader basicShader = new Shader();
	private final Shader texturedShader = new Shader();
	private final Shader skyBasicShader = new Shader();
	private final Shader skyTexturedShader = new Shader();
	private final Shader terrainShader = new Shader();
	private final Shader entitiesShader = new Shader();
	private final Shader cloudsShader = new Shader();
	private final Shader translucentShader = new Shader();
	private final Shader handShader = new Shader();

	public final VertexAttributeFloat attributeID = new VertexAttributeFloat("id", 10); 
	public final VertexAttributeFloat attributeTopVertex = new VertexAttributeFloat("isTopVertex", 11); 
	
	public int[] worldOutputTextures;

	private int fullscreenRectVAO;
	private int fullscreenRectVBO;
	
	private ByteBuffer byteBuffer = ByteBuffer.allocateDirect(256).order(ByteOrder.nativeOrder());
	private IntBuffer intBuffer = byteBuffer.asIntBuffer();
	private FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();

	private boolean enableShaders = true;
	private boolean showTextures = false;
	
	private boolean pressedReloadLast = false;
	private boolean pressedToggleLast = false;
	private boolean pressedShowTexturesLast = false;
	
	public final Uniforms uniforms = new Uniforms(this);
	
	public int fogMode;
	
	private Shader currentShader;
	
	public ShaderRenderer(Minecraft mc) {
		super(mc);
		
		ShaderMod.log("Shader Directory: " + ShaderMod.getShaderDirectory());
	}
	
	private void setup() {
		isSetup = true;
		
		checkError("pre setup");
		
		resetGlErrors();

		delete();
		
		checkError("delete");
		
		ShaderMod.log("Shader setup!");
		
		loadRenderPassConfig();
		
		updateResolution();
		
		setupFramebuffers();
		
		RenderBlocks.SIDE_LIGHT_MULTIPLIER[0] = 1.0f;
		RenderBlocks.SIDE_LIGHT_MULTIPLIER[1] = 1.0f;
		RenderBlocks.SIDE_LIGHT_MULTIPLIER[2] = 1.0f;
		RenderBlocks.SIDE_LIGHT_MULTIPLIER[3] = 1.0f;
		RenderBlocks.SIDE_LIGHT_MULTIPLIER[4] = 1.0f;
		RenderBlocks.SIDE_LIGHT_MULTIPLIER[5] = 1.0f;
		
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

				JsonObject shadow = root.getObject("shadow");
				if(shadow != null) {
					enableShadowmap = shadow.getBoolean("enable");
					if(enableShadowmap) {
						shadowFramebuffer.create(0, null);	
					}
					if(shadow.has("resolution")) {
						shadowMapResolution = shadow.getInt("resolution");
					}else {
						shadowMapResolution = 1024;
					}
					if(shadow.has("distance")) {
						shadowDistance = shadow.getFloat("distance");
					}else {
						shadowDistance = 64.0f;
					}
					if(shadow.has("shader")) {
						shadowShader.setupShader(shadow.getString("shader"));
					}
				}
				
				JsonObject world = root.getObject("world");
				if(world != null) {
					List<VertexAttribute> vertexAttributes = new ArrayList<>();
					vertexAttributes.add(attributeID);
					vertexAttributes.add(attributeTopVertex);
					
					if(world.has("basic")) {
						basicShader.setupShader(world.getString("basic"));
					}
					if(world.has("textured")) {
						texturedShader.setupShader(world.getString("textured"));
					}
					if(world.has("skybasic")) {
						skyBasicShader.setupShader(world.getString("skybasic"));
					}
					if(world.has("skytextured")) {
						skyTexturedShader.setupShader(world.getString("skytextured"));
					}
					if(world.has("terrain")) {
						terrainShader.setupShader(world.getString("terrain"), vertexAttributes);
					}
					if(world.has("entities")) {
						entitiesShader.setupShader(world.getString("entities"));
					}
					if(world.has("clouds")) {
						cloudsShader.setupShader(world.getString("clouds"));
					}
					if(world.has("translucent")) {
						translucentShader.setupShader(world.getString("translucent"), vertexAttributes);
					}
					if(world.has("hand")) {
						handShader.setupShader(world.getString("hand"));
					}
					if(world.has("out")) {
						worldOutputTextures = parseIntArray(world.getArray("out"));
					}else {
						worldOutputTextures = new int[] {0};
					}
				}else {
					worldOutputTextures = new int[] {0};
				}
				
				JsonObject base = root.getObject("base");
				JsonObject post = root.getObject("post");
				
				if(base != null) {
					parseRenderConfig(base, baseRenderPasses, baseFramebuffer);
				}else {
					baseFramebuffer.create(0, new boolean[] {false});
				}
				if(post != null) {
					parseRenderConfig(post, postRenderPasses, postFramebuffer);
				}else {
					postFramebuffer.create(0, new boolean[] {false});
				}
			}
			
			ShaderMod.log("Render Passes: " + baseRenderPasses.size() + " Base, " + postRenderPasses.size() + " Post");
			ShaderMod.log("Color Textures: " + baseFramebuffer.colortex.length + " Base, " + postFramebuffer.colortex.length + " Post");
			ShaderMod.log("Enable Shadowmap: " + enableShadowmap);
		}catch (Exception e) {
			System.err.println("Shader setup error!");
			e.printStackTrace();
			
			delete();
			
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
				
				if(i > 0 || framebuffer == postFramebuffer) {
					JsonArray array = obj.getArray("in");
					if(array == null) {
						throw new RuntimeException("Missing \"in\" array in renderpass \"" + entry.name + "\"!");
					}
					renderPass.in = parseIntArray(array);
				}else {
					renderPass.in = new int[] {0};
				}
				
				if(i < entries.size() - 1) {
					JsonArray array = obj.getArray("out");
					if(array == null) {
						throw new RuntimeException("Missing \"out\" array in renderpass \"" + entry.name + "\"!");
					}
					renderPass.out = parseIntArray(array);
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
	
	public void updateResolution() {
		displayWidth = Math.max(1, Display.getWidth());
		displayHeight = Math.max(1, Display.getHeight());
		
		float renderScale = (float) mc.gameSettings.renderScale.value.scale;
		
		renderWidth = (int) (displayWidth * renderScale);
		renderHeight = (int) (displayHeight * renderScale);
		
		if(renderWidth == 0 || renderHeight == 0) {
			renderWidth = displayWidth;
			renderHeight = displayHeight;
		}
		
		currentWidth = displayWidth;
		currentHeight = displayHeight;
	}
	
	public void setupFramebuffers() {
		ShaderMod.log("Framebuffer Size: " + displayWidth + " x " + displayHeight);
		if(renderWidth != displayWidth || renderHeight != displayHeight) {
			ShaderMod.log("Render Resolution: " + renderWidth + " x " + renderHeight);
		}
		
		baseFramebuffer.setup(displayWidth, displayHeight);
		checkFramebufferStatus();
		
		postFramebuffer.setup(renderWidth, renderHeight);
		checkFramebufferStatus();
		
		if(enableShadowmap) {
			shadowFramebuffer.setup(shadowMapResolution, shadowMapResolution);
			checkFramebufferStatus();
		}
		
		checkError("framebuffer setup");
		
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	private void checkFramebufferStatus() {
		int framebufferStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
		if(framebufferStatus != GL_FRAMEBUFFER_COMPLETE) {
			String errorName = null;
			
			if(framebufferStatus == GL_FRAMEBUFFER_UNDEFINED) errorName = "Framebuffer Undefined";
			if(framebufferStatus == GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT) errorName = "Incomplete Attachment";
			if(framebufferStatus == GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT) errorName = "Incomplete Missing Attachment";
			if(framebufferStatus == GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER) errorName = "Incomplete Draw Buffer";
			if(framebufferStatus == GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER) errorName = "Incomplete Read Buffer";
			if(framebufferStatus == GL_FRAMEBUFFER_UNSUPPORTED) errorName = "Framebuffer Unsupported";
			if(errorName == null) errorName = String.valueOf(errorName);
			
			throw new RuntimeException("Framebuffer not complete! (" + errorName + ")");
		}
	}
	
	@Override
	public void beginRenderGame(float partialTicks) {
		checkError("pre begin render game");
		
		if(fullscreenRectVAO == 0) {
			ShaderMod.log("Create Fullscreen VAO");

			checkError("pre vao setup");
			
			byteBuffer.clear();
			byteBuffer.putShort((short) 0).putShort((short) 0);
			byteBuffer.putShort((short) 0).putShort((short) 1);
			byteBuffer.putShort((short) 1).putShort((short) 1);
			byteBuffer.putShort((short) 1).putShort((short) 0);
			byteBuffer.flip();
			
			fullscreenRectVBO = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, fullscreenRectVBO);
			glBufferData(GL_ARRAY_BUFFER, byteBuffer, GL_STATIC_DRAW);
			
			fullscreenRectVAO = glGenVertexArrays();
			glBindVertexArray(fullscreenRectVAO);
			
			glEnableClientState(GL_VERTEX_ARRAY);
			glVertexPointer(2, GL_SHORT, 4, 0);
			
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			glTexCoordPointer(2, GL_SHORT, 4, 0);
			
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
			
			checkError("vao setup");
		}
		
		if(!isSetup) {
			setup();
		}
		
		updateResolution();
		
		if(displayWidth != baseFramebuffer.width || displayHeight != baseFramebuffer.height || renderWidth != postFramebuffer.width || renderHeight != postFramebuffer.height) {
			setupFramebuffers();
		}
		
		// Handle input
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
			glViewport(0, 0, displayWidth, displayHeight);
			return;
		}
		
		// Update uniforms
		uniforms.update(partialTicks);
		
		// Setup framebuffer for rendering
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
	public boolean beforeSetupCameraTransform(float partialTicks) {
		if(!enableShaders) {
			return false;
		}
		
		if(enableShadowmap && isRenderingShadowmap) {
			// Setup shadowmap camera
			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			glOrtho(-shadowDistance, shadowDistance, -shadowDistance, shadowDistance, -shadowDistance * 3.0f, shadowDistance * 3.0f);
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();
			
			double renderPosX = mc.activeCamera.getX(partialTicks);
			double renderPosY = mc.activeCamera.getY(partialTicks);
			double renderPosZ = mc.activeCamera.getZ(partialTicks);
			
			glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);
			float sunAngle = mc.theWorld.getCelestialAngle(partialTicks);

			if(sunAngle > 0.25f && sunAngle < 0.75f) {
				sunAngle += 0.5f;
			}
			
			glRotatef(-sunAngle * 360.0f - 90.0f, 0.0f, 0.0f, 1.0f);

			glTranslated(renderPosX % 4.0, renderPosY % 4.0, renderPosZ % 4.0);
			
			MatrixHelper.getMatrix(GL_PROJECTION_MATRIX, uniforms.shadowProjectionMatrix);
			MatrixHelper.getMatrix(GL_MODELVIEW_MATRIX, uniforms.shadowModelViewMatrix);
			
			return true;
		}
		return false;
	}

	@Override
	public void afterSetupCameraTransform(float partialTicks) {
		if(!enableShaders) {
			return;
		}
		
		if(!isRenderingShadowmap) {
			// Get matrices for uniforms
			uniforms.previousProjectionMatrix.load(uniforms.projectionMatrix);
			uniforms.previousModelViewMatrix.load(uniforms.modelViewMatrix);
			
			MatrixHelper.getMatrix(GL_PROJECTION_MATRIX, uniforms.projectionMatrix);
			MatrixHelper.getMatrix(GL_MODELVIEW_MATRIX, uniforms.modelViewMatrix);
			
			Matrix4f.invert(uniforms.projectionMatrix, uniforms.projectionInverseMatrix);
			Matrix4f.invert(uniforms.modelViewMatrix, uniforms.modelViewInverseMatrix);
		}
	}
	
	@Override
	public void beginRenderWorld(float partialTicks) {
		if(!enableShaders || isRenderingShadowmap) {
			return;
		}
		
		checkError("pre begin render world");
		
		if(enableShadowmap && mc.theWorld != null && mc.thePlayer != null && !mc.theWorld.worldType.hasCeiling()) {
			// Render shadowmap
			Integer prevImmersiveMode = mc.gameSettings.immersiveMode.value;
			Boolean prevClouds = mc.gameSettings.clouds.value;
			
			try {
				mc.gameSettings.immersiveMode.value = 2;	// Hide Hand
				mc.gameSettings.clouds.value = false;
				
				isRenderingShadowmap = true;
				
				glBindFramebuffer(GL_FRAMEBUFFER, shadowFramebuffer.id);
				glViewport(0, 0, shadowMapResolution, shadowMapResolution);
				
				shadowShader.bind();
				
				mc.worldRenderer.renderWorld(partialTicks, 0L);
				
				glUseProgram(0);
				
				glBindFramebuffer(GL_FRAMEBUFFER, 0);
				glViewport(0, 0, displayWidth, displayHeight);
				
				isRenderingShadowmap = false;
			}finally {
				mc.gameSettings.immersiveMode.value = prevImmersiveMode;
				mc.gameSettings.clouds.value = prevClouds;
			}
		}
		
		if(postRenderPasses.size() > 0) {
			glBindFramebuffer(GL_FRAMEBUFFER, postFramebuffer.id);
			glViewport(0, 0, postFramebuffer.width, postFramebuffer.height);
			currentWidth = renderWidth;
			currentHeight = renderHeight;
			
			intBuffer.clear();
			
			for(int inIndex=0; inIndex < worldOutputTextures.length; inIndex++) {
				glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + inIndex, GL_TEXTURE_2D, postFramebuffer.colortex[worldOutputTextures[inIndex]], 0);
				intBuffer.put(GL_COLOR_ATTACHMENT0 + inIndex);
			}
			
			intBuffer.flip();
			
			glDrawBuffers(intBuffer);
		}

		currentShader = null;
		
		bindWorldShader(basicShader);
		
		checkError("begin render world");
	}
	
	public void bindWorldShader(Shader shader) {
		if(shader == currentShader) {
			return;
		}
		currentShader = shader;
		
		if(currentShader.bind()) {
			glUniform1i(shader.getUniform("fogMode"), fogMode);
			
			if(LightmapHelper.isLightmapEnabled()) {
				glActiveTexture(GL_TEXTURE1);
				glBindTexture(GL_TEXTURE_2D, mc.worldRenderer.lightmapHelper.lightmapTexture);
				glUniform1i(shader.getUniform("lightmap"), 1);
				glActiveTexture(GL_TEXTURE0);
			}
			
			setupCommonUniforms(shader, 0);
		}
	}

	@Override
	public void onClearWorldBuffer() {
		if(isRenderingShadowmap || !enableShaders) {
			return;
		}
		
		if(postFramebuffer.colortex != null) {
			// TODO make color configurable
			floatBuffer.clear();
			floatBuffer.put(0.0f);
			floatBuffer.put(0.0f);
			floatBuffer.put(0.0f);
			floatBuffer.put(0.0f);
			floatBuffer.flip();
			for(int i = 1; i < postFramebuffer.colortex.length; i++) {
				glClearBuffer(GL_COLOR, i, floatBuffer);	
			}
		}
	}
	
	@Override
	public void beginRenderBasic() {
		if(isRenderingShadowmap || !enableShaders) {
			return;
		}
		
		bindWorldShader(basicShader);
	}
	
	@Override
	public void beginRenderTextured() {
		if(isRenderingShadowmap || !enableShaders) {
			return;
		}

		bindWorldShader(texturedShader);
	}
	
	@Override
	public void beginRenderSkyBasic() {
		if(isRenderingShadowmap || !enableShaders) {
			return;
		}
		
		bindWorldShader(skyBasicShader);
	}
	
	@Override
	public void beginRenderSkyTextured() {
		if(isRenderingShadowmap || !enableShaders) {
			return;
		}

		bindWorldShader(skyTexturedShader);
	}

	@Override
	public void beginRenderTerrain() {
		if(isRenderingShadowmap || !enableShaders) {
			return;
		}

		bindWorldShader(terrainShader);
	}
	
	@Override
	public void beginRenderClouds() {
		if(isRenderingShadowmap || !enableShaders) {
			return;
		}

		bindWorldShader(cloudsShader);
	}
	
	@Override
	public void beginRenderEntities() {
		if(isRenderingShadowmap || !enableShaders) {
			return;
		}

		bindWorldShader(entitiesShader);
	}
	
	@Override
	public void beginRenderTranslucent() {
		if(isRenderingShadowmap || !enableShaders) {
			return;
		}

		bindWorldShader(translucentShader);
	}
	
	@Override
	public void beginRenderHand() {
		if(isRenderingShadowmap || !enableShaders) {
			return;
		}

		bindWorldShader(handShader);
	}
	
	
	@Override
	public void endRenderWorld(float partialTicks) {
		if(isRenderingShadowmap) {
			return;
		}

		glViewport(0, 0, displayWidth, displayHeight);
		
		checkError("pre end render world");
		
		glUseProgram(0);

		if(!enableShaders) {
			return;
		}

		if(postRenderPasses.size() > 0) {
			postProcessPipeline(postRenderPasses, postFramebuffer, baseRenderPasses.size() > 0 ? baseFramebuffer : null, true, 0);
		}
		
		glEnable(GL_ALPHA_TEST);
		
		checkError("end render world");
	}
	
	@Override
	public void endRenderGame(float partialTicks) {
		checkError("pre end render game");

		glViewport(0, 0, displayWidth, displayHeight);
		
		if(!enableShaders) {
			return;
		}
		
		if(baseRenderPasses.size() > 0) {
			postProcessPipeline(baseRenderPasses, baseFramebuffer, null, false, 1);
		}
		
		checkError("end render game");

		glUseProgram(0);
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glViewport(0, 0, displayWidth, displayHeight);
		
		if(showTextures) {
			showFramebufferTextures();
		}
	}
	
	public void postProcessPipeline(List<RenderPass> renderPasses, Framebuffer framebuffer, Framebuffer endFramebuffer, boolean enableDepthTex, int stage) {
		glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.id);
		glViewport(0, 0, framebuffer.width, framebuffer.height);
		currentWidth = framebuffer.width;
		currentHeight = framebuffer.height;
		
		int count = renderPasses.size();
		
		for(int renderPassIndex = 0; renderPassIndex < count; renderPassIndex++) {
			RenderPass renderPass = renderPasses.get(renderPassIndex);
			boolean last = renderPassIndex == count - 1;
			
			if(renderPass.shader.bind()) {
				// Setup input textures
				setupCommonUniforms(renderPass.shader, stage);
				
				int textureId = 0;
				
				for(int inIndex=0; inIndex < renderPass.in.length; inIndex++) {
					glActiveTexture(GL_TEXTURE0 + textureId);
					glBindTexture(GL_TEXTURE_2D, framebuffer.colortex[renderPass.in[inIndex]]);
					glUniform1i(renderPass.shader.getUniform(colortexStrings[inIndex]), textureId);
					if(framebuffer.mipmap[renderPass.in[inIndex]]) {
						glGenerateMipmap(GL_TEXTURE_2D);
					}
					textureId++;
				}
				
				if(enableDepthTex) {
					glActiveTexture(GL_TEXTURE0 + textureId);
					glBindTexture(GL_TEXTURE_2D, framebuffer.depthtex);
					glUniform1i(renderPass.shader.getUniform("depthtex0"), textureId);
					textureId++;
					
					if(enableShadowmap) {
						glActiveTexture(GL_TEXTURE0 + textureId);
						glBindTexture(GL_TEXTURE_2D, shadowFramebuffer.depthtex);
						glUniform1i(renderPass.shader.getUniform("shadowtex0"), textureId);
						textureId++;
					}
				}
				
				/*
				if(LightmapHelper.isLightmapEnabled()) {
					glActiveTexture(GL_TEXTURE0 + textureId);
					glBindTexture(GL_TEXTURE_2D, shadowFramebuffer.depthtex);
					glUniform1i(renderPass.shader.getUniform("lightmap"), textureId);
					textureId++;
				}
				*/
				
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
				if(endFramebuffer != null) {
					glBindFramebuffer(GL_FRAMEBUFFER, endFramebuffer.id);
					glViewport(0, 0, endFramebuffer.width, endFramebuffer.height);
					currentWidth = endFramebuffer.width;
					currentHeight = endFramebuffer.height;
				}else {
					glBindFramebuffer(GL_FRAMEBUFFER, 0);
					glViewport(0, 0, displayWidth, displayHeight);
					currentWidth = displayWidth;
					currentHeight = displayHeight;
				}
			}
			
			drawFramebuffer();
		}
		
		glUseProgram(0);
	}
	
	public void showFramebufferTextures() {
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_ALPHA_TEST);
		glDisable(GL_LIGHTING);
		glDisable(GL_FOG);
		glDisable(GL_BLEND);
		
		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		
		/*
		glActiveTexture(GL_TEXTURE1);
		glClientActiveTexture(GL_TEXTURE1);
		
		glDisable(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, 0);
		glMultiTexCoord2f(GL_TEXTURE1, 0.0f, 0.0f);
		
		glActiveTexture(GL_TEXTURE0);
		glClientActiveTexture(GL_TEXTURE0);
		*/
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, 1, 0, 1, -1, 1);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		
		int pos = 0;
		if(baseFramebuffer.colortex != null) {
			showFramebufferTextures(baseFramebuffer, pos++);	
		}
		if(postFramebuffer.colortex != null) {
			showFramebufferTextures(postFramebuffer, pos++);
		}
		if(shadowFramebuffer.colortex != null) {
			showFramebufferTextures(shadowFramebuffer, pos++);
		}
	}
	
	public void showFramebufferTextures(Framebuffer framebuffer, int pos) {
		// Debug
		glPushMatrix();
		glScaled(0.14, 0.14, 1.0);
		glTranslated(0.1, 0.1, 0.0);
		if(pos > 0) {
			glTranslated(0.0, pos * 1.1, 0.0);	
		}
		
		for(int i=0; i < framebuffer.colortex.length + 1; i++) {
			int tex = i >= framebuffer.colortex.length ? framebuffer.depthtex : framebuffer.colortex[i];
			glBindTexture(GL_TEXTURE_2D, tex);

			double pad = 0.02;
			glDisable(GL_TEXTURE_2D);
			glBegin(GL_QUADS);
			glVertex2d(-pad, -pad);
			glVertex2d(1.0 + pad, -pad);
			glVertex2d(1.0 + pad, 1.0 + pad);
			glVertex2d(-pad, 1.0 + pad);
			glEnd();

			glEnable(GL_TEXTURE_2D);
			glBegin(GL_QUADS);
			
			glTexCoord2d(0.0, 0.0);
			glVertex2d(0.0, 0.0);
			
			glTexCoord2d(1.0, 0.0);
			glVertex2d(1.0, 0.0);
			
			glTexCoord2d(1.0, 1.0);
			glVertex2d(1.0, 1.0);
			
			glTexCoord2d(0.0, 1.0);
			glVertex2d(0.0, 1.0);
			glEnd();
			
			glTranslated(1.1, 0.0, 0.0);
		}
		
		glPopMatrix();
	}
	
	private void setupCommonUniforms(Shader shader, int stage) {
		checkError("pre uniforms");
		
		uniforms.apply(shader, stage);
		
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
		glBindVertexArray(fullscreenRectVAO);
		glDrawArrays(GL_QUADS, 0, 4);
		glBindVertexArray(0);
	}
	
	@Override
	public void delete() {
		shadowFramebuffer.delete();
		shadowShader.delete();
		
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

		basicShader.delete();
		texturedShader.delete();
		skyBasicShader.delete();
		skyTexturedShader.delete();
		terrainShader.delete();
		entitiesShader.delete();
		cloudsShader.delete();
		translucentShader.delete();
		handShader.delete();
		
		enableShadowmap = false;
	}
	
	static class Framebuffer {
		
		int id;
		int[] colortex;
		int depthtex;
		boolean[] mipmap;
		
		int width;
		int height;
		
		void create(int colorTextures, boolean[] mipmap) {
			id = glGenFramebuffers();
			
			colortex = new int[colorTextures];
			for(int i=0; i < colortex.length; i++) {
				colortex[i] = GLAllocation.generateTexture();
			}
			
			depthtex = GLAllocation.generateTexture();
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
			
			this.width = width;
			this.height = height;
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
	
	public static void resetGlErrors() {
		try {
			Field errorStatesField = OpenGLHelper.class.getDeclaredField("errorStates");
			errorStatesField.setAccessible(true);
			Set<?> errorStates = (Set<?>) errorStatesField.get(null);
			errorStates.clear();
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setID(float id) {
		attributeID.value = id;
	}

	@Override
	public void setIsTopVertex(float topVertex) {
		attributeTopVertex.value = topVertex;
	}

}
