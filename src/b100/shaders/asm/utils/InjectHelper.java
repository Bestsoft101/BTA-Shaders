package b100.shaders.asm.utils;

import java.lang.reflect.Modifier;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import b100.utils.InvalidCharacterException;
import b100.utils.StringReader;

public class InjectHelper {
	
	public final String listenerClass;
	public final String callbackInfoClass;
	
	public InjectHelper(String listenerClass, String callbackInfoClass) {
		this.listenerClass = listenerClass;
		this.callbackInfoClass = callbackInfoClass;
	}
	
	public InjectHelper(String listenerClass) {
		this.listenerClass = listenerClass;
		this.callbackInfoClass = getCallbackInfoClass();
	}
	
	/**
	 * <code>
	 * <br>	CallbackInfo callbackInfo = new CallbackInfo();
	 * <br>	listenerMethodName(this, args..., callbackInfo);
	 * <br>	if(callbackInfo.isCancelled())  return callbackInfo.getReturnValue();
	 * <code>
	 */
	public InsnList createMethodCallInject(ClassNode classNode, MethodNode method, String listenerMethodName) {
		InsnList insert = new InsnList();
		LabelNode labelStart = new LabelNode();
		LabelNode labelAfterReturn = new LabelNode();
		
		// Create local variable for CallbackInfo callbackInfo
		int callbackInfoIndex = method.localVariables.size();
		method.localVariables.add(new LocalVariableNode("callbackInfo" + callbackInfoIndex, "L"+callbackInfoClass+";", null, labelStart, labelAfterReturn, callbackInfoIndex));
		method.maxLocals++;
		
		// Create CallbackInfo instance and store it in variable
		insert.add(new TypeInsnNode(Opcodes.NEW, callbackInfoClass));
		insert.add(new InsnNode(Opcodes.DUP));
		insert.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, callbackInfoClass, "<init>", "()V"));
		insert.add(new VarInsnNode(Opcodes.ASTORE, callbackInfoIndex));
		
		// Create descriptor of listener method for method call
		StringBuilder listenerMethodDesc = new StringBuilder();
		listenerMethodDesc.append('(');
		if(!Modifier.isStatic(method.access)) {
			listenerMethodDesc.append('L');
			listenerMethodDesc.append(classNode.name);
			listenerMethodDesc.append(';');
		}
		listenerMethodDesc.append(method.desc.substring(1, method.desc.indexOf(')')));
		listenerMethodDesc.append('L').append(callbackInfoClass).append(';');
		listenerMethodDesc.append(")V");

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
		
		insert.add(labelStart);
		insert.add(new VarInsnNode(Opcodes.ALOAD, callbackInfoIndex));
		insert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, listenerMethodName, listenerMethodDesc.toString()));

		insert.add(new VarInsnNode(Opcodes.ALOAD, callbackInfoIndex));
		insert.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, callbackInfoClass, "isCancelled", "()Z"));
		insert.add(new JumpInsnNode(Opcodes.IFEQ, labelAfterReturn)); // if 0 jump
		
		if(returnType == 'Z') {
			insert.add(new VarInsnNode(Opcodes.ALOAD, callbackInfoIndex));
			insert.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, callbackInfoClass, "getBooleanReturnValue", "()Z"));
			insert.add(new InsnNode(Opcodes.IRETURN));	
		}else if(returnType == 'L') {
			insert.add(new VarInsnNode(Opcodes.ALOAD, callbackInfoIndex));
			insert.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, callbackInfoClass, "getReturnValue", "()Ljava/lang/Object;"));
			insert.add(new TypeInsnNode(Opcodes.CHECKCAST, returnClass));
			insert.add(new InsnNode(Opcodes.ARETURN));
		}else if(returnType == 'V') {
			insert.add(new InsnNode(Opcodes.RETURN));
		}else {
			throw new RuntimeException("Unknown return type: " + returnType);
		}
		
		insert.add(labelAfterReturn);
		insert.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
		
		return insert;
	}
	
	private static String getCallbackInfoClass() {
		String thisClass = InjectHelper.class.getName().replace('.', '/');
		
		return thisClass.substring(0, thisClass.lastIndexOf('/') + 1) + "CallbackInfo";
	}
}
