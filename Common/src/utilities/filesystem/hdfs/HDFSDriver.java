/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package utilities.filesystem.hdfs;

import java.io.IOException;
import java.io.FileNotFoundException;

import java.io.InputStream;
import java.io.OutputStream;

import java.net.URI;

import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.fs.FileSystem;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileStatus;

import org.apache.hadoop.fs.BlockLocation;

import utilities.filesystem.FilesystemDriver;
import utilities.filesystem.Filename;

public class HDFSDriver implements FilesystemDriver {
	private static final String HDFS_DEFAULT_DIRECTORY = "/hdfs";

	private static final short HDFS_REPLICATION_FACTOR = 3;

	private static HDFSDriver instance;

	private FileSystem hdfs;

	static {
		instance = new HDFSDriver(HDFS_DEFAULT_DIRECTORY);
	}

	public HDFSDriver(String location) {
		try {
			hdfs = FileSystem.get(URI.create(location), new Configuration());
		} catch (IOException exception) {
			exception.printStackTrace();

			System.exit(1);
		}
	}

	public static HDFSDriver getInstance() {
		return instance;
	}

	public InputStream openR(Filename filename) throws FileNotFoundException {
		try {
			return hdfs.open(new Path(filename.getLocation()));
		} catch (IOException exception) {
			throw new FileNotFoundException();
		}
	}

	public OutputStream openW(Filename filename) throws IOException {
		return hdfs.create(new Path(filename.getLocation()), HDFSDriver.HDFS_REPLICATION_FACTOR);
	}

	public boolean exists(Filename filename) {
		Path path = new Path(filename.getLocation());

		try {
			return hdfs.exists(path);
		} catch (IOException exception) {
			return false;
		}
	}

	public long length(Filename filename) {
		Path path = new Path(filename.getLocation());

		try {
			FileStatus status = hdfs.getFileStatus(path);

			return status.getLen();
		} catch (IOException e) {
			return 0L;
		}
	}

	public boolean move(Filename source, Filename target) {
		Path fileSource = new Path(source.getLocation());
		Path fileTarget = new Path(target.getLocation());

		try {
			return hdfs.rename(fileSource, fileTarget);
		} catch (IOException exception) {
			return false;
		}
	}

	public boolean remove(Filename filename) {
		Path path = new Path(filename.getLocation());

		try {
			return hdfs.delete(path, false);
		} catch (IOException exception) {
			return false;
		}
	}

	public long getBlockSize(Filename filename) throws IOException {
		Path path = new Path(filename.getLocation());

		FileStatus status = hdfs.getFileStatus(path);

		return status.getBlockSize();
	}

	public HDFSBlockLocation[] getFileBlockLocations(Filename filename) throws FileNotFoundException, IOException {
		if(!hdfs.exists(new Path(filename.getLocation()))) {
			throw new FileNotFoundException();
		}

		BlockLocation[] blockLocations1 = hdfs.getFileBlockLocations(hdfs.getFileStatus(new Path(filename.getLocation())), 0, length(filename));

		HDFSBlockLocation[] blockLocations2 = new HDFSBlockLocation[blockLocations1.length];

		for(int i = 0; i < blockLocations1.length; i++) {
			blockLocations2[i] = new HDFSBlockLocation(blockLocations1[i].getHosts(), blockLocations1[i].getOffset(), blockLocations1[i].getLength());
		}

		return blockLocations2;
	}
}
