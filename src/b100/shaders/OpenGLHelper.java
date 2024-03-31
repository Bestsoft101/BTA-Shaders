package b100.shaders;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL11.*;

import java.util.HashSet;
import java.util.Set;

public class OpenGLHelper {
	
	private static Set<String> errorStates = new HashSet<>();
	
	public static void checkError(String state) {
		int err = glGetError();
		if(err == GL_NO_ERROR) {
			return;
		}
		if(errorStates.contains(state)) {
			return;
		}
		
		errorStates.add(state);
		String errorName = getErrorName(err);
		if(errorName == null) {
			errorName = "Unknown Error";
		}
		
		System.err.println("#########################");
		System.err.println("OpenGL Error "+err+" (" + errorName + "): " + state);
		Thread.dumpStack();
		System.err.println("#########################");
	}
	
	public static String getErrorName(int err) {
		if(err == GL_INVALID_VALUE) return "Invalid Value";
		if(err == GL_INVALID_ENUM) return "Invalid Enum";
		if(err == GL_INVALID_OPERATION) return "Invalid Operation";
		if(err == GL_INVALID_FRAMEBUFFER_OPERATION) return "Invalid Framebuffer Operation";
		if(err == GL_OUT_OF_MEMORY) return "Out of Memory";
		if(err == GL_STACK_UNDERFLOW) return "Stack Underflow";
		if(err == GL_STACK_OVERFLOW) return "Stack Overflow";
		return null;
	}
}
