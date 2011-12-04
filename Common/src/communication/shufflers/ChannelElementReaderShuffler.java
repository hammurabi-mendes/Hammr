/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package communication.shufflers;

import java.util.concurrent.TimeUnit;

import java.util.Map;

import java.io.EOFException;
import java.io.IOException;

import communication.channel.ChannelElement;
import communication.channel.InputChannel;
import communication.readers.SHMChannelElementMultiplexer;

public class ChannelElementReaderShuffler {
	private SHMChannelElementMultiplexer multiplexer;

	public ChannelElementReaderShuffler(Map<String, InputChannel> inputs) throws IOException {
		multiplexer = new SHMChannelElementMultiplexer(inputs.keySet());

		Relayer relayer;

		for(String name: inputs.keySet()) {
			relayer = new Relayer(name, inputs.get(name), multiplexer);

			relayer.start();
		}
	}

	public ChannelElement readSomeone() throws EOFException, IOException {
		return multiplexer.read();
	}

	public ChannelElement tryReadSomeone() throws IOException {
		return multiplexer.tryRead();
	}

	public ChannelElement tryReadSomeone(int timeout, TimeUnit timeUnit) throws IOException {
		return multiplexer.tryRead(timeout, timeUnit);
	}
	
	public ChannelElement peek() {
		return multiplexer.peek();
	}

	private class Relayer extends Thread {
		private String origin;

		private InputChannel inputChannel;

		private SHMChannelElementMultiplexer multiplexer;

		public Relayer(String origin, InputChannel inputChannel, SHMChannelElementMultiplexer multiplexer) {
			this.origin = origin;

			this.inputChannel = inputChannel;

			this.multiplexer = multiplexer;
		}

		public void run() {
			ChannelElement channelElement;

			while(true) {
				try {
					channelElement = inputChannel.read();
				} catch (EOFException exception) {
					break;
				} catch (IOException exception) {
					System.err.println("Error reading channel handler for reader shuffler");

					exception.printStackTrace();
					break;
				}

				try {
					multiplexer.write(origin, channelElement);
				} catch (IOException exception) {
					System.err.println("Error writing channel handler data to local reader shuffler multiplexer");

					exception.printStackTrace();
					break;
				}
			}

			multiplexer.close(origin);
		}
	}
}
