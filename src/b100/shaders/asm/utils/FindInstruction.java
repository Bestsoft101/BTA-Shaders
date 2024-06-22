package b100.shaders.asm.utils;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class FindInstruction {
	
	/**
	 * Check if the given instruction has the given opcode 
	 */
	public static boolean opcode(AbstractInsnNode node, int opcode) {
		return node != null && node.getOpcode() == opcode;
	}
	
	/**
	 * Check if the given instruction node is a MethodInsnNode with the given name
	 */
	public static boolean methodInsn(AbstractInsnNode node, String name) {
		if(node instanceof MethodInsnNode) {
			MethodInsnNode node1 = (MethodInsnNode) node;
			return name == null || node1.name.equals(name);
		}
		return false;
	}
	
	/**
	 * Check if the given instruction node is a MethodInsnNode with the given name and descriptor
	 */
	public static boolean methodInsn(AbstractInsnNode node, String name, String desc) {
		if(node instanceof MethodInsnNode) {
			MethodInsnNode node1 = (MethodInsnNode) node;
			return (name == null || node1.name.equals(name)) && (desc == null || node1.desc.equals(desc));
		}
		return false;
	}
	
	/**
	 * Check if the given instruction node is a MethodInsnNode with the given owner, name and descriptor
	 */
	public static boolean methodInsn(AbstractInsnNode node, String owner, String name, String desc) {
		if(node instanceof MethodInsnNode) {
			MethodInsnNode node1 = (MethodInsnNode) node;
			return (owner == null || node1.owner.equals(owner)) && (name == null || node1.name.equals(name)) && (desc == null || node1.desc.equals(desc));
		}
		return false;
	}

	/**
	 * Check if the given instruction node is a FieldInsnNode with the given name
	 */
	public static boolean fieldInsn(AbstractInsnNode node, String name) {
		if(node instanceof FieldInsnNode) {
			FieldInsnNode node1 = (FieldInsnNode) node;
			return name == null || node1.name.equals(name);
		}
		return false;
	}

	/**
	 * Check if the given instruction node is a FieldInsnNode with the given properties
	 */
	public static boolean fieldInsn(AbstractInsnNode node, String owner, String name) {
		if(node instanceof FieldInsnNode) {
			FieldInsnNode node1 = (FieldInsnNode) node;
			return (owner == null || node1.owner.equals(owner)) && (name == null || node1.name.equals(name));
		}
		return false;
	}

	/**
	 * Check if the given instruction node is a FieldInsnNode with the given properties
	 */
	public static boolean fieldInsn(AbstractInsnNode node, String owner, String name, String desc) {
		if(node instanceof FieldInsnNode) {
			FieldInsnNode node1 = (FieldInsnNode) node;
			return (owner == null || node1.owner.equals(owner)) && (name == null || node1.name.equals(name)) && (desc == null || node1.desc.equals(desc));
		}
		return false;
	}

	/**
	 * Check if the given instruction node is a IntInsnNode with the given value
	 */
	public static boolean intInsn(AbstractInsnNode node, int value) {
		if(node instanceof IntInsnNode) {
			IntInsnNode node1 = (IntInsnNode) node;
			return node1.operand == value;
		}
		return false;
	}
	
	/**
	 * Check if the given instruction is a LdcInsnNode with the given value
	 */
	public static boolean ldcInsn(AbstractInsnNode node, Object cst) {
		if(node instanceof LdcInsnNode) {
			LdcInsnNode node1 = (LdcInsnNode) node;
			return node1.cst == cst;
		}
		return false;
	}
	
	/**
	 * Check if the given instruction is a LdcInsnNode with the given integer value
	 */
	public static boolean ldcInsnI(AbstractInsnNode node, int cst) {
		if(node instanceof LdcInsnNode) {
			LdcInsnNode node1 = (LdcInsnNode) node;
			if(node1.cst instanceof Integer) {
				return (Integer) node1.cst == cst;
			}
		}
		return false;
	}
	
	/**
	 * Check if the given instruction is a LdcInsnNode with the given float value
	 */
	public static boolean ldcInsnF(AbstractInsnNode node, float cst) {
		if(node instanceof LdcInsnNode) {
			LdcInsnNode node1 = (LdcInsnNode) node;
			if(node1.cst instanceof Float) {
				return (Float) node1.cst == cst;
			}
		}
		return false;
	}
	
	/**
	 * Check if the given instruction is a LdcInsnNode with the given string value
	 */
	public static boolean ldcInsnS(AbstractInsnNode node, String cst) {
		if(node instanceof LdcInsnNode) {
			LdcInsnNode ldc = (LdcInsnNode) node;
			if(ldc.cst instanceof String) {
				return ldc.cst.equals(cst);
			}
		}
		return false;
	}
	
	/**
	 * Check if the given instruction is a LdcInsnNode with the given value
	 */
	public static boolean varInsn(AbstractInsnNode node, int var) {
		if(node instanceof VarInsnNode) {
			VarInsnNode node1 = (VarInsnNode) node;
			return node1.var == var;
		}
		return false;
	}

	/**
	 * Check if the given instruction is a JumpInsnNode
	 */
	public static boolean jumpInsn(AbstractInsnNode node) {
		if(node instanceof JumpInsnNode) {
			return true;
		}
		return false;
	}

}
