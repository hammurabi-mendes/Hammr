/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package utilities.counting;

import java.util.Set;
import java.util.HashSet;

import java.io.BufferedReader;

import java.io.IOException;

import utilities.InputGenerator;

import utilities.filesystem.Directory;
import utilities.filesystem.Filename;

import communication.channel.ChannelElement;

import mapreduce.communication.MRChannelElement;

public class CountingInputGenerator extends InputGenerator {
	public CountingInputGenerator(Directory directory, String[] inputOutputPairs) {
		super(directory, inputOutputPairs);
	}

	public CountingInputGenerator(Filename[] inputs, Filename[] outputs) {
		super(inputs, outputs);
	}

	protected String obtainBuffer(BufferedReader reader) throws IOException {
		return reader.readLine();
	}

	protected Set<ChannelElement> generateInput(String buffer) {
		Set<ChannelElement> result = new HashSet<ChannelElement>();

		String delimiters = "[^\\w]+";

		String[] words = buffer.split(delimiters);

		for(String word: words) {
			result.add(new MRChannelElement<String,Long>(word.toLowerCase(), 0L));
		}

		return result;
	}

	public static void main(String[] arguments) {
		CountingInputGenerator generator = new CountingInputGenerator(new Directory("."), arguments);

		try {
			generator.run();
		} catch (IOException exception) {
			System.err.println("Error generating input");

			exception.printStackTrace();
		}
	}
}
