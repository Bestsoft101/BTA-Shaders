package b100.shaders.asm.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import b100.utils.interfaces.Condition;

public abstract class ASMHelper {
	
	private static Map<Integer, String> opcodeNames = new HashMap<>();
	
	static {
		opcodeNames.put(Opcodes.NOP, "NOP");

		opcodeNames.put(Opcodes.ACONST_NULL, "ACONST_NULL");
		opcodeNames.put(Opcodes.ICONST_M1, "ICONST_M1");
		opcodeNames.put(Opcodes.ICONST_0, "ICONST_0");
		opcodeNames.put(Opcodes.ICONST_1, "ICONST_1");
		opcodeNames.put(Opcodes.ICONST_2, "ICONST_2");
		opcodeNames.put(Opcodes.ICONST_3, "ICONST_3");
		opcodeNames.put(Opcodes.ICONST_4, "ICONST_4");
		opcodeNames.put(Opcodes.ICONST_5, "ICONST_5");
		
		opcodeNames.put(Opcodes.LCONST_0, "LCONST_0");
		opcodeNames.put(Opcodes.LCONST_1, "LCONST_1");

		opcodeNames.put(Opcodes.FCONST_0, "FCONST_0");
		opcodeNames.put(Opcodes.FCONST_1, "FCONST_1");
		opcodeNames.put(Opcodes.FCONST_2, "FCONST_2");

		opcodeNames.put(Opcodes.DCONST_0, "DCONST_0");
		opcodeNames.put(Opcodes.DCONST_1, "DCONST_1");

		opcodeNames.put(Opcodes.SIPUSH, "SIPUSH");
		opcodeNames.put(Opcodes.BIPUSH, "BIPUSH");
		opcodeNames.put(Opcodes.LDC, "LDC");

		opcodeNames.put(Opcodes.ILOAD, "ILOAD");
		opcodeNames.put(Opcodes.LLOAD, "LLOAD");
		opcodeNames.put(Opcodes.FLOAD, "FLOAD");
		opcodeNames.put(Opcodes.DLOAD, "DLOAD");
		opcodeNames.put(Opcodes.ALOAD, "ALOAD");

		opcodeNames.put(Opcodes.ISTORE, "ISTORE");
		opcodeNames.put(Opcodes.LSTORE, "LSTORE");
		opcodeNames.put(Opcodes.FSTORE, "FSTORE");
		opcodeNames.put(Opcodes.DSTORE, "DSTORE");
		opcodeNames.put(Opcodes.ASTORE, "ASTORE");

		opcodeNames.put(Opcodes.POP, "POP");
		opcodeNames.put(Opcodes.POP2, "POP2");

		opcodeNames.put(Opcodes.DUP, "DUP");
		opcodeNames.put(Opcodes.DUP_X1, "DUP_X1");
		opcodeNames.put(Opcodes.DUP_X2, "DUP_X2");
		
		opcodeNames.put(Opcodes.DUP2, "DUP2");
		opcodeNames.put(Opcodes.DUP2_X1, "DUP2_X1");
		opcodeNames.put(Opcodes.DUP2_X2, "DUP2_X2");

		opcodeNames.put(Opcodes.SWAP, "SWAP");

		opcodeNames.put(Opcodes.IADD, "IADD");
		opcodeNames.put(Opcodes.LADD, "LADD");
		opcodeNames.put(Opcodes.FADD, "FADD");
		opcodeNames.put(Opcodes.DADD, "DADD");

		opcodeNames.put(Opcodes.ISUB, "ISUB");
		opcodeNames.put(Opcodes.LSUB, "LSUB");
		opcodeNames.put(Opcodes.FSUB, "FSUB");
		opcodeNames.put(Opcodes.DSUB, "DSUB");

		opcodeNames.put(Opcodes.IMUL, "IMUL");
		opcodeNames.put(Opcodes.LMUL, "LMUL");
		opcodeNames.put(Opcodes.FMUL, "FMUL");
		opcodeNames.put(Opcodes.DMUL, "DMUL");

		opcodeNames.put(Opcodes.IDIV, "IDIV");
		opcodeNames.put(Opcodes.LDIV, "LDIV");
		opcodeNames.put(Opcodes.FDIV, "FDIV");
		opcodeNames.put(Opcodes.DDIV, "DDIV");
		
		opcodeNames.put(Opcodes.IFEQ, "IFEQ"); // if value == 0 jump
		opcodeNames.put(Opcodes.IFNE, "IFNE"); // if value != 0 jump
		opcodeNames.put(Opcodes.IFLT, "IFLT"); // if value < 0 jump
		opcodeNames.put(Opcodes.IFGE, "IFGE"); // if value >= 0 jump
		opcodeNames.put(Opcodes.IFGT, "IFGT"); // if value > 0 jump
		opcodeNames.put(Opcodes.IFLE, "IFLE"); // if value <= 0 jump
		
		opcodeNames.put(Opcodes.IF_ICMPEQ, "IF_ICMPEQ");
		opcodeNames.put(Opcodes.IF_ICMPNE, "IF_ICMPNE");
		opcodeNames.put(Opcodes.IF_ICMPLT, "IF_ICMPLT");
		opcodeNames.put(Opcodes.IF_ICMPGE, "IF_ICMPGE");
		opcodeNames.put(Opcodes.IF_ICMPGT, "IF_ICMPGT");
		opcodeNames.put(Opcodes.IF_ICMPLE, "IF_ICMPLE");
		opcodeNames.put(Opcodes.IF_ACMPEQ, "IF_ACMPEQ");
		opcodeNames.put(Opcodes.IF_ACMPNE, "IF_ACMPNE");
		
		opcodeNames.put(Opcodes.GOTO, "GOTO");
		
		opcodeNames.put(Opcodes.IRETURN, "IRETURN");
		opcodeNames.put(Opcodes.LRETURN, "LRETURN");
		opcodeNames.put(Opcodes.FRETURN, "FRETURN");
		opcodeNames.put(Opcodes.DRETURN, "DRETURN");
		opcodeNames.put(Opcodes.ARETURN, "ARETURN");
		opcodeNames.put(Opcodes.RETURN, "RETURN");

		opcodeNames.put(Opcodes.GETSTATIC, "GETSTATIC");
		opcodeNames.put(Opcodes.PUTSTATIC, "PUTSTATIC");
		
		opcodeNames.put(Opcodes.GETFIELD, "GETFIELD");
		opcodeNames.put(Opcodes.PUTFIELD, "PUTFIELD");

		opcodeNames.put(Opcodes.INVOKEVIRTUAL, "INVOKEVIRTUAL");
		opcodeNames.put(Opcodes.INVOKESPECIAL, "INVOKESPECIAL");
		opcodeNames.put(Opcodes.INVOKESTATIC, "INVOKESTATIC");
		opcodeNames.put(Opcodes.INVOKEINTERFACE, "INVOKEINTERFACE");
		opcodeNames.put(Opcodes.INVOKEDYNAMIC, "INVOKEDYNAMIC");
		
		opcodeNames.put(Opcodes.NEW, "NEW");

		opcodeNames.put(Opcodes.NEWARRAY, "NEWARRAY");

		opcodeNames.put(Opcodes.CHECKCAST, "CHECKCAST");
	}
	
