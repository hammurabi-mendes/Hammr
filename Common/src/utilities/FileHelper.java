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
import java.io.InputStream;
import java.io.OutputStream;

import appspecs.DirectoryInformation;

public class FileHelper {
	public static boolean exists(FileInformation input) {
		File file = new File(input);

		return file.exists();
	}

	public static FileInformation getFileInformation(String path, String file) {
		return getFileInformation(path, file, FileProtocol.POSIX_COMPATIBLE);
	}

	public static FileInformation getFileInformation(String path, String file, FileProtocol protocol) {
		boolean slashPresent = path.endsWith("/");

		return new FileInformation((slashPresent ? path : path + "/") + file, protocol);
	}

	public static boolean move(FileInformation source, FileInformation target) {
		File fileSource = new File(source);
		File fileTarget = new File(target);

		return fileSource.renameTo(fileTarget);
	}

	public static InputStream openR(FileInformation fileInformation) {
		// TODO Auto-generated method stub
		return null;
	}

	public static OutputStream openW(FileInformation fileInformation) {
		// TODO Auto-generated method stub
		return null;
	}

	public static long getLength(FileInformation fileInformation) {
		// TODO Auto-generated method stub
		return 0;
	}

	public static boolean remove(FileInformation fileInformation) {
		// TODO Auto-generated method stub
		return false;
	}

	public static DirectoryInformation getDirectoryInformation(String string) {
		// TODO Auto-generated method stub
		return null;
	}
