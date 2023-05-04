package task.translate;

public interface ConsoleDecodeable {
	public static String newline = System.getProperty("line.separator");
	String consoleDecoding(int[] model);
}
