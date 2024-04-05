package b100.shaders;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.util.vector.Matrix4f;

import java.nio.ByteOrder;

public class MatrixHelper {
	
	private static FloatBuffer matrixBuffer = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer();
	
	public static void getMatrix(int type, Matrix4f matrix) {
		matrixBuffer.position(0).limit(16);
		glGetFloat(type, matrixBuffer);
		matrixBuffer.position(0);
		matrix.load(matrixBuffer);
	}
	
	public static void uniformMatrix(int location, Matrix4f matrix) {
		matrixBuffer.position(0).limit(16);
		matrix.store(matrixBuffer);
		matrixBuffer.position(0);
		glUniformMatrix4(location, false, matrixBuffer);
	}

}