	public static String getOpcodeName(int opcode) {
		return opcodeNames.get(opcode);
	}
	
	public static String getFrameTypeName(int type) {
		if(type == Opcodes.F_NEW) return "F_NEW";
		if(type == Opcodes.F_FULL) return "F_FULL";
		if(type == Opcodes.F_APPEND) return "F_APPEND";
		if(type == Opcodes.F_CHOP) return "F_CHOP";
		if(type == Opcodes.F_SAME) return "F_SAME";
		if(type == Opcodes.F_SAME1) return "F_SAME1";
		
		return null;
	}
	
	//////////////////////////////////////////
	
	private static void print(String string) {
		System.out.print(string + "\n");
	}
	
	//////////////////////////////////////////
	
	public static ClassNode getClassNode(byte[] bytes) {
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		return classNode;
	}
	
	public static byte[] getBytes(ClassNode classNode) {
		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(classWriter);
		return classWriter.toByteArray();
	}
	
	//////////////////////////////////////////
	
	public static MethodNode findMethod(ClassNode classNode, String name) {
		return findMethod(classNode, name, null);
	}
	
	public static MethodNode findMethod(ClassNode classNode, String name, String desc) {
		List<MethodNode> foundMethods = new ArrayList<MethodNode>();
		
		for(MethodNode methodNode : classNode.methods) {
			if((name == null || methodNode.name.equals(name)) && (desc == null || methodNode.desc.equals(desc))) {
				foundMethods.add(methodNode);
			}
		}
		
		if(foundMethods.size() != 1) {
			StringBuilder msg = new StringBuilder();
			msg.append("\n\n");
			
			if(foundMethods.size() < 1) {
				msg.append("Found no methods with name '"+name+"' and descriptor '"+desc+"' in class '"+classNode.name+"'!\n");
			}else {
				msg.append("Found more than one method matching name '"+name+"' and descriptor '"+desc+"' in class '"+classNode.name+"'!\n");
			}
			
			msg.append('\n');
			msg.append("All methods in class '"+classNode.name+"': \n");
			for(MethodNode method : classNode.methods) {
				msg.append("    ").append(method.name).append(method.desc).append('\n');
			}
			
			throw new RuntimeException(msg.toString());
		}
		
		return foundMethods.get(0);
	}
	
