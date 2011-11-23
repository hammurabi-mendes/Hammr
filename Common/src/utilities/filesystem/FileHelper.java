/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package utilities.filesystem;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;

import utilities.filesystem.Protocol;

import utilities.filesystem.hdfs.HDFSDriver;
import utilities.filesystem.posix.PosixDriver;

public class FileHelper {
	public static Filename getFileInformation(String path, String file, Protocol protocol) {
		boolean slashPresent = path.endsWith("/");

		return new Filename((slashPresent ? path : path + "/") + file, protocol);
	}

	public static Directory getDirectoryInformation(String string, Protocol protocol) {
		return new Directory(string, protocol);
	}

	public static InputStream openR(Filename filename) throws FileNotFoundException {
		return getFilesystemDriver(filename).openR(filename);
	}

	public static OutputStream openW(Filename filename) throws IOException {
		return getFilesystemDriver(filename).openW(filename);
	}

	public static boolean exists(Filename filename) {
		return getFilesystemDriver(filename).exists(filename);
	}

	public static long length(Filename filename) {
		return getFilesystemDriver(filename).length(filename);
	}

	public static boolean move(Filename source, Filename target) {
		FilesystemDriver driver1 = getFilesystemDriver(source);
		FilesystemDriver driver2 = getFilesystemDriver(target);

		if(driver1 != driver2) {
			return false;
		}

		return getFilesystemDriver(source).move(source, target);
	}

	public static boolean remove(Filename filename) {
		return getFilesystemDriver(filename).remove(filename);
	}

	private static FilesystemDriver getFilesystemDriver(Filename filename) {
		switch(filename.getProtocol()) {
		case POSIX_COMPATIBLE:
			return PosixDriver.getInstance();
		case HDFS:
			return HDFSDriver.getInstance();
		default:
			throw new IllegalArgumentException();
		}
	}
}
