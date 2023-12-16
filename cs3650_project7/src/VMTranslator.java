import java.io.File;

public class VMTranslator {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Error: Please provide a Virtual Machine file.");
			return;
		}

		String inputFileName = args[0];
		String outputFileName = args[0].substring(0, args[0].length() - 3) + ".asm";
		File inputFile = new File(inputFileName);
		File outputFile = new File(outputFileName);

		Parser parser = new Parser(inputFile);
		CodeWriter codeWriter = new CodeWriter(outputFile);

		while (parser.hasMoreCommands()) {
			parser.advance();

			codeWriter.comment(parser.currentCommand);

			String commandType = parser.commandType();
			if (commandType.equals("C_ARITHMETIC")) {
				codeWriter.write_arithmetic(parser.arg1());
			} else if (commandType.equals("C_PUSH") || commandType.equals("C_POP")) {
				String argument1 = parser.arg1();
				int argument2 = parser.arg2();
				codeWriter.write_push_pop(commandType, argument1, argument2);
			} else {
				throw new RuntimeException("Unsupported Command Type");
			}
		}
		codeWriter.close();
	}
}
