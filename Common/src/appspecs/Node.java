/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package appspecs;

import java.util.concurrent.TimeUnit;

import java.io.EOFException;
import java.io.IOException;
import java.io.Serializable;

import java.util.Collection;

import java.util.Set;
import java.util.Map;

import java.util.HashSet;
import java.util.HashMap;

import communication.channel.ChannelElement;

import communication.channel.FileInputChannel;
import communication.channel.InputChannel;
import communication.channel.OutputChannel;

import communication.shufflers.ChannelElementReaderShuffler;
import communication.shufflers.ChannelElementWriterShuffler;

import utilities.MutableInteger;

import execinfo.NodeGroup;

public abstract class Node implements Serializable, Runnable {
	private static final long serialVersionUID = 1L;

	///////////////////////////////
	// SPECIFICATION INFORMATION //
	///////////////////////////////

	protected String name;

	protected Map<String, InputChannel> inputs;
	protected Map<String, OutputChannel> outputs;

	protected Map<String, InputChannel> structuralInputs;
	protected Map<String, OutputChannel> structuralOutputs;


	protected Map<String, InputChannel> applicationInputs;
	protected Map<String, OutputChannel> applicationOutputs;

	protected ChannelElementReaderShuffler readersShuffler;
	protected ChannelElementWriterShuffler writersShuffler;

	/////////////////////////
	// RUNNING INFORMATION //
	/////////////////////////

	protected NodeGroup nodeGroup;

	/////////////////////////
	// PARSING INFORMATION //
	/////////////////////////

	protected MutableInteger mark;

	public Node() {
		this(null);
	}

