package task.translate;

import java.io.File;
import java.io.IOException;

public interface FileDecodable {
	void fileDecoding(File dir, String filePrefix, int[] model) throws IOException;
	void fileDecoding(String filePrefix, int[] model) throws IOException;
}
