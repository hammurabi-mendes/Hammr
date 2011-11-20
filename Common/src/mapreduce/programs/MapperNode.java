package mapreduce.programs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import utilities.Logging;
import utilities.Pair;

import communication.writer.ChannelElementWriter;

import mapreduce.communication.MRChannelElement;
import appspecs.Node;

public final class MapperNode<INKEY, INVALUE, OUTKEY extends Comparable<OUTKEY>, OUTVALUE> extends Node {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Mapper<INKEY, INVALUE, OUTKEY, OUTVALUE> mapper;
	private final Reducer<OUTKEY, OUTVALUE, OUTKEY, OUTVALUE> combiner;

	/**
	 * Output of mapper will first be stored in the MapperCache, then write to
	 * combiner.
	 * 
	 * @author ljin
	 * 
	 */
	private final class MapperCache implements ChannelElementWriter<MRChannelElement<OUTKEY, OUTVALUE>> {
		private final Map<OUTKEY, List<OUTVALUE>> cache = new TreeMap<OUTKEY, List<OUTVALUE>>(new Comparator<OUTKEY>() {
			@Override
			public int compare(OUTKEY k1, OUTKEY k2) {
				return k1.compareTo(k2);
			}

		});

		private final Reducer<OUTKEY, OUTVALUE, OUTKEY, OUTVALUE> combiner;

		private final ChannelElementWriter<MRChannelElement<OUTKEY, OUTVALUE>> writer;

		MapperCache(Reducer<OUTKEY, OUTVALUE, OUTKEY, OUTVALUE> combiner,
				ChannelElementWriter<MRChannelElement<OUTKEY, OUTVALUE>> writer) {
			this.combiner = combiner;
			this.writer = writer;
		}

		@Override
		public boolean close() throws IOException {
			writer.flush();
			return writer.close();
		}

		@Override
		public boolean flush() throws IOException {
			try {
				int count = 0;
				for (Entry<OUTKEY, List<OUTVALUE>> entry : cache.entrySet()) {
					combiner.reduce(entry.getKey(), entry.getValue(), writer);
					count++;
				}
			} catch (IOException e) {
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
			}
			writer.flush();
			cache.clear();
			return true;
		}

		@Override
		public boolean write(MRChannelElement<OUTKEY, OUTVALUE> channelElement) throws IOException {
			List<OUTVALUE> lOutvalue = cache.get(channelElement.getKey());
			if (lOutvalue == null) {
				lOutvalue = new ArrayList<OUTVALUE>();
				cache.put(channelElement.getKey(), lOutvalue);
			}
			return lOutvalue.add(channelElement.getValue());
		}

	}

	public MapperNode(Mapper<INKEY, INVALUE, OUTKEY, OUTVALUE> mapper) {
		this(mapper, null);
	}

	public MapperNode(Mapper<INKEY, INVALUE, OUTKEY, OUTVALUE> mapper,
			Reducer<OUTKEY, OUTVALUE, OUTKEY, OUTVALUE> combiner) {
		super("default-mapper-name");
		this.mapper = mapper;
		this.combiner = combiner;
	}

	@Override
	public void run() {
		MRChannelElement<INKEY, INVALUE> channelElement;

		if (combiner != null) {
			MapperCache mapperCache = new MapperCache(combiner, writersShuffler);
			setWritersShuffler(mapperCache);
		}

		try {
			while (!isKilled()) {
				channelElement = (MRChannelElement<INKEY, INVALUE>) readSomeone();

				if (channelElement == null) {
					break;
				}
				INKEY key = channelElement.getKey();
				INVALUE value = channelElement.getValue();
				mapper.map(key, value, writersShuffler);
			}

			if (!isKilled()) {
				mapper.cleanup(writersShuffler);
			} else {
				Logging.Info(String.format("[MapperNode] Mapper node %s is killed.", getName()));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			flushAndCloseOutputs();
		}
	}

}
