import java.io.File;
import java.util.ArrayList;

public class VMTranslator {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Error: Please provide a Virtual Machine file.");
			return;
		}

		ArrayList<File> inputFiles = new ArrayList<>();
		File inputPath = new File(args[0]);
		File outputFile;

		if (inputPath.isFile()) {
			inputFiles.add(inputPath);
			outputFile = new File(inputPath.getName().substring(0, inputPath.getName().length() - 3) + ".asm");
		} else if (inputPath.isDirectory()) {
			if (inputPath.getName().endsWith("/")) {
				inputPath = new File(inputPath.getName().substring(0, inputPath.getName().length() - 1));
			}
			String[] allFiles = inputPath.list();
			assert allFiles != null;
			for (String fn : allFiles) {
				if (fn.endsWith(".vm")) {
					inputFiles.add(new File(inputPath + "/" + fn));
				}
			}
			if (inputFiles.size() == 0) {
				throw new RuntimeException("No Input File Found");
			}
			outputFile = new File(inputPath + ".asm");
		} else {
			throw new RuntimeException("Unknown Input Path");
		}
		CodeWriter codeWriter = new CodeWriter(outputFile);


		codeWriter.write_init();
		if (inputPath.isDirectory()) {
			codeWriter.comment("Bootstrap Code");
			codeWriter.write_bootstrap();
		}

		for (File input : inputFiles) {
			String[] split = input.getName().split("/");
			String last = split[split.length - 1];
			String fileName = last.substring(0, last.length() - 3);
			codeWriter.set_file_name(fileName);

			Parser parser = new Parser(input);

			while (parser.hasMoreCommands()) {
				parser.advance();
				codeWriter.comment(parser.currentCommand);
				String commandType = parser.commandType();
				String functionName;
				int num;
				switch (commandType) {
					case "C_ARITHMETIC" -> codeWriter.write_arithmetic(parser.arg1());
					case "C_PUSH", "C_POP" -> {
						String segment = parser.arg1();
						int index = parser.arg2();
						codeWriter.write_push_pop(commandType, segment, index);
					}
					case "C_LABEL" -> codeWriter.write_label(parser.arg1());
					case "C_GOTO" -> codeWriter.write_goto(parser.arg1());
					case "C_IF" -> codeWriter.write_if(parser.arg1());
					case "C_FUNCTION" -> {
						functionName = parser.arg1();
						num = parser.arg2();
						codeWriter.write_function(functionName, num);
					}
					case "C_CALL" -> {
						functionName = parser.arg1();
						num = parser.arg2();
						codeWriter.write_call(functionName, num);
					}
					case "C_RETURN" -> codeWriter.write_return();
				}
			}
		}
		codeWriter.close();
	}
}
