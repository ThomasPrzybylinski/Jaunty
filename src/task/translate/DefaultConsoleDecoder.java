package task.translate;

import java.util.Arrays;

public class DefaultConsoleDecoder implements ConsoleDecodeable {

	@Override
	public String consoleDecoding(int[] model) {
		return Arrays.toString(model);
	}

}
