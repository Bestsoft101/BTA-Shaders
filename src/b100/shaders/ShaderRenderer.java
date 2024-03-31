package b100.shaders;

import static b100.shaders.OpenGLHelper.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Renderer;

public class ShaderRenderer extends Renderer {
	
	private List<Shader> postShaders;
	private List<Shader> baseShaders;
	
	private boolean isSetup = false;
	
	private final Framebuffer gameFramebuffer = new Framebuffer();
	private final Framebuffer worldFramebuffer = new Framebuffer();
	
	private int fullscreenRectList = 0;
	
	private int framebufferWidth = -1;
	private int framebufferHeight = -1;
	
	private IntBuffer intBuffer = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asIntBuffer();

	private boolean enableShaders = true;
	
	private boolean pressedReloadLast = false;
	private boolean pressedToggleLast = false;
	
	public ShaderRenderer(Minecraft mc) {
		super(mc);
		
		ShaderMod.log("Shader Directory: " + ShaderMod.getShaderDirectory());
	}
	
	private void setup() {
		isSetup = true;
		
		checkError("pre setup");
		
		if(postShaders != null) {
			for(int i=0; i < postShaders.size(); i++) {
				postShaders.get(i).delete();
			}
		}
		if(baseShaders != null) {
			for(int i=0; i < baseShaders.size(); i++) {
				baseShaders.get(i).delete();
			}
		}
		gameFramebuffer.delete();
		worldFramebuffer.delete();

		checkError("delete");
		
		ShaderMod.log("Shader setup!");
		
		String[] postNames = new String[0];
		String[] baseNames = new String[] {"composite", "final"};
		
		postShaders = new ArrayList<>();
		baseShaders = new ArrayList<>();

		for(int i=0; i < postNames.length; i++) {
			Shader shader = new Shader();
			String name = postNames[i];
			boolean success = shader.setupShader(name);
			ShaderMod.log(name + ": " + success);
			postShaders.add(shader);
		}
		
		for(int i=0; i < baseNames.length; i++) {
			Shader shader = new Shader();
			String name = baseNames[i];
			boolean success = shader.setupShader(name);
			ShaderMod.log(name + ": " + success);
			baseShaders.add(shader);
		}
		
		gameFramebuffer.create(3);
		worldFramebuffer.create(1);
		
		setupFramebuffers();
		
		checkError("setup");
	}
	
	private void setupFramebuffers() {
		int width = Display.getWidth();
		int height = Display.getHeight();
		
		ShaderMod.log("Framebuffer Size: " + width + " x " + height);
		
		this.framebufferWidth = width;
		this.framebufferHeight = height;
		
		gameFramebuffer.setup(width, height);
		checkFramebufferStatus();
		
		worldFramebuffer.setup(width, height);
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
		
		if(!enableShaders) {
			return;
		}
		
		if(baseShaders.size() > 0) {
			glBindFramebuffer(GL_FRAMEBUFFER, gameFramebuffer.id);
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, gameFramebuffer.colortex[0], 0);	
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, 0, 0);
			glDrawBuffers(GL_COLOR_ATTACHMENT0);
		}
		
		checkError("begin render game");
	}
	
	@Override
	public void beginRenderWorld(float partialTicks) {
		checkError("pre begin render world");

		if(!enableShaders) {
			return;
		}
		
		checkError("begin render world");
	}
	
	@Override
	public void endRenderWorld(float partialTicks) {
		checkError("pre end render world");

		if(!enableShaders) {
			return;
		}
		
		checkError("end render world");
	}
	
	@Override
	public void endRenderGame(float partialTicks) {
		checkError("pre end render game");

		if(!enableShaders) {
			return;
		}
		
		if(baseShaders.size() > 0) {
			// Pass 1
			// Render Texture [0] into [1, 2]
			
			glBindFramebuffer(GL_FRAMEBUFFER, gameFramebuffer.id);
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, gameFramebuffer.colortex[1], 0);
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, gameFramebuffer.colortex[2], 0);
			intBuffer.position(0).limit(2);
			intBuffer.put(GL_COLOR_ATTACHMENT0);
			intBuffer.put(GL_COLOR_ATTACHMENT1);
			intBuffer.position(0);
			glDrawBuffers(intBuffer);
			
			Shader shaderComposite = baseShaders.get(0);
			if(shaderComposite.bind()) {
				setupCommonUniforms(shaderComposite);

				glActiveTexture(GL_TEXTURE0);
				glBindTexture(GL_TEXTURE_2D, gameFramebuffer.colortex[0]);
				glGenerateMipmap(GL_TEXTURE_2D);
				glUniform1i(shaderComposite.getUniform("colortex0"), 0);
			}else {
				glBindTexture(GL_TEXTURE_2D, gameFramebuffer.colortex[0]);
			}
			drawFramebuffer();
			
			glBindFramebuffer(GL_FRAMEBUFFER, 0);
			
			// Pass 2
			// Render Textures [1, 2] into [0]
			
			Shader shaderFinal = baseShaders.get(1);
			if(shaderFinal.bind()) {
				setupCommonUniforms(shaderFinal);

				glActiveTexture(GL_TEXTURE0);
				glBindTexture(GL_TEXTURE_2D, gameFramebuffer.colortex[1]);
				glUniform1i(shaderFinal.getUniform("colortex0"), 0);
				
				glActiveTexture(GL_TEXTURE1);
				glBindTexture(GL_TEXTURE_2D, gameFramebuffer.colortex[2]);
				glUniform1i(shaderFinal.getUniform("colortex1"), 1);
				
				glActiveTexture(GL_TEXTURE0);
			}else {
				glBindTexture(GL_TEXTURE_2D, gameFramebuffer.colortex[1]);
			}
			
			glBindFramebuffer(GL_FRAMEBUFFER, 0);
			drawFramebuffer();
			
			glUseProgram(0);
		}
		
		checkError("end render game");
	}
	
	private void setupCommonUniforms(Shader shader) {
		checkError("pre uniforms");
		
		glUniform1f(shader.getUniform("viewWidth"), Display.getWidth());
		glUniform1f(shader.getUniform("viewHeight"), Display.getHeight());
		
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
	
	public void destroy() {
		
	}
	
	static class Framebuffer {
		
		int id;
		int[] colortex;
		int depthtex;
		
		void create(int colorTextures) {
			id = glGenFramebuffers();
			
			colortex = new int[colorTextures];
			for(int i=0; i < colortex.length; i++) {
				colortex[i] = glGenTextures();
			}
			
			depthtex = glGenTextures();
		}
		
		void setup(int width, int height) {
			glBindFramebuffer(GL_FRAMEBUFFER, id);
			
			for(int i=0; i < colortex.length; i++) {
				boolean mipmap = i == 0;
				
				glBindTexture(GL_TEXTURE_2D, colortex[i]);
				glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
				if(mipmap) glGenerateMipmap(GL_TEXTURE_2D);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, mipmap ? GL_LINEAR_MIPMAP_LINEAR : GL_LINEAR);
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

}
