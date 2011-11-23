package utilities.filesystem;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;

public interface FilesystemDriver {
	public InputStream openR(Filename filename) throws FileNotFoundException;

	public OutputStream openW(Filename filename) throws IOException;

	public boolean exists(Filename filename);

	public long length(Filename filename);

	public boolean move(Filename source, Filename target);

	public boolean remove(Filename filename);
}
