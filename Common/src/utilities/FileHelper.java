package utilities;

import java.io.File;

public class FileHelper {
	public static boolean exists(String name) {
		File file = new File(name);
		
		return file.exists();
	}
}
