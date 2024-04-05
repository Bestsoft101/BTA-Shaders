package b100.shaders.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import b100.asmloader.ClassTransformer;
import b100.shaders.asm.utils.ASMHelper;
import b100.shaders.asm.utils.InjectHelper;

public class Transformers {
	
	private static final String listenerClass = "b100/shaders/asm/Listener";
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
			FieldNode mc = ASMHelper.findField(classNode, "mc"); 
			mc.access = (mc.access & ~Opcodes.ACC_PRIVATE) | Opcodes.ACC_PUBLIC;

			MethodNode setupCameraTransform = ASMHelper.findMethod(classNode, "setupCameraTransform");
			
			ASMHelper.findAllInstructions(setupCameraTransform.instructions, (n) -> n.getOpcode() == Opcodes.RETURN).forEach((returnNode) -> {
				setupCameraTransform.instructions.insertBefore(returnNode, injectHelper.createMethodCallInject(classNode, setupCameraTransform, "afterSetupCameraTransform"));
			});
			
			InsnList insert = injectHelper.createMethodCallInject(classNode, setupCameraTransform, "beforeSetupCameraTransform");
			setupCameraTransform.instructions.insertBefore(setupCameraTransform.instructions.getFirst(), insert);
		}
		
	}

}
