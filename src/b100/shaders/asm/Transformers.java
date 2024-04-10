package b100.shaders.asm;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import b100.asmloader.ClassTransformer;
import b100.shaders.asm.utils.ASMHelper;
import b100.shaders.asm.utils.FindInstruction;
import b100.shaders.asm.utils.InjectHelper;

public class Transformers {
	
	private static final String listenerClass = "b100/shaders/asm/Listeners";
	private static final String callbackInfoClass = "b100/shaders/asm/utils/CallbackInfo";
	
	private static final InjectHelper injectHelper = new InjectHelper(listenerClass, callbackInfoClass);
	
	class MinecraftTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/Minecraft");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode setRenderer = ASMHelper.findMethod(classNode, "setRenderer", null);
			InsnList insert = injectHelper.createMethodCallInject(classNode, setRenderer, "onSetRenderer");
			setRenderer.instructions.insertBefore(setRenderer.instructions.getFirst(), insert);
		}
		
	}
	
	class WorldRendererTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/render/WorldRenderer");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			makePublic(ASMHelper.findField(classNode, "mc"));
			makePublic(ASMHelper.findField(classNode, "fogManager"));
			
			MethodNode setupCameraTransform = ASMHelper.findMethod(classNode, "setupCameraTransform");
			MethodNode renderRainSnow = ASMHelper.findMethod(classNode, "renderRainSnow");
			MethodNode renderWorld = ASMHelper.findMethod(classNode, "renderWorld");
			
			ASMHelper.findAllInstructions(setupCameraTransform.instructions, (n) -> n.getOpcode() == Opcodes.RETURN).forEach((returnNode) -> {
				setupCameraTransform.instructions.insertBefore(returnNode, injectHelper.createMethodCallInject(classNode, setupCameraTransform, "afterSetupCameraTransform"));
			});
			
			{
				InsnList insert = injectHelper.createMethodCallInject(classNode, setupCameraTransform, "beforeSetupCameraTransform");
				setupCameraTransform.instructions.insertBefore(setupCameraTransform.instructions.getFirst(), insert);	
			}
			
			{

				InsnList insert = injectHelper.createMethodCallInject(classNode, renderRainSnow, "renderRainSnowCancel");
				renderRainSnow.instructions.insertBefore(renderRainSnow.instructions.getFirst(), insert);	
			}
			
			{
				AbstractInsnNode firstSortAndRenderNode = ASMHelper.findInstruction(renderWorld, false, (n) -> FindInstruction.methodInsn(n, "sortAndRender"));
				if(firstSortAndRenderNode == null) {
					firstSortAndRenderNode = ASMHelper.findInstruction(renderWorld, false, (n) -> FindInstruction.methodInsn(n, "b100/natrium/asm/Listeners", "onSortAndRender", null));
				}
				renderWorld.instructions.insertBefore(firstSortAndRenderNode, new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "beginRenderTerrain", "()V"));
			}
			
			{
				renderWorld.instructions.insert(ASMHelper.findInstruction(renderWorld, false, (n) -> FindInstruction.methodInsn(n, "glClear")), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "onClearWorldBuffer", "()V"));
			}
		}
		
	}
	
	class FogManagerTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/render/FogManager");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			makePublic(ASMHelper.findField(classNode, "fogRed"));
			makePublic(ASMHelper.findField(classNode, "fogGreen"));
			makePublic(ASMHelper.findField(classNode, "fogBlue"));
			
			MethodNode setupFog = ASMHelper.findMethod(classNode, "setupFog");
			
			List<AbstractInsnNode> nodes = ASMHelper.findAllInstructions(setupFog.instructions, (n) -> FindInstruction.methodInsn(n, "glFogi"));
			
			for(int i=0; i < nodes.size(); i++) {
				ASMHelper.replaceInstruction(setupFog, nodes.get(i), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "setFogMode", "(II)V"));
			}
		}
		
	}
	
	class EntityCameraTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/render/camera/EntityCamera");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode showPlayer = ASMHelper.findMethod(classNode, "showPlayer");
			InsnList insert = injectHelper.createMethodCallInject(classNode, showPlayer, "showPlayerOverride");
			showPlayer.instructions.insertBefore(showPlayer.instructions.getFirst(), insert);
		}
		
	}
	
	class RenderGlobalTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/render/RenderGlobal");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode updateRenderers = ASMHelper.findMethod(classNode, "updateRenderers");
			MethodNode drawSky = ASMHelper.findMethod(classNode, "drawSky");
			
			{
				InsnList insert = injectHelper.createMethodCallInject(classNode, updateRenderers, "updateRenderersCancel");
				updateRenderers.instructions.insertBefore(updateRenderers.instructions.getFirst(), insert);	
			}
			
			{
				drawSky.instructions.insertBefore(drawSky.instructions.getFirst(), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "beginRenderSkyBasic", "()V"));
			}
			
			List<AbstractInsnNode> glEnableTexture = ASMHelper.findAllInstructions(drawSky.instructions, (n) -> FindInstruction.methodInsn(n, "glEnable") && FindInstruction.intInsn(n.getPrevious(), GL11.GL_TEXTURE_2D));
			List<AbstractInsnNode> glDisableTexture = ASMHelper.findAllInstructions(drawSky.instructions, (n) -> FindInstruction.methodInsn(n, "glDisable") && FindInstruction.intInsn(n.getPrevious(), GL11.GL_TEXTURE_2D));
			
			drawSky.instructions.insert(glEnableTexture.get(0), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "beginRenderSkyTextured", "()V"));
			drawSky.instructions.insert(glDisableTexture.get(2), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "beginRenderSkyBasic", "()V"));
		}
		
	}
	
	public static void makePublic(FieldNode field) {
		field.access = (field.access & ~Opcodes.ACC_PRIVATE) | Opcodes.ACC_PUBLIC;
	}

}
