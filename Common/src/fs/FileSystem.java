package fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface FileSystem {
	public InputStream open(String path) throws IOException;
	public OutputStream create(String path) throws IOException;
	public long getFileLength(String path) throws IOException;
	public boolean remove(String path) throws IOException;
	public List<String> list(String dir) throws IOException;
}
