/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package utilities.counting;

import java.io.IOException;

import utilities.OutputExtractor;

import communication.channel.ChannelElement;

import mapreduce.communication.MRChannelElement;

import utilities.filesystem.Filename;
import utilities.filesystem.Directory;

public class CountingOutputExtractor extends OutputExtractor {
	public CountingOutputExtractor(Directory directory, String[] inputOutputPairs) {
		super(directory, inputOutputPairs);
	}

	public CountingOutputExtractor(Filename[] inputs, Filename[] outputs) {
		super(inputs, outputs);
	}

	protected String obtainInformation(ChannelElement genericChannelElement) {
		@SuppressWarnings("unchecked")
		MRChannelElement<String,Long> channelElement = (MRChannelElement<String,Long>) genericChannelElement;

		return (channelElement.getObject()) +  " - " + channelElement.getValue() + "\n";
	}

	public static void main(String[] arguments) {
		if(arguments.length <= 3) {
			System.err.println("Usage: CountingOutputExtractor <directory> <input> ... <input> : <output> ... <output>");

			System.exit(1);
		}

		String directory = arguments[0];

		String[] filenames = new String[arguments.length - 1];

		for(int i = 1; i < arguments.length; i++) {
			filenames[i - 1] = arguments[i];
		}

		CountingOutputExtractor extractor = new CountingOutputExtractor(new Directory(directory), filenames);

		try {
			extractor.run();
		} catch (IOException exception) {
			System.err.println("Error generating input");

			exception.printStackTrace();
		}
	}
}