	public static MethodNode findMethod(ClassNode classNode, Condition<MethodNode> condition) {
		List<MethodNode> foundMethods = new ArrayList<MethodNode>();
		
		for(MethodNode methodNode : classNode.methods) {
			if(condition.isTrue(methodNode)) {
				foundMethods.add(methodNode);
			}
		}
		
		if(foundMethods.size() != 1) {
			StringBuilder msg = new StringBuilder();
			msg.append("\n\n");
			
			if(foundMethods.size() < 1) {
				msg.append("Found no methods matching condition '"+condition+"' in class '"+classNode.name+"'!\n");
			}else {
				msg.append("Found more than one method matching condition '"+condition+"' in class '"+classNode.name+"'!\n");
			}
			
			msg.append('\n');
			msg.append("All methods in class '"+classNode.name+"': \n");
			for(MethodNode method : classNode.methods) {
				msg.append("    ").append(method.name).append(method.desc).append('\n');
			}
			
			throw new RuntimeException(msg.toString());
		}
		
		return foundMethods.get(0);
	}

	public static List<AbstractInsnNode> findAllInstructions(MethodNode method, Condition<AbstractInsnNode> condition) {
		return findAllInstructions(method.instructions, condition);
	}
	
	public static List<AbstractInsnNode> findAllInstructions(InsnList instructions, Condition<AbstractInsnNode> condition) {
		List<AbstractInsnNode> list = new ArrayList<AbstractInsnNode>();
		
		AbstractInsnNode instruction = instructions.getFirst();
		while(true) {
			if(condition.isTrue(instruction)) {
				list.add(instruction);
			}
			instruction = instruction.getNext();
			if(instruction == null) {
				break;
			}
		}
		
		return list;
	}
	
	public static AbstractInsnNode findInstruction(MethodNode method, boolean backwards, Condition<AbstractInsnNode> condition) {
		return findInstruction(backwards ? method.instructions.getLast() : method.instructions.getFirst(), backwards, condition);
	}
	
	public static AbstractInsnNode findInstruction(AbstractInsnNode startInstruction, boolean backwards, Condition<AbstractInsnNode> condition) {
		AbstractInsnNode instruction = startInstruction;
		while(true) {
			if(instruction == null) {
				return null;
			}
			if(condition.isTrue(instruction)) {
				return instruction;
			}
			instruction = backwards ? instruction.getPrevious() : instruction.getNext();
		}
	}
	
