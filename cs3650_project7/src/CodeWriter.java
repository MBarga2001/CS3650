import java.io.*;
import java.util.HashMap;

public class CodeWriter {
	private final String fileName;
	private PrintWriter fileOut = null;
	private int labelCounter = 0;
	private static final HashMap<String, String> symbols = new HashMap<>();

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
		fileName = file.getName().substring(0, file.getName().length() - 4);
		String fileN = file.getName();

		try {
			fileOut = new PrintWriter(fileN);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void comment(String command) {
			fileOut.write("//" + command + "\n");
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

	public void close() {
		fileOut.close();
	}
}
