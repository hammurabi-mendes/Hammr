/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package utilities;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileHelper {
	public static Filename getFileInformation(String path, String file, Protocol protocol) {
		boolean slashPresent = path.endsWith("/");

		return new Filename((slashPresent ? path : path + "/") + file, protocol);
	}

	public static Directory getDirectoryInformation(String string, Protocol protocol) {
		return new Directory(string, protocol);
	}

	public static InputStream openR(Filename filename) throws FileNotFoundException {
		return new FileInputStream(filename.getLocation());
	}

	public static OutputStream openW(Filename filename) throws FileNotFoundException {
		return new FileOutputStream(filename.getLocation());
	}

	public static boolean exists(Filename filename) {
		File file = new File(filename.getLocation());

		return file.exists();
	}

	public static long length(Filename filename) {
		File file = new File(filename.getLocation());

		return file.length();
	}

	public static boolean move(Filename source, Filename target) {
		File fileSource = new File(source.getLocation());
		File fileTarget = new File(target.getLocation());

		return fileSource.renameTo(fileTarget);
	}

	public static boolean remove(Filename filename) {
		File file = new File(filename.getLocation());

		return file.delete();
	}
}
