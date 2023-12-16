import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class Parser {
	public String currentCommand = "";
	private int current = -1;
	private final ArrayList<String> commands = new ArrayList<>();

	public Parser(File file) {
		String line;
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			while ((line = br.readLine()) != null) {
				if (line.split("//").length > 0) {
					line = line.split("//")[0];
					line = line.strip();
				}
				if (!line.equals("") && !line.equals("//")) {
					commands.add(line);
				}
			}
			fr.close();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean hasMoreCommands() {
		return (current + 1) < commands.size();
	}

	public void advance() {
		current += 1;
		currentCommand = commands.get(current);
	}

	public String commandType() {
		ArrayList<String> arithmeticCommands = new ArrayList<>();
		arithmeticCommands.add("add");
		arithmeticCommands.add("sub");
		arithmeticCommands.add("neg");
		arithmeticCommands.add("eq");
		arithmeticCommands.add("gt");
		arithmeticCommands.add("lt");
		arithmeticCommands.add("and");
		arithmeticCommands.add("or");
		arithmeticCommands.add("not");

		String cmd = currentCommand.trim().split("\\s+")[0];
		if (arithmeticCommands.contains(cmd)) {
			return "C_ARITHMETIC";
		} else if (cmd.equals("push")) {
			return "C_PUSH";
		} else if (cmd.equals("pop")) {
			return "C_POP";
		} else if (cmd.equals("label")) {
			return "C_LABEL";
		} else if (cmd.equals("goto")) {
			return "C_GOTO";
		} else if (cmd.equals("if-goto")) {
			return "C_IF";
		} else if (cmd.equals("function")) {
			return "C_FUNCTION";
		} else if (cmd.equals("call")) {
			return "C_CALL";
		} else if (cmd.equals("return")) {
			return "C_RETURN";
		} else {
			throw new RuntimeException("Unexpected Command Type");
		}
	}

	public String arg1() {
		if (commandType().equals("C_ARITHMETIC")) {
			return currentCommand.split("\\s+")[0];
		} else {
			return currentCommand.split("\\s+")[1];
		}
	}

	public int arg2() {
		return Integer.parseInt(currentCommand.split("\\s+")[2]);
	}
}
