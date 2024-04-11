package b100.shaders.asm;

import java.lang.reflect.Modifier;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import b100.asmloader.ClassTransformer;
import b100.shaders.asm.utils.ASMHelper;
import b100.shaders.asm.utils.FindInstruction;
import b100.shaders.asm.utils.InjectHelper;
import b100.utils.InvalidCharacterException;
import b100.utils.StringReader;

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
			MethodNode setupPlayerCamera = ASMHelper.findMethod(classNode, "setupPlayerCamera");
			
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
			
			{
				AbstractInsnNode water = ASMHelper.findInstruction(renderWorld, false, (n) -> FindInstruction.ldcInsnS(n, "water"));
				renderWorld.instructions.insert(water.getNext(), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "beginRenderTranslucent", "()V"));
				
				AbstractInsnNode clouds = ASMHelper.findInstruction(renderWorld, false, (n) -> FindInstruction.ldcInsnS(n, "clouds"));
				renderWorld.instructions.insert(clouds.getNext(), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "beginRenderClouds", "()V"));
				
				AbstractInsnNode entities = ASMHelper.findInstruction(renderWorld, false, (n) -> FindInstruction.ldcInsnS(n, "entities"));
				renderWorld.instructions.insert(entities.getNext(), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "beginRenderEntities", "()V"));
				
				AbstractInsnNode particles = ASMHelper.findInstruction(renderWorld, false, (n) -> FindInstruction.ldcInsnS(n, "particles"));
				renderWorld.instructions.insert(particles.getNext(), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "beginRenderTextured", "()V"));
			}
			
			{
				setupPlayerCamera.instructions.insertBefore(setupPlayerCamera.instructions.getFirst(), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "beginRenderHand", "()V"));
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
	
	class EntityRendererTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/render/entity/EntityRenderer");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode renderShadow = ASMHelper.findMethod(classNode, "renderShadow");
			InsnList insert = createMethodCancel(classNode, renderShadow, "beforeRenderShadow");
			renderShadow.instructions.insertBefore(renderShadow.instructions.getFirst(), insert);
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
			
			MethodNode drawSelectionBox = ASMHelper.findMethod(classNode, "drawSelectionBox");
			MethodNode drawDebugEntityOutlines = ASMHelper.findMethod(classNode, "drawDebugEntityOutlines");
			MethodNode drawDebugChunkBorders = ASMHelper.findMethod(classNode, "drawDebugChunkBorders");

			drawSelectionBox.instructions.insertBefore(drawSelectionBox.instructions.getFirst(), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "beginRenderBasic", "()V"));
			drawDebugEntityOutlines.instructions.insertBefore(drawDebugEntityOutlines.instructions.getFirst(), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "beginRenderBasic", "()V"));
			drawDebugChunkBorders.instructions.insertBefore(drawDebugChunkBorders.instructions.getFirst(), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "beginRenderBasic", "()V"));
		}
	}
	
	public static void makePublic(FieldNode field) {
		field.access = (field.access & ~Opcodes.ACC_PRIVATE) | Opcodes.ACC_PUBLIC;
	}
	
	public static InsnList createMethodCancel(ClassNode classNode, MethodNode method, String listenerMethodName) {
		// TODO Add suppoort for non-void methods
		
		InsnList insert = new InsnList();
		LabelNode labelAfterReturn = new LabelNode();
		
		// Create descriptor of listener method for method call
		StringBuilder listenerMethodDesc = new StringBuilder();
		listenerMethodDesc.append('(');
		if(!Modifier.isStatic(method.access)) {
			listenerMethodDesc.append('L');
			listenerMethodDesc.append(classNode.name);
			listenerMethodDesc.append(';');
		}
		listenerMethodDesc.append(method.desc.substring(1, method.desc.indexOf(')')));
		listenerMethodDesc.append(")Z");

		// Call listener method
		int parameterIndex = 0;
		if(!Modifier.isStatic(method.access)) {
			insert.add(new VarInsnNode(Opcodes.ALOAD, parameterIndex++));
		}
		
		char returnType;
		String returnClass = null;
		try {
			StringReader reader = new StringReader(method.desc);
			reader.expectAndSkip('(');
			
			while(true) {
				char c = reader.getAndSkip();
				if(c == ')') {
					returnType = reader.getAndSkip();
					if(returnType == 'L') {
						returnClass = reader.readUntilCharacter(';');
					}
					break;
				}
				
				// TODO add all possible types
				int opcode;
				if(c == 'I' || c == 'Z') {
					opcode = Opcodes.ILOAD;
				}else if(c == 'F') {
					opcode = Opcodes.FLOAD;
				}else if(c == 'D') {
					opcode = Opcodes.DLOAD;
				}else if(c == 'L') {
					opcode = Opcodes.ALOAD;
					reader.readUntilCharacter(';');
					reader.next();
				}else {
					throw new InvalidCharacterException(reader);
				}
				
				insert.add(new VarInsnNode(opcode, parameterIndex++));
				if(opcode == Opcodes.DLOAD) {
					parameterIndex++;
				}
			}
		}catch (RuntimeException e) {
			throw new RuntimeException("Invalid method descriptor '" + method.desc + "' ?", e);
		}
		
		insert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, listenerMethodName, listenerMethodDesc.toString()));
		insert.add(new JumpInsnNode(Opcodes.IFEQ, labelAfterReturn)); // if 0 jump
		insert.add(new InsnNode(Opcodes.RETURN));
		insert.add(labelAfterReturn);

		return insert;
	}

}
