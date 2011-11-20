package fs;

import java.io.IOException;
import java.io.FileNotFoundException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import utilities.Logging;

import conf.Config;

public class HDFSDriver implements DistributedFileSystem {

	private static FileSystem hdfs;

	public HDFSDriver(String hdfsUri) {
		try {
			hdfs = FileSystem.get(URI.create(hdfsUri), new Configuration());
		} catch (IOException e) {
			Logging.Info("[HDFSDriver] Failed to initial HDFS, exit. Exception: " + e);
			System.exit(0);
		}
	}

	@Override
	public OutputStream create(String path) {
		try {
			return hdfs.create(new Path(path), Config.HDFS_REPLICATION);
		} catch (IOException e) {
			Logging.Info("[HDFSDriver] Failed to create OutputStream " + path + ". Exception: " + e);
			return null;
		}
	}

	@Override
	public long getFileLength(String path) throws IOException {
		return hdfs.getFileStatus(new Path(path)).getLen();
	}

	@Override
	public InputStream open(String path) {
		try {
			return hdfs.open(new Path(path));
		} catch (IOException e) {
			Logging.Info("[HDFSDriver] Failed to create InputStream " + path + "." + "Exception: " + e);
			return null;
		}
	}

	@Override
	public BlockLocation[] getFileBlockLocations(String path) throws IOException {
		if (hdfs == null) {
			Logging.Info("[HDFSDriver] HDFS is null.");
			throw new IOException();
		}
		if (!hdfs.exists(new Path(path))) {
			Logging.Info("[HDFSDriver] File doesn't exist. Path: " + path);
			throw new FileNotFoundException();
		}
		org.apache.hadoop.fs.BlockLocation[] hdfsBlockLocations = hdfs.getFileBlockLocations(
				hdfs.getFileStatus(new Path(path)), 0, getFileLength(path));
		BlockLocation[] blockLocations = new BlockLocation[hdfsBlockLocations.length];
		int i = 0;
		for (org.apache.hadoop.fs.BlockLocation hdfsBlockLoc : hdfsBlockLocations) {
			blockLocations[i++] = new BlockLocation(hdfsBlockLoc.getHosts(), hdfsBlockLoc.getOffset(),
					hdfsBlockLoc.getLength());
		}

		return blockLocations;
	}

	@Override
	public long getBlockSize(String path) throws IOException {
		FileStatus status = hdfs.getFileStatus(new Path(path));
		return status.getBlockSize();
	}

	@Override
	public boolean remove(String path) throws IOException {
		return hdfs.delete(new Path(path), true);
	}

	@Override
	public List<String> list(String sPath) throws IOException {
		Path path = new Path(sPath);

		FileStatus _fs = hdfs.getFileStatus(path);
		
		List<String> l = null;
		
		if (_fs.isDir()) {
			FileStatus[] fss = hdfs.listStatus(path);
			l = new ArrayList<String>();
			for (FileStatus fs : fss) {
				l.add(sPath + "/" + fs.getPath().getName());
			}
		} else
		{
			l = Collections.singletonList(sPath);
		}
		return l;
	}

}
