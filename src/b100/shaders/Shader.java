package b100.shaders;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import b100.natrium.vertex.VertexAttribute;

public class Shader {
	
	private int shaderProgram;
	
	private int vertexShader;
	private int fragmentShader;
	
	private Map<String, Integer> uniforms = new HashMap<>();

	public boolean setupShader(String name) {
		return setupShader(name, null);
	}
	
	public boolean setupShader(String name, List<VertexAttribute> attribs) {
		delete();
		
		String vertexShaderSource = getShaderSource(name + ".vsh");
		String fragmentShaderSource = getShaderSource(name + ".fsh");
		
		if(vertexShaderSource == null || fragmentShaderSource == null) {
			return false;
		}
		
		vertexShader = glCreateShader(GL_VERTEX_SHADER);
		fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		
		glShaderSource(vertexShader, vertexShaderSource);
		glShaderSource(fragmentShader, fragmentShaderSource);
		
		glCompileShader(vertexShader);
		glCompileShader(fragmentShader);
		
		int vertexShaderStatus = glGetShaderi(vertexShader, GL_COMPILE_STATUS);
		int fragmentShaderStatus = glGetShaderi(fragmentShader, GL_COMPILE_STATUS);
		
		if(vertexShaderStatus != GL_TRUE || fragmentShaderStatus != GL_TRUE) {
			StringBuilder msg = new StringBuilder("Could not compile shader '"+name+"':\n");
			
			if(vertexShaderStatus != GL_TRUE) {
				msg.append("Vertex Shader Info Log:\n");
				msg.append(glGetShaderInfoLog(vertexShader, GL_INFO_LOG_LENGTH));
			}
			if(fragmentShaderStatus != GL_TRUE) {
				msg.append("Fragment Shader Info Log:\n");
				msg.append(glGetShaderInfoLog(fragmentShader, GL_INFO_LOG_LENGTH));
			}
			
			System.err.println(msg.toString());
			
			deleteShaders();
			
			return false;
		}
		
		shaderProgram = glCreateProgram();
		
		glAttachShader(shaderProgram, vertexShader);
		glAttachShader(shaderProgram, fragmentShader);
		
		if(attribs != null) {
			for(int i=0; i < attribs.size(); i++) {
				VertexAttribute attrib = attribs.get(i);
				
				glBindAttribLocation(shaderProgram, attrib.id, attrib.name);	
			}
		}
		
		glLinkProgram(shaderProgram);
		
		deleteShaders();
		
		return true;
	}
	
	private void deleteShaders() {
		glDeleteShader(vertexShader);
		glDeleteShader(fragmentShader);
		
		vertexShader = 0;
		fragmentShader = 0;
	}
	
	public String getShaderSource(String name) {
		File shaderFile = new File(ShaderMod.getCurrentShaderPackDirectory(), name);
		if(!shaderFile.exists()) {
			return null;
		}
		
		BufferedReader br = null;
		StringBuilder string = new StringBuilder();
		
		try {
			br = new BufferedReader(new FileReader(shaderFile));
			
			while(true) {
				String line = br.readLine();
				if(line == null) {
					break;
				}
				string.append(line).append("\n");
			}
			
			return string.toString();
		}catch (Exception e) {
			System.err.println("Error reading shader file '" + shaderFile.getAbsolutePath() + "'!");
			e.printStackTrace();
			return null;
		}finally {
			try {
				br.close();
			}catch (Exception e) {}
		}
	}
	
	public boolean bind() {
		glUseProgram(shaderProgram);
		return shaderProgram != 0;
	}
	
	public boolean isGenerated() {
		return shaderProgram != 0;
	}
	
	public int getUniform(String name) {
		if(shaderProgram == 0) {
			throw new NullPointerException("Shader is not generated!");
		}
		Integer uniform = uniforms.get(name);
		if(uniform != null) {
			return uniform;
		}
		int newUniform = glGetUniformLocation(shaderProgram, name);
		uniforms.put(name, newUniform);
		return newUniform;
	}
	
	public boolean delete() {
		if(isGenerated()) {
			glDeleteProgram(shaderProgram);
			shaderProgram = 0;
			
			uniforms.clear();
			
			return true;
		}
		return false;
	}
	
	public int getProgramId() {
		return shaderProgram;
	}

}
