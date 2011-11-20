package mapreduce.programs;

import java.io.Serializable;

import communication.writer.ChannelElementWriter;

import mapreduce.communication.MRChannelElement;

public abstract class Reducer<INKEY extends Comparable<INKEY>,INVALUE,OUTKEY extends Comparable<OUTKEY>,OUTVALUE> implements Serializable
{
	private static final long serialVersionUID = 1L;
	public abstract void reduce(INKEY key, Iterable<INVALUE> values, ChannelElementWriter<MRChannelElement<OUTKEY, OUTVALUE>> writer) throws Exception;
	/*
	 * Default cleanup function does nothing
	 */
	public void cleanup(ChannelElementWriter<MRChannelElement<OUTKEY, OUTVALUE>> writer) throws Exception {};
}
