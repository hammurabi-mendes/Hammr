package appspecs;

import java.io.EOFException;
import java.io.IOException;
import java.io.Serializable;

import java.util.Collection;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import mapreduce.programs.MapperNode;
import mapreduce.programs.ReducerNode;

import communication.channel.ChannelElement;
import communication.channel.Channel;
import communication.channel.FileInputChannel;
import communication.channel.InputChannel;
import communication.channel.OutputChannel;
import communication.reader.ChannelElementReader;
import communication.writer.ChannelElementWriter;

import execinfo.NodeGroup;

import utilities.HashChannelElementWriterShuffler;
import utilities.Logging;
import utilities.RandomChannelElementReaderShuffler;
import utilities.RandomChannelElementWriterShuffler;

import utilities.MutableInteger;

public abstract class Node implements Serializable, Runnable {
	private static final long serialVersionUID = 1L;

	protected boolean killed = false;
	protected boolean blocked = false;
	
	protected String name;

	protected NodeType type;

	protected Map<String, InputChannel> inputs;
	protected Map<String, OutputChannel> outputs;

	protected MutableInteger mark;

	protected NodeGroup nodeGroup;

	protected ChannelElementReader readersShuffler = null;
	protected ChannelElementWriter writersShuffler = null;

	public Node(String name, NodeType type) {
		this.name = name;
		this.type = type;
		inputs = new HashMap<String, InputChannel>();
		outputs = new HashMap<String, OutputChannel>();
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

	public final Collection<InputChannel> getInputChannels() {
		return inputs.values();
	}

	public final void addInputChannel(InputChannel input) {
		inputs.put(input.getName(), input);
	}

	public final InputChannel getInputChannel(String source) {
		return inputs.get(source);
	}

	/* OUTPUT getters/adders */

	public Set<String> getOutputChannelNames() {
		return outputs.keySet();
	}

	public final Collection<OutputChannel> getOutputChannels() {
		return outputs.values();
	}

	public final void addOutputChannel(OutputChannel output) {
		outputs.put(output.getName(), output);
	}

	public final OutputChannel getOutputChannel(String target) {
		return outputs.get(target);
	}

	/* Read Functions */

	public ChannelElement read(String name) {
		if (readersShuffler != null){
			Logging.Info("[Node][read] ***ReadersShuffler is not null!***");
			System.exit(1);
		}
		
		InputChannel channelHandler = getInputChannel(name);

		if (channelHandler != null) {
			try {
				return channelHandler.read();
			} catch (EOFException exception) {
				Logging.Info("[Node][read] EOF. Node: " + this.name + " Channel: " + name);
				return null;
			} catch (IOException exception) {
				Logging.Info("Error reading channel element from node " + name + " for node " + this);

				exception.printStackTrace();
			}
		}

		Logging.Info("Couldn't find channel handler " + name + " for node " + this);

		return null;
	}

	public final ChannelElement readSomeone() {
		if (readersShuffler == null)
			createReadersShuffler();

		try {
			return readersShuffler.read();
		} catch (EOFException exception) {
			return null;
		} catch (Exception exception) {
			Logging.Info("Error reading from arbitrary channel element from node " + this);

			exception.printStackTrace();
		}

		return null;
	}

	/* Write Functions */

	public final boolean write(ChannelElement channelElement, String name) {
		OutputChannel channelHandler = getOutputChannel(name);

		if (channelHandler != null) {
			try {
				channelHandler.write(channelElement);

				return true;
			} catch (IOException exception) {
				Logging.Info("Error writing channel element to node " + name + " for node " + this);

				exception.printStackTrace();
				return false;
			}
		}

		Logging.Info("Couldn't find channel handler " + name + " for node " + this);

		return false;
	}

	public final boolean write(ChannelElement channelElement) {
		try {
			return writersShuffler.write(channelElement);
		} catch (Exception exception) {
			Logging.Info("Error writing to arbitary channel element from node " + this);

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

	public final boolean flushAndCloseOutputs() {
		try {
			writersShuffler.flush();
			return writersShuffler.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/* ReaderShuffler and WriterShuffler functions */

	public void createReadersShuffler() {
		try {
			readersShuffler = new RandomChannelElementReaderShuffler(inputs);
		} catch (IOException exception) {
			Logging.Info("Error creating read shuffler for node " + this);

			exception.printStackTrace();
		}
	}

	public void createWritersShuffler() {
		Collection<OutputChannel> channelHandlers = getOutputChannels();
		
		/**
		 * TODO: Fix this instanceof, should use better programming style
		 */
		if(this instanceof MapperNode || this instanceof ReducerNode)
		{
			writersShuffler = new HashChannelElementWriterShuffler(channelHandlers);
		}
		else
		{
			writersShuffler = new RandomChannelElementWriterShuffler(channelHandlers);
		}
	}

	public void setWritersShuffler(ChannelElementWriter writersShuffler) {
		this.writersShuffler = writersShuffler;
	}

	public ChannelElementWriter getWritersShuffler() {
		return writersShuffler;
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

	@Override
	public String toString() {
		return name;
	}
	
	/* preemption */
	public final void kill() {
		killed = true;
	}

	public final boolean isKilled() {
		return killed;
	}

	public final void block() {
		blocked = true;
	}

	public final boolean isBlocked() {
		return blocked;
	}

	public final Long getInputFilesLength() {
		Long totalLength = 0L;
		for (Channel handler : inputs.values()) {
			if (handler instanceof FileInputChannel) {
				totalLength += ((FileInputChannel) handler).getLength();
			}
		}
		return totalLength;
	}
	
}