	public static FieldNode findField(ClassNode classNode, String name) {
		for(FieldNode fieldNode : classNode.fields) {
			if(fieldNode.name.equals(name)) {
				return fieldNode;
			}
		}
		throw new NullPointerException("Field '"+name+"' does not exist in class "+classNode+"!");
	}

	//////////////////////////////////////////
	
	public static void insertAtStart(MethodNode methodNode, InsnList insert) {
		insertAtStart(methodNode.instructions, insert);
	}
	
	public static void insertAtStart(InsnList instructions, InsnList insert) {
		instructions.insertBefore(instructions.getFirst(), insert);
	}
	
	public static void insertBeforeLastReturn(MethodNode methodNode, InsnList insert) {
		insertBeforeLastReturn(methodNode.instructions, insert);
	}
	
	public static void insertBeforeLastReturn(InsnList instructions, InsnList insert) {
		AbstractInsnNode returnNode = findInstruction(instructions.getLast(), true, (n) -> FindInstruction.returnInsn(n));
		
		insert.insertBefore(returnNode, insert);
	}

	//////////////////////////////////////////
	
	public static void replaceInstruction(MethodNode method, AbstractInsnNode oldInstruction, AbstractInsnNode newInstruction) {
		InsnList newInstructions = new InsnList();
		newInstructions.add(newInstruction);
		replaceInstruction(method.instructions, oldInstruction, newInstructions);
	}
	
	public static void replaceInstruction(InsnList instructions, AbstractInsnNode oldInstruction, AbstractInsnNode newInstruction) {
		InsnList newInstructions = new InsnList();
		newInstructions.add(newInstruction);
		replaceInstruction(instructions, oldInstruction, newInstructions);
	}
	
	public static void replaceInstruction(MethodNode method, AbstractInsnNode oldInstruction, InsnList newInstructions) {
		replaceInstruction(method.instructions, oldInstruction, newInstructions);
	}
	
	public static void replaceInstruction(InsnList instructions, AbstractInsnNode oldInstruction, InsnList newInstructions) {
		if(!instructions.contains(oldInstruction)) {
			throw new RuntimeException("Can't replace instruction because it's not in list!");
		}
		AbstractInsnNode prev = oldInstruction.getPrevious();
		if(prev != null) {
			instructions.remove(oldInstruction);
			instructions.insert(prev, newInstructions);
			return;
		}
		AbstractInsnNode next = oldInstruction.getNext();
		if(next != null) {
			instructions.remove(oldInstruction);
			instructions.insertBefore(next, newInstructions);
			return;
		}
		throw new RuntimeException("No previous or next instruction!");
	}
	
	//////////////////////////////////////////
	
	public static Map<Label, String> generateLabelNames(MethodNode methodNode) {
		return generateLabelNames(methodNode.instructions);
	}
	
	public static Map<Label, String> generateLabelNames(InsnList instructions) {
		Set<Label> labels = new HashSet<>();
		List<Label> labelList = new ArrayList<>();
		for(AbstractInsnNode ins : instructions) {
			if(ins instanceof LabelNode) {
				LabelNode labelNode = (LabelNode) ins;
				Label label = labelNode.getLabel();
				if(!labels.contains(label)) {
					labels.add(label);
					labelList.add(label);
				}
			}
		}
		Map<Label, String> labelNames = new HashMap<>();
		int nameCount = 0;
		for(Label label : labelList) {
			labelNames.put(label, generateLabelName(nameCount++));
		}
		return labelNames;
	}
	
	private static String generateLabelName(int i) {
		StringBuilder name = new StringBuilder();
		while(true) {
			char c = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(i % 26);
			name.insert(0, c);
			i = i / 26 - 1;
			if(i < 0) {
				break;
			}
		}
		return name.toString();
	}
	
	private static String getLabelName(LabelNode labelNode, Map<Label, String> labelNames) {
		Label label = labelNode.getLabel();
		if(labelNames != null) {
			if(labelNames.get(label) != null) {
				return labelNames.get(label);
			}
		}
		return label.toString();
	}
	
