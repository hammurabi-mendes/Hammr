package appspecs;

import java.io.EOFException;
import java.io.IOException;
import java.io.Serializable;

import java.util.Collection;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import communication.ChannelHandler;
import communication.ChannelElement;

import execinfo.NodeGroup;

import utilities.ChannelElementReaderShuffler;
import utilities.ChannelElementWriterShuffler;

import utilities.MutableInteger;

public abstract class Node implements Serializable, Runnable {
	private static final long serialVersionUID = 1L;

	protected String name;

	protected NodeType type;

	protected Map<String, ChannelHandler> inputs;
	protected Map<String, ChannelHandler> outputs;

	protected MutableInteger mark;

	protected NodeGroup nodeGroup;

	protected ChannelElementReaderShuffler readersShuffler;
	protected ChannelElementWriterShuffler writersShuffler;

	public Node(String name, NodeType type) {
		setType(type);

		inputs = new HashMap<String, ChannelHandler>();
		outputs = new HashMap<String, ChannelHandler>();
	}

	public Node() {
		this(null, NodeType.COMMON);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setType(NodeType type) {
		this.type = type;
	}

	public NodeType getType() {
		return type;
	}

	/* INPUT getters/adders */

	public Set<String> getInputChannelNames() {
		return inputs.keySet();
	}

	public Collection<ChannelHandler> getInputChannelHandlers() {
		return inputs.values();
	}

	public void addInputChannelHandler(ChannelHandler input) {
		inputs.put(input.getName(), input);
	}

	public ChannelHandler getInputChannelHandler(String source) {
		return inputs.get(source);
	}

	/* OUTPUT getters/adders */

	public Set<String> getOutputChannelNames() {
		return outputs.keySet();
	}

	public Collection<ChannelHandler> getOutputChannelHandlers() {
		return outputs.values();
	}

	public void addOutputChannelHandler(ChannelHandler output) {
		outputs.put(output.getName(), output);
	}

	public ChannelHandler getOutputChannelHandler(String target) {
		return outputs.get(target);
	}

	/* Read Functions */

	public ChannelElement read(String name) {
		ChannelHandler channelHandler = getInputChannelHandler(name);

		if(channelHandler != null) {
			try {
				return channelHandler.read();
			} catch (EOFException exception) {
				return null;
			} catch (IOException exception) {
				System.err.println("Error reading channel element from node " + name + " for node " + this);

				exception.printStackTrace();
			}
		}

		System.err.println("Couldn't find channel handler " + name +  " for node " + this);

		return null;
	}

	public ChannelElement readSomeone() {
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

	/* Write Functions */

	public boolean write(ChannelElement channelElement, String name) {
		ChannelHandler channelHandler = getOutputChannelHandler(name);

		if(channelHandler != null) {
			try {
				channelHandler.write(channelElement);

				return true;
			} catch (IOException exception) {
				System.err.println("Error writing channel element to node " + name +  " for node " + this);

				exception.printStackTrace();
				return false;
			}
		}

		System.err.println("Couldn't find channel handler " + name +  " for node " + this);

		return false;
	}

	public boolean writeSomeone(ChannelElement channelElement) {
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

	public boolean writeEveryone(ChannelElement channelElement) {
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

	public boolean closeOutputs() {
		Collection<ChannelHandler> outputChannelHandlers = getOutputChannelHandlers();

		return closeChannelHandlers(outputChannelHandlers);
	}

	public boolean closeChannelHandlers(Collection<ChannelHandler> channelHandlers) {
		boolean finalResult = true;

		for(ChannelHandler channelHandler: channelHandlers) {
			try {
				boolean immediateResult = channelHandler.close();

				if(immediateResult == false) {
					System.err.println("Error closing channel handler " + channelHandler.getName() + " for node " + this);
				}

				finalResult |= immediateResult;
			} catch (IOException exception) {
				System.err.println("Error closing channel handler " + channelHandler.getName() + " for node " + this + " (I/O error)");

				exception.printStackTrace();
				finalResult |= false;
			}
		}

		return finalResult;
	}

	/* ReaderShuffler and WriterShuffler functions */

	private void createReaderShuffler() {
		try {
			readersShuffler = new ChannelElementReaderShuffler(inputs);
		} catch (IOException exception) {
			System.err.println("Error creating read shuffler for node " + this);

			exception.printStackTrace();
		}
	}

	private void createWriterShuffler() {
		Collection<ChannelHandler> channelHandlers = getOutputChannelHandlers();

		writersShuffler = new ChannelElementWriterShuffler(channelHandlers);
	}

	/* Marking functions */

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

	/* NodeGroup functions */

	public void setNodeGroup(NodeGroup nodeGroup) {
		this.nodeGroup = nodeGroup;
	}

	public NodeGroup getNodeGroup() {
		return nodeGroup;
	}

	/* Run & print functions */

	public abstract void run();

	public String toString() {
		return name;
	}
}
