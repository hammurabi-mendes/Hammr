package fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.List;

import utilities.Logging;


public class LocalFileSystemImp implements FileSystem {
	private String _rootDir;

	public LocalFileSystemImp(String root) {
		if (!root.equals("")) {
			File rootDir = new File(root);
			rootDir.mkdir();

			// debug code
			if (!rootDir.isDirectory()) {
				Logging.Info("[LocalFileSystemImp] Failed to create rootDir " + root);
				System.exit(0);
			}
		}
		_rootDir = root;
	}

	private String absolutePath(String path){
		return _rootDir.equals("")? path : _rootDir + "/" + path;
	}
	
	@Override
	public long getFileLength(String filePath) {
		String path = absolutePath(filePath);
		File file = new File(path);
		if (!file.exists() || !file.isFile()) {
			Logging.Info("File " + path + "doesn't exist!");
			return -1;
		}
		return file.length();
	}

	@Override
	public OutputStream create(String filePath) {
		String path = absolutePath(filePath);
		try {
			return new BufferedOutputStream(new FileOutputStream(path));
		} catch (FileNotFoundException e) {
			Logging.Info("[LocalFileSystemImp] Fail to create file " + path);
			return null;
		}
	}

	@Override
	public InputStream open(String filePath) {
		String path = absolutePath(filePath);
		try {
			return new BufferedInputStream(new FileInputStream(path));
		} catch (FileNotFoundException e) {
			Logging.Info("[LocalFileSystemImp] Fail to open file " + path);
			return null;
		}
	}
	
	@Override
	public boolean remove(String filePath)
	{
		String path = absolutePath(filePath);
		File f = new File(path);
		return f.delete();
	}

	@Override
	public List<String> list(String dir) throws IOException {
		throw new IOException("Not Implemented");
	}
}
