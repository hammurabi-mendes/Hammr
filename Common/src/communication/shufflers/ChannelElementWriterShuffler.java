/*
Copyright (c) 2010, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package communication.shufflers;

import java.util.Random;

import java.util.Collection;

import java.util.List;
import java.util.ArrayList;

import communication.channel.OutputChannel;
import communication.channel.ChannelElement;

import java.io.IOException;

public class ChannelElementWriterShuffler {
	private List<OutputChannel> outputChannels;

	Random random;

	public ChannelElementWriterShuffler(Collection<OutputChannel> outputChannels) {
		this.outputChannels = new ArrayList<OutputChannel>(outputChannels);

		random = new Random();
	}

	public boolean writeSomeone(ChannelElement channelElement) throws IOException {
		if(outputChannels.size() == 0) {
			return false;
		}

		int index = random.nextInt(outputChannels.size());

		OutputChannel outputChannel = outputChannels.get(index);

		return outputChannel.write(channelElement);
	}
}
