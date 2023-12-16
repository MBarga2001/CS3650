import java.io.*;
import java.util.HashMap;

public class CodeWriter {
	private String fileName;
	private PrintWriter fileOut = null;
	private int labelCounter = 0;
	private static final HashMap<String, String> symbols = new HashMap<>();
	private String functionName = "OS";

	static {
		symbols.put("add", "M=D+M");
		symbols.put("sub", "M=M-D");
		symbols.put("and", "M=D&M");
		symbols.put("or", "M=D|M");
		symbols.put("neg", "M=-M");
		symbols.put("not", "M=!M");
		symbols.put("eq", "D;JEQ");
		symbols.put("gt", "D;JGT");
		symbols.put("lt", "D;JLT");
		symbols.put("local", "@LCL");
		symbols.put("argument", "@ARG");
		symbols.put("this", "@THIS");
		symbols.put("that", "@THAT");
		symbols.put("constant", "");
		symbols.put("static", "");
		symbols.put("pointer", "@3");
		symbols.put("temp", "@5");
	}

	public CodeWriter(File file) {
		fileName = file.getName();
		String fileN = file.getName();
		try {
			fileOut = new PrintWriter(fileN);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void write_init() {
		fileOut.write("// init\n");
		fileOut.write("@256\n");
		fileOut.write("D=A\n");
		fileOut.write("@SP\n");
		fileOut.write("M=D\n");
		fileOut.write("@300\n");
		fileOut.write("D=A\n");
		fileOut.write("@LCL\n");
		fileOut.write("M=D\n");
		fileOut.write("@400\n");
		fileOut.write("D=A\n");
		fileOut.write("@ARG\n");
		fileOut.write("M=D\n");
		fileOut.write("@3000\n");
		fileOut.write("D=A\n");
		fileOut.write("@THIS\n");
		fileOut.write("M=D\n");
		fileOut.write("@3010\n");
		fileOut.write("D=A\n");
		fileOut.write("@THAT\n");
		fileOut.write("M=D\n");
	}

	public void write_bootstrap() {
		fileOut.write("@256\n");
		fileOut.write("D=A\n");
		fileOut.write("@SP\n");
		fileOut.write("M=D\n");
		String sys_init_ret_add = "return-address-sysinit";
		fileOut.write(("@" + sys_init_ret_add + "\n"));
		fileOut.write("D=A\n");
		fileOut.write("@SP\n");
		fileOut.write("A=M\n");
		fileOut.write("M=D\n");
		fileOut.write("@SP\n");
		fileOut.write("M=M+1\n");
		fileOut.write("@LCL\n");
		fileOut.write("D=M\n");
		fileOut.write("@SP\n");
		fileOut.write("A=M\n");
		fileOut.write("M=D\n");
		fileOut.write("@SP\n");
		fileOut.write("M=M+1\n");
		fileOut.write("@ARG\n");
		fileOut.write("D=M\n");
		fileOut.write("@SP\n");
		fileOut.write("A=M\n");
		fileOut.write("M=D\n");
		fileOut.write("@SP\n");
		fileOut.write("M=M+1\n");
		fileOut.write("@THIS\n");
		fileOut.write("D=M\n");
		fileOut.write("@SP\n");
		fileOut.write("A=M\n");
		fileOut.write("M=D\n");
		fileOut.write("@SP\n");
		fileOut.write("M=M+1\n");
		fileOut.write("@THAT\n");
		fileOut.write("D=M\n");
		fileOut.write("@SP\n");
		fileOut.write("A=M\n");
		fileOut.write("M=D\n");
		fileOut.write("@SP\n");
		fileOut.write("M=M+1\n");
		fileOut.write("@SP\n");
		fileOut.write("D=M\n");
		fileOut.write("@5\n");
		fileOut.write("D=D-A\n");
		fileOut.write("@ARG\n");
		fileOut.write("M=D\n");
		fileOut.write("@SP\n");
		fileOut.write("D=M\n");
		fileOut.write("@LCL\n");
		fileOut.write("M=D\n");
		String func_name = "Sys.init";
		fileOut.write("@" + func_name + "\n");
		fileOut.write("0;JMP\n");
		fileOut.write("(" + sys_init_ret_add + ")\n");
	}

	public void set_file_name(String fn) {
		fileName = fn;
	}

	public void comment(String input) {
		fileOut.write("// " + input + "\n");
	}

	public void write_arithmetic(String command) {
		switch (command) {
			case "add", "sub", "and", "or" -> {
				fileOut.write("@SP\n");
				fileOut.write("AM=M-1\n");
				fileOut.write("D=M\n");
				fileOut.write("@SP\n");
				fileOut.write("A=M-1\n");
				fileOut.write(symbols.get(command) + "\n");
			}
			case "neg", "not" -> {
				fileOut.write("@SP\n");
				fileOut.write("A=M-1\n");
				fileOut.write(symbols.get(command) + "\n");
			}
			case "eq", "gt", "lt" -> {
				String jumpLabel = "CompLabel" + labelCounter;
				labelCounter += 1;
				fileOut.write("@SP\n");
				fileOut.write("AM=M-1\n");
				fileOut.write("D=M\n");
				fileOut.write("@SP\n");
				fileOut.write("A=M-1\n");
				fileOut.write("D=M-D\n");
				fileOut.write("M=-1\n");
				fileOut.write("@" + jumpLabel + "\n");
				fileOut.write(symbols.get(command) + "\n");
				fileOut.write("@SP\n");
				fileOut.write("A=M-1\n");
				fileOut.write("M=0\n");
				fileOut.write("(" + jumpLabel + ")\n");
			}
			default -> throw new RuntimeException("Unexpected Arithmetic Command");
		}
	}

	public void write_push_pop(String cmd, String seg, int index) {
		if (cmd.equals("C_PUSH")) {
			switch (seg) {
				case "constant" -> {
					fileOut.write("@" + index + "\n");
					fileOut.write("D=A\n");
					fileOut.write("@SP\n");
					fileOut.write("AM=M+1\n");
					fileOut.write("A=A-1\n");
					fileOut.write("M=D\n");
				}
				case "local", "argument", "this", "that", "temp", "pointer" -> {
					fileOut.write("@" + index + "\n");
					fileOut.write("D=A\n");
					if (seg.equals("temp") || seg.equals("pointer")) {
						fileOut.write(symbols.get(seg) + "\n");
					} else {
						fileOut.write(symbols.get(seg) + "\n");
						fileOut.write("A=M\n");
					}
					fileOut.write("A=D+A\n");
					fileOut.write("D=M\n");
					fileOut.write("@SP\n");
					fileOut.write("A=M\n");
					fileOut.write("M=D\n");
					fileOut.write("@SP\n");
					fileOut.write("M=M+1\n");
				}
				case "static" -> {
					fileOut.write("@" + fileName + "." + index + "\n");
					fileOut.write("D=M\n");
					fileOut.write("@SP\n");
					fileOut.write("A=M\n");
					fileOut.write("M=D\n");
					fileOut.write("@SP\n");
					fileOut.write("M=M+1\n");
				}
				default -> throw new RuntimeException("Unexpected Push Segment");
			}
		} else if (cmd.equals("C_POP")) {
			switch (seg) {
				case "constant" -> throw new RuntimeException("Cannot Pop Constant Segment");
				case "local", "argument", "this", "that", "temp", "pointer" -> {
					fileOut.write("@" + index + "\n");
					fileOut.write("D=A\n");
					if (seg.equals("temp") || seg.equals("pointer")) {
						fileOut.write(symbols.get(seg) + "\n");
					} else {
						fileOut.write(symbols.get(seg) + "\n");
						fileOut.write("A=M\n");
					}
					fileOut.write("D=D+A\n");
					fileOut.write("@R13\n");
					fileOut.write("M=D\n");
					fileOut.write("@SP\n");
					fileOut.write("AM=M-1\n");
					fileOut.write("D=M\n");
					fileOut.write("@R13\n");
					fileOut.write("A=M\n");
					fileOut.write("M=D\n");
				}
				case "static" -> {
					fileOut.write("@SP\n");
					fileOut.write("AM=M-1\n");
					fileOut.write("D=M\n");
					fileOut.write("@" + fileName + "." + index + "\n");
					fileOut.write("M=D\n");
				}
				default -> throw new RuntimeException("Unexpected Pop Segment");
			}
		} else {
			throw new RuntimeException("Unexpected Command Type");
		}
	}

	public void write_label(String label) {
		String labelName = functionName + "$" + label;
		fileOut.write("(" + labelName + ")\n");
	}

	public void write_goto(String label) {
		String labelName = functionName + "$" + label;
		fileOut.write("@" + labelName + "\n");
		fileOut.write("0;JMP\n");
	}

	public void write_if(String label) {
		String labelName = functionName + "$" + label;
		fileOut.write("@SP\n");
		fileOut.write("AM=M-1\n");
		fileOut.write("D=M\n");
		fileOut.write("@" + labelName + "\n");
		fileOut.write("D;JNE\n");
	}

	public void write_function(String fn, int num) {
		functionName = fn;
		fileOut.write("(" + functionName + ")\n");
		for (int i = 0; i < num; i++) {
			write_push_pop("C_PUSH", "constant", 0);
		}
	}

	public void write_call(String fn, int num) {
		String returnLabel = functionName + "$ret." + labelCounter;
		labelCounter += 1;
		fileOut.write("@" + returnLabel + "\n");
		fileOut.write("D=A\n");
		fileOut.write("@SP\n");
		fileOut.write("A=M\n");
		fileOut.write("M=D\n");
		fileOut.write("@SP\n");
		fileOut.write("M=M+1\n");
		String[] arr = {"LCL", "ARG", "THIS", "THAT"};
		for (String segment : arr) {
			fileOut.write("@" + segment + "\n");
			fileOut.write("D=M\n");
			fileOut.write("@SP\n");
			fileOut.write("A=M\n");
			fileOut.write("M=D\n");
			fileOut.write("@SP\n");
			fileOut.write("M=M+1\n");
		}
		fileOut.write("@SP\n");
		fileOut.write("D=M\n");
		fileOut.write("@5\n");
		fileOut.write("D=D-A\n");
		fileOut.write("@" + num + "\n");
		fileOut.write("D=D-A\n");
		fileOut.write("@ARG\n");
		fileOut.write("M=D\n");
		fileOut.write("@SP\n");
		fileOut.write("D=M\n");
		fileOut.write("@LCL\n");
		fileOut.write("M=D\n");
		fileOut.write("@" + fn + "\n");
		fileOut.write("0;JMP\n");
		fileOut.write("(" + returnLabel + ")\n");
	}

	public void write_return() {
		fileOut.write("@LCL\n");
		fileOut.write("D=M\n");
		fileOut.write("@R13\n");
		fileOut.write("M=D\n");
		fileOut.write("@5\n");
		fileOut.write("A=D-A\n");
		fileOut.write("D=M\n");
		fileOut.write("@R14\n");
		fileOut.write("M=D\n");
		fileOut.write("@SP\n");
		fileOut.write("AM=M-1\n");
		fileOut.write("D=M\n");
		fileOut.write("@ARG\n");
		fileOut.write("A=M\n");
		fileOut.write("M=D\n");
		fileOut.write("@ARG\n");
		fileOut.write("D=M+1\n");
		fileOut.write("@SP\n");
		fileOut.write("M=D\n");
		String[] arr = {"THAT", "THIS", "ARG", "LCL"};
		for (String segment : arr) {
			fileOut.write("@R13\n");
			fileOut.write("AM=M-1\n");
			fileOut.write("D=M\n");
			fileOut.write("@" + segment + "\n");
			fileOut.write("M=D\n");
		}
		fileOut.write("@R14\n");
		fileOut.write("A=M\n");
		fileOut.write("0;JMP\n");
	}

	public void close() {
		fileOut.close();
	}
}