	public Node(String name) {
		inputs = new HashMap<String, InputChannel>();
		outputs = new HashMap<String, OutputChannel>();

		applicationInputs = new HashMap<String, InputChannel>();
		applicationOutputs = new HashMap<String, OutputChannel>();

		structuralInputs = new HashMap<String, InputChannel>();
		structuralOutputs = new HashMap<String, OutputChannel>();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/* INPUT getters/adders */

	public Set<String> getInputChannelNames() {
		return inputs.keySet();
	}

	public Set<String> getApplicationInputChannelNames() {
		return applicationInputs.keySet();
	}

	public Set<String> getInputChannelNames(Class<? extends InputChannel> type) {
		Set<String> result = new HashSet<String>();

		for(InputChannel inputChannel: getInputChannels()) {
			if(type.isInstance(inputChannel)) {
				result.add(inputChannel.getName());
			}
		}

		return result;
	}

	public Set<String> getStrucutralInputChannelNames() {
		return applicationInputs.keySet();
	}

	public Collection<InputChannel> getInputChannels() {
		return inputs.values();
	}

	public Set<InputChannel> getInputChannels(Class<? extends InputChannel> type) {
		Set<InputChannel> result = new HashSet<InputChannel>();

		for(InputChannel inputChannel: getInputChannels()) {
			if(type.isInstance(inputChannel)) {
				result.add(inputChannel);
			}
		}

		return result;
	}

	public Collection<InputChannel> getApplicationInputChannels() {
		return applicationInputs.values();
	}

	public Collection<InputChannel> getStructuralInputChannels() {
		return structuralInputs.values();
	}

	public void addInputChannel(String source, InputChannel input, boolean applicationInput) {
		inputs.put(source, input);

		if(applicationInput) {
			applicationInputs.put(source, input);
		}
		else {
			structuralInputs.put(source, input);
		}
	}

	public InputChannel delInputChannel(String source) {
		return inputs.remove(source);
	}

	public InputChannel getInputChannel(String source) {
		return inputs.get(source);
	}

	/* OUTPUT getters/adders */

	public Set<String> getOutputChannelNames() {
		return outputs.keySet();
	}

	public Set<String> getOutputChannelNames(Class<? extends OutputChannel> type) {
		Set<String> result = new HashSet<String>();

		for(OutputChannel outputChannel: getOutputChannels()) {
			if(type.isInstance(outputChannel)) {
				result.add(outputChannel.getName());
			}
		}

		return result;
	}

	public Set<String> getApplicationOutputChannelNames() {
		return applicationOutputs.keySet();
	}

	public Set<String> getStructuralOutputChannelNames() {
		return structuralOutputs.keySet();
	}

	public Collection<OutputChannel> getOutputChannels() {
		return outputs.values();
	}

	public Set<OutputChannel> getOutputChannels(Class<? extends OutputChannel> type) {
		Set<OutputChannel> result = new HashSet<OutputChannel>();

		for(OutputChannel outputChannel: getOutputChannels()) {
			if(type.isInstance(outputChannel)) {
				result.add(outputChannel);
			}
		}

		return result;
	}

	public Collection<OutputChannel> getApplicationOutputChannels() {
		return applicationOutputs.values();
	}

	public Collection<OutputChannel> getStructuralOutputChannels() {
		return structuralOutputs.values();
	}

	public void addOutputChannel(String target, OutputChannel output, boolean applicationOutput) {
		outputs.put(target, output);

		if(applicationOutput) {
			applicationOutputs.put(target, output);
		}
		else {
			structuralOutputs.put(target, output);
		}
	}

	public OutputChannel delOutputChannel(String target) {
		return outputs.remove(target);
	}

	public OutputChannel getOutputChannel(String target) {
		return outputs.get(target);
	}

	/* Read Functions */

	protected ChannelElement read(String name) {
		InputChannel inputChannel = getInputChannel(name);

		if(inputChannel != null) {
			try {
				return inputChannel.read();
			} catch (EOFException exception) {
				return null;
			} catch (IOException exception) {
				System.err.println("Error reading channel element from node " + name + " for node " + this);

				exception.printStackTrace();
			}
		}

		System.err.println("Couldn't find input channel " + name +  " for node " + this);

		return null;
	}

	protected ChannelElement readSomeone() {
		if(readersShuffler == null) {
			createReaderShuffler();
		}

		try {
			return readersShuffler.readSomeone();
		} catch (EOFException exception) {
			return null;
		} catch (IOException exception) {
			System.err.println("Error reading from arbitrary channel element from node " + this);

			exception.printStackTrace();
		}

		return null;
	}

	protected ChannelElement tryReadSomeone() {
		// You need to create the read shuffler manually if you want to use this method

		if(readersShuffler == null) {
			createReaderShuffler();
		}

		try {
			return readersShuffler.tryReadSomeone();
		} catch (IOException exception) {
			System.err.println("Error reading from arbitrary channel element from node " + this);

			exception.printStackTrace();
		}

		return null;
	}

	protected ChannelElement tryReadSomeone(int timeout, TimeUnit timeUnit) {
		// You need to create the read shuffler manually if you want to use this method

		if(readersShuffler == null) {
			createReaderShuffler();
		}

		try {
			return readersShuffler.tryReadSomeone(timeout, timeUnit);
		} catch (IOException exception) {
			System.err.println("Error reading from arbitrary channel element from node " + this);

			exception.printStackTrace();
		}

		return null;
	}

	/* Write Functions */

	protected boolean write(ChannelElement channelElement, String name) {
		OutputChannel outputChannel = getOutputChannel(name);

		if(outputChannel != null) {
			try {
				outputChannel.write(channelElement);

				return true;
			} catch (IOException exception) {
				System.err.println("Error writing channel element to node " + name +  " for node " + this);

				exception.printStackTrace();
				return false;
			}
		}

		System.err.println("Couldn't find output channel " + name +  " for node " + this);

		return false;
	}

	protected boolean writeSomeone(ChannelElement channelElement) {
		if(writersShuffler == null) {
			createWriterShuffler();
		}

		try {
			return writersShuffler.writeSomeone(channelElement);
		} catch (IOException exception) {
			System.err.println("Error writing to arbitary channel element from node " + this);

			exception.printStackTrace();
		}

		return false;
	}

	protected boolean writeEveryone(ChannelElement channelElement) {
		Set<String> outputChannelNames = getOutputChannelNames();

		boolean finalResult = true;

		for(String outputChannelName: outputChannelNames) {
			boolean immediateResult = write(channelElement, outputChannelName);

			if(immediateResult == false) {
				System.err.println("Error writing to all channel elements (error on channel element " + outputChannelName + ") from node " + this);
			}

			finalResult |= immediateResult;
		}

		return finalResult;
	}

	/* Close functions */

	protected void shutdown() {
		closeInputs(getInputChannels(FileInputChannel.class));

		closeOutputs();
	}

	protected void closeInputs() {
		Collection<InputChannel> inputChannels = getInputChannels();

		closeInputs(inputChannels);		
	}

	protected void closeInputs(Collection<InputChannel> inputChannels) {
		for(InputChannel inputChannel: inputChannels) {
			try {
				inputChannel.close();
			} catch (IOException exception) {
				System.err.println("Error closing output channel " + inputChannel.getName() + " for node " + this + " (I/O error)");

				exception.printStackTrace();
			}
		}
	}

	protected boolean closeOutputs() {
		Collection<OutputChannel> outputChannels = getOutputChannels();

		return closeOutputs(outputChannels);
	}

	protected boolean closeOutputs(Collection<OutputChannel> outputChannels) {
		boolean finalResult = true;

		for(OutputChannel outputChannel: outputChannels) {
			try {
				boolean immediateResult = outputChannel.close();

				if(immediateResult == false) {
					System.err.println("Error closing output channel " + outputChannel.getName() + " for node " + this);
				}

				finalResult |= immediateResult;
			} catch (IOException exception) {
				System.err.println("Error closing output channel " + outputChannel.getName() + " for node " + this + " (I/O error)");

				exception.printStackTrace();
				finalResult |= false;
			}
		}

		return finalResult;
	}

	/* ReaderShuffler and WriterShuffler functions */

	protected void createReaderShuffler() {
		try {
			readersShuffler = new ChannelElementReaderShuffler(inputs);
		} catch (IOException exception) {
			System.err.println("Error creating read shuffler for node " + this);

			exception.printStackTrace();
		}
	}

	protected void createReaderShuffler(boolean structural, boolean application) {
		Map<String, InputChannel> selected = null;

		if(structural && application) {
			selected = inputs;
		}

		if(structural && !application) {
			selected = structuralInputs;
		}

		if(!structural && application) {
			selected = applicationInputs;
		}

		try {
			readersShuffler = new ChannelElementReaderShuffler(selected);
		} catch (IOException exception) {
			System.err.println("Error creating read shuffler for node " + this);

			exception.printStackTrace();
		}
	}

	protected void createWriterShuffler() {
		Collection<OutputChannel> outputChannels = getOutputChannels();

		writersShuffler = new ChannelElementWriterShuffler(outputChannels);
	}

	/* Running functions */

	public void setNodeGroup(NodeGroup nodeGroup) {
		this.nodeGroup = nodeGroup;
	}

	public NodeGroup getNodeGroup() {
		return nodeGroup;
	}

	public void prepareSchedule() {
		setMark(null);
	}

	/* Parsing functions */

	public MutableInteger getMark() {
		return mark;
	}

	public void setMark(MutableInteger mark) {
		if(!isMarked() || mark == null) {
			this.mark = mark;
		}
		else {
			this.mark.setValue(mark.getValue());
		}
	}

	public boolean isMarked() {
		return (mark != null);
	}

	/* Run & print functions */

	public abstract void run();

	public String toString() {
		return name;
	}
}
