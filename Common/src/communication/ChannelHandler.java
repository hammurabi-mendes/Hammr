/*
Copyright (c) 2010, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package communication;

import java.io.EOFException;
import java.io.IOException;

import java.io.Serializable;

public abstract class ChannelHandler implements Serializable {
	private static final long serialVersionUID = 1L;

	private Type type;
	private Mode mode;
	private String name;

	private ChannelElementReader channelElementReader;
	private ChannelElementWriter channelElementWriter;

	public ChannelHandler(Type type, Mode mode, String name) {
		this.type = type;
		this.mode = mode;

		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public Mode getMode() {
		return mode;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public ChannelElementReader getChannelElementReader() {
		return channelElementReader;
	}

	public void setChannelElementReader(ChannelElementReader channelElementReader) {
		this.channelElementReader = channelElementReader;
	}

	public ChannelElementWriter getChannelElementWriter() {
		return channelElementWriter;
	}

	public void setChannelElementWriter(ChannelElementWriter channelElementWriter) {
		this.channelElementWriter = channelElementWriter;
	}

	public ChannelElement read() throws EOFException, IOException {
		return channelElementReader.read();
	}

	public boolean write(ChannelElement channelElement) throws IOException {
		return channelElementWriter.write(channelElement);
	}

	public boolean close() throws IOException {
		if(channelElementWriter != null) {
			return channelElementWriter.close();
		}

		return true;
	}

	public enum Type {
		SHM, TCP, FILE;
	}

	public enum Mode {
		INPUT, OUTPUT;
	}
}
