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
			MethodNode startGame = ASMHelper.findMethod(classNode, "startGame");
			MethodNode setRenderer = ASMHelper.findMethod(classNode, "setRenderer", null);
			MethodNode checkBoundInputs = ASMHelper.findMethod(classNode, "checkBoundInputs");
			
			{
				InsnList insert = injectHelper.createMethodCallInject(classNode, setRenderer, "onSetRenderer");
				setRenderer.instructions.insertBefore(setRenderer.instructions.getFirst(), insert);	
			}
			{
				InsnList insert = new InsnList();
				insert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "beforeGameStart", "()V"));
				startGame.instructions.insertBefore(startGame.instructions.getFirst(), insert);
			}
			{
				InsnList insert = injectHelper.createMethodCallInject(classNode, checkBoundInputs, "checkBoundInputs");
				checkBoundInputs.instructions.insertBefore(checkBoundInputs.instructions.getFirst(), insert);	
			}
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
			MethodNode renderHand = ASMHelper.findMethod(classNode, "renderHand");
			
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
				AbstractInsnNode clear = ASMHelper.findInstruction(renderWorld, false, (n) -> FindInstruction.methodInsn(n, "glClear"));
				b100.natrium.asm.Transformers.removeMethodCall(renderWorld.instructions, clear);
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
				renderHand.instructions.insertBefore(renderHand.instructions.getFirst(), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "beginRenderHand", "()V"));
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
	
	class ChunkRendererTransformer extends ClassTransformer {
		
		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/render/ChunkRenderer");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode updateRenderer = ASMHelper.findMethod(classNode, "updateRenderer", "()V");
			
			AbstractInsnNode render = ASMHelper.findInstruction(updateRenderer, false, (n) -> FindInstruction.methodInsn(n, "net/minecraft/client/render/block/model/BlockModel", "render", "(Lnet/minecraft/client/render/tessellator/Tessellator;III)Z"));
			InsnList insert = new InsnList();
			insert.add(new VarInsnNode(Opcodes.ALOAD, 0));  // this
			insert.add(new VarInsnNode(Opcodes.ALOAD, 19)); // block
			insert.add(new VarInsnNode(Opcodes.ILOAD, 17)); // x
			insert.add(new VarInsnNode(Opcodes.ILOAD, 15)); // y
			insert.add(new VarInsnNode(Opcodes.ILOAD, 16)); // z
			insert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "beforeRenderBlock", "(Lnet/minecraft/client/render/ChunkRenderer;Lnet/minecraft/core/block/Block;III)V"));
			updateRenderer.instructions.insertBefore(render, insert);
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
			MethodNode renderAurora = ASMHelper.findMethod(classNode, "renderAurora");
			MethodNode clipRenderersByFrustum = ASMHelper.findMethod(classNode, "clipRenderersByFrustum");
			
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
			drawSky.instructions.insert(glDisableTexture.get(2), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "updateCelestialPosition", "()V"));
			
			MethodNode drawSelectionBox = ASMHelper.findMethod(classNode, "drawSelectionBox");
			MethodNode drawDebugEntityOutlines = ASMHelper.findMethod(classNode, "drawDebugEntityOutlines");
			MethodNode drawDebugChunkBorders = ASMHelper.findMethod(classNode, "drawDebugChunkBorders");

			drawSelectionBox.instructions.insertBefore(drawSelectionBox.instructions.getFirst(), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "beginRenderBasic", "()V"));
			drawDebugEntityOutlines.instructions.insertBefore(drawDebugEntityOutlines.instructions.getFirst(), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "beginRenderBasic", "()V"));
			drawDebugChunkBorders.instructions.insertBefore(drawDebugChunkBorders.instructions.getFirst(), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "beginRenderBasic", "()V"));
			
			renderAurora.instructions.insertBefore(renderAurora.instructions.getFirst(), createMethodCancel(classNode, renderAurora, "beginRenderAurora"));
			
			{
				AbstractInsnNode glPushMatrix = ASMHelper.findAllInstructions(drawSky.instructions, (n) -> FindInstruction.methodInsn(n, "glPushMatrix")).get(1);
				AbstractInsnNode glTranslatef = ASMHelper.findInstruction(glPushMatrix, false, (n) -> FindInstruction.methodInsn(n, "glTranslatef"));
				drawSky.instructions.insert(glTranslatef, new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "setSunPathRotation", "()V"));
			}
			
			{
				InsnList insert = createMethodCancel(classNode, clipRenderersByFrustum, "cancelFrustumCulling");
				clipRenderersByFrustum.instructions.insertBefore(clipRenderersByFrustum.instructions.getFirst(), insert);
			}
		}
	}
	
	class RenderBlocksTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/render/block/model/BlockModelCrossedSquares") || className.equals("net/minecraft/client/render/block/model/BlockModelCropsWheat");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			int pattern = 0b1001100110011001;
			String[] methods = new String[] {"render"};
			
			for(int j=0; j < methods.length; j++) {
				MethodNode meth = ASMHelper.findMethod(classNode, methods[j]);
				List<AbstractInsnNode> nodes = ASMHelper.findAllInstructions(meth.instructions, (n) -> FindInstruction.methodInsn(n, "addVertexWithUV"));
				for(int i=0; i < nodes.size(); i++) {
					AbstractInsnNode n = nodes.get(i);
					
					InsnList insert = new InsnList();
					if(((pattern >> (i & 15)) & 1) > 0) {
						insert.add(new InsnNode(Opcodes.FCONST_1));
					}else {
						insert.add(new InsnNode(Opcodes.FCONST_0));
					}
					insert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "setIsTopVertex", "(F)V"));
					
					meth.instructions.insertBefore(n, insert);
				}
			}
		}
	}
	
	class LightmapHelperTransformer extends ClassTransformer {
		
		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/render/LightmapHelper");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			makePublic(ASMHelper.findField(classNode, "lightmapTexture"));
		}
	}
	
	class OptionsPagesTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/gui/options/data/OptionsPages");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode staticInit = ASMHelper.findMethod(classNode, "<clinit>");
			ASMHelper.findAllInstructions(staticInit, (n) -> n.getOpcode() == Opcodes.RETURN).forEach((returnNode) -> {
				staticInit.instructions.insertBefore(returnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "onInitGui", "()V"));	
			});;
		}
		
	}
	
	class GuiOptionsTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/gui/options/GuiOptions");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			makePublic(ASMHelper.findField(classNode, "selectedPage"));
			
			MethodNode mouseClicked = ASMHelper.findMethod(classNode, "mouseClicked");
			
			AbstractInsnNode refreshIfReady = ASMHelper.findInstruction(mouseClicked, false, (n) -> FindInstruction.methodInsn(n, "refreshIfReady"));
			InsnList insert = new InsnList();
			insert.add(new VarInsnNode(Opcodes.ALOAD, 0));
			insert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "onClickOptionsPage", "(Lnet/minecraft/client/gui/options/GuiOptions;)V"));
			mouseClicked.instructions.insert(refreshIfReady, insert);
		}
		
	}
	
	class GuiScreenTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/gui/GuiScreen");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			classNode.interfaces.add("b100/shaders/gui/IGuiScreen");
			
			MethodNode handleInput = ASMHelper.findMethod(classNode, "handleInput");

			AbstractInsnNode mouseNext = ASMHelper.findInstruction(handleInput, false, (n) -> FindInstruction.methodInsn(n, "org/lwjgl/input/Mouse", "next", "()Z"));
			AbstractInsnNode keyboardNext = ASMHelper.findInstruction(handleInput, false, (n) -> FindInstruction.methodInsn(n, "org/lwjgl/input/Keyboard", "next", "()Z"));

			// The label before Mouse.next()
			LabelNode labelBeforeMouseNext = (LabelNode) ASMHelper.findInstruction(mouseNext, true, (n) -> n instanceof LabelNode);
			// The label before Keyboard.next()
			LabelNode labelBeforeKeyboardNext = (LabelNode) ASMHelper.findInstruction(keyboardNext, true, (n) -> n instanceof LabelNode);
			
			// The first label after Mouse.next(), insert before this node
			AbstractInsnNode labelAfterMouseNext = ASMHelper.findInstruction(mouseNext, false, (n) -> n instanceof LabelNode);
			// The first label after Keyboard.next(), insert before this node
			AbstractInsnNode labelAfterKeyboardNext = ASMHelper.findInstruction(keyboardNext, false, (n) -> n instanceof LabelNode);

			InsnList insert = new InsnList();
			insert.add(new LabelNode());
			insert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "checkGlobalMouseInput", "()Z"));
			insert.add(new JumpInsnNode(Opcodes.IFNE, labelBeforeMouseNext)); // If checkGlobalMouseInput returns true, jump to Mouse.next()
			handleInput.instructions.insertBefore(labelAfterMouseNext, insert);
			
			InsnList insert1 = new InsnList();
			insert1.add(new LabelNode());
			insert1.add(new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "checkGlobalKeyboardInput", "()Z"));
			insert1.add(new JumpInsnNode(Opcodes.IFNE, labelBeforeKeyboardNext)); // If checkGlobalKeyboardInput returns true, jump to Keyboard.next()
			handleInput.instructions.insertBefore(labelAfterKeyboardNext, insert1);
		}
		
	}
	
	class I18nTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/core/lang/I18n");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode reload = ASMHelper.findMethod(classNode, "reload", "(Ljava/lang/String;Z)V");
			
			AbstractInsnNode returnNode = ASMHelper.findInstruction(reload, true, (n) -> n.getOpcode() == Opcodes.RETURN);
			
			InsnList insert = new InsnList();
			insert.add(new VarInsnNode(Opcodes.ALOAD, 0));
			insert.add(new VarInsnNode(Opcodes.ALOAD, 1));
			insert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "onReloadLanguages", "(Lnet/minecraft/core/lang/I18n;Ljava/lang/String;)V"));
			
			reload.instructions.insertBefore(returnNode, insert);
		}
	}
	
	class RenderEngineTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/render/RenderEngine");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode refreshTextures = ASMHelper.findMethod(classNode, "refreshTextures");
			
			InsnList insert = new InsnList();
			insert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "onRefreshTextures", "()V"));
			
			ASMHelper.insertBeforeLastReturn(refreshTextures, insert);
		}
		
	}
	
	public static void makePublic(FieldNode field) {
		field.access = (field.access & ~Opcodes.ACC_PRIVATE) | Opcodes.ACC_PUBLIC;
	}
	
	public static InsnList createMethodCancel(ClassNode classNode, MethodNode method, String listenerMethodName) {
		// TODO Add support for non-void methods
		
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
