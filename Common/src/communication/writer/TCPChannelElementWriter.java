/*
Copyright (c) 2010, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package communication.writer;

import java.net.Socket;
import java.net.InetSocketAddress;

import java.io.IOException;

import communication.channel.ChannelElement;
import communication.interfaces.ChannelElementWriter;

import communication.stream.ChannelElementOutputStream;

public class TCPChannelElementWriter implements ChannelElementWriter {
	private String name;
	private ChannelElementOutputStream channelElementOutputStream;

	public TCPChannelElementWriter(String name, InetSocketAddress socketAddress) throws IOException {
		this.name = name;

		Socket socket = new Socket(socketAddress.getAddress(), socketAddress.getPort());

		this.channelElementOutputStream = new ChannelElementOutputStream(socket.getOutputStream());

		channelElementOutputStream.writeObject(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean write(ChannelElement channelElement) throws IOException {
		channelElementOutputStream.writeChannelElement(channelElement);

		return true;
	}

	public boolean flush() throws IOException {
		channelElementOutputStream.flush();
		channelElementOutputStream.reset();

		return true;
	}

	public boolean close() throws IOException {
		channelElementOutputStream.flush();

		channelElementOutputStream.close();

		return true;
	}
}
