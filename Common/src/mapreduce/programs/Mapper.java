package mapreduce.programs;

import java.io.Serializable;

import communication.writer.ChannelElementWriter;

import mapreduce.communication.MRChannelElement;

public abstract class Mapper<INKEY, INVALUE, OUTKEY extends Comparable<OUTKEY>, OUTVALUE> implements Serializable
{
	public abstract void map(INKEY key, INVALUE value, ChannelElementWriter<MRChannelElement<OUTKEY, OUTVALUE>> writer) throws Exception;
	/*
	 * Default cleanup function does nothing
	 */
	public void cleanup(ChannelElementWriter<MRChannelElement<OUTKEY, OUTVALUE>> writer) throws Exception {}; 
}