	//////////////////////////////////////////
	
	public static void printMethod(MethodNode method) {
		Map<Label, String> names = ASMHelper.generateLabelNames(method);
		print("METHOD: " + method.name);
		print("  DESC: " + method.desc);
		print("  MAXLOCAL: " + method.maxLocals);
		print("  MAXSTACK: " + method.maxStack);
		print("  ATTRS: " + method.attrs);
		print("  SIGNATURE: " + method.signature);
		print("  LOCALVARIABLES: ");
		for(LocalVariableNode node : method.localVariables) {
			System.out.println("    " + node.name + " " + node.desc + " " + node.index + " start " + names.get(node.start.getLabel()) + " end " + names.get(node.end.getLabel()));
		}
		
		print("INSTRUCTIONS: ");
		ASMHelper.printInstructions(method, names);
	}
	
	public static void printInstructions(MethodNode method) {
		printInstructions(method.instructions, generateLabelNames(method));
	}
	
	public static void printInstructions(MethodNode method, Map<Label, String> labelNames) {
		printInstructions(method.instructions, labelNames);
	}
	
	public static void printInstructions(InsnList instructions) {
		printInstructions(instructions, generateLabelNames(instructions));
	}
	
	public static void printInstructions(InsnList instructions, Map<Label, String> labelNames) {
		AbstractInsnNode instruction = instructions.getFirst();
		
		while(instruction != null) {
			if(instruction instanceof LabelNode) {
				LabelNode labelNode = (LabelNode) instruction;
				print(labelNames.get(labelNode.getLabel())+":");
			}else {
				print("  " + toString(instruction, labelNames));	
			}
			
			instruction = instruction.getNext();
		}
	}
	
	public static String toString(AbstractInsnNode instruction) {
		return toString(instruction, null);
	}
	
	public static String toString(AbstractInsnNode instruction, Map<Label, String> labelNames) {
		if(instruction == null) {
			return null;
		}
		
		int opcode = instruction.getOpcode();

		String type = instruction.getClass().getSimpleName();
		String opcodeName = getOpcodeName(opcode);
		
		if(opcodeName == null) {
			opcodeName = String.valueOf(opcode);
		}
		
		type = opcodeName + " " + type;

		if(instruction instanceof LabelNode) {
			LabelNode node = (LabelNode) instruction;
			type = type + " " + getLabelName(node, labelNames);
		}
		if(instruction instanceof FrameNode) {
			FrameNode node = (FrameNode) instruction;
			type = type + " " + getFrameTypeName(node.type) + " " + node.local + " " + node.stack;
		}
		if(instruction instanceof LineNumberNode) {
			LineNumberNode node = (LineNumberNode) instruction;
			type = type + " " + node.line;
		}
		if(instruction instanceof TypeInsnNode) {
			TypeInsnNode node = (TypeInsnNode) instruction;
			type = type + " " + node.desc;
		}
		if(instruction instanceof FieldInsnNode) {
			FieldInsnNode node = (FieldInsnNode) instruction;
			type = type + " " + node.owner + " " + node.name + " " + node.desc;
		}
		if(instruction instanceof MethodInsnNode) {
			MethodInsnNode node = (MethodInsnNode) instruction;
			type = type + " " + node.owner + " " + node.name + " " + node.desc;
		}
		if(instruction instanceof LdcInsnNode) {
			LdcInsnNode node = (LdcInsnNode) instruction;
			type = type + " " + node.cst + " (" + node.cst.getClass() + ")";
		}
		if(instruction instanceof IntInsnNode) {
			IntInsnNode node = (IntInsnNode) instruction;
			type = type + " " + node.operand;
		}
		if(instruction instanceof VarInsnNode) {
			VarInsnNode node = (VarInsnNode) instruction;
			type = type + " " + node.var;
		}
		if(instruction instanceof JumpInsnNode) {
			JumpInsnNode node = (JumpInsnNode) instruction;
			type = type + " " + getLabelName(node.label, labelNames);
		}
		
		return type;
	}

}
